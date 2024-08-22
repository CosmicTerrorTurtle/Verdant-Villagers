package io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.JsonUtils;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.ServerVillage;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.structure.PointOfInterest;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.structure.SaplingLocationPoint;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class RawSaplingLocationPoint extends RawPointOfInterest {

    public int saplings;
    public int treeDiameter;
    public String soilType;

    public RawSaplingLocationPoint(JsonReader reader) throws IOException {
        pos = null;
        saplings = 1;
        treeDiameter = 5;
        soilType = "grass";

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "pos" -> pos = JsonUtils.readBlockPos(reader);
                case "saplings" -> saplings = reader.nextInt();
                case "tree_diameter" -> treeDiameter = reader.nextInt();
                case "soil_type" -> soilType = reader.nextString();
            }
        }
        reader.endObject();

        if (pos == null) {
            throw new IOException("Invalid data for "+RawSaplingLocationPoint.class.getName());
        }
    }

    @Override
    public PointOfInterest toPointOfInterest(ServerVillage village) {
        return new SaplingLocationPoint(new BlockPos(pos), saplings, treeDiameter, soilType);
    }
}
