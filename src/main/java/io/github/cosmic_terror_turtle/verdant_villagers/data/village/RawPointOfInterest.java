package io.github.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.ServerVillage;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.PointOfInterest;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.ArrayList;

public class RawPointOfInterest {

    ArrayList<Integer> pos;

    public static RawPointOfInterest createNew(JsonReader reader) throws IOException {
        String subclassName;
        RawPointOfInterest poi;

        reader.beginObject();
        if (!reader.hasNext() || !reader.nextName().equals("subclass_name")) {
            throw new IOException();
        }
        subclassName = reader.nextString();
        if (!reader.hasNext() || !reader.nextName().equals("subclass_data")) {
            throw new IOException();
        }
        poi = DataRegistry.getPointOfInterestJsonConstructor(subclassName).apply(reader);
        reader.endObject();

        return poi;
    }

    public PointOfInterest toPointOfInterest(ServerVillage village) {
        return new PointOfInterest(new BlockPos(pos.get(0), pos.get(1), pos.get(2)));
    }
}
