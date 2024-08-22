package io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SaplingData {

    public ArrayList<String> soil_types;
    public HashMap<Integer, Integer> diametersPerTreeType;

    public SaplingData(JsonReader reader) throws IOException {
        soil_types = null;
        diametersPerTreeType = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "soil_types" -> soil_types = JsonUtils.readList(reader, JsonReader::nextString);
                case "diameters_per_tree_type" -> {
                    diametersPerTreeType = new HashMap<>();
                    reader.beginObject();
                    while (reader.hasNext()){
                        diametersPerTreeType.put(Integer.parseInt(reader.nextName()), reader.nextInt());
                    }
                    reader.endObject();
                }
            }
        }
        reader.endObject();

        if (soil_types == null || diametersPerTreeType == null) {
            throw new IOException();
        }
    }

    public static HashMap<String, SaplingData> readSaplingInfo(JsonReader reader) throws IOException {
        HashMap<String, SaplingData> saplingData = new HashMap<>();

        reader.beginObject();
        while (reader.hasNext()) {
            saplingData.put(reader.nextName(), new SaplingData(reader));
        }
        reader.endObject();

        return saplingData;
    }
}
