package io.github.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.verdant_villagers.data.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class VillageTypeData {

    public ArrayList<String> structureTypesToBuild;

    public VillageTypeData(JsonReader reader) throws IOException {
        structureTypesToBuild = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "structure_types_to_build" -> structureTypesToBuild = JsonUtils.readStringArray(reader);
            }
        }
        reader.endObject();

        if (structureTypesToBuild == null) {
            throw new IOException();
        }
    }

    public static HashMap<String, VillageTypeData> readVillageTypes(JsonReader reader) throws IOException {
        HashMap<String, VillageTypeData> villageTypes = new HashMap<>();

        reader.beginObject();
        while (reader.hasNext()) {
            villageTypes.put(reader.nextName(), new VillageTypeData(reader));
        }
        reader.endObject();

        return villageTypes;
    }
}
