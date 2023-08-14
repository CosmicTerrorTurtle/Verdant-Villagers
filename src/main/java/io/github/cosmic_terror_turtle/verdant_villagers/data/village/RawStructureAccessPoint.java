package io.github.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.verdant_villagers.data.JsonUtils;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.ServerVillage;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.StructureAccessPoint;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.PointOfInterest;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.HashMap;

public class RawStructureAccessPoint extends RawPointOfInterest {

    public String accessPathRoadType;

    public RawStructureAccessPoint(JsonReader reader) throws IOException {
        pos = null;
        accessPathRoadType = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "pos" -> pos = JsonUtils.readList(reader, JsonReader::nextInt);
                case "access_path_road_type" -> accessPathRoadType = reader.nextString();
            }
        }
        reader.endObject();

        if (pos == null || accessPathRoadType == null) {
            throw new IOException("Invalid data for "+RawStructureAccessPoint.class.getName());
        }
    }

    @Override
    public PointOfInterest toPointOfInterest(ServerVillage village) {
        return new StructureAccessPoint(new BlockPos(pos.get(0), pos.get(1), pos.get(2)), accessPathRoadType);
    }
}
