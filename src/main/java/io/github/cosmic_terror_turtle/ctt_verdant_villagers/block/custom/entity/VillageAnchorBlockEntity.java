package io.github.cosmic_terror_turtle.ctt_verdant_villagers.block.custom.entity;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.block.ModBlocks;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.VillageHeartEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class VillageAnchorBlockEntity extends BlockEntity {

    public static final int TICKS_BETWEEN_CALLS = 100;
    /**
     * The maximum distance at which village hearts will be called by this anchor block.
     */
    public static final double MAX_CALL_DISTANCE = 100.0;

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
        if (world == null) return;

        List<VillageHeartEntity> nearHearts = world.getEntitiesByClass(
                VillageHeartEntity.class, new Box(pos).expand(MAX_CALL_DISTANCE), entity -> true);
        Vec3d center = Vec3d.ofCenter(pos);
        double bestDistance = -1.0;
        double distance;
        VillageHeartEntity closestVillageHeart = null;
        for (VillageHeartEntity villageHeart : nearHearts) {
            distance = villageHeart.squaredDistanceTo(center);
            if (closestVillageHeart != null && bestDistance <= distance) continue;
            bestDistance = distance;
            closestVillageHeart = villageHeart;
        }
        if (closestVillageHeart != null) {
            closestVillageHeart.setHoverTargetPosition(center, 1.0);
        }
    }
}
