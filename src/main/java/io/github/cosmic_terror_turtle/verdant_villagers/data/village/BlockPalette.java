package io.github.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.verdant_villagers.data.JsonUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;

public class BlockPalette {

    public final Identifier typeId;
    public final Identifier id;
    public final ArrayList<Block> indicatorBlocks;
    private final ArrayList<ReplacementRule> replacementRules;

    public BlockPalette(BlockPaletteType type, Identifier id, ArrayList<Block> indicatorBlocks, ArrayList<ReplacementRule> replacementRules) {
        typeId = type.id;
        this.id = id;
        this.indicatorBlocks = indicatorBlocks;
        this.replacementRules = replacementRules;
    }

    /**
     * Replaces a given block state if possible. For example, if this {@link BlockPalette} contains a rule to replace
     * oak wood planks with spruce wood planks and the given state is oak wood planks, then spruce wood planks would be
     * returned.
     * @param state The default block state.
     * @return The replaced state (or the input state, if no rule replaced it).
     */
    public BlockState getBlockState(BlockState state) {
        for (ReplacementRule rule : replacementRules) {
            state = rule.replace(state);
        }
        return state;
    }

    public static void createNew(JsonReader reader) throws IOException {
        BlockPaletteType type = null;
        Identifier id = null;
        boolean addToDefaults = false;
        ArrayList<Block> indicatorBlocks = null;
        ArrayList<ReplacementRule> replacementRules = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "palette_type_id" -> type = DataRegistry.getPaletteType(reader.nextString());
                case "palette_id" -> id = new Identifier(reader.nextString());
                case "add_to_defaults" -> addToDefaults = reader.nextBoolean();
                case "indicator_blocks" -> indicatorBlocks = new ArrayList<>(
                        JsonUtils.readStringArray(reader).stream().map(block_id -> Registries.BLOCK.get(new Identifier(block_id))).toList()
                );
                case "replacement_rules" -> {
                    replacementRules = new ArrayList<>();
                    reader.beginArray();
                    while (reader.hasNext()) {
                        replacementRules.add(new ReplacementRule(reader));
                    }
                    reader.endArray();
                }
            }
        }
        reader.endObject();

        if (type==null || id==null || indicatorBlocks==null || replacementRules==null) {
            throw new IOException();
        }

        DataRegistry.addBlockPalette(new BlockPalette(type, id, indicatorBlocks, replacementRules), addToDefaults);
    }
}
