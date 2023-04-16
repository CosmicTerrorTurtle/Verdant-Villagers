package io.github.cosmic_terror_turtle.verdant_villagers.data.village;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.GeoFeatureBit;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.GeoFeatureBitOption;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.ServerVillage;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.StructureTemplate;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;

public class BlockStateParsing {

    public static final String COLLISION_SPACE = "collision_space";

    /**
     * Parses a block state cube for a new {@link StructureTemplate}. Each entry of {@code blockStateCube} can either be {@code null},
     * {@link BlockStateParsing#COLLISION_SPACE}, 'random----value1----value2 etc.' or just a value, where value is specified by
     * {@link BlockStateParsing#parseBlockState}. {@code null} values will not be converted into {@link GeoFeatureBit}s; collision
     * space means that the village will ignore that position for block placing/removing, but will add it to the structure hit box.
     * @param blockStateCube The string cube of block states.
     * @param center A list that contains the x, y and z coordinate of the cube's center point.
     * @param village The village for which the structure template is being created.
     * @return The list of parsed {@link GeoFeatureBit}s with positions relative to {@code center}.
     */
    public static ArrayList<GeoFeatureBit> parseBlockStateCube(String[][][] blockStateCube, ArrayList<Integer> center, ServerVillage village) {

        ArrayList<GeoFeatureBit> bits = new ArrayList<>();
        int centerX = center.get(0);
        int centerY = center.get(1);
        int centerZ = center.get(2);

        String[] split;
        BlockState[] options;
        for (int i = 0; i<blockStateCube.length; i++) {
            for (int j = 0; j<blockStateCube[i].length; j++) {
                for (int k = 0; k<blockStateCube[i][j].length; k++) {
                    // Fetch block states from the registry and create the bits.
                    if (blockStateCube[i][j][k] != null) {
                        split = blockStateCube[i][j][k].split("----");
                        if (split[0].equals("random")) {
                            options = new BlockState[split.length-1];
                            for (int l=1; l<split.length; l++) {
                                options[l-1] = BlockStateParsing.parseBlockState(split[l], village);
                            }
                            bits.add(new GeoFeatureBitOption(
                                    village.random, options, new BlockPos(i-centerX, j-centerY, k-centerZ)
                            ));
                        } else {
                            bits.add(new GeoFeatureBit(
                                    BlockStateParsing.parseBlockState(blockStateCube[i][j][k], village), new BlockPos(i-centerX, j-centerY, k-centerZ)
                            ));
                        }
                    }
                }
            }
        }

        return bits;
    }

    /**
     * Parses a block state from a string. Can be a plain state, a palette state or collision space, defined by
     * 'state:plain_state_string', 'palette:palette_index:mod_namespace:palette_path___plain_state_string' or
     * {@link BlockStateParsing#COLLISION_SPACE}, where plain_state_string is as specified by
     * {@link BlockStateParsing#parsePlainBlockState}. An example for a palette string is
     * 'palette:0:verdant_villagers:wood___minecraft:oak_log:axis:x'; palettes will then replace the respective block states,
     * for example oak with spruce.
     * @param stateString The string representation of the block state.
     * @param village The village from which the block palettes will be used.
     * @return The block state.
     */
    public static BlockState parseBlockState(String stateString, ServerVillage village) {
        String[] split1 = stateString.split("___");
        String[] split2 = split1[0].split(":");
        switch (split2[0]) {
            case "state":
                return parsePlainBlockState(stateString.substring(stateString.indexOf(":")+1));
            case "palette":
                return village.getBlockPaletteOf(new Identifier(split2[2], split2[3]), Integer.parseInt(split2[1])).getBlockState(parsePlainBlockState(split1[1]));
            case COLLISION_SPACE:
            default:
                return null;
        }
    }

    /**
     * Parses a plain block state from a string. Has to be of the form 'namespace:block_id:property1:value1:property2:value2 etc.',
     * where the first two substrings together are the block identifier and the following pairs define properties and their
     * values that should be set, for example 'minecraft:oak_log:axis:x'. If not specified, a property will have its default value.
     * @param stateString The {@link String} representing the block state.
     * @return The block state.
     */
    public static BlockState parsePlainBlockState(String stateString) {
        String[] split = stateString.split(":");

        HashMap<String, String> properties = new HashMap<>();
        for (int i=3; i<split.length; i+=2) {
            properties.put(split[i-1], split[i]);
        }

        BlockState state = Registries.BLOCK.get(new Identifier(split[0], split[1])).getDefaultState();
        for (Property<?> property : state.getProperties()) {
            if (properties.containsKey(property.getName())) {
                state = getStateWith(state, property, properties.get(property.getName()));
            }
        }
        return state;
    }
    private static <T extends Comparable<T>> BlockState getStateWith(BlockState state, Property<T> property, String value) {
        return state.with(property, property.parse(value).get());
    }

    /**
     * Convenience method for retrieving a block state from a village block palette.
     * @param village The village that the block palette belongs to.
     * @param paletteModId The mod id under which the palette type is registered.
     * @param paletteType The name of the palette type.
     * @param defaultBlockState The default block state to be replaced, for example oak wood for wood palettes.
     * @param paletteIndex The block palette index for the village.
     * @return The block state returned by the village.
     */
    public static BlockState getBlockStateFrom(ServerVillage village, String paletteModId, String paletteType, BlockState defaultBlockState, int paletteIndex) {
        return village.getBlockPaletteOf(new Identifier(paletteModId, paletteType), paletteIndex).getBlockState(defaultBlockState);
    }
}
