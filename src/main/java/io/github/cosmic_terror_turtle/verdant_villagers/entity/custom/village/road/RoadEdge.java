package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.*;
import io.github.cosmic_terror_turtle.verdant_villagers.util.MathUtils;
import io.github.cosmic_terror_turtle.verdant_villagers.util.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Random;

public class RoadEdge extends GeoFeature {

    public static final double ROUNDING_OFFSET = 0.5; // Makes the block position coordinates be centered in a block.
    public static final double ROAD_STEP = 0.1; // Lower step -> higher precision in placing the road blocks
    public static final double ROAD_DOT_SPACE = 2.5; // Space between road dots

    public static final int FIRST = 1;
    public static final int SECOND = 2;
    public static final int THIRD = 3;


    public RoadJunction from;
    public RoadJunction to;
    public ArrayList<BlockPos> sidewalkPositions = new ArrayList<>();
    public ArrayList<RoadDot> roadDots = new ArrayList<>();
    public final double radius;
    private int polynomialDegree;
    private double c;
    private final double d; // The distance between both ends on the X-Z-plane.
    private double e;
    private final boolean adjustToTerrain;
    private double ySlope; // The slope of this edge's delta-Y versus d.

    public RoadEdge(int elementID, ServerVillage village, RoadJunction from, RoadJunction to, boolean overwriteJunctions, boolean adjustToTerrain,
                    RoadType type) {
        this(elementID, village, from, to, type.edgeRadius, overwriteJunctions, adjustToTerrain,
                type.edgeBlockColumnRadii, type.edgeTemplateBlockColumns, type.edgeMiddleColumnSpace, type.edgeTemplateMiddleColumn);
    }
    public RoadEdge(int elementID, ServerVillage village, RoadJunction from, RoadJunction to, double radius, boolean overwriteJunctions, boolean adjustToTerrain,
                    VerticalBlockColumn templateRoadColumn) {
        this(elementID, village, from, to, radius, overwriteJunctions, adjustToTerrain,
                new double[]{radius}, new VerticalBlockColumn[]{templateRoadColumn}, 1000000.0, null);
    }
    public RoadEdge(int elementID, ServerVillage village, RoadJunction from, RoadJunction to, double radius, boolean overwriteJunctions, boolean adjustToTerrain,
                    double[] roadColumnRadii, VerticalBlockColumn[] templateRoadColumns, double middleColumnSpace, VerticalBlockColumn templateMiddleColumn) {
        super(elementID);
        this.from = from;
        this.to = to;
        this.radius = radius;
        this.adjustToTerrain = adjustToTerrain;

        d = Math.sqrt(MathHelper.square(from.pos.getX()-to.pos.getX())+MathHelper.square(from.pos.getZ()-to.pos.getZ()));
        if (d <= 0) {
            throw new RuntimeException("Illegal road edge length (on the x-z-plane).");
        }

        preparePolynomialFunction(village.random);
        setBitsMegaBlocksAndRoadDots(village, overwriteJunctions, middleColumnSpace, templateMiddleColumn, roadColumnRadii, templateRoadColumns);
    }

    private void preparePolynomialFunction(Random random) {
        double fraction; // The fraction of d that abs(function) should return at max.
        int deg = MathUtils.nextInt(1, 3);
        switch (deg) {
            case 1 -> {
                polynomialDegree = FIRST;
                e = 0;
                c = 0;
            }
            case 2 -> {
                polynomialDegree = SECOND;
                fraction = 0.4;
                e = 0;
                c = random.nextDouble(-4 * fraction / d, 4 * fraction / d);
            }
            case 3 -> {
                fraction = 0.3;
                polynomialDegree = THIRD;
                e = random.nextDouble( d / 3, d * 2 / 3);
                double aMax1 = (d + e) / 3 + Math.sqrt((d + e) * (d + e) / 9 - d * e / 3);
                double aMax2 = (d + e) / 3 - Math.sqrt((d + e) * (d + e) / 9 - d * e / 3);
                double cMax = fraction * d / Math.max(Math.abs(aMax1 * (d - aMax1) * (e - aMax1)), Math.abs(aMax2 * (d - aMax2) * (e - aMax2)));
                c = random.nextDouble( -cMax, cMax);
            }
        }
    }

    /**
     * Calculates the polynomial function for one input value.
     * @param a The input value of the function.
     * @return The output value of the function.
     */
    private double getFunctionAt(double a) {
        switch (polynomialDegree) {
            default:
            case FIRST:
                return 0;
            case SECOND:
                return c*a*(d-a);
            case THIRD:
                return c*a*(d-a)*(e-a);
        }
    }

    private double getSlopeAt(double a) {
        switch (polynomialDegree) {
            default:
            case FIRST:
                return 0;
            case SECOND:
                return c*(d-2*a);
            case THIRD:
                return c*(3*a*a-2*(d+e)*a+d*e);
        }
    }

    private void setBitsMegaBlocksAndRoadDots(ServerVillage village,
                                              boolean overwriteJunctions, double middleColumnSpace, VerticalBlockColumn templateMiddleColumn,
                                              double[] roadColumnRadii, VerticalBlockColumn[] templateRoadColumns) {

        ArrayList<VerticalBlockColumn> normalColumns = new ArrayList<>();
        ArrayList<VerticalBlockColumn> middleColumns = new ArrayList<>();
        double angleAtFrom;
        if (to.pos.getZ()>from.pos.getZ()) {
            angleAtFrom = Math.acos((to.pos.getX()-from.pos.getX())/d);
        } else {
            angleAtFrom = -Math.acos((to.pos.getX()-from.pos.getX())/d);
        }
        double aOffsetStart = from.sameHeightRadius;
        double aOffsetEnd = to.sameHeightRadius;
        if (d > aOffsetStart + aOffsetEnd) {
            ySlope = (to.pos.getY()-from.pos.getY())/(d-aOffsetStart-aOffsetEnd);
        } else {
            ySlope = 1000000;
        }
        TerrainAdjustment terrainAdjustment;
        if (adjustToTerrain) {
            terrainAdjustment = new TerrainAdjustment(village, angleAtFrom);
        } else {
            terrainAdjustment = null;
        }
        double aCoord;
        double faCoord;
        double f_of_a;
        double f_slope;
        double tmp;
        double yCoord;
        double yAdjustingOffset;
        VerticalBlockColumn templateColumn;
        double spaceAfterLastMiddleColumn = 0;
        double spaceAfterLastDot = 0;
        for (double a=0; a<d; a+=ROAD_STEP) {
            f_of_a = getFunctionAt(a);
            f_slope = getSlopeAt(a);
            if (a<aOffsetStart) {
                yCoord = 0;
            } else if (a<d-aOffsetEnd) {
                yCoord = ySlope*(a-aOffsetStart);
            } else {
                yCoord = to.pos.getY()-from.pos.getY();
            }
            if (terrainAdjustment != null) {
                yAdjustingOffset = terrainAdjustment.getYOffset(a);
            } else {
                yAdjustingOffset = 0;
            }
            // Bulk bits
            for (double offset=-radius; offset<=radius; offset+=ROAD_STEP) {
                tmp = offset/Math.sqrt(1+f_slope*f_slope);
                aCoord = a+f_slope*tmp;
                faCoord = f_of_a-tmp;
                templateColumn = templateRoadColumns[0];
                for (int i=0; i<roadColumnRadii.length; i++) {
                    if (Math.abs(offset) <= roadColumnRadii[i]) {
                        templateColumn = templateRoadColumns[i];
                        break;
                    }
                }
                addToColumns(normalColumns, templateColumn.copyWith(BlockPos.ofFloored(
                        aCoord*Math.cos(angleAtFrom) - faCoord*Math.sin(angleAtFrom) + ROUNDING_OFFSET,
                        yCoord + yAdjustingOffset + ROUNDING_OFFSET,
                        aCoord*Math.sin(angleAtFrom) + faCoord*Math.cos(angleAtFrom) + ROUNDING_OFFSET
                )), true);
            }
            // Middle column bits
            spaceAfterLastMiddleColumn += ROAD_STEP;
            if (spaceAfterLastMiddleColumn > middleColumnSpace) {
                spaceAfterLastMiddleColumn = 0;
                if (templateMiddleColumn != null && a>from.radius && a<d-to.radius) {
                    middleColumns.add(templateMiddleColumn.copyWith(BlockPos.ofFloored(
                            a*Math.cos(angleAtFrom) - f_of_a*Math.sin(angleAtFrom) + ROUNDING_OFFSET,
                            yCoord + yAdjustingOffset + ROUNDING_OFFSET,
                            a*Math.sin(angleAtFrom) + f_of_a*Math.cos(angleAtFrom) + ROUNDING_OFFSET
                    )));
                }
            }
            // Dots
            spaceAfterLastDot += ROAD_STEP;
            if (spaceAfterLastDot > ROAD_DOT_SPACE) {
                spaceAfterLastDot = 0;
                for (double offset : new double[]{1.0-radius, radius-1.0}) {
                    tmp = offset/Math.sqrt(1+f_slope*f_slope);
                    aCoord = a+f_slope*tmp;
                    faCoord = f_of_a-tmp;
                    roadDots.add(new RoadDot(this, from.pos.add(BlockPos.ofFloored(
                            aCoord*Math.cos(angleAtFrom) - faCoord*Math.sin(angleAtFrom) + ROUNDING_OFFSET,
                            yCoord + yAdjustingOffset + ROUNDING_OFFSET,
                            aCoord*Math.sin(angleAtFrom) + faCoord*Math.cos(angleAtFrom) + ROUNDING_OFFSET
                    ))));
                }
            }
        }
        for (VerticalBlockColumn middleColumn : middleColumns) {
            addToColumns(normalColumns, middleColumn, true);
        }
        boolean columnOverlapsFrom;
        boolean columnOverlapsTo;
        boolean writeEntireColumn;
        BlockPos relPos;
        GeoFeatureBit bit;
        ArrayList<GeoFeatureBit> relativeBits = new ArrayList<>();
        for (VerticalBlockColumn column : normalColumns) {
            columnOverlapsFrom = Math.pow(column.anchor.getX(), 2) + Math.pow(column.anchor.getZ(), 2) <= Math.pow(from.radius, 2);
            columnOverlapsTo = Math.pow(column.anchor.getX()+from.pos.getX()-to.pos.getX(), 2) + Math.pow(column.anchor.getZ()+from.pos.getZ()-to.pos.getZ(), 2) <= Math.pow(to.radius, 2);
            writeEntireColumn = overwriteJunctions || !columnOverlapsFrom && !columnOverlapsTo;
            for (int i=0; i<column.states.length; i++) {
                relPos = column.anchor.up(i-column.baseLevelIndex);
                // Add the bit only when overwriting all junctions, or when no overlap with junctions exists, or when
                // the bit is not part of the edge's sidewalk AND the bit is part of one of the junctions' sidewalk.
                if (writeEntireColumn || column.ints[i] != 1 && (
                        columnOverlapsFrom && from.positionIsSidewalk(from.pos.add(relPos))
                        || columnOverlapsTo && to.positionIsSidewalk(from.pos.add(relPos))
                )) {
                    // Add bit.
                    bit = new GeoFeatureBit(column.states[i], relPos);
                    relativeBits.add(bit);
                    // If the bit is part of the sidewalk (int = 1 for that index), add its position.
                    if (column.ints[i] == 1) {
                        sidewalkPositions.add(from.pos.add(relPos));
                    }
                }
            }
        }
        setBits(relativeBits, from.pos, ROTATE_NOT);
    }

    /**
     * Adds a vertical road column to a list if the x/z coordinate combination of the column's anchor is unique.
     * @param columns The list of columns.
     * @param newColumn The column to be added.
     * @param replaceOld Whether the first old column with the same anchor x/z should be replaced with the new column.
     */
    private static void addToColumns(ArrayList<VerticalBlockColumn> columns, VerticalBlockColumn newColumn, boolean replaceOld) {
        for (VerticalBlockColumn oldColumn : columns) {
            if (oldColumn.anchor.getX()==newColumn.anchor.getX() && oldColumn.anchor.getZ()==newColumn.anchor.getZ()) {
                if (replaceOld) {
                    columns.remove(oldColumn);
                    columns.add(newColumn);
                }
                return;
            }
        }
        columns.add(newColumn);
    }

    public double getYSlope() {
        return ySlope;
    }

    /**
     * Tests if a {@link BlockPos} is part of this {@link RoadEdge}'s sidewalk.
     * @param pos The {@link BlockPos} to test.
     * @return True if {@code pos} is a sidewalk position.
     */
    public boolean positionIsSidewalk(BlockPos pos) {
        for (BlockPos sidewalkPos : sidewalkPositions) {
            if (pos.equals(sidewalkPos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new RoadEdge from an NbtCompound.
     * @param nbt The compound representing a RoadEdge.
     * @param junctions All junctions of the village. If null (use this for reading access paths), then the two junctions
     *                  that belong to this edge will be read from it's nbt tag.
     */
    public RoadEdge(@NotNull NbtCompound nbt, @Nullable ArrayList<RoadJunction> junctions) {
        super(nbt);
        if (junctions != null) {
            // Major road
            int fromId = nbt.getInt("fromId");
            for (RoadJunction junction : junctions) {
                if (junction.elementID == fromId) {
                    from = junction;
                    break;
                }
            }
            int toId = nbt.getInt("toId");
            for (RoadJunction junction : junctions) {
                if (junction.elementID == toId) {
                    to = junction;
                    break;
                }
            }
        } else {
            // Access path
            from = new RoadJunction(nbt.getCompound("from"));
            to = new RoadJunction(nbt.getCompound("to"));
        }
        NbtCompound sidewalkNbt = nbt.getCompound("sidewalk");
        for (String key : sidewalkNbt.getKeys()) {
            sidewalkPositions.add(NbtUtils.blockPosFromNbt(sidewalkNbt.getCompound(key)));
        }
        NbtCompound roadDotsNbt = nbt.getCompound("roadDots");
        for (String key : roadDotsNbt.getKeys()) {
            roadDots.add(new RoadDot(roadDotsNbt.getCompound(key), this));
        }
        radius = nbt.getDouble("radius");
        polynomialDegree = nbt.getInt("polynomialDegree");
        c = nbt.getDouble("c");
        d = nbt.getDouble("d");
        e = nbt.getDouble("e");
        adjustToTerrain = nbt.getBoolean("adjustToTerrain");
        ySlope = nbt.getDouble("ySlope");
    }
    /**
     * Saves this RoadEdge to an NbtCompound.
     * @param isMajorRoad If this edge should be saved as a major road or as an access path. If true, only the elementID
     *                    from both junctions will be saved. If false, both junctions will be saved under this edge's nbt tag.
     * @return The compound representing this RoadEdge.
     */
    public NbtCompound toNbt(boolean isMajorRoad) {
        NbtCompound nbt = super.toNbt();
        int i;
        if (isMajorRoad) {
            nbt.putInt("fromId", from.elementID);
            nbt.putInt("toId", to.elementID);
        } else {
            // Is access path
            nbt.put("from", from.toNbt());
            nbt.put("to", to.toNbt());
        }
        NbtCompound sidewalkNbt = new NbtCompound();
        i=0;
        for (BlockPos pos : sidewalkPositions) {
            sidewalkNbt.put(Integer.toString(i), NbtUtils.blockPosToNbt(pos));
            i++;
        }
        nbt.put("sidewalk", sidewalkNbt);
        NbtCompound roadDotsNbt = new NbtCompound();
        i=0;
        for (RoadDot dot : roadDots) {
            roadDotsNbt.put(Integer.toString(i), dot.toNbt());
            i++;
        }
        nbt.put("roadDots", roadDotsNbt);
        nbt.putDouble("radius", radius);
        nbt.putInt("polynomialDegree", polynomialDegree);
        nbt.putDouble("c", c);
        nbt.putDouble("d", d);
        nbt.putDouble("e", e);
        nbt.putBoolean("adjustToTerrain", adjustToTerrain);
        nbt.putDouble("ySlope", ySlope);
        return nbt;
    }

    public class TerrainAdjustment {

        public static final double TERRAIN_ADJUSTING_SPACE = 3.0; // Space between terrain adjusting points
        public static final double MAX_SLOPE_DEVIATION = 0.15; // Maximum difference between overall slope and the slopes from the adjusted points
        public static final double SMOOTHING_FACTOR = 0.2; // Factor by which a point gets adjusted towards its neighbors
        public static final int MAX_SMOOTHING_ITERATIONS = 40; // The maximum number of iterations that smooth the adjusting offsets.


        private final double aOffsetStart;
        private final double aOffsetEnd;
        private ArrayList<Double> yOffsetValues;

        public TerrainAdjustment(ServerVillage village, double angleAtFrom) {
            aOffsetStart = from.sameHeightRadius;
            aOffsetEnd = to.sameHeightRadius;

            yOffsetValues = new ArrayList<>();

            // Get offset values
            for (double a=0; a<d; a+=TERRAIN_ADJUSTING_SPACE) {
                if (a<aOffsetStart || d-aOffsetEnd<a) {
                    yOffsetValues.add(0.0);
                } else {
                    yOffsetValues.add(getTerrainOffset(village, angleAtFrom, a, ySlope*(a-aOffsetStart), 0.2*d));
                }
            }

            // Smooth offset values
            for (int i=0; i<MAX_SMOOTHING_ITERATIONS; i++) {
                if (!smoothOffsets(false)) {
                    break;
                }
            }
            // Final smooth
            smoothOffsets(true);
        }

        private double getTerrainOffset(ServerVillage village, double angleAtFrom, double a, double yCoord, double maxOffset) {
            BlockPos startPosition = from.pos.add(
                    (int) (a*Math.cos(angleAtFrom) - getFunctionAt(a)*Math.sin(angleAtFrom)),
                    (int) yCoord,
                    (int) (a*Math.sin(angleAtFrom) + getFunctionAt(a)*Math.cos(angleAtFrom))
            );
            BlockPos surfaceBlock = village.getSurfaceBlock(startPosition, (int) (startPosition.getY()-maxOffset), (int) (startPosition.getY()+maxOffset), false);
            double terrainOffset;
            if (surfaceBlock != null) {
                terrainOffset = surfaceBlock.getY() - yCoord - from.pos.getY();
            } else {
                terrainOffset = 0;
            }
            if (terrainOffset > 0) {
                return Math.min(terrainOffset, maxOffset);
            } else {
                return Math.max(terrainOffset, -maxOffset);
            }
        }

        /**
         * Smooths offsets that lead to large slope deviations.
         * @param smoothAll If true, values will be smoothed even if their slopes are acceptable.
         * @return True if more smoothing may be needed.
         */
        private boolean smoothOffsets(boolean smoothAll) {
            ArrayList<Double> newOffsets = new ArrayList<>();
            double a;
            boolean smoothingNeeded = false;

            for (int i=0; i<yOffsetValues.size(); i++) {
                // Only values between the aOffsets can be smoothed.
                a = i*TERRAIN_ADJUSTING_SPACE;
                if (a<aOffsetStart || d-aOffsetEnd<a) {
                    newOffsets.add(yOffsetValues.get(i));
                    continue;
                }

                // Is the slope deviation of this offset value too large or is smoothAll enabled?
                if (smoothAll
                        || Math.abs(getYOffsetFromIndex(i)-getYOffsetFromIndex(i-1))/TERRAIN_ADJUSTING_SPACE > MAX_SLOPE_DEVIATION
                        || Math.abs(getYOffsetFromIndex(i+1)-getYOffsetFromIndex(i))/TERRAIN_ADJUSTING_SPACE > MAX_SLOPE_DEVIATION) {
                    smoothingNeeded = true;
                    newOffsets.add( yOffsetValues.get(i) + SMOOTHING_FACTOR * (getYOffsetFromIndex(i+1)+getYOffsetFromIndex(i-1)-2*yOffsetValues.get(i)) );
                } else {
                    newOffsets.add(yOffsetValues.get(i));
                }
            }
            yOffsetValues = newOffsets;

            return smoothingNeeded;
        }

        private double getYOffsetFromIndex(int index) {
            if (index < 0 || index >= yOffsetValues.size()) {
                return 0;
            }
            return yOffsetValues.get(index);
        }

        private double interpolateOffset(int leftIndex, double percentageToRightPoint) {
            if (leftIndex < -1) {
                leftIndex = -1;
            }
            if (leftIndex > yOffsetValues.size()-1) {
                leftIndex = yOffsetValues.size()-1;
            }
            return (1-percentageToRightPoint) * getYOffsetFromIndex(leftIndex) + percentageToRightPoint * getYOffsetFromIndex(leftIndex+1);
        }

        public double getYOffset(double a) {
            int leftIndex = (int) (a/TERRAIN_ADJUSTING_SPACE);
            return interpolateOffset(leftIndex, (a-leftIndex*TERRAIN_ADJUSTING_SPACE)/TERRAIN_ADJUSTING_SPACE);
        }
    }
}
