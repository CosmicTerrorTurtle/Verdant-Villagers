package io.github.cosmic_terror_turtle.verdant_villagers.util;

import java.util.Random;

public class MathUtils {

    private static final Random random = new Random();

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
     * Determines a random value that is uniformly distributed between {@code avg}*(1-{@code fraction}) (inclusive) and
     * {@code avg}(1+{@code fraction}) (exclusive).
     * @param avg The average value.
     * @param fraction The fraction that determines the range.
     * @return The value.
     */
    public static double getRand(double avg, double fraction) {
        return avg * (1 + random.nextDouble(-fraction, fraction));
    }
}
