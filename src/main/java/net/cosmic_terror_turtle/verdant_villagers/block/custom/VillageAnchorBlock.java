package net.cosmic_terror_turtle.verdant_villagers.block.custom;

import net.cosmic_terror_turtle.verdant_villagers.block.ModBlocks;
import net.cosmic_terror_turtle.verdant_villagers.block.custom.entity.VillageAnchorBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VillageAnchorBlock extends BlockWithEntity {

    public VillageAnchorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlocks.VILLAGE_ANCHOR_BLOCK_ENTITY_TYPE, VillageAnchorBlockEntity::tick);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VillageAnchorBlockEntity(pos, state);
    }
}
