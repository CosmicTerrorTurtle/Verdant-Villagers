package io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class BlockPaletteType {

    public Identifier id;

    public BlockPaletteType(JsonReader reader) throws IOException {
        id = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "id" -> id = new Identifier(reader.nextString());
            }
        }
        reader.endObject();

        if (id == null) {
            throw new IOException();
        }
    }
}
