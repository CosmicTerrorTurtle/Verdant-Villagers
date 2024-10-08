package io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.geo_feature.road;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.geo_feature.GeoFeatureBit;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.ServerVillage;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.TerrainTypeUtils;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.geo_feature.VerticalBlockColumn;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.util.MathUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Random;

public class RoadEdge extends RoadFeature {

    public static final double ROUNDING_OFFSET = 0.5; // Makes the block position coordinates be centered in a block.
    public static final double ROAD_STEP = 0.1; // Lower step -> higher precision in placing the road blocks
    public static final double ROAD_DOT_SPACE = 2.5; // Space between road dots
    public static final double ROAD_TYPE_TERRAIN_SPACE = 0.9; // Space between terrain type checks
    public static final int SPIRAL_BASE_Y_DIFF = 16;
    public static final int SPIRAL_BASE_RADIUS = 11;
    public static final int MAX_SPIRALS = 3;

    public static final int FIRST = 1;
    public static final int SECOND = 2;
    public static final int THIRD = 3;


    public RoadJunction from;
    public RoadJunction to;
    public ArrayList<RoadDot> roadDots = new ArrayList<>();
    public final double radius;
    private int polynomialDegree;
    private double c;
    private final double d; // The distance between both ends on the X-Z-plane.
    private double e;
    private final boolean adjustToTerrain;
    private double ySlope; // The slope of this edge's delta-Y versus d.
    private int spiralNum; // The number of upward spirals. If negative, the spirals go down.
    private boolean spiralsLeft; // Whether the spirals are to the left or the right (starting at 'from').

    /**
     * Instantiates a road edge using {@link RoadType}.
     * @param elementID The village-wide unique ID.
     * @param village The {@link ServerVillage} this edge belongs to.
     * @param from The {@link RoadJunction} this edge starts from.
     * @param to The {@link RoadJunction} this edge leads to.
     * @param adjustToTerrain If this edge should try to match the terrain slope underneath it.
     * @param roadType The {@link RoadType} used for this edge.
     * @param isAccessPath Whether this edge is an access path.
     * @param spiral Whether this edge should overcome great height differences using spiral ramps.
     */
    public RoadEdge(long elementID, ServerVillage village, RoadJunction from, RoadJunction to,
                     boolean adjustToTerrain, RoadType roadType, boolean isAccessPath, boolean spiral, boolean fluidIsSurfaceForCoasts) {
        super(elementID);
        this.from = from;
        this.to = to;
        radius = roadType.edgeRadius;
        this.adjustToTerrain = adjustToTerrain;

        d = Math.sqrt(MathHelper.square(from.pos.getX()-to.pos.getX())+MathHelper.square(from.pos.getZ()-to.pos.getZ()));
        if (d <= 0) {
            throw new RuntimeException("Illegal road edge length (on the x-z-plane).");
        }

        preparePolynomialFunction(village.random, isAccessPath, spiral);
        setBitsMegaBlocksAndRoadDots(village, roadType, isAccessPath, spiral, fluidIsSurfaceForCoasts);
    }

    private void preparePolynomialFunction(Random random, boolean isAccessPath, boolean spiral) {
        double fraction; // The fraction of d that abs(function) should return at max.
        int deg = spiral ? 1 : MathUtils.nextInt(1, 3);
        switch (deg) {
            case 1 -> {
                polynomialDegree = FIRST;
                e = 0;
                c = 0;
            }
            case 2 -> {
                polynomialDegree = SECOND;
                fraction = isAccessPath ? 0.3 : 0.4;
                e = 0;
                c = random.nextDouble(-4 * fraction / d, 4 * fraction / d);
            }
            case 3 -> {
                fraction = isAccessPath ? 0.2 : 0.3;
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

    /**
     * Calculates the polynomial function's derivative for one input value.
     * @param a The input value.
     * @return The output value of the function's derivative.
     */
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

    private void setBitsMegaBlocksAndRoadDots(ServerVillage village, RoadType roadType,
                                              boolean isAccessPath, boolean spiral, boolean fluidIsSurfaceForCoasts) {
        boolean overwriteJunctions = isAccessPath;

        ArrayList<VerticalBlockColumn> normalColumns = new ArrayList<>();
        ArrayList<VerticalBlockColumn> specialColumns = new ArrayList<>();
        ArrayList<VerticalBlockColumn> outerNormalColumns = new ArrayList<>();
        ArrayList<VerticalBlockColumn> outerSpecialColumns = new ArrayList<>();
        ArrayList<VerticalBlockColumn> columnsToAddTo;
        double angleAtFrom;
        if (to.pos.getZ()>from.pos.getZ()) {
            angleAtFrom = Math.acos((to.pos.getX()-from.pos.getX())/d);
        } else {
            angleAtFrom = -Math.acos((to.pos.getX()-from.pos.getX())/d);
        }
        double sin = Math.sin(angleAtFrom);
        double cos = Math.cos(angleAtFrom);
        double aOffsetStart = from.sameHeightRadius;
        double aOffsetEnd = to.sameHeightRadius;
        spiralNum = 0;
        spiralsLeft = true;
        double spiralYDiff = SPIRAL_BASE_Y_DIFF*roadType.scale;
        double spiralRadius = SPIRAL_BASE_RADIUS*roadType.scale;
        if (d > aOffsetStart + aOffsetEnd) {
            int yDiff = to.pos.getY()-from.pos.getY();
            if (spiral) {
                spiralNum = (int) (yDiff/spiralYDiff);
                spiralsLeft = village.random.nextBoolean();
            }
            ySlope = (yDiff-spiralNum*spiralYDiff)/(d-aOffsetStart-aOffsetEnd);
        } else {
            ySlope = 1000000;
        }
        TerrainAdjustment terrainAdjustment;
        if (adjustToTerrain) {
            terrainAdjustment = new TerrainAdjustment(village, sin, cos, fluidIsSurfaceForCoasts);
        } else {
            terrainAdjustment = null;
        }
        double aCopy;
        double aCoord;
        double faCoord;
        double f_of_a;
        double f_slope;
        double tmp;
        double yCoord;
        double yAdjustingOffset;
        int completedSpirals = 0;
        boolean onSpiral;
        double spiralAngle = 0;
        BlockPos centerPos;
        ArrayList<BlockPos> anchorPositions = new ArrayList<>();
        VerticalBlockColumn columnTop;
        VerticalBlockColumn columnBottom;
        VerticalBlockColumn merged;
        double spaceAfterLastSpecialColumn = 0.0;
        double spaceAfterLastDot = 0.0;
        double spaceAfterLastTerrainCheck = 0.0;
        World world = village.getWorld();
        String topTerrain;
        String bottomTerrain;
        ArrayList<VerticalBlockColumn> columnsTop;
        ArrayList<VerticalBlockColumn> columnsBottom;
        ArrayList<VerticalBlockColumn> specialColumnsTop;
        ArrayList<VerticalBlockColumn> specialColumnsBottom;
        topTerrain = from.terrainTypeTop;
        bottomTerrain = from.terrainTypeBottom;
        columnsTop = roadType.edgeTemplateBlockColumns.get(RoadType.TERRAIN_TYPE_ABOVE_KEY).get(topTerrain);
        columnsBottom = roadType.edgeTemplateBlockColumns.get(RoadType.TERRAIN_TYPE_BELOW_KEY).get(bottomTerrain);
        specialColumnsTop = roadType.edgeSpecialTemplateBlockColumns.get(RoadType.TERRAIN_TYPE_ABOVE_KEY).get(topTerrain);
        specialColumnsBottom = roadType.edgeSpecialTemplateBlockColumns.get(RoadType.TERRAIN_TYPE_BELOW_KEY).get(bottomTerrain);
        for (double a=0; a<d; a+=ROAD_STEP) {
            if (spiral && completedSpirals<Math.abs(spiralNum) && a > d/2) {
                a-=ROAD_STEP;
                onSpiral = true;
            } else {
                onSpiral = false;
            }
            if (onSpiral) {
                aCopy = a + Math.sin(spiralAngle) * spiralRadius;
                f_of_a = (1-Math.cos(spiralAngle)) * spiralRadius * (spiralsLeft ? 1: -1);
                tmp = f_of_a - spiralRadius * (spiralsLeft ? 1: -1);
                if (tmp == 0) {
                    f_slope = -(aCopy-a) * 1000000.0;
                } else {
                    f_slope = -(aCopy-a)/tmp;
                }
            } else {
                aCopy = a;
                f_of_a = getFunctionAt(a);
                f_slope = getSlopeAt(a);
            }
            if (a<aOffsetStart) {
                yCoord = 0;
            } else if (a<d-aOffsetEnd) {
                yCoord = ySlope*(a-aOffsetStart);
                if (spiral) {
                    yCoord += completedSpirals*spiralYDiff * (spiralNum>=0 ? 1: -1);
                    if (onSpiral) {
                        yCoord += spiralAngle/(2*Math.PI)*spiralYDiff * (spiralNum>=0 ? 1: -1);
                    }
                }
            } else {
                yCoord = to.pos.getY()-from.pos.getY();
            }
            if (terrainAdjustment != null) {
                yAdjustingOffset = terrainAdjustment.getYOffset(a);
            } else {
                yAdjustingOffset = 0;
            }
            centerPos = from.pos.add(BlockPos.ofFloored(
                    aCopy*cos - f_of_a*sin + ROUNDING_OFFSET,
                    yCoord + yAdjustingOffset + ROUNDING_OFFSET,
                    aCopy*sin + f_of_a*cos + ROUNDING_OFFSET
            ));
            // Road dots
            spaceAfterLastDot += ROAD_STEP;
            if (!isAccessPath && spaceAfterLastDot > ROAD_DOT_SPACE) {
                spaceAfterLastDot = 0;
                roadDots.add(new RoadDot(this, centerPos));
            }
            // Check terrain above and underneath the road, if outside the junction radii.
            spaceAfterLastTerrainCheck += ROAD_STEP;
            if (spaceAfterLastTerrainCheck > ROAD_TYPE_TERRAIN_SPACE && from.radius < a && a < d-to.radius) {
                spaceAfterLastTerrainCheck = 0;
                topTerrain = TerrainTypeUtils.getTerrainType(true, world, centerPos, (int) (radius+2));
                bottomTerrain = TerrainTypeUtils.getTerrainType(false, world, centerPos, (int) (radius+2));
                columnsTop = roadType.edgeTemplateBlockColumns.get(RoadType.TERRAIN_TYPE_ABOVE_KEY).get(topTerrain);
                columnsBottom = roadType.edgeTemplateBlockColumns.get(RoadType.TERRAIN_TYPE_BELOW_KEY).get(bottomTerrain);
                specialColumnsTop = roadType.edgeSpecialTemplateBlockColumns.get(RoadType.TERRAIN_TYPE_ABOVE_KEY).get(topTerrain);
                specialColumnsBottom = roadType.edgeSpecialTemplateBlockColumns.get(RoadType.TERRAIN_TYPE_BELOW_KEY).get(bottomTerrain);
            }
            // Determine if special columns need to be calculated.
            spaceAfterLastSpecialColumn += ROAD_STEP;
            if (spaceAfterLastSpecialColumn > roadType.edgeSpecialColumnSpace) {
                spaceAfterLastSpecialColumn = 0.0;
            }
            // Bits
            for (double rad=0; rad<=radius; rad+=ROAD_STEP) {
                // Position math
                anchorPositions.clear();
                for (double offset : new Double[]{rad, -rad}) {
                    tmp = offset/Math.sqrt(1+f_slope*f_slope);
                    aCoord = aCopy+f_slope*tmp;
                    faCoord = f_of_a-tmp;
                    anchorPositions.add(BlockPos.ofFloored(
                            aCoord*cos - faCoord*sin + ROUNDING_OFFSET,
                            yCoord + yAdjustingOffset + ROUNDING_OFFSET,
                            aCoord*sin + faCoord*cos + ROUNDING_OFFSET
                    ));
                }
                // Normal columns
                columnsToAddTo = normalColumns;
                columnTop = null;
                columnBottom = null;
                for (int i=0; i<roadType.edgeBlockColumnRadii.size(); i++) {
                    if (rad <= roadType.edgeBlockColumnRadii.get(i)) {
                        if (i < columnsTop.size()) {
                            columnTop = columnsTop.get(i);
                        }
                        if (i < columnsBottom.size()) {
                            columnBottom = columnsBottom.get(i);
                        }
                        // If the column is the outermost column, replace old columns.
                        if (i==roadType.edgeBlockColumnRadii.size()-1) {
                            columnsToAddTo = outerNormalColumns;
                        }
                        break;
                    }
                }
                // Avoid placing the default merge column.
                if (columnTop != null || columnBottom != null) {
                    merged = VerticalBlockColumn.merge(columnTop, columnBottom);
                    for (BlockPos anchor : anchorPositions) {
                        addToColumns(columnsToAddTo, merged.copyWith(anchor));
                    }
                }
                // Special columns (only place when outside the junction's same height radii)
                if (spaceAfterLastSpecialColumn==0 && from.sameHeightRadius<a && a<d-to.sameHeightRadius) {
                    columnsToAddTo = specialColumns;
                    columnTop = null;
                    columnBottom = null;
                    for (int i=0; i<roadType.edgeSpecialBlockColumnRadii.size(); i++) {
                        if (rad <= roadType.edgeSpecialBlockColumnRadii.get(i)) {
                            if (i < specialColumnsTop.size()) {
                                columnTop = specialColumnsTop.get(i);
                            }
                            if (i < specialColumnsBottom.size()) {
                                columnBottom = specialColumnsBottom.get(i);
                            }
                            if (i==roadType.edgeSpecialBlockColumnRadii.size()-1) {
                                columnsToAddTo = outerSpecialColumns;
                            }
                            break;
                        }
                    }
                    // Avoid placing the default merge column.
                    if (columnTop != null || columnBottom != null) {
                        merged = VerticalBlockColumn.merge(columnTop, columnBottom);
                        for (BlockPos anchor : anchorPositions) {
                            addToColumns(columnsToAddTo, merged.copyWith(anchor));
                        }
                    }
                }
            }
            if (onSpiral) {
               spiralAngle += ROAD_STEP/(spiralRadius);
               if (spiralAngle > 2*Math.PI) {
                   spiralAngle = 0;
                   completedSpirals++;
               }
            }
        }
        // Merge the lists of columns: First the outer special/normal columns, then the inner special/normal columns.
        for (VerticalBlockColumn column : outerNormalColumns) {
            addToColumns(outerSpecialColumns, column);
        }
        for (VerticalBlockColumn column : specialColumns) {
            addToColumns(outerSpecialColumns, column);
        }
        for (VerticalBlockColumn column : normalColumns) {
            addToColumns(outerSpecialColumns, column);
        }
        // Extract bits from columns.
        boolean columnOverlapsFrom;
        boolean columnOverlapsTo;
        boolean writeEntireColumn;
        BlockPos relPos;
        BlockPos absPos;
        GeoFeatureBit bit;
        ArrayList<GeoFeatureBit> relativeBits = new ArrayList<>();
        for (VerticalBlockColumn column : outerSpecialColumns) {
            columnOverlapsFrom =
                    Math.pow(column.anchor.getX(), 2)
                    + Math.pow(column.anchor.getZ(), 2)
                    <= Math.pow(from.radius, 2);
            columnOverlapsTo =
                    Math.pow(column.anchor.getX()+from.pos.getX()-to.pos.getX(), 2)
                    + Math.pow(column.anchor.getZ()+from.pos.getZ()-to.pos.getZ(), 2)
                    <= Math.pow(to.radius, 2);
            writeEntireColumn = overwriteJunctions || !columnOverlapsFrom && !columnOverlapsTo;
            for (int i=0; i<column.states.length; i++) {
                relPos = column.anchor.up(i-column.baseLevelIndex);
                absPos = from.pos.add(relPos);
                // Add the bit, when:
                // -overwriting all junctions
                // -or no overlap with junctions exists
                // -or the bit is not marked as arch and the bit's column overlaps one of the junctions and:
                //     -the bit is not sidewalk and the position is part of the junction's sidewalk
                //     -or the position is part of the junction's arch
                if (writeEntireColumn || column.ints[i] != 2 && (
                        columnOverlapsFrom && (
                                column.ints[i] != 1 && from.sidewalkPositions.contains(absPos)
                                || from.archPositions.contains(absPos))
                        || columnOverlapsTo && (
                                column.ints[i] != 1 && to.sidewalkPositions.contains(absPos)
                                || to.archPositions.contains(absPos))
                )) {
                    // Add bit.
                    bit = new GeoFeatureBit(column.states[i], relPos);
                    relativeBits.add(bit);
                    // Check ints of the column: 1 for sidewalk, 2 for arch, 3 for pillar.
                    switch (column.ints[i]) {
                        default -> {}
                        case 1 -> sidewalkPositions.add(absPos);
                        case 2 -> archPositions.add(absPos);
                        case 3 -> pillarStartBits.add(new GeoFeatureBit(column.states[i], absPos));
                    }
                }
            }
        }
        setBits(relativeBits, from.pos, ROTATE_NOT);
    }

    /**
     * Adds a vertical road column to a list if it does not overlap any existing columns. If there is overlap, the
     * first overlapping column will copy the anchor of {@code newColumn} instead.
     * @param columns   The list of road columns.
     * @param newColumn The column to be added.
     */
    private static void addToColumns(ArrayList<VerticalBlockColumn> columns, VerticalBlockColumn newColumn) {
        for (VerticalBlockColumn oldColumn : columns) {
            if (VerticalBlockColumn.columnsOverlap(oldColumn, newColumn)) {
                oldColumn.anchor = newColumn.anchor;
                return;
            }
        }
        columns.add(newColumn);
    }

    public double getYSlope() {
        return ySlope;
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
            long fromId = nbt.getLong("fromId");
            for (RoadJunction junction : junctions) {
                if (junction.elementID == fromId) {
                    from = junction;
                    break;
                }
            }
            long toId = nbt.getLong("toId");
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
        spiralNum = nbt.getInt("spiralNum");
        spiralsLeft = nbt.getBoolean("spiralsLeft");
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
            nbt.putLong("fromId", from.elementID);
            nbt.putLong("toId", to.elementID);
        } else {
            // Is access path
            nbt.put("from", from.toNbt());
            nbt.put("to", to.toNbt());
        }
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
        nbt.putInt("ySlope", spiralNum);
        nbt.putBoolean("ySlope", spiralsLeft);
        return nbt;
    }

    public class TerrainAdjustment {

        public static final double TERRAIN_ADJUSTING_SPACE = 4.0; // Space between terrain adjusting points
        public static final double SMOOTHING_FACTOR = 0.3; // Factor by which a point gets adjusted towards its neighbors
        public static final int MAX_SMOOTHING_ITERATIONS = 100; // The maximum number of iterations that smooth the adjusting offsets
        /**
         * The minimum difference between an adjusting point and the average of its two encompassing points for a point
         * to be classified as a bump.
         */
        public static final double BUMP_THRESHOLD = 0.35;


        private final double aOffsetStart;
        private final double aOffsetEnd;
        private ArrayList<Double> yOffsetValues;

        public TerrainAdjustment(ServerVillage village, double sin, double cos, boolean fluidIsSurfaceForCoasts) {
            aOffsetStart = from.sameHeightRadius;
            aOffsetEnd = to.sameHeightRadius;

            yOffsetValues = new ArrayList<>();

            // Get offset values
            for (double a=0; a<d; a+=TERRAIN_ADJUSTING_SPACE) {
                if (a<aOffsetStart || d-aOffsetEnd<a) {
                    yOffsetValues.add(0.0);
                } else {
                    yOffsetValues.add(getTerrainOffset(village, sin, cos, a, ySlope*(a-aOffsetStart), 0.2*d, fluidIsSurfaceForCoasts));
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
            // Smooth single adjusting points that stick out
            smoothOutliers();
        }

        private double getTerrainOffset(ServerVillage village, double sin, double cos, double a,
                                        double yCoord, double maxOffset, boolean fluidIsSurfaceForCoasts) {
            BlockPos startPosition = from.pos.add(
                    (int) (a*cos - getFunctionAt(a)*sin),
                    (int) yCoord,
                    (int) (a*sin + getFunctionAt(a)*cos)
            );
            BlockPos surfaceBlock = village.getSurfaceBlock(
                    startPosition,
                    (int) (startPosition.getY()-maxOffset),
                    (int) (startPosition.getY()+maxOffset),
                    fluidIsSurfaceForCoasts
            );
            double terrainOffset;
            if (surfaceBlock != null) {
                terrainOffset = surfaceBlock.getY() - yCoord - from.pos.getY();
            } else {
                // No surface block found. For air, start offset at max in order to get a bridge-like arched slope. For
                // other terrain types on top, start at zero.
                if (TerrainTypeUtils.getTerrainType(true, village.getWorld(), startPosition, 6).equals("air")) {
                    terrainOffset = maxOffset;
                } else {
                    terrainOffset = 0;
                }
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
                        || Math.abs((getYOffsetFromIndex(i)-getYOffsetFromIndex(i-1))/TERRAIN_ADJUSTING_SPACE+ySlope) > ServerVillage.ROAD_EDGE_MAX_Y_SLOPE
                        || Math.abs((getYOffsetFromIndex(i+1)-getYOffsetFromIndex(i))/TERRAIN_ADJUSTING_SPACE+ySlope) > ServerVillage.ROAD_EDGE_MAX_Y_SLOPE
                ) {
                    smoothingNeeded = true;
                    newOffsets.add( yOffsetValues.get(i) + SMOOTHING_FACTOR * (getYOffsetFromIndex(i+1)+getYOffsetFromIndex(i-1)-2*yOffsetValues.get(i)) );
                } else {
                    newOffsets.add(yOffsetValues.get(i));
                }
            }
            yOffsetValues = newOffsets;

            return smoothingNeeded;
        }

        /**
         * Smooths all terrain adjusting points that stick out by themselves compared to their neighbors. This aims to
         * avoid single-adjusting-point bumps in the road.
         */
        private void smoothOutliers() {
            ArrayList<Double> newOffsets = new ArrayList<>();
            double a;
            double average;

            for (int i=0; i<yOffsetValues.size(); i++) {
                // Only values between the aOffsets can be smoothed.
                a = i*TERRAIN_ADJUSTING_SPACE;
                if (a<aOffsetStart || d-aOffsetEnd<a) {
                    newOffsets.add(yOffsetValues.get(i));
                    continue;
                }

                // If there is a bump at this adjusting point, then replace that value with the average of both neighbors.
                average = (getYOffsetFromIndex(i+1) + getYOffsetFromIndex(i-1)) / 2;
                if (Math.abs(getYOffsetFromIndex(i) - average) > BUMP_THRESHOLD) {
                    newOffsets.add(average);
                } else {
                    newOffsets.add(yOffsetValues.get(i));
                }
            }

            yOffsetValues = newOffsets;
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
