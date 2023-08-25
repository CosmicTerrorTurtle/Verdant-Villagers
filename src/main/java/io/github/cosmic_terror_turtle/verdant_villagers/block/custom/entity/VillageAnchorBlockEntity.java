package io.github.cosmic_terror_turtle.verdant_villagers.block.custom.entity;

import io.github.cosmic_terror_turtle.verdant_villagers.block.ModBlocks;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.VillageHeartEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class VillageAnchorBlockEntity extends BlockEntity {

    public static final int TICKS_BETWEEN_CALLS = 100;

    private int ticksSinceLastCall;

    public VillageAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.VILLAGE_ANCHOR_BLOCK_ENTITY_TYPE, pos, state);

        ticksSinceLastCall = 0;
    }

    public static void tick(World world, BlockPos pos, BlockState state, VillageAnchorBlockEntity entity) {
        // Only update on the server
        if(world!=null && !world.isClient()){

            entity.ticksSinceLastCall++;
            if (entity.ticksSinceLastCall > TICKS_BETWEEN_CALLS) {
                entity.ticksSinceLastCall = 0;

                if (world.random.nextBoolean()) entity.callVillageHeart();
            }
        }
    }

    /**
     * Searches for the nearest village heart within a box and orders it to move towards a position above this block.
     */
    private void callVillageHeart() {
        if (world != null) {
            List<VillageHeartEntity> list = world.getEntitiesByClass(VillageHeartEntity.class, new Box(pos).expand(VillageHeartEntity.MAX_ANCHOR_CALL_DISTANCE), entity -> true);
            double bestD = -1.0;
            double d;
            VillageHeartEntity closestVillageHeart = null;
            for (VillageHeartEntity villageHeart : list) {
                d = villageHeart.squaredDistanceTo(Vec3d.ofCenter(pos));
                if (closestVillageHeart != null && bestD <= d) continue;
                bestD = d;
                closestVillageHeart = villageHeart;
            }
            if (closestVillageHeart != null) {
                closestVillageHeart.setTargetPosition(Vec3d.ofCenter(pos).add(0, VillageHeartEntity.ANCHOR_HOVER_HEIGHT, 0), 1.0);
            }
        }
    }

}
