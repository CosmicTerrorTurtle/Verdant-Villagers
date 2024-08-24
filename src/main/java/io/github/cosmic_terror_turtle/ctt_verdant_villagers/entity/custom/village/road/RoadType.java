package io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.road;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.village.RawRoadType;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.village.RawVerticalBlockColumn;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.ServerVillage;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.VerticalBlockColumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoadType {

    public static final String TERRAIN_TYPE_ABOVE_KEY = "top";
    public static final String TERRAIN_TYPE_BELOW_KEY = "bottom";


    public double scale;

    public double edgeRadius;
    ArrayList<Double> edgeBlockColumnRadii;
    HashMap<String, HashMap<String, ArrayList<VerticalBlockColumn>>> edgeTemplateBlockColumns;
    double edgeSpecialColumnSpace;
    ArrayList<Double> edgeSpecialBlockColumnRadii;
    HashMap<String, HashMap<String, ArrayList<VerticalBlockColumn>>> edgeSpecialTemplateBlockColumns;

    public double junctionRadius;
    public double junctionSameHeightRadius;
    ArrayList<Double> junctionBlockColumnRadii;
    HashMap<String, HashMap<String, ArrayList<VerticalBlockColumn>>> junctionTemplateBlockColumns;
    ArrayList<Double> junctionSpecialBlockColumnRadii;
    HashMap<String, HashMap<String, HashMap<String, ArrayList<VerticalBlockColumn>>>> junctionSpecialTemplateBlockColumns;

    public RoadType(ServerVillage village, RawRoadType rawRoadType) {
        // Edge
        scale = rawRoadType.scale;
        edgeRadius = 0.0;
        edgeBlockColumnRadii = rawRoadType.edgeBlockColumnRadii;
        for (Double d : edgeBlockColumnRadii) {
            if (d > edgeRadius) {
                edgeRadius = d;
            }
        }
        edgeTemplateBlockColumns = new HashMap<>();
        for (Map.Entry<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> entry1 : rawRoadType.edgeTemplateBlockColumns.entrySet()) {
            edgeTemplateBlockColumns.put(entry1.getKey(), new HashMap<>());
            for (Map.Entry<String, ArrayList<RawVerticalBlockColumn>> entry2 : entry1.getValue().entrySet()) {
                edgeTemplateBlockColumns.get(entry1.getKey()).put(entry2.getKey(), new ArrayList<>());
                for (RawVerticalBlockColumn raw : entry2.getValue()) {
                    edgeTemplateBlockColumns.get(entry1.getKey()).get(entry2.getKey()).add(raw.toVerticalBlockColumn(village));
                }
            }
        }
        edgeSpecialColumnSpace = rawRoadType.edgeSpecialColumnSpace;
        edgeSpecialBlockColumnRadii = rawRoadType.edgeSpecialBlockColumnRadii;
        for (Double d : edgeSpecialBlockColumnRadii) {
            if (d > edgeRadius) {
                edgeRadius = d;
            }
        }
        edgeSpecialTemplateBlockColumns = new HashMap<>();
        for (Map.Entry<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> entry1 : rawRoadType.edgeSpecialTemplateBlockColumns.entrySet()) {
            edgeSpecialTemplateBlockColumns.put(entry1.getKey(), new HashMap<>());
            for (Map.Entry<String, ArrayList<RawVerticalBlockColumn>> entry2 : entry1.getValue().entrySet()) {
                edgeSpecialTemplateBlockColumns.get(entry1.getKey()).put(entry2.getKey(), new ArrayList<>());
                for (RawVerticalBlockColumn raw : entry2.getValue()) {
                    edgeSpecialTemplateBlockColumns.get(entry1.getKey()).get(entry2.getKey()).add(raw.toVerticalBlockColumn(village));
                }
            }
        }
        // Junction
        junctionRadius = 0.0;
        junctionBlockColumnRadii = rawRoadType.junctionBlockColumnRadii;
        if (junctionBlockColumnRadii != null) {
            for (Double d : junctionBlockColumnRadii) {
                if (d > junctionRadius) {
                    junctionRadius = d;
                }
            }
        }
        junctionTemplateBlockColumns = new HashMap<>();
        if (rawRoadType.junctionTemplateBlockColumns != null) {
            for (Map.Entry<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> entry1 : rawRoadType.junctionTemplateBlockColumns.entrySet()) {
                junctionTemplateBlockColumns.put(entry1.getKey(), new HashMap<>());
                for (Map.Entry<String, ArrayList<RawVerticalBlockColumn>> entry2 : entry1.getValue().entrySet()) {
                    junctionTemplateBlockColumns.get(entry1.getKey()).put(entry2.getKey(), new ArrayList<>());
                    for (RawVerticalBlockColumn raw : entry2.getValue()) {
                        junctionTemplateBlockColumns.get(entry1.getKey()).get(entry2.getKey()).add(raw.toVerticalBlockColumn(village));
                    }
                }
            }
        }
        junctionSpecialBlockColumnRadii = rawRoadType.junctionSpecialBlockColumnRadii;
        if (junctionSpecialBlockColumnRadii != null) {
            for (Double d : junctionSpecialBlockColumnRadii) {
                if (d > junctionRadius) {
                    junctionRadius = d;
                }
            }
        }
        junctionSpecialTemplateBlockColumns = new HashMap<>();
        if (rawRoadType.junctionSpecialTemplateBlockColumns != null) {
            for (Map.Entry<String, HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>>> entry1 : rawRoadType.junctionSpecialTemplateBlockColumns.entrySet()) {
                junctionSpecialTemplateBlockColumns.put(entry1.getKey(), new HashMap<>());
                for (Map.Entry<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> entry2 : entry1.getValue().entrySet()) {
                    junctionSpecialTemplateBlockColumns.get(entry1.getKey()).put(entry2.getKey(), new HashMap<>());
                    for (Map.Entry<String, ArrayList<RawVerticalBlockColumn>> entry3 : entry2.getValue().entrySet()) {
                        junctionSpecialTemplateBlockColumns.get(entry1.getKey()).get(entry2.getKey()).put(entry3.getKey(), new ArrayList<>());
                        for (RawVerticalBlockColumn raw : entry3.getValue()) {
                            junctionSpecialTemplateBlockColumns.get(entry1.getKey()).get(entry2.getKey()).get(entry3.getKey()).add(raw.toVerticalBlockColumn(village));
                        }
                    }
                }
            }
        }
        junctionSameHeightRadius = 1.6 * junctionRadius;
    }

}
