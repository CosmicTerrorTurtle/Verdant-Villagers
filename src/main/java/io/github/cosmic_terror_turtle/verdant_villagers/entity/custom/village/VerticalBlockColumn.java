package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import io.github.cosmic_terror_turtle.verdant_villagers.util.NbtUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class VerticalBlockColumn {

    public BlockPos anchor = new BlockPos(0, 0, 0);
    public BlockState[] states;
    public int baseLevelIndex;

    public VerticalBlockColumn(BlockState[] baseStates, int baseLevelIndex, BlockState topState, int addOnTopCount) {
        states = new BlockState[baseStates.length+addOnTopCount];
        for (int i=0; i<states.length; i++) {
            if (i<baseStates.length) {
                states[i] = baseStates[i];
            } else {
                states[i] = topState;
            }
        }
        this.baseLevelIndex = baseLevelIndex;
    }
    public VerticalBlockColumn(BlockState[] states, int baseLevelIndex) {
        this.states = states;
        this.baseLevelIndex = baseLevelIndex;
    }
    public VerticalBlockColumn(BlockState[] states, int baseLevelIndex, BlockPos anchor) {
        this.states = states;
        this.baseLevelIndex = baseLevelIndex;
        this.anchor = anchor;
    }

    public VerticalBlockColumn copyWith(BlockPos anchor) {
        return new VerticalBlockColumn(states, baseLevelIndex, anchor);
    }

    /**
     * Creates a new VerticalBlockColumn from an NbtCompound.
     * @param nbt The compound representing a VerticalBlockColumn.
     */
    public VerticalBlockColumn(@NotNull NbtCompound nbt) {
        anchor = NbtUtils.blockPosFromNbt(nbt.getCompound("anchor"));
        NbtCompound statesNbt = nbt.getCompound("states");
        int statesLength = statesNbt.getSize();
        states = new BlockState[statesLength];
        for (int i=0; i<statesLength; i++) {
            states[i] = NbtUtils.blockStateFromNbt(statesNbt.getCompound(Integer.toString(i)));
        }
        baseLevelIndex = nbt.getInt("baseLevelIndex");
    }
    /**
     * Saves this VerticalBlockColumn to an NbtCompound.
     * @return The compound representing this VerticalBlockColumn.
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("anchor", NbtUtils.blockPosToNbt(anchor));
        NbtCompound statesNbt = new NbtCompound();
        for (int i=0; i<states.length; i++) {
            statesNbt.put(Integer.toString(i), NbtUtils.blockStateToNbt(states[i]));
        }
        nbt.put("states", statesNbt);
        nbt.putInt("baseLevelIndex", baseLevelIndex);
        return nbt;
    }
}
