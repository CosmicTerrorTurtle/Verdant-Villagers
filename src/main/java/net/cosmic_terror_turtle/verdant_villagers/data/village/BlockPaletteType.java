package net.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import net.cosmic_terror_turtle.verdant_villagers.data.JsonUtils;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;

public class BlockPaletteType {

    public Identifier id;
    public ArrayList<String> elementKeys;

    public BlockPaletteType(JsonReader reader) throws IOException {
        id = null;
        elementKeys = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "id" -> id = new Identifier(reader.nextString());
                case "elements" -> elementKeys = JsonUtils.readStringArray(reader);
            }
        }
        reader.endObject();

        if (id == null || elementKeys == null) {
            throw new IOException();
        }
    }
}
