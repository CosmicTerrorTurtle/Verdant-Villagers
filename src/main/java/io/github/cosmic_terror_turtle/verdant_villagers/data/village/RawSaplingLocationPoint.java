package io.github.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.verdant_villagers.data.JsonUtils;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.ServerVillage;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.PointOfInterest;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.SaplingLocationPoint;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class RawSaplingLocationPoint extends RawPointOfInterest {

    public int saplings;

    public RawSaplingLocationPoint(JsonReader reader) throws IOException {
        pos = null;
        saplings = 1;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "pos" -> pos = JsonUtils.readList(reader, JsonReader::nextInt);
                case "saplings" -> saplings = reader.nextInt();
            }
        }
        reader.endObject();

        if (pos == null) {
            throw new IOException("Invalid data for "+RawSaplingLocationPoint.class.getName());
        }
    }

    @Override
    public PointOfInterest toPointOfInterest(ServerVillage village) {
        return new SaplingLocationPoint(new BlockPos(pos.get(0), pos.get(1), pos.get(2)), saplings);
    }
}
