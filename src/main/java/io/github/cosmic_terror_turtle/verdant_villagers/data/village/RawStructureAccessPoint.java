package io.github.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.verdant_villagers.data.JsonUtils;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.ServerVillage;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.PointOfInterest;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.StructureAccessPoint;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.util.ArrayList;

public class RawStructureAccessPoint extends RawPointOfInterest {

    private String accessPathRoadType;
    private ArrayList<BlockPos> connectionVolume = new ArrayList<>();
    private ArrayList<BlockPos> sidewalkPositions = new ArrayList<>();
    private ArrayList<BlockPos> archPositions = new ArrayList<>();

    public RawStructureAccessPoint(JsonReader reader) throws IOException {
        pos = null;
        accessPathRoadType = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "pos" -> pos = JsonUtils.readBlockPos(reader);
                case "access_path_road_type" -> accessPathRoadType = reader.nextString();
                case "connection_volume" -> connectionVolume =
                        JsonUtils.readList(reader, JsonUtils::readBlockPos);
                case "sidewalk_positions" -> sidewalkPositions =
                        JsonUtils.readList(reader, JsonUtils::readBlockPos);
                case "arch_positions" -> archPositions =
                        JsonUtils.readList(reader, JsonUtils::readBlockPos);
            }
        }
        reader.endObject();

        if (pos == null || accessPathRoadType == null || connectionVolume.size() != 2) {
            throw new IOException("Invalid data for "+RawStructureAccessPoint.class.getName());
        }
    }

    @Override
    public PointOfInterest toPointOfInterest(ServerVillage village) {
        return new StructureAccessPoint(new BlockPos(pos), accessPathRoadType,
                new ArrayList<>(connectionVolume), new ArrayList<>(sidewalkPositions), new ArrayList<>(archPositions));
    }
}
