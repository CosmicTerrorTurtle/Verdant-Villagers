package io.github.cosmic_terror_turtle.ctt_verdant_villagers.util;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NbtUtils {

    /**
     * Creates a new BlockPos from an NbtCompound.
     * @param nbt The compound representing a BlockPos.
     */
    public static BlockPos blockPosFromNbt(@NotNull NbtCompound nbt) {
        return new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
    }
    /**
     * Saves this BlockPos to an NbtCompound.
     * @return The compound representing this BlockPos.
     */
    public static NbtCompound blockPosToNbt(BlockPos blockPos) {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("x", blockPos.getX());
        nbt.putInt("y", blockPos.getY());
        nbt.putInt("z", blockPos.getZ());
        return nbt;
    }

    /**
     * Creates a new BlockState from an NbtCompound.
     * @param nbt The compound representing a BlockState.
     * @return A BlockState or null if the compound was empty.
     */
    public static BlockState blockStateFromNbt(@NotNull NbtCompound nbt) {
        if (!nbt.contains("blockId")) {
            return null;
        }
        BlockState state = Registries.BLOCK.get(new Identifier(nbt.getString("blockId"))).getDefaultState();
        NbtCompound propertiesNbt = nbt.getCompound("properties");
        for (Property<?> property : state.getProperties()) {
            if (propertiesNbt.contains(property.getName())) {
                state = getStateWith(state, property, propertiesNbt.getString(property.getName()));
            }
        }
        return state;
    }
    private static <T extends Comparable<T>> BlockState getStateWith(BlockState state, Property<T> property, String value) {
        return state.with(property, property.parse(value).get());
    }
    /**
     * Saves a BlockState to an NbtCompound.
     * @param state The state to be saved. If it is null, then an empty compound will be returned.
     * @return The compound representing this BlockState.
     */
    public static NbtCompound blockStateToNbt(@Nullable BlockState state) {
        NbtCompound nbt = new NbtCompound();
        if (state != null) {
            nbt.putString("blockId", Registries.BLOCK.getId(state.getBlock()).toString());
            NbtCompound propertiesNbt = new NbtCompound();
            for (Property<?> property : state.getProperties()) {
                propertiesNbt.putString(property.getName(), getPropertyValueAsString(state, property));
            }
            nbt.put("properties", propertiesNbt);
        }
        return nbt;
    }
    private static <T extends Comparable<T>> String getPropertyValueAsString(BlockState state, Property<T> property) {
        return property.name(state.get(property));
    }
}
