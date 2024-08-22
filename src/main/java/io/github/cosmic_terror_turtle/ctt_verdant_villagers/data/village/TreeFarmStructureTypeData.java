package io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class TreeFarmStructureTypeData extends StructureTypeData {

    public ArrayList<String> saplingSoilTypes;

    public TreeFarmStructureTypeData(JsonReader reader) throws IOException {
        searchDistanceMultiplier = 1.0;
        structureCheckMethod = "count_villagers";
        saplingSoilTypes = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "search_distance_multiplier" -> searchDistanceMultiplier = reader.nextDouble();
                case "structure_check_method" -> structureCheckMethod = reader.nextString();
                case "sapling_soil_types" -> saplingSoilTypes = JsonUtils.readList(reader, JsonReader::nextString);
            }
        }
        reader.endObject();
    }

    public static HashMap<String, TreeFarmStructureTypeData> readTreeFarmStructureTypes(JsonReader reader) throws IOException {
        HashMap<String, TreeFarmStructureTypeData> villageTypes = new HashMap<>();

        reader.beginObject();
        while (reader.hasNext()) {
            villageTypes.put(reader.nextName(), new TreeFarmStructureTypeData(reader));
        }
        reader.endObject();

        return villageTypes;
    }
}
