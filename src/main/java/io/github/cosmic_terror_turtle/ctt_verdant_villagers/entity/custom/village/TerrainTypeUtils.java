package io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.geo_feature.road.RoadType;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.util.ModTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class TerrainTypeUtils {

    public static final String ABOVE_TYPE_AIR = "air";
    public static final String ABOVE_TYPE_FLUID = "fluid";
    public static final String ABOVE_TYPE_TERRAIN = "terrain";

    public static final String BELOW_TYPE_AIR = "air";
    public static final String BELOW_TYPE_AIR_ABOVE_TERRAIN = "air_above_terrain";
    public static final String BELOW_TYPE_FLUID = "fluid";
    public static final String BELOW_TYPE_TERRAIN = "terrain";

    /**
     * Determines the terrain type used for geo features at the given position.
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
                return ABOVE_TYPE_FLUID;
            } else if (terrain >= 0.4*total*startPositions.size()) {
                return ABOVE_TYPE_TERRAIN;
            }
            return ABOVE_TYPE_AIR;
        } else {
            // Bottom
            // Return fluid if the first two blocks below startPos are fluid (the block at startPos is ignored).
            if (!world.getFluidState(startPos.down()).isEmpty()
                    && !world.getFluidState(startPos.down(2)).isEmpty()) {
                return BELOW_TYPE_FLUID;
            }
            // Return air when no terrain was found.
            for (int i=0; i<ServerVillage.ROAD_PILLAR_EXTENSION_LENGTH; i++) {
                pos = startPos.down(i);
                if (world.getBlockState(pos).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS)) {
                    terrain++;
                }
            }
            if (terrain == 0) {
                return BELOW_TYPE_AIR;
            }
            // Terrain was found.
            // If the first two blocks below startPos are not terrain, it is air above terrain (the block at startPos
            // is ignored).
            if (!world.getBlockState(startPos.down()).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS)
                    && !world.getBlockState(startPos.down(2)).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS)) {
                return BELOW_TYPE_AIR_ABOVE_TERRAIN;
            }
            // It's probably terrain.
            return BELOW_TYPE_TERRAIN;
        }
    }
}
