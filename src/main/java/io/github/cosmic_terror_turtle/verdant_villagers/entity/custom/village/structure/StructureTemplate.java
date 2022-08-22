package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure;

import io.github.cosmic_terror_turtle.verdant_villagers.data.village.BlockStateParsing;
import io.github.cosmic_terror_turtle.verdant_villagers.data.village.RawStructureTemplate;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.GeoFeatureBit;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.GeoFeatureBitOption;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.ServerVillage;

import java.util.ArrayList;
import java.util.HashMap;

public class StructureTemplate {


    HashMap<String, HashMap<String, String>> dataPerStructureType;
    private final ArrayList<GeoFeatureBit> bits;
    public ArrayList<PointOfInterest> pointsOfInterest = new ArrayList<>();

    /**
     * Creates a village-specific structure template using a raw template and the village's block palettes.
     * @param village The village that this template belongs to.
     * @param rawTemplate The raw template that provides all the data.
     */
    public StructureTemplate(ServerVillage village, RawStructureTemplate rawTemplate) {
        dataPerStructureType = rawTemplate.dataPerStructureType;
        bits = BlockStateParsing.parseBlockStateCube(rawTemplate.blockStateCube, rawTemplate.center, village);
        pointsOfInterest.addAll(rawTemplate.pointsOfInterest.stream().map(poi -> poi.toPointOfInterest(village)).toList());
    }

    public ArrayList<GeoFeatureBit> getBits() {
        for (GeoFeatureBit bit : bits) {
            if (bit instanceof GeoFeatureBitOption) {
                ((GeoFeatureBitOption) bit).randomize();
            }
        }
        return bits;
    }
}
