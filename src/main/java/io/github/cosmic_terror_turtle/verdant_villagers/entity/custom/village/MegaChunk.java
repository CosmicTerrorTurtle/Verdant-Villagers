package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import io.github.cosmic_terror_turtle.verdant_villagers.util.NbtUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MegaChunk {

    public static final int LENGTH = 128;

    public final int elementID;
    private final BlockPos lowerTip;

    /**
     * Creates a new MegaChunk.
     * @param pos A position within the new MegaChunk.
     */
    public MegaChunk(int elementID, BlockPos pos) {
        this.elementID = elementID;
        lowerTip = new BlockPos(toMegaCoord(pos.getX()), toMegaCoord(pos.getY()), toMegaCoord(pos.getZ()));
    }
    private int toMegaCoord(int coord) {
        if (coord < 0) {
            return (coord+1)/LENGTH*LENGTH-LENGTH;
        } else {
            return coord/LENGTH*LENGTH;
        }
    }

    /**
     * Scans the blocks in this mega chunk. This includes counting block types and removing trees.
     * @param blockCounts The map that the block type counts will be added to.
     * @param world The world this mega chunk exists in.
     */
    public void scanBlocks(World world, HashMap<Identifier, Integer> blockCounts, boolean removeTrees) {
        BlockPos pos;
        BlockState state;
        Identifier blockId;
        for (int i=0; i<LENGTH; i++) {
            for (int j=0; j<LENGTH; j++) {
                for (int k=0; k<LENGTH; k++) {
                    pos = lowerTip.add(i, j, k);
                    state = world.getBlockState(pos);
                    blockId = Registries.BLOCK.getId(state.getBlock());
                    blockCounts.put(blockId, blockCounts.getOrDefault(blockId, 0)+1);
                    if (removeTrees && state.isIn(BlockTags.LOGS)) {
                        world.removeBlock(pos, false);
                    }
                }
            }
        }
    }

    public BlockPos getLowerTip() {
        return lowerTip;
    }

    public boolean posIsWithin(BlockPos pos) {
        return lowerTip.getX()<=pos.getX() && pos.getX()<lowerTip.getX()+LENGTH
                && lowerTip.getY()<=pos.getY() && pos.getY()<lowerTip.getY()+LENGTH
                && lowerTip.getZ()<=pos.getZ() && pos.getZ()<lowerTip.getZ()+LENGTH;
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
