package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import io.github.cosmic_terror_turtle.verdant_villagers.util.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

/**
 * A feature that occupies a physical space.
 */
public class GeoFeature {

    public static final int ROTATE_NOT = 0;
    public static final int ROTATE_COUNTER_CLOCKWISE = 1;
    public static final int ROTATE_OPPOSITE = 2;
    public static final int ROTATE_CLOCKWISE = 3;

    public static int getRandomRotation(Random random) {
        double p = random.nextDouble();
        if (p<0.25) {
            return ROTATE_COUNTER_CLOCKWISE;
        } else if (p<0.5) {
            return ROTATE_CLOCKWISE;
        } else if (p<0.75) {
            return ROTATE_OPPOSITE;
        } else {
            return ROTATE_NOT;
        }
    }

    private static int toMegaCoord(int coord) {
        int length = ServerVillage.MEGA_BLOCK_LENGTH;
        if (coord < 0) {
            return (coord+1)/length*length-length;
        } else {
            return coord/length*length;
        }
    }

    public final int elementID;
    private int xMin = 0;
    private int xMax = 0;
    private int yMin = 0;
    private int yMax = 0;
    private int zMin = 0;
    private int zMax = 0;
    private final ArrayList<BlockPos> touchedMegaBlocks = new ArrayList<>();
    protected final ArrayList<GeoFeatureBit> bits = new ArrayList<>();

    public GeoFeature(int elementID) {
        this.elementID = elementID;
    }

    /**
     * Returns the bounding box of this feature.
     * @return A list of the mega blocks (2x2x2 blocks, represented by a BlockPos in the lower tip of the mega block).
     */
    public ArrayList<BlockPos> getTouchedMegaBlocks() {
        return touchedMegaBlocks;
    }

    public ArrayList<GeoFeatureBit> getBits() {
        return bits;
    }

    /**
     * Updates the list of touched mega blocks.
     */
    protected void updateMegaBlocks() {
        touchedMegaBlocks.clear();
        for (GeoFeatureBit bit : bits) {
            addPosToTouchedMegaBlocks(bit.blockPos);
        }
    }
    /**
     * Attempts to add a new mega block to the list.
     * @param pos The block position of a normal block (pos will be converted to a mega block position in this method).
     */
    private void addPosToTouchedMegaBlocks(BlockPos pos) {
        pos = new BlockPos(toMegaCoord(pos.getX()), toMegaCoord(pos.getY()), toMegaCoord(pos.getZ()));
        for (BlockPos megaPos : touchedMegaBlocks) {
            if (megaPos.equals(pos)) {
                return;
            }
        }
        touchedMegaBlocks.add(pos);
    }

    /**
     * Sets the bits of this feature based on the template and updates the touched mega blocks and the bounds.
     * @param relativeBits A list of bits with relative positions.
     * @param anchor The absolute position that the resulting bit positions will be relative to.
     * @param rotation The direction to rotate the template to.
     */
    protected void setBits(ArrayList<GeoFeatureBit> relativeBits, BlockPos anchor, int rotation) {
        bits.clear();
        touchedMegaBlocks.clear();

        for (GeoFeatureBit bit : relativeBits) {
            if (bit.blockState == null) {
                bits.add(new GeoFeatureBit(null, anchor.add(bit.blockPos)));
            } else {
                switch (rotation) {
                    default -> bits.add(new GeoFeatureBit(
                            bit.blockState,
                            anchor.add(bit.blockPos)
                    ));
                    case ROTATE_COUNTER_CLOCKWISE -> bits.add(new GeoFeatureBit(
                            bit.blockState.rotate(BlockRotation.COUNTERCLOCKWISE_90),
                            anchor.add(new BlockPos(bit.blockPos.getZ(), bit.blockPos.getY(), -bit.blockPos.getX()))
                    ));
                    case ROTATE_OPPOSITE -> bits.add(new GeoFeatureBit(
                            bit.blockState.rotate(BlockRotation.CLOCKWISE_180),
                            anchor.add(new BlockPos(-bit.blockPos.getX(), bit.blockPos.getY(), -bit.blockPos.getZ()))
                    ));
                    case ROTATE_CLOCKWISE -> bits.add(new GeoFeatureBit(
                            bit.blockState.rotate(BlockRotation.CLOCKWISE_90),
                            anchor.add(new BlockPos(-bit.blockPos.getZ(), bit.blockPos.getY(), bit.blockPos.getX()))
                    ));
                }
            }
        }
        for (GeoFeatureBit bit : bits) {
            addPosToTouchedMegaBlocks(bit.blockPos);
        }

        updateBounds();
    }

    /**
     * Adds new {@link GeoFeatureBit}s to this feature. Updates mega blocks and bounds afterwards.
     * @param absoluteBits A list of bits to add that should only contain bits with new positions.
     */
    public void addBits(ArrayList<GeoFeatureBit> absoluteBits) {
        for (GeoFeatureBit bit : absoluteBits) {
            bits.add(bit);
            addPosToTouchedMegaBlocks(bit.blockPos);
        }
        updateBounds();
    }

    /**
     * Removes all bits that match the given positions from this feature. Updates bounds and mega blocks afterwards.
     * @param absolutePositions The list of positions to be removed.
     */
    public void removeBits(ArrayList<BlockPos> absolutePositions) {
        ArrayList<GeoFeatureBit> toBeRemoved = new ArrayList<>();
        for (GeoFeatureBit bit : bits) {
            if (absolutePositions.contains(bit.blockPos)) {
                toBeRemoved.add(bit);
            }
        }
        bits.removeAll(toBeRemoved);
        updateBounds();
        updateMegaBlocks();
    }

    /**
     * Updates the coordinate bounds that this feature occupies.
     */
    public void updateBounds() {
        if (bits.isEmpty()) {
            return;
        }
        GeoFeatureBit firstBit = bits.get(0);
        xMin = xMax = firstBit.blockPos.getX();
        yMin = yMax = firstBit.blockPos.getY();
        zMin = zMax = firstBit.blockPos.getZ();
        for (GeoFeatureBit bit : bits) {
            if (bit.blockPos.getX() < xMin) {
                xMin = bit.blockPos.getX();
            } else if (bit.blockPos.getX() > xMax) {
                xMax = bit.blockPos.getX();
            }
            if (bit.blockPos.getY() < yMin) {
                yMin = bit.blockPos.getY();
            } else if (bit.blockPos.getY() > yMax) {
                yMax = bit.blockPos.getY();
            }
            if (bit.blockPos.getZ() < zMin) {
                zMin = bit.blockPos.getZ();
            } else if (bit.blockPos.getZ() > zMax) {
                zMax = bit.blockPos.getZ();
            }
        }
    }

    /**
     * Checks if the bounds of this feature collide with the bounds of another one (bounds = a cuboid defined by xMin, xMax, yMin, yMax, zMin, zMax).
     * @param feature The other feature.
     * @return If the bounds overlap.
     */
    public boolean boundsCollideWith(GeoFeature feature) {
        return xMin <= feature.xMax && feature.xMin <= xMax
                && yMin <= feature.yMax && feature.yMin <= yMax
                && zMin <= feature.zMax && feature.zMin <= zMax;
    }

    /**
     * Checks if a given position matches a position of this feature's bits.
     * @param testPos The block position to test.
     * @return True if a match was found, false otherwise.
     */
    public boolean bitsCollideWith(BlockPos testPos) {
        if (testPos.getX() < xMin || xMax < testPos.getX()
                || testPos.getY() < yMin || yMax < testPos.getY()
                || testPos.getZ() < zMin || zMax < testPos.getZ()) {
            return false;
        }
        for (GeoFeatureBit bit : bits) {
            if (bit.blockPos.equals(testPos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new GeoFeature from an NbtCompound.
     * @param nbt The compound representing a GeoFeature.
     */
    public GeoFeature(@NotNull NbtCompound nbt) {
        elementID = nbt.getInt("id");
        NbtCompound megaBlocksNbt = nbt.getCompound("megaBlocks");
        for (String key : megaBlocksNbt.getKeys()) {
            touchedMegaBlocks.add(NbtUtils.blockPosFromNbt(megaBlocksNbt.getCompound(key)));
        }
        NbtCompound bitsNbt = nbt.getCompound("bits");
        for (String key : bitsNbt.getKeys()) {
            bits.add(new GeoFeatureBit(bitsNbt.getCompound(key)));
        }

        updateBounds();
    }
    /**
     * Saves this GeoFeature to an NbtCompound.
     * @return The compound representing this GeoFeature.
     */
    public NbtCompound toNbt() {
        int i;

        NbtCompound nbt = new NbtCompound();
        nbt.putInt("id", elementID);
        NbtCompound megaBlocksNbt = new NbtCompound();
        i=0;
        for (BlockPos megaBlock : touchedMegaBlocks) {
            megaBlocksNbt.put(Integer.toString(i), NbtUtils.blockPosToNbt(megaBlock));
            i++;
        }
        nbt.put("megaBlocks", megaBlocksNbt);
        NbtCompound bitsNbt = new NbtCompound();
        i=0;
        for (GeoFeatureBit bit : bits) {
            bitsNbt.put(Integer.toString(i), bit.toNbt());
            i++;
        }
        nbt.put("bits", bitsNbt);
        return nbt;
    }
}
