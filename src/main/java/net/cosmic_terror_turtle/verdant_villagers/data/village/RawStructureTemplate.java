package net.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import net.cosmic_terror_turtle.verdant_villagers.data.JsonUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class RawStructureTemplate {
    public static final String NULL_KEY = "null";


    public ArrayList<Integer> availableForVillagerCount;
    public HashMap<String, HashMap<String, String>> dataPerStructureType;
    public String[][][] blockStateCube;
    public ArrayList<Integer> center;
    public ArrayList<RawPointOfInterest> pointsOfInterest;

    /**
     * Creates a new RawStructureTemplate and registers it to RawStructureTemplates.templatesPerVillageType.
     * @param reader The reader used to read the template from a json file.
     * @throws IOException If something goes wrong.
     */
    public static void createNew(JsonReader reader) throws IOException {
        ArrayList<String> villageTypes = null;

        ArrayList<Integer> availableForVillagerCount = null;
        HashMap<String, HashMap<String, String>> dataPerStructureType = null;
        HashMap<String, String> abbreviationMap = null;
        ArrayList<ArrayList<ArrayList<String>>> blockStateArray = null;
        ArrayList<Integer> center = null;
        ArrayList<RawPointOfInterest> pointsOfInterest = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "village_types" -> villageTypes = JsonUtils.readStringArray(reader);
                case "available_for_villager_count" -> availableForVillagerCount = JsonUtils.readIntArray(reader);
                case "data_per_structure_type" -> dataPerStructureType = JsonUtils.readNestedMap(reader);
                case "abbreviation_map" -> abbreviationMap = JsonUtils.readMap(reader);
                case "block_state_cube" -> blockStateArray = readBlockStateArray(reader);
                case "center" -> center = JsonUtils.readIntArray(reader);
                case "points_of_interest" -> pointsOfInterest = readPointsOfInterest(reader);
            }
        }
        reader.endObject();

        if (villageTypes == null || availableForVillagerCount==null || availableForVillagerCount.size()!=2 || dataPerStructureType == null
                || abbreviationMap == null || blockStateArray == null || center == null || center.size()!=3 || pointsOfInterest==null) {
            throw new IOException();
        }

        DataRegistry.addTemplate(
                new RawStructureTemplate(availableForVillagerCount, dataPerStructureType, getCube(blockStateArray, abbreviationMap), center, pointsOfInterest),
                villageTypes
        );
    }

    public RawStructureTemplate(ArrayList<Integer> availableForVillagerCount, HashMap<String, HashMap<String, String>> dataPerStructureType, String[][][] blockStateCube,
                                ArrayList<Integer> center, ArrayList<RawPointOfInterest> pointsOfInterest) {
        this.availableForVillagerCount = availableForVillagerCount;
        this.dataPerStructureType = dataPerStructureType;
        this.blockStateCube = blockStateCube;
        this.center = center;
        this.pointsOfInterest = pointsOfInterest;
    }

    private static ArrayList<ArrayList<ArrayList<String>>> readBlockStateArray(JsonReader reader) throws IOException {
        int i, j;
        ArrayList<ArrayList<ArrayList<String>>> blockStateArray = new ArrayList<>();
        reader.beginArray();
        i = 0;
        while (reader.hasNext()) {
            blockStateArray.add(new ArrayList<>());
            reader.beginArray();
            j = 0;
            while (reader.hasNext()) {
                blockStateArray.get(i).add(new ArrayList<>());
                reader.beginArray();
                while (reader.hasNext()) {
                    blockStateArray.get(i).get(j).add(reader.nextString());
                }
                reader.endArray();
                j++;
            }
            reader.endArray();
            i++;
        }
        reader.endArray();
        return blockStateArray;
    }

    private static ArrayList<RawPointOfInterest> readPointsOfInterest(JsonReader reader) throws IOException {
        ArrayList<RawPointOfInterest> array = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            array.add(RawPointOfInterest.createNew(reader));
        }
        reader.endArray();
        return array;
    }

    /**
     * Deep copies the given array into a block state cube (flipping the x-coordinate) and replaces all occurrences of keys found in {@code abbreviationMap} with
     * the respective map value. After that, occurrences of {@link #NULL_KEY} will be replaced with {@code null}.
     *
     * @param blockStateArray A three-dimensional array of strings representing block states.
     * @param abbreviationMap Maps abbreviations used in blockStateArray to full block state strings.
     * @return The block state cube.
     */
    private static String[][][] getCube(@NotNull ArrayList<ArrayList<ArrayList<String>>> blockStateArray, @NotNull HashMap<String, String> abbreviationMap) {
        String[][][] blockStateCube = new String[blockStateArray.size()][][];
        int ii = blockStateCube.length-1; // Used for flipping the x-coordinate.
        for (int i = 0; i < blockStateCube.length; i++) {
            blockStateCube[i] = new String[blockStateArray.get(ii).size()][];
            for (int j = 0; j < blockStateCube[i].length; j++) {
                blockStateCube[i][j] = new String[blockStateArray.get(ii).get(j).size()];
                for (int k = 0; k < blockStateCube[i][j].length; k++) {
                    // Replace abbreviations with their mapped values and copy values not contained in the map.
                    if (abbreviationMap.containsKey(blockStateArray.get(ii).get(j).get(k))) {
                        blockStateCube[i][j][k] = abbreviationMap.get(blockStateArray.get(ii).get(j).get(k));
                    } else {
                        blockStateCube[i][j][k] = blockStateArray.get(ii).get(j).get(k);
                    }
                    // Set NULL_KEY values to null.
                    if (blockStateCube[i][j][k].equals(NULL_KEY)) {
                        blockStateCube[i][j][k] = null;
                    }
                }
            }
            ii--;
        }
        return blockStateCube;
    }
}
