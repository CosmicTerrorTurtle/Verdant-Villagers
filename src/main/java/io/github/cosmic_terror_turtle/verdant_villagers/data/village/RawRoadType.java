package io.github.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.verdant_villagers.data.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class RawRoadType {

    public ArrayList<Integer> availableForVillagerCount;

    public double scale;
    public double edgeRoadDotRadius;
    /**
     * Contains the radii to which each of the block columns of a road edge will extend.
     */
    public ArrayList<Double> edgeBlockColumnRadii;
    /**
     * Contains the block columns belonging to {@link RawRoadType#edgeBlockColumnRadii}. Each list of columns is mapped to
     * by two Strings: The first denotes whether the columns are for the top or the bottom part of the road; the second
     * denotes the terrain type surrounding the road (For example: 'top' -> 'fluid' -> [column1, column2]).
     */
    public HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> edgeTemplateBlockColumns;
    public double edgeSpecialColumnSpace;
    /**
     * Same as {@link RawRoadType#edgeBlockColumnRadii}, but only get placed every few blocks
     * ({@link RawRoadType#edgeSpecialColumnSpace}).
     */
    public ArrayList<Double> edgeSpecialBlockColumnRadii;
    public HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> edgeSpecialTemplateBlockColumns;

    /**
     * Same as {@link RawRoadType#edgeBlockColumnRadii}, but for junctions.
     */
    public ArrayList<Double> junctionBlockColumnRadii;
    public HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> junctionTemplateBlockColumns;
    /**
     * Same as {@link RawRoadType#junctionBlockColumnRadii}, but for adding extra features which override the normal
     * block columns up to a certain radius, for example a small tree in the middle of the junction. Multiple variants
     * can be chosen from the third column map layer ('top' -> 'air' -> 'small_tree' -> [column1, column2]).
     */
    public ArrayList<Double> junctionSpecialBlockColumnRadii;
    public HashMap<String, HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>>> junctionSpecialTemplateBlockColumns;

    /**
     * Creates a new RawStructureTemplate and registers it to RawStructureTemplates.templatesPerVillageType.
     * @param reader The reader used to read the template from a json file.
     * @throws IOException If something goes wrong.
     */
    public static void createNew(JsonReader reader) throws IOException {
        final HashMap<String, String> abbreviationMap;
        ArrayList<Integer> availableForVillagerCount = null;
        double scale = 1.0;
        double edgeRoadDotRadius = 1.0;
        ArrayList<Double> edgeBlockColumnRadii = null;
        HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> edgeTemplateBlockColumns = null;
        double edgeSpecialColumnSpace = 1000000.0;
        ArrayList<Double> edgeSpecialBlockColumnRadii = null;
        HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> edgeSpecialTemplateBlockColumns = null;
        ArrayList<Double> junctionBlockColumnRadii = null;
        HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> junctionTemplateBlockColumns = null;
        ArrayList<Double> junctionSpecialBlockColumnRadii = null;
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
                case "scale" -> scale = reader.nextDouble();
                case "edge_road_dot_radius" -> edgeRoadDotRadius = reader.nextDouble();
                case "edge_block_column_radii" -> edgeBlockColumnRadii = JsonUtils.readList(reader, JsonReader::nextDouble);
                case "edge_template_block_columns" -> edgeTemplateBlockColumns = JsonUtils.readMap(reader, reader1 -> JsonUtils.readMap(reader1, reader2 -> JsonUtils.readList(reader2, reader3 -> new RawVerticalBlockColumn(reader3, abbreviationMap))));
                case "edge_special_column_space" -> edgeSpecialColumnSpace = reader.nextDouble();
                case "edge_special_block_column_radii" -> edgeSpecialBlockColumnRadii = JsonUtils.readList(reader, JsonReader::nextDouble);
                case "edge_special_template_block_columns" -> edgeSpecialTemplateBlockColumns = JsonUtils.readMap(reader, reader1 -> JsonUtils.readMap(reader1, reader2 -> JsonUtils.readList(reader2, reader3 -> new RawVerticalBlockColumn(reader3, abbreviationMap))));
                case "junction_block_column_radii" -> junctionBlockColumnRadii = JsonUtils.readList(reader, JsonReader::nextDouble);
                case "junction_template_block_columns" -> junctionTemplateBlockColumns = JsonUtils.readMap(reader, reader1 -> JsonUtils.readMap(reader1, reader2 -> JsonUtils.readList(reader2, reader3 -> new RawVerticalBlockColumn(reader3, abbreviationMap))));
                case "junction_special_block_column_radii" -> junctionSpecialBlockColumnRadii = JsonUtils.readList(reader, JsonReader::nextDouble);
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
                scale,
                edgeRoadDotRadius,
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
            double scale,
            double edgeRoadDotRadius,
            ArrayList<Double> edgeBlockColumnRadii,
            HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> edgeTemplateBlockColumns,
            double edgeSpecialColumnSpace,
            ArrayList<Double> edgeSpecialBlockColumnRadii,
            HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> edgeSpecialTemplateBlockColumns,
            ArrayList<Double> junctionBlockColumnRadii,
            HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> junctionTemplateBlockColumns,
            ArrayList<Double> junctionSpecialBlockColumnRadii,
            HashMap<String, HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>>> junctionSpecialTemplateBlockColumns) {
        this.availableForVillagerCount = availableForVillagerCount;
        this.scale = scale;
        this.edgeRoadDotRadius = edgeRoadDotRadius;
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
