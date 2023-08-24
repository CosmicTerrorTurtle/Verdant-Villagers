package io.github.cosmic_terror_turtle.verdant_villagers.util;

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
}
