package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.GeoFeature;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.GeoFeatureBit;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.VerticalBlockColumn;
import io.github.cosmic_terror_turtle.verdant_villagers.util.MathUtils;
import io.github.cosmic_terror_turtle.verdant_villagers.util.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class RoadJunction extends GeoFeature {

    public BlockPos pos;
    public ArrayList<BlockPos> sidewalkPositions = new ArrayList<>();
    public ArrayList<BlockPos> archPositions = new ArrayList<>();
    public ArrayList<GeoFeatureBit> pillarStartBits = new ArrayList<>();
    public double radius;
    public double sameHeightRadius;
    public String terrainTypeTop;
    public String terrainTypeBottom;

    public RoadJunction(int elementID, BlockPos pos, double radius, double sameHeightRadius) {
        super(elementID);
        this.pos = pos;
        this.radius = radius;
        this.sameHeightRadius = sameHeightRadius;
        terrainTypeTop = "";
        terrainTypeBottom = "";
    }
    public RoadJunction(int elementID, World world, BlockPos pos, RoadType type) {
        super(elementID);
        this.pos = pos;
        radius = type.junctionRadius;
        sameHeightRadius = type.junctionSameHeightRadius;
        terrainTypeTop = RoadType.getTerrainType(true, world, pos);
        terrainTypeBottom = RoadType.getTerrainType(false, world, pos);

        setBitsAndMegaBlocks(type);
    }

    private void setBitsAndMegaBlocks(RoadType type) {
        // Normal columns
        ArrayList<VerticalBlockColumn> templateBlockColumnsTop = type.junctionTemplateBlockColumns.get("top").get(terrainTypeTop);
        ArrayList<VerticalBlockColumn> templateBlockColumnsBottom = type.junctionTemplateBlockColumns.get("bottom").get(terrainTypeBottom);
        // Special columns (choose random keys for top/bottom)
        String specialKey;
        ArrayList<String> keys;
        HashMap<String, ArrayList<VerticalBlockColumn>> specialColumnsMapTop = type.junctionSpecialTemplateBlockColumns.get("top").get(terrainTypeTop);
        ArrayList<VerticalBlockColumn> specialColumnsTop = null;
        if (!specialColumnsMapTop.isEmpty()) {
            keys = new ArrayList<>(specialColumnsMapTop.keySet());
            specialKey = keys.get(MathUtils.nextInt(0, keys.size()-1));
            specialColumnsTop = specialColumnsMapTop.get(specialKey);
        }
        HashMap<String, ArrayList<VerticalBlockColumn>> specialColumnsMapBottom = type.junctionSpecialTemplateBlockColumns.get("bottom").get(terrainTypeBottom);
        ArrayList<VerticalBlockColumn> specialColumnsBottom = null;
        if (!specialColumnsMapBottom.isEmpty()) {
            keys = new ArrayList<>(specialColumnsMapBottom.keySet());
            specialKey = keys.get(MathUtils.nextInt(0, keys.size()-1));
            specialColumnsBottom = specialColumnsMapBottom.get(specialKey);
        }

        ArrayList<GeoFeatureBit> relativeBits = new ArrayList<>();
        BlockPos position;
        VerticalBlockColumn templateColumn;
        VerticalBlockColumn templateColumnTop;
        VerticalBlockColumn templateColumnBottom;
        for (int x = (int) -radius; x<=radius; x++) {
            for (int z = (int) -radius; z<=radius; z++) {
                if (x*x+z*z > radius*radius) {
                    continue;
                }
                // Get normal columns.
                templateColumnTop = null;
                templateColumnBottom = null;
                for (int i = 0; i<type.junctionBlockColumnRadii.size(); i++) {
                    if (x*x+z*z <= type.junctionBlockColumnRadii.get(i)*type.junctionBlockColumnRadii.get(i)) {
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
                    if (x*x+z*z <= type.junctionSpecialBlockColumnRadii.get(i)*type.junctionSpecialBlockColumnRadii.get(i)) {
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
                // Create bits.
                position = new BlockPos(x, 0, z);
                for (int i=0; i<templateColumn.states.length; i++) {
                    // Add bit.
                    GeoFeatureBit bit = new GeoFeatureBit(templateColumn.states[i], position.up(i-templateColumn.baseLevelIndex));
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
        NbtCompound sidewalkNbt = nbt.getCompound("sidewalk");
        for (String key : sidewalkNbt.getKeys()) {
            sidewalkPositions.add(NbtUtils.blockPosFromNbt(sidewalkNbt.getCompound(key)));
        }
        NbtCompound archNbt = nbt.getCompound("arch");
        for (String key : archNbt.getKeys()) {
            archPositions.add(NbtUtils.blockPosFromNbt(archNbt.getCompound(key)));
        }
        NbtCompound pillarNbt = nbt.getCompound("pillar");
        for (String key : pillarNbt.getKeys()) {
            pillarStartBits.add(new GeoFeatureBit(pillarNbt.getCompound(key)));
        }
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
        int i;
        nbt.put("pos", NbtUtils.blockPosToNbt(pos));
        NbtCompound sidewalkNbt = new NbtCompound();
        i=0;
        for (BlockPos pos : sidewalkPositions) {
            sidewalkNbt.put(Integer.toString(i), NbtUtils.blockPosToNbt(pos));
            i++;
        }
        nbt.put("sidewalk", sidewalkNbt);
        NbtCompound archNbt = new NbtCompound();
        i=0;
        for (BlockPos pos : archPositions) {
            archNbt.put(Integer.toString(i), NbtUtils.blockPosToNbt(pos));
            i++;
        }
        nbt.put("arch", archNbt);
        NbtCompound pillarNbt = new NbtCompound();
        i=0;
        for (GeoFeatureBit bit : pillarStartBits) {
            pillarNbt.put(Integer.toString(i), bit.toNbt());
            i++;
        }
        nbt.put("pillar", pillarNbt);
        nbt.putDouble("radius", radius);
        nbt.putDouble("sameHeightRadius", sameHeightRadius);
        nbt.putString("terrainTypeTop", terrainTypeTop);
        nbt.putString("terrainTypeBottom", terrainTypeBottom);
        return nbt;
    }
}
