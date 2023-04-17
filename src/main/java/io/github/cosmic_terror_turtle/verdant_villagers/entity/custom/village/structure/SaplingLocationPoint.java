package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure;

import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class SaplingLocationPoint extends PointOfInterest {

    public int saplings;

    /**
     * Creates a new {@link SaplingLocationPoint}.
     * @param pos The position of this point (can be relative).
     * @param saplings The number of neighboring {@link SaplingLocationPoint}s that should be used to grow a tree together,
     *                 for example 1 for oak trees, 4 for dark oak trees etc.
     */
    public SaplingLocationPoint(BlockPos pos, int saplings) {
        super(pos);
        this.saplings = saplings;
    }

    @Override
    public PointOfInterest copy() {
        return new SaplingLocationPoint(new BlockPos(pos), saplings);
    }

    /**
     * Creates a new {@link SaplingLocationPoint} from an NbtCompound.
     * @param nbt The compound representing a {@link SaplingLocationPoint}.
     */
    public SaplingLocationPoint(@NotNull NbtCompound nbt) {
        super(nbt);
        saplings = Integer.parseInt(nbt.getString("saplings"));
    }
    /**
     * Saves this {@link SaplingLocationPoint} to an NbtCompound.
     * @return The compound representing this {@link SaplingLocationPoint}.
     */
    @Override
    public NbtCompound toNbt() {
        NbtCompound nbt = super.toNbt();
        nbt.putString("subclass", "SaplingLocationPoint");
        nbt.putString("saplings", String.valueOf(saplings));
        return nbt;
    }
}
