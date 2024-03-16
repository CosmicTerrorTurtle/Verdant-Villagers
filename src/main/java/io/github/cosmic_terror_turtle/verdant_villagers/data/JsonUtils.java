package io.github.cosmic_terror_turtle.verdant_villagers.data;

import com.google.gson.stream.JsonReader;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class JsonUtils {

    public static <T> ArrayList<T> readList(JsonReader reader, JsonProcessor<T> inner) throws IOException {
        ArrayList<T> list = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            list.add(inner.process(reader));
        }
        reader.endArray();
        return list;
    }
    public static <T> HashMap<String, T> readMap(JsonReader reader, JsonProcessor<T> inner) throws IOException {
        HashMap<String, T> map = new HashMap<>();
        reader.beginObject();
        while (reader.hasNext()) {
            map.put(reader.nextName(), inner.process(reader));
        }
        reader.endObject();
        return map;
    }
    public interface JsonProcessor<T> {
        T process(JsonReader reader) throws IOException;
    }

    public static BlockPos readBlockPos(JsonReader reader) throws IOException {
        int x = 0;
        int y = 0;
        int z = 0;
        ArrayList<Integer> coordinates = readList(reader, JsonReader::nextInt);
        if (coordinates.size() == 3) {
            if (coordinates.get(0) != null) {
                x = coordinates.get(0);
            }
            if (coordinates.get(1) != null) {
                y = coordinates.get(1);
            }
            if (coordinates.get(2) != null) {
                z = coordinates.get(2);
            }
        }
        return new BlockPos(x, y, z);
    }
}
