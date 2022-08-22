package io.github.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.verdant_villagers.data.JsonUtils;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BlockPalette {

    public final Identifier typeId;
    public final Identifier id;
    public final ArrayList<Block> indicatorBlocks;
    private final HashMap<String, String> blockStates = new HashMap<>();

    public BlockPalette(BlockPaletteType type, Identifier id, ArrayList<Block> indicatorBlocks, ArrayList<String> elements) {
        typeId = type.id;
        this.id = id;
        this.indicatorBlocks = indicatorBlocks;

        if (type.elementKeys.size() != elements.size()) {
            throw new RuntimeException("Elements are invalid!");
        }
        for (int i=0; i< elements.size(); i++) {
            blockStates.put(type.elementKeys.get(i), elements.get(i));
        }
    }

    public String getElement(String elementKey) {
        return blockStates.get(elementKey);
    }

    public static void createNew(JsonReader reader) throws IOException {
        BlockPaletteType type = null;
        Identifier id = null;
        boolean addToDefaults = false;
        ArrayList<Block> indicatorBlocks = null;
        ArrayList<String> elements = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "palette_type_id" -> type = DataRegistry.getPaletteType(reader.nextString());
                case "palette_id" -> id = new Identifier(reader.nextString());
                case "add_to_defaults" -> addToDefaults = reader.nextBoolean();
                case "indicator_blocks" -> indicatorBlocks = new ArrayList<>(
                        JsonUtils.readStringArray(reader).stream().map(block_id -> Registry.BLOCK.get(new Identifier(block_id))).toList()
                );
                case "elements" -> elements = JsonUtils.readStringArray(reader);
            }
        }
        reader.endObject();

        if (type==null || id==null || indicatorBlocks==null || elements==null) {
            throw new IOException();
        }

        DataRegistry.addBlockPalette(new BlockPalette(type, id, indicatorBlocks, elements), addToDefaults);
    }
}
