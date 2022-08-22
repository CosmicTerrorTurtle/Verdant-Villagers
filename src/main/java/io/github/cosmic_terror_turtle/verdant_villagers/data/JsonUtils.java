package io.github.cosmic_terror_turtle.verdant_villagers.data;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class JsonUtils {

    public static ArrayList<Integer> readIntArray(JsonReader reader) throws IOException {
        ArrayList<Integer> array = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            array.add(reader.nextInt());
        }
        reader.endArray();
        return array;
    }

    public static ArrayList<String> readStringArray(JsonReader reader) throws IOException {
        ArrayList<String> array = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            array.add(reader.nextString());
        }
        reader.endArray();
        return array;
    }

    public static HashMap<String, String> readMap(JsonReader reader) throws IOException {
        HashMap<String, String> map = new HashMap<>();

        reader.beginObject();
        while (reader.hasNext()) {
            map.put(reader.nextName(), reader.nextString());
        }
        reader.endObject();

        return map;
    }

    public static HashMap<String, HashMap<String, String>> readNestedMap(JsonReader reader) throws IOException {
        HashMap<String, HashMap<String, String>> map = new HashMap<>();

        reader.beginObject();
        while (reader.hasNext()) {
            map.put(reader.nextName(), readMap(reader));
        }
        reader.endObject();

        return map;
    }
}
