package io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.JsonUtils;

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
    public ArrayList<String> treeFarmStructureTypesToBuild;

    public VillageTypeData(JsonReader reader) throws IOException {
        dimensions = null;
        biomes = null;
        terrainCategory = null;
        structureTypesToBuild = null;
        ArrayList<Integer> structureTypesWeights = null;
        treeFarmStructureTypesToBuild = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "dimensions" -> dimensions = JsonUtils.readList(reader, JsonReader::nextString);
                case "biomes" -> biomes = JsonUtils.readList(reader, JsonReader::nextString);
                case "terrain_category" -> terrainCategory = reader.nextString();
                case "structure_types_to_build" -> structureTypesToBuild = JsonUtils.readList(reader, JsonReader::nextString);
                case "structure_types_weights" -> structureTypesWeights = JsonUtils.readList(reader, JsonReader::nextInt);
                case "tree_farm_structure_types_to_build"
                        -> treeFarmStructureTypesToBuild = JsonUtils.readList(reader, JsonReader::nextString);
            }
        }
        reader.endObject();

        if (dimensions==null || biomes==null || terrainCategory==null || structureTypesToBuild==null || structureTypesWeights==null
                || structureTypesToBuild.size() != structureTypesWeights.size() || treeFarmStructureTypesToBuild ==null) {
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

    /**
     * Randomly selects a tree farm structure type belonging to a random sapling soil type from the given list.
     * @param saplingSoilTypesNeeded A list of all soil types that the village needs for its saplings, duplicates are
     *                               allowed.
     * @param random The random instance to use as a randomizer.
     * @return The selected type.
     */
    public String getRandomTreeFarmStructureTypeToBuild(ArrayList<String> saplingSoilTypesNeeded, Random random) {
        // Randomly select one of the needed soil types.
        String saplingSoilType = saplingSoilTypesNeeded.get(random.nextInt(saplingSoilTypesNeeded.size()));

        ArrayList<String> structureTypes = new ArrayList<>();
        TreeFarmStructureTypeData treeFarmStructureTypeData;
        for (String structureType : treeFarmStructureTypesToBuild) {
            treeFarmStructureTypeData = DataRegistry.getTreeFarmStructureTypeData(structureType);
            if (treeFarmStructureTypeData.saplingSoilTypes.contains(saplingSoilType)) {
                structureTypes.add(structureType);
            }
        }
        // Randomly return a structure type for the selected soil type.
        return structureTypes.get(random.nextInt(structureTypes.size()));
    }
}
