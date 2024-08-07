package io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.structure;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.GeoFeature;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.util.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class PointOfInterest {

    public BlockPos pos;

    public PointOfInterest(BlockPos pos) {
        this.pos = pos;
    }

    public PointOfInterest copy() {
        return new PointOfInterest(new BlockPos(pos));
    }

    /**
     * Turns all block positions from relative positions to absolute ones while also rotating.
     * @param anchor The block position that this {@link PointOfInterest}'s positions are relative to.
     * @param rotation How much the relative positions will be rotated around {@code  anchor}.
     */
    public void setToAbsolutePositions(BlockPos anchor, int rotation) {
        pos = GeoFeature.rotate(anchor, pos, rotation);
    }

    /**
     * Creates a new PointOfInterest from an NbtCompound.
     * @param nbt The compound representing a PointOfInterest.
     */
    public PointOfInterest(@NotNull NbtCompound nbt) {
        pos = NbtUtils.blockPosFromNbt(nbt.getCompound("pos"));
    }
    /**
     * Saves this PointOfInterest to an NbtCompound.
     * @return The compound representing this PointOfInterest.
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("pos", NbtUtils.blockPosToNbt(pos));
        return nbt;
    }
}
