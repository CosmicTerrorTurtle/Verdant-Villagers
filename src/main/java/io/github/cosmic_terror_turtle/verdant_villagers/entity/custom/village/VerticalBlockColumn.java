package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import io.github.cosmic_terror_turtle.verdant_villagers.util.NbtUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VerticalBlockColumn {

    private static final VerticalBlockColumn MERGE_DEFAULT_COLUMN = new VerticalBlockColumn(
            new BlockState[]{Blocks.CRAFTING_TABLE.getDefaultState()},
            new int[]{0},
            0,
            new BlockPos(0, 0, 0)
    );

    public BlockPos anchor = new BlockPos(0, 0, 0);
    public BlockState[] states;
    public int[] ints;
    public int baseLevelIndex;

    public VerticalBlockColumn(BlockState[] states, int[] ints, int baseLevelIndex) {
        this.states = states;
        this.ints = ints;
        this.baseLevelIndex = baseLevelIndex;
    }
    public VerticalBlockColumn(BlockState[] states, int[] ints, int baseLevelIndex, BlockPos anchor) {
        this.states = states;
        this.ints = ints;
        this.baseLevelIndex = baseLevelIndex;
        this.anchor = anchor;
    }

    public VerticalBlockColumn copyWith(BlockPos anchor) {
        return new VerticalBlockColumn(states, ints, baseLevelIndex, anchor);
    }

    /**
     * Tests of two columns overlap.
     * @param column1 The first columns to test.
     * @param column2 The second columns to test.
     * @return True if the columns overlap.
     */
    public static boolean columnsOverlap(VerticalBlockColumn column1, VerticalBlockColumn column2) {
        return column1.anchor.getX()==column2.anchor.getX()
                && column1.anchor.getZ()==column2.anchor.getZ()
                && column1.anchor.getY()-column1.baseLevelIndex <= column2.anchor.getY()-column2.baseLevelIndex+column2.states.length-1
                && column2.anchor.getY()-column2.baseLevelIndex <= column1.anchor.getY()-column1.baseLevelIndex+column1.states.length-1;
    }

    /**
     * Attempts to merge two block columns. Only works if the top column sits directly on top of the bottom one or if
     *  its lowest part overlaps one of the bottom positions.
     * @param top The column on top, whose anchor will be used for the merged column.
     * @param bottom The column below, whose base level index will be used for the merged column.
     * @return A new column that has the merged states and ints of both inputs with the top column taking priority. If
     * one of the inputs is null, then the other one gets returned. If both are null, a default column gets returned.
     */
    public static VerticalBlockColumn merge(@Nullable VerticalBlockColumn top, @Nullable VerticalBlockColumn bottom) {
        if (top == null) {
            if (bottom == null) {
                return MERGE_DEFAULT_COLUMN;
            } else {
                return bottom;
            }
        } else if (bottom == null) {
            return top;
        }
        int indexDiff = bottom.baseLevelIndex - top.baseLevelIndex;
        if (indexDiff < 0 || bottom.states.length < indexDiff) {
            throw new RuntimeException("Block columns could not be merged because base level indexes violate bounds.");
        }
        BlockState[] states = new BlockState[top.states.length + indexDiff];
        int[] ints = new int[states.length];
        for (int i=0; i<states.length; i++) {
            if (i < indexDiff) {
                states[i] = bottom.states[i];
                ints[i] = bottom.ints[i];
            } else {
                states[i] = top.states[i-indexDiff];
                ints[i] = top.ints[i-indexDiff];
            }
        }
        return new VerticalBlockColumn(states, ints, bottom.baseLevelIndex, top.anchor);
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
        NbtCompound intsNbt = nbt.getCompound("ints");
        int intsLength = intsNbt.getSize();
        ints = new int[intsLength];
        for (int i=0; i<intsLength; i++) {
            ints[i] = intsNbt.getInt(Integer.toString(i));
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
        NbtCompound intsNbt = new NbtCompound();
        for (int i=0; i<ints.length; i++) {
            intsNbt.putInt(Integer.toString(i), ints[i]);
        }
        nbt.put("ints", intsNbt);
        nbt.putInt("baseLevelIndex", baseLevelIndex);
        return nbt;
    }
}
