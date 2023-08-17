package io.github.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.verdant_villagers.data.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class VillageTypeData {

    public ArrayList<String> dimensions;
    public ArrayList<String> biomes;
    public String terrainCategory;
    public ArrayList<String> structureTypesToBuild;
    public float[] structureTypesCumulativeChances;

    public VillageTypeData(JsonReader reader) throws IOException {
        dimensions = null;
        biomes = null;
        terrainCategory = null;
        structureTypesToBuild = null;
        ArrayList<Integer> structureTypesWeights = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "dimensions" -> dimensions = JsonUtils.readList(reader, JsonReader::nextString);
                case "biomes" -> biomes = JsonUtils.readList(reader, JsonReader::nextString);
                case "terrain_category" -> terrainCategory = reader.nextString();
                case "structure_types_to_build" -> structureTypesToBuild = JsonUtils.readList(reader, JsonReader::nextString);
                case "structure_types_weights" -> structureTypesWeights = JsonUtils.readList(reader, JsonReader::nextInt);
            }
        }
        reader.endObject();

        if (dimensions==null || biomes==null || terrainCategory==null || structureTypesToBuild==null || structureTypesWeights==null
                || structureTypesToBuild.size() != structureTypesWeights.size()) {
            throw new IOException();
        }
        structureTypesCumulativeChances = new float[structureTypesToBuild.size()];
        int sum = 0;
        for (Integer number : structureTypesWeights) {
            sum += number;
        }
        float accumulator = 0;
        for (int i=0; i<structureTypesCumulativeChances.length; i++) {
            accumulator += (float) structureTypesWeights.get(i) / sum;
            structureTypesCumulativeChances[i] = accumulator;
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

    public String getRandomStructureTypeToBuild(Random random) {
        float f = random.nextFloat();
        for (int i=0; i< structureTypesCumulativeChances.length; i++) {
            if (f < structureTypesCumulativeChances[i]) {
                return structureTypesToBuild.get(i);
            }
        }
        return structureTypesToBuild.get(structureTypesToBuild.size()-1);
    }
}
