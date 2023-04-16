package io.github.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class ReplacementRule {

    public enum FindingMode {MATCH_BLOCK, MATCH_STATE}
    public enum ReplacementMode {STATE, BLOCK_AND_COPY_STATES}

    public FindingMode findingMode;
    public BlockState find;
    public ReplacementMode replacementMode;
    public BlockState replaceWith;

    public ReplacementRule(JsonReader reader) throws IOException {
        findingMode = null;
        find = null;
        replacementMode = null;
        replaceWith = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "replace_block" -> {
                    findingMode = FindingMode.MATCH_BLOCK;
                    find = Registries.BLOCK.get(new Identifier(reader.nextString())).getDefaultState();
                }
                case "replace_state" -> {
                    findingMode = FindingMode.MATCH_STATE;
                    find = BlockStateParsing.parsePlainBlockState(reader.nextString());
                }
                case "with_state" -> {
                    replacementMode = ReplacementMode.STATE;
                    replaceWith = BlockStateParsing.parsePlainBlockState(reader.nextString());
                }
                case "with_block_and_copy_states" -> {
                    replacementMode = ReplacementMode.BLOCK_AND_COPY_STATES;
                    replaceWith = Registries.BLOCK.get(new Identifier(reader.nextString())).getDefaultState();
                }
            }
        }
        reader.endObject();

        if (findingMode==null || find==null || replacementMode==null || replaceWith==null) {
            throw new IOException("Invalid data for "+ReplacementRule.class.getName());
        }
    }

    public BlockState replace(BlockState state) {
        boolean replace = false;
        switch (findingMode) {
            case MATCH_BLOCK -> replace = state.isOf(find.getBlock());
            case MATCH_STATE -> replace = state.equals(find);
        }
        if (replace) {
            switch (replacementMode) {
                case STATE -> {
                    return replaceWith;
                }
                case BLOCK_AND_COPY_STATES -> {
                    BlockState result = replaceWith.getBlock().getDefaultState();
                    for (Property<?> property : state.getProperties()) {
                        if (result.getProperties().contains(property)) {
                            result = getStateWith(result, state, property);
                        }
                    }
                    return result;
                }
            }
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState getStateWith(BlockState copyTo, BlockState copyFrom, Property<T> property) {
        return copyTo.with(property, copyFrom.get(property));
    }
}
