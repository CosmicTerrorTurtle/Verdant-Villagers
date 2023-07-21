package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.GeoFeature;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.GeoFeatureBit;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.ServerVillage;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.VerticalBlockColumn;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road.RoadType;
import io.github.cosmic_terror_turtle.verdant_villagers.util.MathUtils;
import io.github.cosmic_terror_turtle.verdant_villagers.util.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RoadJunction extends GeoFeature {

    public BlockPos pos;
    public ArrayList<BlockPos> sidewalkPositions = new ArrayList<>();
    public double radius;
    public double sameHeightRadius;

    public RoadJunction(int elementID, BlockPos pos, double radius, double sameHeightRadius) {
        super(elementID);
        this.pos = pos;
        this.radius = radius;
        this.sameHeightRadius = sameHeightRadius;
    }
    public RoadJunction(int elementID, World world, BlockPos pos, RoadType type) {
        super(elementID);
        this.pos = pos;
        radius = type.junctionRadius;
        sameHeightRadius = type.junctionSameHeightRadius;

        setBitsAndMegaBlocks(world, type);
    }

    private void setBitsAndMegaBlocks(World world, RoadType type) {
        String top = RoadType.getTerrainType(true, world, pos);
        String bottom = RoadType.getTerrainType(false, world, pos);
        // Normal columns
        ArrayList<Double> blockColumnRadiiTop = type.junctionBlockColumnRadii.get("top").get(top);
        ArrayList<Double> blockColumnRadiiBottom = type.junctionBlockColumnRadii.get("bottom").get(bottom);
        ArrayList<VerticalBlockColumn> templateBlockColumnsTop = type.junctionTemplateBlockColumns.get("top").get(top);
        ArrayList<VerticalBlockColumn> templateBlockColumnsBottom = type.junctionTemplateBlockColumns.get("bottom").get(bottom);
        // Special columns (choose random keys for top/bottom)
        String specialKey;
        ArrayList<String> keys;
        HashMap<String, ArrayList<Double>> specialRadiiMapTop = type.junctionSpecialBlockColumnRadii.get("top").get(top);
        HashMap<String, ArrayList<VerticalBlockColumn>> specialColumnsMapTop = type.junctionSpecialTemplateBlockColumns.get("top").get(top);
        ArrayList<Double> specialRadiiTop = null;
        ArrayList<VerticalBlockColumn> specialColumnsTop = null;
        if (!specialRadiiMapTop.isEmpty()) {
            keys = new ArrayList<>(specialRadiiMapTop.keySet());
            specialKey = keys.get(MathUtils.nextInt(0, keys.size()-1));
            specialRadiiTop = specialRadiiMapTop.get(specialKey);
            specialColumnsTop = specialColumnsMapTop.get(specialKey);
        }
        HashMap<String, ArrayList<Double>> specialRadiiMapBottom = type.junctionSpecialBlockColumnRadii.get("bottom").get(bottom);
        HashMap<String, ArrayList<VerticalBlockColumn>> specialColumnsMapBottom = type.junctionSpecialTemplateBlockColumns.get("bottom").get(bottom);
        ArrayList<Double> specialRadiiBottom = null;
        ArrayList<VerticalBlockColumn> specialColumnsBottom = null;
        if (!specialRadiiMapBottom.isEmpty()) {
            keys = new ArrayList<>(specialRadiiMapBottom.keySet());
            specialKey = keys.get(MathUtils.nextInt(0, keys.size()-1));
            specialRadiiBottom = specialRadiiMapBottom.get(specialKey);
            specialColumnsBottom = specialColumnsMapBottom.get(specialKey);
        }

        ArrayList<GeoFeatureBit> relativeBits = new ArrayList<>();
        BlockPos position;
        VerticalBlockColumn templateColumn;
        VerticalBlockColumn templateColumnTop;
        VerticalBlockColumn templateColumnBottom;
        for (int x = (int) -radius; x<=radius; x++) {
            for (int z = (int) -radius; z<=radius; z++) {
                if (x*x+z*z <= radius*radius) {
                    // Get normal column (top).
                    templateColumnTop = null;
                    for (int i = 0; i< blockColumnRadiiTop.size(); i++) {
                        if (x*x+z*z <= blockColumnRadiiTop.get(i)*blockColumnRadiiTop.get(i)) {
                            templateColumnTop = templateBlockColumnsTop.get(i);
                            break;
                        }
                    }
                    // If possible, use special instead of normal column (top).
                    if (specialRadiiTop != null) {
                        for (int i = 0; i< specialRadiiTop.size(); i++) {
                            if (x*x+z*z <= specialRadiiTop.get(i)*specialRadiiTop.get(i)) {
                                templateColumnTop = specialColumnsTop.get(i);
                                break;
                            }
                        }
                    }
                    // Get normal column (bottom).
                    templateColumnBottom = null;
                    for (int i = 0; i< blockColumnRadiiBottom.size(); i++) {
                        if (x*x+z*z <= blockColumnRadiiBottom.get(i)*blockColumnRadiiBottom.get(i)) {
                            templateColumnBottom = templateBlockColumnsBottom.get(i);
                            break;
                        }
                    }
                    // If possible, use special instead of normal column (bottom).
                    if (specialRadiiBottom != null) {
                        for (int i = 0; i< specialRadiiBottom.size(); i++) {
                            if (x*x+z*z <= specialRadiiBottom.get(i)*specialRadiiBottom.get(i)) {
                                templateColumnBottom = specialColumnsBottom.get(i);
                                break;
                            }
                        }
                    }
                    // Merge both columns.
                    templateColumn = VerticalBlockColumn.merge(templateColumnTop, templateColumnBottom);
                    // Create bits.
                    position = new BlockPos(x, 0, z);
                    for (int i=0; i<templateColumn.states.length; i++) {
                        // Add bit.
                        GeoFeatureBit bit = new GeoFeatureBit(templateColumn.states[i], position.up(i-templateColumn.baseLevelIndex));
                        relativeBits.add(bit);
                        // If the bit is part of the sidewalk (int = 1 for that index), add its position.
                        if (templateColumn.ints[i] == 1) {
                            sidewalkPositions.add(pos.add(bit.blockPos));
                        }
                    }
                }
            }
        }
        setBits(relativeBits, pos, ROTATE_NOT);
    }

    /**
     * Tests if a {@link BlockPos} is part of this {@link RoadJunction}'s sidewalk.
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
     * Creates a new RoadJunction from an NbtCompound.
     * @param nbt The compound representing a RoadJunction.
     */
    public RoadJunction(@NotNull NbtCompound nbt) {
        super(nbt);
        pos = NbtUtils.blockPosFromNbt(nbt.getCompound("pos"));
        NbtCompound sidewalkNbt = nbt.getCompound("sidewalk");
        for (String key : sidewalkNbt.getKeys()) {
            sidewalkPositions.add(NbtUtils.blockPosFromNbt(sidewalkNbt.getCompound(key)));
        }
        radius = nbt.getDouble("radius");
        sameHeightRadius = nbt.getDouble("sameHeightRadius");
    }
    /**
     * Saves this RoadJunction to an NbtCompound.
     * @return The compound representing this RoadJunction.
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = super.toNbt();
        int i;
        nbt.put("pos", NbtUtils.blockPosToNbt(pos));
        NbtCompound sidewalkNbt = new NbtCompound();
        i=0;
        for (BlockPos pos : sidewalkPositions) {
            sidewalkNbt.put(Integer.toString(i), NbtUtils.blockPosToNbt(pos));
            i++;
        }
        nbt.put("sidewalk", sidewalkNbt);
        nbt.putDouble("radius", radius);
        nbt.putDouble("sameHeightRadius", sameHeightRadius);
        return nbt;
    }
}
