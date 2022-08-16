package net.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import net.cosmic_terror_turtle.verdant_villagers.data.JsonUtils;
import net.cosmic_terror_turtle.verdant_villagers.entity.custom.village.ServerVillage;
import net.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.StructureAccessPoint;
import net.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.PointOfInterest;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class RawStructureAccessPoint extends RawPointOfInterest {

    public double radius;
    public RawVerticalBlockColumn rawTemplateRoadColumn;

    public RawStructureAccessPoint(JsonReader reader) throws IOException {
        pos = null;
        radius = 0;
        rawTemplateRoadColumn = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "pos" -> pos = JsonUtils.readIntArray(reader);
                case "radius" -> radius = reader.nextDouble();
                case "template_column" -> rawTemplateRoadColumn = new RawVerticalBlockColumn(reader);
            }
        }
        reader.endObject();

        if (pos == null || radius < 0 || rawTemplateRoadColumn == null) {
            throw new IOException("Invalid data for "+RawStructureAccessPoint.class.getName());
        }
    }

    @Override
    public PointOfInterest toPointOfInterest(ServerVillage village) {
        return new StructureAccessPoint(new BlockPos(pos.get(0), pos.get(1), pos.get(2)), radius, rawTemplateRoadColumn.toVerticalBlockColumn(village));
    }
}
