package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road;

import io.github.cosmic_terror_turtle.verdant_villagers.data.village.RawRoadType;
import io.github.cosmic_terror_turtle.verdant_villagers.data.village.RawVerticalBlockColumn;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.ServerVillage;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.VerticalBlockColumn;
import io.github.cosmic_terror_turtle.verdant_villagers.util.ModTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoadType {

    public double scale;

    public double edgeRadius;
    ArrayList<Double> edgeBlockColumnRadii;
    HashMap<String, HashMap<String, ArrayList<VerticalBlockColumn>>> edgeTemplateBlockColumns;
    double edgeSpecialColumnSpace;
    ArrayList<Double> edgeSpecialBlockColumnRadii;
    HashMap<String, HashMap<String, ArrayList<VerticalBlockColumn>>> edgeSpecialTemplateBlockColumns;

    public double junctionRadius;
    public double junctionSameHeightRadius;
    ArrayList<Double> junctionBlockColumnRadii;
    HashMap<String, HashMap<String, ArrayList<VerticalBlockColumn>>> junctionTemplateBlockColumns;
    ArrayList<Double> junctionSpecialBlockColumnRadii;
    HashMap<String, HashMap<String, HashMap<String, ArrayList<VerticalBlockColumn>>>> junctionSpecialTemplateBlockColumns;

    public RoadType(ServerVillage village, RawRoadType rawRoadType) {
        // Edge
        scale = rawRoadType.scale;
        edgeRadius = 0.0;
        edgeBlockColumnRadii = rawRoadType.edgeBlockColumnRadii;
        for (Double d : edgeBlockColumnRadii) {
            if (d > edgeRadius) {
                edgeRadius = d;
            }
        }
        edgeTemplateBlockColumns = new HashMap<>();
        for (Map.Entry<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> entry1 : rawRoadType.edgeTemplateBlockColumns.entrySet()) {
            edgeTemplateBlockColumns.put(entry1.getKey(), new HashMap<>());
            for (Map.Entry<String, ArrayList<RawVerticalBlockColumn>> entry2 : entry1.getValue().entrySet()) {
                edgeTemplateBlockColumns.get(entry1.getKey()).put(entry2.getKey(), new ArrayList<>());
                for (RawVerticalBlockColumn raw : entry2.getValue()) {
                    edgeTemplateBlockColumns.get(entry1.getKey()).get(entry2.getKey()).add(raw.toVerticalBlockColumn(village));
                }
            }
        }
        edgeSpecialColumnSpace = rawRoadType.edgeSpecialColumnSpace;
        edgeSpecialBlockColumnRadii = rawRoadType.edgeSpecialBlockColumnRadii;
        for (Double d : edgeSpecialBlockColumnRadii) {
            if (d > edgeRadius) {
                edgeRadius = d;
            }
        }
        edgeSpecialTemplateBlockColumns = new HashMap<>();
        for (Map.Entry<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> entry1 : rawRoadType.edgeSpecialTemplateBlockColumns.entrySet()) {
            edgeSpecialTemplateBlockColumns.put(entry1.getKey(), new HashMap<>());
            for (Map.Entry<String, ArrayList<RawVerticalBlockColumn>> entry2 : entry1.getValue().entrySet()) {
                edgeSpecialTemplateBlockColumns.get(entry1.getKey()).put(entry2.getKey(), new ArrayList<>());
                for (RawVerticalBlockColumn raw : entry2.getValue()) {
                    edgeSpecialTemplateBlockColumns.get(entry1.getKey()).get(entry2.getKey()).add(raw.toVerticalBlockColumn(village));
                }
            }
        }
        // Junction
        junctionRadius = 0.0;
        junctionBlockColumnRadii = rawRoadType.junctionBlockColumnRadii;
        if (junctionBlockColumnRadii != null) {
            for (Double d : junctionBlockColumnRadii) {
                if (d > junctionRadius) {
                    junctionRadius = d;
                }
            }
        }
        junctionTemplateBlockColumns = new HashMap<>();
        if (rawRoadType.junctionTemplateBlockColumns != null) {
            for (Map.Entry<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> entry1 : rawRoadType.junctionTemplateBlockColumns.entrySet()) {
                junctionTemplateBlockColumns.put(entry1.getKey(), new HashMap<>());
                for (Map.Entry<String, ArrayList<RawVerticalBlockColumn>> entry2 : entry1.getValue().entrySet()) {
                    junctionTemplateBlockColumns.get(entry1.getKey()).put(entry2.getKey(), new ArrayList<>());
                    for (RawVerticalBlockColumn raw : entry2.getValue()) {
                        junctionTemplateBlockColumns.get(entry1.getKey()).get(entry2.getKey()).add(raw.toVerticalBlockColumn(village));
                    }
                }
            }
        }
        junctionSpecialBlockColumnRadii = rawRoadType.junctionSpecialBlockColumnRadii;
        if (junctionSpecialBlockColumnRadii != null) {
            for (Double d : junctionSpecialBlockColumnRadii) {
                if (d > junctionRadius) {
                    junctionRadius = d;
                }
            }
        }
        junctionSpecialTemplateBlockColumns = new HashMap<>();
        if (rawRoadType.junctionSpecialTemplateBlockColumns != null) {
            for (Map.Entry<String, HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>>> entry1 : rawRoadType.junctionSpecialTemplateBlockColumns.entrySet()) {
                junctionSpecialTemplateBlockColumns.put(entry1.getKey(), new HashMap<>());
                for (Map.Entry<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> entry2 : entry1.getValue().entrySet()) {
                    junctionSpecialTemplateBlockColumns.get(entry1.getKey()).put(entry2.getKey(), new HashMap<>());
                    for (Map.Entry<String, ArrayList<RawVerticalBlockColumn>> entry3 : entry2.getValue().entrySet()) {
                        junctionSpecialTemplateBlockColumns.get(entry1.getKey()).get(entry2.getKey()).put(entry3.getKey(), new ArrayList<>());
                        for (RawVerticalBlockColumn raw : entry3.getValue()) {
                            junctionSpecialTemplateBlockColumns.get(entry1.getKey()).get(entry2.getKey()).get(entry3.getKey()).add(raw.toVerticalBlockColumn(village));
                        }
                    }
                }
            }
        }
        junctionSameHeightRadius = 1.6 * junctionRadius;
    }

    /**
     * Determines the terrain type used for road junctions and edges at the given position.
     * @param top If true, the terrain above {@code pos} will be analyzed, otherwise the terrain below.
     * @param world The world of {@code pos}.
     * @param startPos The position from which the terrain scan should start. This is usually the height of the road's
     *                 upmost surface blocks.
     * @param radius The maximum x- or z-distance the scanned blocks will have to {@code startPos}.
     * @return A String representing the terrain type that can be used to access the template columns and radii of
     * {@link RoadType}.
     */
    public static String getTerrainType(boolean top, World world, BlockPos startPos, int radius) {
        BlockPos pos;
        int terrain = 0;
        if (top) {
            // Top (check the four positions around startPos as well)
            int distance;
            ArrayList<BlockPos> startPositions = new ArrayList<>();
            startPositions.add(startPos);
            for (float percentage : new float[]{0.34f, 0.67f, 1.0f}) {
                distance = (int) (percentage * radius);
                startPositions.add(startPos.north(distance));
                startPositions.add(startPos.north(distance).east(distance));
                startPositions.add(startPos.east(distance));
                startPositions.add(startPos.east(distance).south(distance));
                startPositions.add(startPos.south(distance));
                startPositions.add(startPos.south(distance).west(distance));
                startPositions.add(startPos.west(distance));
                startPositions.add(startPos.west(distance).north(distance));
            }
            int total = 10;
            int fluid = 0;
            for (int i=1; i<=total; i++) {
                for (BlockPos p : startPositions) {
                    pos = p.up(i);
                    if (world.getBlockState(pos).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS)) {
                        terrain++;
                    } else if (!world.getBlockState(pos).getFluidState().isEmpty()) {
                        fluid++;
                    }
                }
            }
            // Return fluid if at least one of the blocks above are fluids.
            if (fluid > 0) {
                return "fluid";
            } else if (terrain >= 0.4*total*startPositions.size()) {
                return "terrain";
            }
            return "air";
        } else {
            // Bottom
            // Return fluid if the first two blocks below startPos are fluid (the block at startPos is ignored).
            if (!world.getFluidState(startPos.down()).isEmpty()
                    && !world.getFluidState(startPos.down(2)).isEmpty()) {
                return "fluid";
            }
            // Return air when no terrain was found.
            for (int i=0; i<ServerVillage.ROAD_PILLAR_EXTENSION_LENGTH; i++) {
                pos = startPos.down(i);
                if (world.getBlockState(pos).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS)) {
                    terrain++;
                }
            }
            if (terrain == 0) {
                return "air";
            }
            // Terrain was found.
            // If the first two blocks below startPos are not terrain, it is air above terrain (the block at startPos
            // is ignored).
            if (!world.getBlockState(startPos.down()).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS)
                    && !world.getBlockState(startPos.down(2)).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS)) {
                return "air_above_terrain";
            }
            // It's probably terrain.
            return "terrain";
        }
    }
}
