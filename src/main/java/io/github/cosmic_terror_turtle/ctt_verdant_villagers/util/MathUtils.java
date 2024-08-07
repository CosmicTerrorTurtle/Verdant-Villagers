package io.github.cosmic_terror_turtle.ctt_verdant_villagers.util;

import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class MathUtils {

    private static final Random random = new Random();

    public static Random getRandom() {
        return random;
    }

    /**
     * Generates a random int from within the given bounds.
     * @param min Minimum value - inclusive.
     * @param max Maximum value - inclusive.
     * @return A random int.
     */
    public static int nextInt(int min, int max) {
        return min + random.nextInt(1 + max - min);
    }

    /**
     * Converts a block coordinate into the lowest value within a chunk-like cube.
     * @param cubeSideLength The length that divides the coordinate axis into sections. For cubic chunks, this would be 16.
     * @param coordinate The block coordinate (can be any axis).
     * @return The respective coordinate of the chunk-like cube.
     */
    public static int getCubeCoordinate(int cubeSideLength, int coordinate) {
        if (coordinate < 0) {
            return (coordinate+1)/cubeSideLength*cubeSideLength-cubeSideLength;
        } else {
            return coordinate/cubeSideLength*cubeSideLength;
        }
    }

    /**
     * Determines if a block position is within a cube-like chunk.
     * @param pos The block position to test.
     * @param cubeSideLength The length that divides the coordinate axis into sections. For cubic chunks, this would be 16.
     * @param cubeLowerTip The block position of the lower tip of the cube (meaning the lowest x, y and z coordinates
     *                     that are within the chunk).
     * @return True if the given position is within the chunk.
     */
    public static boolean posIsInChunklikeCube(BlockPos pos, int cubeSideLength, BlockPos cubeLowerTip) {
        return cubeLowerTip.getX() <= pos.getX() && pos.getX() < cubeLowerTip.getX() + cubeSideLength
                && cubeLowerTip.getY() <= pos.getY() && pos.getY() < cubeLowerTip.getY() + cubeSideLength
                && cubeLowerTip.getZ() <= pos.getZ() && pos.getZ() < cubeLowerTip.getZ() + cubeSideLength;
    }
}
