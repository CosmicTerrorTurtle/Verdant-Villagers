package io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.util.MathUtils;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.util.NbtUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

import static io.github.cosmic_terror_turtle.ctt_verdant_villagers.util.MathUtils.getCubeCoordinate;

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

    /**
     * Rotates a block position on the x-z-plane.
     * @param anchor The block position around which the rotation happens.
     * @param relPos The relative block position to be rotated.
     * @param rotation The rotation index.
     * @return An absolute block position that is the result of rotating the relative position and adding it to
     * the anchor.
     */
    public static BlockPos rotate(BlockPos anchor, BlockPos relPos, int rotation) {
        switch (rotation) {
            default -> {
                return anchor.add(relPos);
            }
            case ROTATE_COUNTER_CLOCKWISE -> {
                return anchor.add(new BlockPos(relPos.getZ(), relPos.getY(), -relPos.getX()));
            }
            case ROTATE_OPPOSITE -> {
                return anchor.add(new BlockPos(-relPos.getX(), relPos.getY(), -relPos.getZ()));
            }
            case ROTATE_CLOCKWISE -> {
                return anchor.add(new BlockPos(-relPos.getZ(), relPos.getY(), relPos.getX()));
            }
        }
    }

    public final int elementID;
    private int xMin = 0;
    private int xMax = 0;
    private int yMin = 0;
    private int yMax = 0;
    private int zMin = 0;
    private int zMax = 0;
    /**
     * A list of cubic chunks (16x16x16 blocks, represented by a BlockPos in the lower tip of the mega block).
     */
    private final ArrayList<BlockPos> boundingBoxChunks16 = new ArrayList<>();
    /**
     * A list of cubic chunks (4x4x4 blocks, represented by a BlockPos in the lower tip of the mega block).
     */
    private final ArrayList<BlockPos> boundingBoxChunks4 = new ArrayList<>();
    protected final ArrayList<GeoFeatureBit> bits = new ArrayList<>();

    public GeoFeature(int elementID) {
        this.elementID = elementID;
    }

    /**
     * Gets the cubic chunks with side length 16 defining the bounding box of this feature.
     * @return The list of chunk-like cubes.
     */
    public ArrayList<BlockPos> getBoundingBoxChunks16() {
        return boundingBoxChunks16;
    }
    /**
     * Gets the cubic chunks with side length 4 defining the bounding box of this feature.
     * @return The list of chunk-like cubes.
     */
    public ArrayList<BlockPos> getBoundingBoxChunks4() {
        return boundingBoxChunks4;
    }

    public ArrayList<GeoFeatureBit> getBits() {
        return bits;
    }

    /**
     * Updates the lists of touched cubic chunks of all side lengths.
     */
    protected void updateBoundingBoxChunks() {
        boundingBoxChunks16.clear();
        boundingBoxChunks4.clear();
        for (GeoFeatureBit bit : bits) {
            addPosToBoundingBoxChunks(bit.blockPos);
        }
    }
    /**
     * Attempts to add a cubic chunk to the lists defining the bounding box of this feature.
     * @param pos The block position of the normal block (pos will be converted to a cubic chunk position in this method).
     */
    private void addPosToBoundingBoxChunks(BlockPos pos) {
        // 16
        BlockPos newLowerTip = new BlockPos(
                getCubeCoordinate(16, pos.getX()),
                getCubeCoordinate(16, pos.getY()),
                getCubeCoordinate(16, pos.getZ()));
        boolean tipIsNew = true;
        for (BlockPos oldLowerTip : boundingBoxChunks16) {
            if (oldLowerTip.equals(newLowerTip)) {
                tipIsNew = false;
                break;
            }
        }
        if (tipIsNew) {
            boundingBoxChunks16.add(newLowerTip);
        }
        // 4
        newLowerTip = new BlockPos(
                getCubeCoordinate(4, pos.getX()),
                getCubeCoordinate(4, pos.getY()),
                getCubeCoordinate(4, pos.getZ()));
        tipIsNew = true;
        for (BlockPos oldLowerTip : boundingBoxChunks4) {
            if (oldLowerTip.equals(newLowerTip)) {
                tipIsNew = false;
                break;
            }
        }
        if (tipIsNew) {
            boundingBoxChunks4.add(newLowerTip);
        }

    }

    /**
     * Sets the bits of this feature based on the template and updates the touched mega blocks and the bounds.
     * @param relativeBits A list of bits with relative positions.
     * @param anchor The absolute position that the resulting bit positions will be relative to.
     * @param rotation The direction to rotate the template to.
     */
    protected void setBits(ArrayList<GeoFeatureBit> relativeBits, BlockPos anchor, int rotation) {
        bits.clear();
        boundingBoxChunks16.clear();
        boundingBoxChunks4.clear();

        BlockState rotatedState;
        for (GeoFeatureBit bit : relativeBits) {
            if (bit.blockState == null) {
                rotatedState = null;
            } else {
                switch (rotation) {
                    default -> rotatedState = bit.blockState;
                    case ROTATE_COUNTER_CLOCKWISE -> rotatedState = bit.blockState.rotate(BlockRotation.COUNTERCLOCKWISE_90);
                    case ROTATE_OPPOSITE -> rotatedState = bit.blockState.rotate(BlockRotation.CLOCKWISE_180);
                    case ROTATE_CLOCKWISE -> rotatedState = bit.blockState.rotate(BlockRotation.CLOCKWISE_90);
                }
            }
            bits.add(new GeoFeatureBit(rotatedState, rotate(anchor, bit.blockPos, rotation)));
        }
        for (GeoFeatureBit bit : bits) {
            addPosToBoundingBoxChunks(bit.blockPos);
        }
        updateBounds();
    }

    /**
     * Adds new {@link GeoFeatureBit}s to this feature. Updates bounding box chunks and bounds afterwards.
     * @param absoluteBits A list of bits to add that should only contain bits with new positions.
     */
    public void addBits(ArrayList<GeoFeatureBit> absoluteBits) {
        for (GeoFeatureBit bit : absoluteBits) {
            bits.add(bit);
            addPosToBoundingBoxChunks(bit.blockPos);
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
        updateBoundingBoxChunks();
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
        // Test bounds.
        if (testPos.getX() < xMin || xMax < testPos.getX()
                || testPos.getY() < yMin || yMax < testPos.getY()
                || testPos.getZ() < zMin || zMax < testPos.getZ()) {
            return false;
        }
        // Test 16 chunks.
        boolean posIsInChunks = false;
        for (BlockPos lowerTip : boundingBoxChunks16) {
            if (MathUtils.posIsInChunklikeCube(testPos, 16, lowerTip)) {
                posIsInChunks = true;
                break;
            }
        }
        if (!posIsInChunks) return false;
        // Test 4 chunks.
        posIsInChunks = false;
        for (BlockPos lowerTip : boundingBoxChunks4) {
            if (MathUtils.posIsInChunklikeCube(testPos, 4, lowerTip)) {
                posIsInChunks = true;
                break;
            }
        }
        if (!posIsInChunks) return false;
        // Test bits.
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
        NbtCompound boundingBoxChunks16Nbt = nbt.getCompound("boundingBoxChunks16");
        for (String key : boundingBoxChunks16Nbt.getKeys()) {
            boundingBoxChunks16.add(NbtUtils.blockPosFromNbt(boundingBoxChunks16Nbt.getCompound(key)));
        }
        NbtCompound boundingBoxChunks4Nbt = nbt.getCompound("boundingBoxChunks4");
        for (String key : boundingBoxChunks4Nbt.getKeys()) {
            boundingBoxChunks4.add(NbtUtils.blockPosFromNbt(boundingBoxChunks4Nbt.getCompound(key)));
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
        NbtCompound boundingBoxChunks16Nbt = new NbtCompound();
        i=0;
        for (BlockPos lowerTip : boundingBoxChunks16) {
            boundingBoxChunks16Nbt.put(Integer.toString(i), NbtUtils.blockPosToNbt(lowerTip));
            i++;
        }
        nbt.put("boundingBoxChunks16", boundingBoxChunks16Nbt);
        NbtCompound boundingBoxChunks4Nbt = new NbtCompound();
        i=0;
        for (BlockPos lowerTip : boundingBoxChunks4) {
            boundingBoxChunks4Nbt.put(Integer.toString(i), NbtUtils.blockPosToNbt(lowerTip));
            i++;
        }
        nbt.put("boundingBoxChunks4", boundingBoxChunks4Nbt);
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
