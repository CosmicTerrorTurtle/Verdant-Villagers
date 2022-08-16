package net.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class GeoFeatureBitOption extends GeoFeatureBit {

    private final Random random;
    private final BlockState[] options;

    /**
     * Creates a new GeoFeatureBitOption.
     *
     * @param options The block state options of this bit.
     * @param blockPos   The position of this bit.
     */
    public GeoFeatureBitOption(Random random, BlockState[] options, BlockPos blockPos) {
        super(options[0], blockPos);
        this.random = random;
        this.options = options;
        randomize();
    }

    /**
     * Randomizes the selected block state.
     */
    public void randomize() {
        blockState = options[random.nextInt(options.length)];
    }
}
