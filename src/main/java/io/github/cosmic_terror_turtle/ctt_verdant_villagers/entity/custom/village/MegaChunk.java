package io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.util.ModTags;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.util.NbtUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import static io.github.cosmic_terror_turtle.ctt_verdant_villagers.util.MathUtils.getCubeCoordinate;

public class MegaChunk {

    /**
     * The side length of a {@link MegaChunk}.
     */
    public static final int LENGTH = 128;

    public final int elementID;
    private final BlockPos lowerTip;

    /**
     * Creates a new MegaChunk.
     * @param pos A position within the new MegaChunk.
     */
    public MegaChunk(int elementID, BlockPos pos) {
        this.elementID = elementID;
        lowerTip = new BlockPos(
                getCubeCoordinate(LENGTH, pos.getX()),
                getCubeCoordinate(LENGTH, pos.getY()),
                getCubeCoordinate(LENGTH, pos.getZ())
        );
    }

    /**
     * Scans the blocks in this mega chunk. This includes counting block types, scanning for blocks to mine and
     * possibly removing them.
     * @param world The world this mega chunk exists in.
     * @param blockCounts The map that the block type counts will be added to.
     * @param removeBlocksImmediately Whether blocks found to mine should be immediately removed.
     */
    public void scanBlocks(World world, HashMap<Identifier, Integer> blockCounts, boolean removeBlocksImmediately) {
        BlockPos pos;
        BlockState state;
        Identifier blockId;
        ArrayList<BlockPos> toRemoveImmediately = new ArrayList<>();
        for (int i=0; i<LENGTH; i++) {
            for (int j=0; j<LENGTH; j++) {
                for (int k=0; k<LENGTH; k++) {
                    pos = lowerTip.add(i, j, k);
                    state = world.getBlockState(pos);
                    blockId = Registries.BLOCK.getId(state.getBlock());
                    blockCounts.put(blockId, blockCounts.getOrDefault(blockId, 0)+1);
                    // Check for block to mine and add them to the village's list or remove them immediately.
                    if (state.isIn(ModTags.Blocks.VILLAGE_TREE_BLOCKS)) {
                        if (removeBlocksImmediately) {
                            toRemoveImmediately.add(pos);
                        }
                    }
                }
            }
        }
        for (BlockPos removePos : toRemoveImmediately) {
            world.removeBlock(removePos, false);
        }
    }

    public BlockPos getLowerTip() {
        return lowerTip;
    }

    /**
     * Creates a {@link Box} which encompasses the entire {@link MegaChunk}.
     * @return The {@link Box}.
     */
    public Box getBox() {
        return new Box(lowerTip, lowerTip.add(LENGTH, LENGTH, LENGTH));
    }

    /**
     * Creates a new MegaChunk from an NbtCompound.
     * @param nbt The compound representing a MegaChunk.
     */
    public MegaChunk(@NotNull NbtCompound nbt) {
        elementID = nbt.getInt("id");
        lowerTip = NbtUtils.blockPosFromNbt(nbt.getCompound("lowerTip"));
    }

    /**
     * Saves this MegaChunk to an NbtCompound.
     * @return The compound representing this MegaChunk.
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("id", elementID);
        nbt.put("lowerTip", NbtUtils.blockPosToNbt(lowerTip));
        return nbt;
    }
}
