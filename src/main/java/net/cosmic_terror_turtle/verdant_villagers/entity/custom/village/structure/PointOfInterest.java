package net.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure;

import net.cosmic_terror_turtle.verdant_villagers.util.NbtUtils;
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
