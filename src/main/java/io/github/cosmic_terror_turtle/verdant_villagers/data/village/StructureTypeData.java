package io.github.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.util.HashMap;

public class StructureTypeData {

    public double searchDistanceMultiplier;
    public String structureCheckMethod;

    public StructureTypeData(JsonReader reader) throws IOException {
        searchDistanceMultiplier = 1.0;
        structureCheckMethod = "count_villagers";

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "search_distance_multiplier" -> searchDistanceMultiplier = reader.nextDouble();
                case "structure_check_method" -> structureCheckMethod = reader.nextString();
            }
        }
        reader.endObject();
    }

    public static HashMap<String, StructureTypeData> readStructureTypes(JsonReader reader) throws IOException {
        HashMap<String, StructureTypeData> villageTypes = new HashMap<>();

        reader.beginObject();
        while (reader.hasNext()) {
            villageTypes.put(reader.nextName(), new StructureTypeData(reader));
        }
        reader.endObject();

        return villageTypes;
    }
}
