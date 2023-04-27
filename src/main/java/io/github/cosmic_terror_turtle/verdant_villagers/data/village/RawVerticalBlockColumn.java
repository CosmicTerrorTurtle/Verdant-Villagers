package io.github.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.verdant_villagers.data.JsonUtils;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.ServerVillage;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.VerticalBlockColumn;
import net.minecraft.block.BlockState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class RawVerticalBlockColumn {

    public String[] blockStateStrings;
    public int[] ints;
    public int baseLevelIndex;

    public RawVerticalBlockColumn(JsonReader reader) throws IOException {
        baseLevelIndex = 0;
        HashMap<String, String> abbreviationMap = null;
        ArrayList<String> blockStateList = null;
        ArrayList<Integer> intsArray = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "base_level_index" -> baseLevelIndex = reader.nextInt();
                case "abbreviation_map" -> abbreviationMap = JsonUtils.readMap(reader);
                case "block_states" -> blockStateList = JsonUtils.readStringArray(reader);
                case "ints" -> intsArray = JsonUtils.readIntArray(reader);
            }
        }
        reader.endObject();

        if (abbreviationMap == null || blockStateList == null || intsArray == null || blockStateList.size() != intsArray.size()) {
            throw new IOException();
        }

        blockStateStrings = new String[blockStateList.size()];
        for (int i=0; i<blockStateStrings.length; i++) {
            // Replace abbreviations with their mapped values and copy values not contained in the map.
            if (abbreviationMap.containsKey(blockStateList.get(i))) {
                blockStateStrings[i] = abbreviationMap.get(blockStateList.get(i));
            } else {
                blockStateStrings[i] = blockStateList.get(i);
            }
            // Set NULL_KEY values to null.
            if (blockStateStrings[i].equals(RawStructureTemplate.NULL_KEY)) {
                blockStateStrings[i] = null;
            }
        }
        ints = new int[intsArray.size()];
        for (int i=0; i<ints.length; i++) {
            ints[i] = intsArray.get(i);
        }
    }

    public VerticalBlockColumn toVerticalBlockColumn(ServerVillage village) {
        BlockState[] states = new BlockState[blockStateStrings.length];
        for (int i=0; i< states.length; i++) {
            states[i] = BlockStateParsing.parseBlockState(blockStateStrings[i], village);
        }
        return new VerticalBlockColumn(states, ints, baseLevelIndex);
    }
}
