package io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.road;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.GeoFeatureBit;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.TerrainTypeUtils;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.VerticalBlockColumn;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.util.MathUtils;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.util.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class RoadJunction extends RoadFeature {

    public BlockPos pos;
    public double radius;
    public double sameHeightRadius;
    public String terrainTypeTop;
    public String terrainTypeBottom;

    /**
     * Creates a {@link RoadJunction} with radius of 0 for access paths.
     * @param elementID The ID of the junction.
     * @param world The world the junction being is placed in.
     * @param pos The position of the junction.
     * @param sameHeightRadius The same height radius of the junction.
     */
    public RoadJunction(long elementID, World world, BlockPos pos, double sameHeightRadius) {
        super(elementID);
        this.pos = pos;
        this.radius = 0.0;
        this.sameHeightRadius = sameHeightRadius;
        terrainTypeTop = TerrainTypeUtils.getTerrainType(true, world, pos, (int) (radius+2));
        terrainTypeBottom = TerrainTypeUtils.getTerrainType(false, world, pos, (int) (radius+2));
    }

    /**
     * Creates a regular {@link RoadJunction}.
     * @param elementID The ID of the junction.
     * @param world The world the junction being is placed in.
     * @param pos The position of the junction.
     * @param type The road type used.
     */
    public RoadJunction(long elementID, World world, BlockPos pos, RoadType type) {
        super(elementID);
        this.pos = pos;
        radius = type.junctionRadius;
        sameHeightRadius = type.junctionSameHeightRadius;
        terrainTypeTop = TerrainTypeUtils.getTerrainType(true, world, pos, (int) (radius+2));
        terrainTypeBottom = TerrainTypeUtils.getTerrainType(false, world, pos, (int) (radius+2));

        setBitsAndMegaBlocks(type);
    }

    private void setBitsAndMegaBlocks(RoadType type) {
        // Normal columns
        ArrayList<VerticalBlockColumn> templateBlockColumnsTop = type.junctionTemplateBlockColumns.get(RoadType.TERRAIN_TYPE_ABOVE_KEY).get(terrainTypeTop);
        ArrayList<VerticalBlockColumn> templateBlockColumnsBottom = type.junctionTemplateBlockColumns.get(RoadType.TERRAIN_TYPE_BELOW_KEY).get(terrainTypeBottom);
        // Special columns (choose random keys for top/bottom)
        String specialKey;
        ArrayList<String> keys;
        HashMap<String, ArrayList<VerticalBlockColumn>> specialColumnsMapTop = type.junctionSpecialTemplateBlockColumns.get(RoadType.TERRAIN_TYPE_ABOVE_KEY).get(terrainTypeTop);
        ArrayList<VerticalBlockColumn> specialColumnsTop = null;
        if (!specialColumnsMapTop.isEmpty()) {
            keys = new ArrayList<>(specialColumnsMapTop.keySet());
            specialKey = keys.get(MathUtils.nextInt(0, keys.size()-1));
            specialColumnsTop = specialColumnsMapTop.get(specialKey);
        }
        HashMap<String, ArrayList<VerticalBlockColumn>> specialColumnsMapBottom = type.junctionSpecialTemplateBlockColumns.get(RoadType.TERRAIN_TYPE_BELOW_KEY).get(terrainTypeBottom);
        ArrayList<VerticalBlockColumn> specialColumnsBottom = null;
        if (!specialColumnsMapBottom.isEmpty()) {
            keys = new ArrayList<>(specialColumnsMapBottom.keySet());
            specialKey = keys.get(MathUtils.nextInt(0, keys.size()-1));
            specialColumnsBottom = specialColumnsMapBottom.get(specialKey);
        }

        int squaredRadius;
        VerticalBlockColumn templateColumn;
        VerticalBlockColumn templateColumnTop;
        VerticalBlockColumn templateColumnBottom;
        BlockPos position;
        GeoFeatureBit bit;
        boolean rotateAlternative = MathUtils.getRandom().nextBoolean();
        BlockRotation rotation;
        double angle;
        ArrayList<GeoFeatureBit> relativeBits = new ArrayList<>();
        for (int x = (int) -radius; x<=radius; x++) {
            for (int z = (int) -radius; z<=radius; z++) {
                squaredRadius = x*x+z*z;
                if (squaredRadius > radius*radius) {
                    continue;
                }
                // Get normal columns.
                templateColumnTop = null;
                templateColumnBottom = null;
                for (int i = 0; i<type.junctionBlockColumnRadii.size(); i++) {
                    if (squaredRadius <= type.junctionBlockColumnRadii.get(i)*type.junctionBlockColumnRadii.get(i)) {
                        // Both top and bottom need to have the same length as the radii list or less.
                        if (i < templateBlockColumnsTop.size()) {
                            templateColumnTop = templateBlockColumnsTop.get(i);
                        }
                        if (i < templateBlockColumnsBottom.size()) {
                            templateColumnBottom = templateBlockColumnsBottom.get(i);
                        }
                        break;
                    }
                }
                // If possible, use special instead of normal columns.
                for (int i = 0; i<type.junctionSpecialBlockColumnRadii.size(); i++) {
                    if (squaredRadius <= type.junctionSpecialBlockColumnRadii.get(i)*type.junctionSpecialBlockColumnRadii.get(i)) {
                        // Check if the special columns top/bottom have an entry here (the radii list should be as long
                        // as the longest columns list).
                        if (specialColumnsTop != null && i < specialColumnsTop.size()) {
                            templateColumnTop = specialColumnsTop.get(i);
                        }
                        if (specialColumnsBottom != null && i < specialColumnsBottom.size()) {
                            templateColumnBottom = specialColumnsBottom.get(i);
                        }
                        break;
                    }
                }
                // Avoid placing the default column.
                if (templateColumnTop == null && templateColumnBottom == null) {
                    continue;
                }
                // Merge both columns.
                templateColumn = VerticalBlockColumn.merge(templateColumnTop, templateColumnBottom);
                // Determine rotation.
                if (Math.abs(x) == Math.abs(z)) {
                    if (z <= 0) {
                        // Northern half
                        if (x >= 0) {
                            rotation = BlockRotation.NONE;
                        } else {
                            rotation = BlockRotation.COUNTERCLOCKWISE_90;
                        }
                    } else {
                        // Southern half
                        if (x >= 0) {
                            rotation = BlockRotation.CLOCKWISE_90;
                        } else {
                            rotation = BlockRotation.CLOCKWISE_180;
                        }
                    }
                    if (rotateAlternative) {
                        rotation = rotation.rotate(BlockRotation.CLOCKWISE_90);
                    }
                } else {
                    angle = Math.asin(x/Math.sqrt(squaredRadius))/Math.PI;
                    if (z<0) {
                        // Northern half
                        if (angle <= -0.25) {
                            rotation = BlockRotation.COUNTERCLOCKWISE_90;
                        } else if (-0.25 < angle && angle <= 0.25) {
                            rotation = BlockRotation.NONE;
                        } else {
                            rotation = BlockRotation.CLOCKWISE_90;
                        }
                    } else {
                        // Southern half
                        if (angle < -0.25) {
                            rotation = BlockRotation.COUNTERCLOCKWISE_90;
                        } else if (-0.25 <= angle && angle < 0.25) {
                            rotation = BlockRotation.CLOCKWISE_180;
                        } else {
                            rotation = BlockRotation.CLOCKWISE_90;
                        }
                    }
                }
                // Create bits.
                position = new BlockPos(x, 0, z);
                for (int i=0; i<templateColumn.states.length; i++) {
                    // Add bit.
                    bit = new GeoFeatureBit(templateColumn.states[i], position.up(i-templateColumn.baseLevelIndex));
                    if (bit.blockState != null) {
                        bit.blockState = bit.blockState.rotate(rotation);
                    }
                    relativeBits.add(bit);
                    // Check ints of the column: 1 for sidewalk, 2 for arch, 3 for pillar.
                    switch (templateColumn.ints[i]) {
                        default -> {}
                        case 1 -> sidewalkPositions.add(pos.add(bit.blockPos));
                        case 2 -> archPositions.add(pos.add(bit.blockPos));
                        case 3 -> pillarStartBits.add(new GeoFeatureBit(bit.blockState, pos.add(bit.blockPos)));
                    }
                }
            }
        }
        setBits(relativeBits, pos, ROTATE_NOT);
    }

    /**
     * Creates a new RoadJunction from an NbtCompound.
     * @param nbt The compound representing a RoadJunction.
     */
    public RoadJunction(@NotNull NbtCompound nbt) {
        super(nbt);
        pos = NbtUtils.blockPosFromNbt(nbt.getCompound("pos"));
        radius = nbt.getDouble("radius");
        sameHeightRadius = nbt.getDouble("sameHeightRadius");
        terrainTypeTop = nbt.getString("terrainTypeTop");
        terrainTypeBottom = nbt.getString("terrainTypeBottom");
    }
    /**
     * Saves this RoadJunction to an NbtCompound.
     * @return The compound representing this RoadJunction.
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = super.toNbt();
        nbt.put("pos", NbtUtils.blockPosToNbt(pos));
        nbt.putDouble("radius", radius);
        nbt.putDouble("sameHeightRadius", sameHeightRadius);
        nbt.putString("terrainTypeTop", terrainTypeTop);
        nbt.putString("terrainTypeBottom", terrainTypeBottom);
        return nbt;
    }
}
