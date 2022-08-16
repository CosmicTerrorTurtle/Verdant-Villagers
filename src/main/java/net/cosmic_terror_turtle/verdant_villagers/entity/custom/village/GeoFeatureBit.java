package net.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import net.cosmic_terror_turtle.verdant_villagers.util.NbtUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A single block state bit for a geo feature.
 */
public class GeoFeatureBit {

    @Nullable
    public BlockState blockState;
    public BlockPos blockPos;

    /**
     * Creates a new GeoFeatureBit.
     * @param blockState The block state of this bit.
     * @param blockPos The position of this bit.
     */
    public GeoFeatureBit(@Nullable BlockState blockState, BlockPos blockPos) {
        this.blockState = blockState;
        this.blockPos = blockPos;
    }

    /**
     * Creates a new GeoFeatureBit from an NbtCompound.
     * @param nbt The compound representing a GeoFeatureBit.
     */
    public GeoFeatureBit(@NotNull NbtCompound nbt) {
        blockState = NbtUtils.blockStateFromNbt(nbt.getCompound("blockState"));
        blockPos = NbtUtils.blockPosFromNbt(nbt.getCompound("blockPos"));
    }
    /**
     * Saves this GeoFeatureBit to an NbtCompound.
     * @return The compound representing this GeoFeatureBit.
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("blockState", NbtUtils.blockStateToNbt(blockState));
        nbt.put("blockPos", NbtUtils.blockPosToNbt(blockPos));
        return nbt;
    }
}
