package io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.road;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.village.RawRoadType;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.ServerVillage;

import java.util.ArrayList;
import java.util.HashMap;

public class RoadTypeProvider {

    private static final ArrayList<RoadTypeProvider> providers = new ArrayList<>();

    public static void resetProviders() {
        providers.forEach(RoadTypeProvider::resetTemplates);
    }



    private final ServerVillage village;
    private final HashMap<RawRoadType, RoadType> templateMap = new HashMap<>();

    public RoadTypeProvider(ServerVillage village) {
        this.village = village;

        providers.add(this);
    }

    public void remove() {
        providers.remove(this);
    }

    /**
     * Sets all templates to null. Call this when the village heart has updated its block palettes.
     */
    public void resetTemplates() {
        templateMap.clear();
    }

    /**
     * Fetches a {@link RoadType} from {@code templateMap} (if {@code templateMap} does not contain the respective
     * {@link RawRoadType} before, a new {@link RoadType} is created from {@code rawType} and put into the map).
     * @param rawType The raw road type used for the road type.
     * @return The road type.
     */
    public RoadType getRoadType(RawRoadType rawType) {
        if (!templateMap.containsKey(rawType)) {
            RoadType type = new RoadType(village, rawType);
            templateMap.put(rawType, type);
        }
        return templateMap.get(rawType);
    }
}
