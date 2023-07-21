package io.github.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.verdant_villagers.data.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class RawRoadType {

    public ArrayList<Integer> availableForVillagerCount;

    public double edgeMinMaxLengthMultiplier;
    /**
     * Contains the radii to which each of the block columns of a road edge will extend. Each list of radii is mapped to
     * by two Strings: The first denotes whether the columns are for the top or the bottom part of the road; the second
     * denotes the terrain type surrounding the road (For example: 'top' -> 'fluid' -> [0.0, 3.0]).
     */
    public HashMap<String, HashMap<String, ArrayList<Double>>> edgeBlockColumnRadii;
    /**
     * Contains the block columns belonging to {@link RawRoadType#edgeBlockColumnRadii}.
     */
    public HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> edgeTemplateBlockColumns;
    public double edgeSpecialColumnSpace;
    /**
     * Same as {@link RawRoadType#edgeBlockColumnRadii}, but only get placed every few blocks
     * ({@link RawRoadType#edgeSpecialColumnSpace}).
     */
    public HashMap<String, HashMap<String, ArrayList<Double>>> edgeSpecialBlockColumnRadii;
    public HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> edgeSpecialTemplateBlockColumns;

    /**
     * Same as {@link RawRoadType#edgeBlockColumnRadii}, but for junctions.
     */
    public HashMap<String, HashMap<String, ArrayList<Double>>> junctionBlockColumnRadii;
    public HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> junctionTemplateBlockColumns;
    /**
     * Same as {@link RawRoadType#junctionBlockColumnRadii}, but for adding extra features which override the normal
     * block columns up to a certain radius, for example a small tree in the middle of the junction. Multiple variants can be chosen from the
     * third map layer ('top' -> 'air' -> 'small_tree' -> [0.0, 1.0]).
     */
    public HashMap<String, HashMap<String, HashMap<String, ArrayList<Double>>>> junctionSpecialBlockColumnRadii;
    public HashMap<String, HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>>> junctionSpecialTemplateBlockColumns;

    /**
     * Creates a new RawStructureTemplate and registers it to RawStructureTemplates.templatesPerVillageType.
     * @param reader The reader used to read the template from a json file.
     * @throws IOException If something goes wrong.
     */
    public static void createNew(JsonReader reader) throws IOException {
        final HashMap<String, String> abbreviationMap;
        ArrayList<Integer> availableForVillagerCount = null;
        double edgeMinMaxLengthMultiplier = 1.0;
        HashMap<String, HashMap<String, ArrayList<Double>>> edgeBlockColumnRadii = null;
        HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> edgeTemplateBlockColumns = null;
        double edgeSpecialColumnSpace = 1000000.0;
        HashMap<String, HashMap<String, ArrayList<Double>>> edgeSpecialBlockColumnRadii = null;
        HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> edgeSpecialTemplateBlockColumns = null;
        HashMap<String, HashMap<String, ArrayList<Double>>> junctionBlockColumnRadii = null;
        HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> junctionTemplateBlockColumns = null;
        HashMap<String, HashMap<String, HashMap<String, ArrayList<Double>>>> junctionSpecialBlockColumnRadii = null;
        HashMap<String, HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>>> junctionSpecialTemplateBlockColumns = null;

        reader.beginObject();
        if (reader.hasNext() && reader.nextName().equals("abbreviation_map")) {
            abbreviationMap = JsonUtils.readMap(reader, JsonReader::nextString);
        } else {
            throw new IOException("First element must be the abbreviation map.");
        }
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                default -> throw new IOException();
                case "available_for_villager_count" -> availableForVillagerCount = JsonUtils.readList(reader, JsonReader::nextInt);
                case "edge_min_max_length_multiplier" -> edgeMinMaxLengthMultiplier = reader.nextDouble();
                case "edge_block_column_radii" -> edgeBlockColumnRadii = JsonUtils.readMap(reader, reader1 -> JsonUtils.readMap(reader1, reader2 -> JsonUtils.readList(reader2, JsonReader::nextDouble)));
                case "edge_template_block_columns" -> edgeTemplateBlockColumns = JsonUtils.readMap(reader, reader1 -> JsonUtils.readMap(reader1, reader2 -> JsonUtils.readList(reader2, reader3 -> new RawVerticalBlockColumn(reader3, abbreviationMap))));
                case "edge_special_column_space" -> edgeSpecialColumnSpace = reader.nextDouble();
                case "edge_special_block_column_radii" -> edgeSpecialBlockColumnRadii = JsonUtils.readMap(reader, reader1 -> JsonUtils.readMap(reader1, reader2 -> JsonUtils.readList(reader2, JsonReader::nextDouble)));
                case "edge_special_template_block_columns" -> edgeSpecialTemplateBlockColumns = JsonUtils.readMap(reader, reader1 -> JsonUtils.readMap(reader1, reader2 -> JsonUtils.readList(reader2, reader3 -> new RawVerticalBlockColumn(reader3, abbreviationMap))));
                case "junction_block_column_radii" -> junctionBlockColumnRadii = JsonUtils.readMap(reader, reader1 -> JsonUtils.readMap(reader1, reader2 -> JsonUtils.readList(reader2, JsonReader::nextDouble)));
                case "junction_template_block_columns" -> junctionTemplateBlockColumns = JsonUtils.readMap(reader, reader1 -> JsonUtils.readMap(reader1, reader2 -> JsonUtils.readList(reader2, reader3 -> new RawVerticalBlockColumn(reader3, abbreviationMap))));
                case "junction_special_block_column_radii" -> junctionSpecialBlockColumnRadii = JsonUtils.readMap(reader, reader1 -> JsonUtils.readMap(reader1, reader2 -> JsonUtils.readMap(reader2, reader3 -> JsonUtils.readList(reader3, JsonReader::nextDouble))));
                case "junction_special_template_block_columns" -> junctionSpecialTemplateBlockColumns = JsonUtils.readMap(reader, reader1 -> JsonUtils.readMap(reader1, reader2 -> JsonUtils.readMap(reader2, reader3 -> JsonUtils.readList(reader3, reader4 -> new RawVerticalBlockColumn(reader4, abbreviationMap)))));
            }
        }
        reader.endObject();

        if (availableForVillagerCount==null || availableForVillagerCount.size()!=2
                || edgeBlockColumnRadii==null
                || edgeTemplateBlockColumns==null
                || edgeSpecialBlockColumnRadii==null
                || edgeSpecialTemplateBlockColumns==null
                || junctionBlockColumnRadii==null
                || junctionTemplateBlockColumns==null
                || junctionSpecialBlockColumnRadii==null
                || junctionSpecialTemplateBlockColumns==null) {
            throw new IOException("RawRoadType could not be created from json.");
        }

        DataRegistry.addRoadType(new RawRoadType(
                availableForVillagerCount,
                edgeMinMaxLengthMultiplier,
                edgeBlockColumnRadii,
                edgeTemplateBlockColumns,
                edgeSpecialColumnSpace,
                edgeSpecialBlockColumnRadii,
                edgeSpecialTemplateBlockColumns,
                junctionBlockColumnRadii,
                junctionTemplateBlockColumns,
                junctionSpecialBlockColumnRadii,
                junctionSpecialTemplateBlockColumns));
    }

    public RawRoadType(
            ArrayList<Integer> availableForVillagerCount,
            double edgeMinMaxLengthMultiplier,
            HashMap<String, HashMap<String, ArrayList<Double>>> edgeBlockColumnRadii,
            HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> edgeTemplateBlockColumns,
            double edgeSpecialColumnSpace,
            HashMap<String, HashMap<String, ArrayList<Double>>> edgeSpecialBlockColumnRadii,
            HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> edgeSpecialTemplateBlockColumns,
            HashMap<String, HashMap<String, ArrayList<Double>>> junctionBlockColumnRadii,
            HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> junctionTemplateBlockColumns,
            HashMap<String, HashMap<String, HashMap<String, ArrayList<Double>>>> junctionSpecialBlockColumnRadii,
            HashMap<String, HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>>> junctionSpecialTemplateBlockColumns) {
        this.availableForVillagerCount = availableForVillagerCount;
        this.edgeMinMaxLengthMultiplier = edgeMinMaxLengthMultiplier;
        this.edgeBlockColumnRadii = edgeBlockColumnRadii;
        this.edgeTemplateBlockColumns = edgeTemplateBlockColumns;
        this.edgeSpecialColumnSpace = edgeSpecialColumnSpace;
        this.edgeSpecialBlockColumnRadii = edgeSpecialBlockColumnRadii;
        this.edgeSpecialTemplateBlockColumns = edgeSpecialTemplateBlockColumns;
        this.junctionBlockColumnRadii = junctionBlockColumnRadii;
        this.junctionTemplateBlockColumns = junctionTemplateBlockColumns;
        this.junctionSpecialBlockColumnRadii = junctionSpecialBlockColumnRadii;
        this.junctionSpecialTemplateBlockColumns = junctionSpecialTemplateBlockColumns;
    }
}
