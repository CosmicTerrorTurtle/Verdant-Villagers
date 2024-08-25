package io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.VerdantVillagers;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.geo_feature.structure.PointOfInterest;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.geo_feature.structure.SaplingLocationPoint;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.geo_feature.structure.StructureAccessPoint;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DataRegistry {

    private static final Random random = new Random();

    private static final HashMap<String, BlockPaletteType> blockPaletteTypes = new HashMap<>();
    private static final HashMap<String, HashMap<String, BlockPalette>> blockPalettes = new HashMap<>();
    private static final HashMap<String, HashMap<String, BlockPalette>> defaultBlockPalettes = new HashMap<>();
    private static final HashMap<String, SaplingData> saplingData = new HashMap<>();
    private static String saplingSoilTypeWoodBlockPaletteTypeId = VerdantVillagers.MOD_ID + ":wood";
    private static final HashMap<Identifier, ArrayList<String>> saplingSoilTypesPerPaletteId = new HashMap<>();
    private static final HashMap<String, VillageTypeData> villageTypes = new HashMap<>();
    private static final HashMap<String, StructureTypeData> structureTypes = new HashMap<>();
    private static final HashMap<String, TreeFarmStructureTypeData> treeFarmStructureTypes = new HashMap<>();
    private static final HashMap<String, ArrayList<RawStructureTemplate>> templatesPerVillageType = new HashMap<>();
    private static final HashMap<String, RawRoadType> roadTypes = new HashMap<>();
    private static final HashMap<String, RawRoadType> accessPathRoadTypes = new HashMap<>();
    private static final ArrayList<String> villageNames = new ArrayList<>();

    private static final HashMap<String, Function<NbtCompound, ? extends PointOfInterest>> pointOfInterestNbtConstructors = new HashMap<>();
    private static final HashMap<String, BiFunction<JsonReader, HashMap<String, String>, ? extends RawPointOfInterest>> pointOfInterestJsonConstructors = new HashMap<>();

    public static void clearData() {
        blockPaletteTypes.clear();
        blockPalettes.clear();
        defaultBlockPalettes.clear();
        saplingData.clear();
        saplingSoilTypesPerPaletteId.clear();
        villageTypes.clear();
        structureTypes.clear();
        treeFarmStructureTypes.clear();
        templatesPerVillageType.clear();
        roadTypes.clear();
        accessPathRoadTypes.clear();
        villageNames.clear();
    }

    /**
     * Checks if all resource data is valid.
     * @throws RuntimeException if an error gets detected.
     */
    public static void checkData() {

        // For each palette type, there must be at least one palette and default palette.
        for (Map.Entry<String, HashMap<String, BlockPalette>> entry : blockPalettes.entrySet()) {
            if (entry.getValue().isEmpty()) {
                throw new RuntimeException("No block palettes found for palette type '"+entry.getKey()+"'.");
            }
        }
        for (Map.Entry<String, HashMap<String, BlockPalette>> entry : defaultBlockPalettes.entrySet()) {
            if (entry.getValue().isEmpty()) {
                throw new RuntimeException("No default block palettes found for palette type '"+entry.getKey()+"'.");
            }
        }

        // There must be at least one village type present.
        if (villageTypes.isEmpty()) {
            throw new RuntimeException("No village types found.");
        }

        // Each structure type listed in VillageTypeData.structureTypesToBuild must exist.
        for (VillageTypeData typeData : villageTypes.values()) {
            for (String structureType : typeData.structureTypesToBuild) {
                if (!structureTypes.containsKey(structureType)) {
                    throw new RuntimeException("Structure type '"+structureType+"' does not exist.");
                }
            }
        }

        // Each tree farm structure type listed in VillageTypeData.treeFarmStructureTypesToBuild must exist.
        for (VillageTypeData typeData : villageTypes.values()) {
            for (String treeFarmStructureType : typeData.treeFarmStructureTypesToBuild) {
                if (!treeFarmStructureTypes.containsKey(treeFarmStructureType)) {
                    throw new RuntimeException("Tree farm structure type '"+treeFarmStructureType+"' does not exist.");
                }
            }
        }

        // There must be at least one road type present.
        if (roadTypes.isEmpty()) {
            throw new RuntimeException("No road types found.");
        }
        int radiiNum;
        for (RawRoadType type : roadTypes.values()) {
            // Edge columns
            radiiNum = type.edgeBlockColumnRadii.size();
            for (HashMap<String, ArrayList<RawVerticalBlockColumn>> map : type.edgeTemplateBlockColumns.values()) {
                for (ArrayList<RawVerticalBlockColumn> list : map.values()) {
                    if (list.size() > radiiNum) {
                        throw new RuntimeException("Invalid road type: Edge column list must have size <="+radiiNum+".");
                    }
                }
            }
            // Special edge columns
            radiiNum = type.edgeSpecialBlockColumnRadii.size();
            for (HashMap<String, ArrayList<RawVerticalBlockColumn>> map : type.edgeSpecialTemplateBlockColumns.values()) {
                for (ArrayList<RawVerticalBlockColumn> list : map.values()) {
                    if (list.size() > radiiNum) {
                        throw new RuntimeException("Invalid road type: Special edge column list must have size <="+radiiNum+".");
                    }
                }
            }
            // Junction columns
            radiiNum = type.junctionBlockColumnRadii.size();
            for (HashMap<String, ArrayList<RawVerticalBlockColumn>> map : type.junctionTemplateBlockColumns.values()) {
                for (ArrayList<RawVerticalBlockColumn> list : map.values()) {
                    if (list.size() != radiiNum) {
                        throw new RuntimeException("Invalid road type: Junction column list must be of size "+radiiNum+".");
                    }
                }
            }
            // Junction special columns
            radiiNum = type.junctionSpecialBlockColumnRadii.size();
            for (HashMap<String, HashMap<String, ArrayList<RawVerticalBlockColumn>>> map1 : type.junctionSpecialTemplateBlockColumns.values()) {
                for (HashMap<String, ArrayList<RawVerticalBlockColumn>> map2 : map1.values()) {
                    for (ArrayList<RawVerticalBlockColumn> list : map2.values()) {
                        if (list.size() > radiiNum) {
                            throw new RuntimeException("Invalid road type: Special junction column list must have size <="+radiiNum+".");
                        }
                    }
                }
            }
        }

        // There must be at least one access path road type present.
        if (accessPathRoadTypes.isEmpty()) {
            throw new RuntimeException("No access path road types found.");
        }
        for (RawRoadType type : accessPathRoadTypes.values()) {
            // Edge columns
            radiiNum = type.edgeBlockColumnRadii.size();
            for (HashMap<String, ArrayList<RawVerticalBlockColumn>> map : type.edgeTemplateBlockColumns.values()) {
                for (ArrayList<RawVerticalBlockColumn> list : map.values()) {
                    if (list.size() > radiiNum) {
                        throw new RuntimeException("Invalid road type: Edge column list must have size <="+radiiNum+".");
                    }
                }
            }
            // Special edge columns
            radiiNum = type.edgeSpecialBlockColumnRadii.size();
            for (HashMap<String, ArrayList<RawVerticalBlockColumn>> map : type.edgeSpecialTemplateBlockColumns.values()) {
                for (ArrayList<RawVerticalBlockColumn> list : map.values()) {
                    if (list.size() > radiiNum) {
                        throw new RuntimeException("Invalid road type: Special edge column list must have size <="+radiiNum+".");
                    }
                }
            }
        }

        // There must be at least one village name present.
        if (villageNames.isEmpty()) {
            throw new RuntimeException("No village names found.");
        }
    }

    public static void addBlockPaletteType(BlockPaletteType type) {
        blockPaletteTypes.put(type.id.toString(), type);
        blockPalettes.put(type.id.toString(), new HashMap<>());
        defaultBlockPalettes.put(type.id.toString(), new HashMap<>());
    }
    public static BlockPaletteType getPaletteType(String typeKey) {
        if (blockPaletteTypes.containsKey(typeKey)) {
            return blockPaletteTypes.get(typeKey);
        }
        throw new RuntimeException("Block palette type of key "+typeKey+" does not exist.");
    }

    public static void addBlockPalette(BlockPalette palette, boolean addToDefaults) {
        if (blockPalettes.containsKey(palette.typeId.toString())) {
            blockPalettes.get(palette.typeId.toString()).put(palette.id.toString(), palette);
            if (addToDefaults) {
                defaultBlockPalettes.get(palette.typeId.toString()).put(palette.id.toString(), palette);
            }
        }
    }
    public static HashMap<String, BlockPalette> getBlockPalettesOfType(String typeKey) {
        return blockPalettes.get(typeKey);
    }
    public static Set<String> getBlockPaletteTypeKeys() {
        return blockPalettes.keySet();
    }
    public static BlockPalette getBlockPalette(String typeId, String paletteId) {
        return blockPalettes.get(typeId).get(paletteId);
    }
    /**
     * Fetches a random block palette.
     * @param fromDefaults If the palette should be a default palette (true) or if it can be any palette (false).
     * @param typeKey The palette type key.
     * @param blackList A list of block palettes that shall not be returned (this makes sure that no unwanted duplicates get returned).
     * @param random The random instance used for the selection.
     * @return A random default palette for the given key or null if no valid (= not on the black list) palette was found.
     */
    public static BlockPalette getRandomBlockPalette(boolean fromDefaults, String typeKey, ArrayList<BlockPalette> blackList, Random random) {
        BlockPalette[] values;
        if (fromDefaults) {
            values = defaultBlockPalettes.get(typeKey).values().toArray(new BlockPalette[0]);
        } else {
            values = blockPalettes.get(typeKey).values().toArray(new BlockPalette[0]);
        }
        if (values.length <= 0) {
            return null;
        }
        ArrayList<BlockPalette> candidates = new ArrayList<>();
        boolean addToCandidates;
        for (BlockPalette palette : values) {
            addToCandidates = true;
            if (blackList != null) {
                for (BlockPalette onBlackList : blackList) {
                    if (onBlackList.id.equals(palette.id) && onBlackList.typeId.equals(palette.typeId)) {
                        addToCandidates = false;
                        break;
                    }
                }
            }
            if (addToCandidates) {
                candidates.add(palette);
            }
        }
        if (candidates.size() > 0) {
            return candidates.get(random.nextInt(candidates.size()));
        } else {
            return null;
        }
    }

    public static void addSaplingData(HashMap<String, SaplingData> newData) {
        saplingData.putAll(newData);
    }
    public static SaplingData getSaplingData(String id) {
        return saplingData.get(id);
    }

    public static void setSaplingSoilTypeWoodBlockPaletteTypeId(String id) {
        saplingSoilTypeWoodBlockPaletteTypeId = id;
    }
    public static String getSaplingSoilTypeWoodBlockPaletteTypeId() {
        return saplingSoilTypeWoodBlockPaletteTypeId;
    }
    public static void addSaplingSoilTypes(HashMap<String, ArrayList<String>> newTypes) {
        Identifier key;
        for (String strKey : newTypes.keySet()) {
            key = new Identifier(strKey);
            if (!saplingSoilTypesPerPaletteId.containsKey(key)) saplingSoilTypesPerPaletteId.put(key, new ArrayList<>());
            saplingSoilTypesPerPaletteId.get(key).addAll(newTypes.get(strKey));
        }
    }
    public static ArrayList<String> getSaplingSoilTypesForPalette(Identifier paletteId) {
        return saplingSoilTypesPerPaletteId.get(paletteId);
    }

    public static void addVillageTypes(HashMap<String, VillageTypeData> newVillageTypes) {
        for (Map.Entry<String, VillageTypeData> entry : newVillageTypes.entrySet()) {
            villageTypes.put(entry.getKey(), entry.getValue());
            templatesPerVillageType.put(entry.getKey(), new ArrayList<>());
        }
    }
    public static Set<String> getVillageTypes() {
        return villageTypes.keySet();
    }
    public static VillageTypeData getVillageTypeData(String villageType) {
        return villageTypes.get(villageType);
    }

    public static void addStructureTypes(HashMap<String, StructureTypeData> newStructureTypes) {
        structureTypes.putAll(newStructureTypes);
    }
    public static StructureTypeData getStructureTypeData(String structureType) {
        return structureTypes.get(structureType);
    }

    public static void addTreeFarmStructureTypes(HashMap<String, TreeFarmStructureTypeData> newStructureTypes) {
        treeFarmStructureTypes.putAll(newStructureTypes);
    }
    public static TreeFarmStructureTypeData getTreeFarmStructureTypeData(String structureType) {
        return treeFarmStructureTypes.get(structureType);
    }

    public static void addTemplate(RawStructureTemplate template, ArrayList<String> villageTypes) {
        for (String type : villageTypes) {
            templatesPerVillageType.get(type).add(template);
        }
    }
    /**
     * Selects a random raw structure template that matches the given constraints.
     * @param villageType The type of the village.
     * @param structureType The wanted structure type.
     * @param villagerCount The villager count of the village.
     * @return A random template that matches the village and structure types, or null if no template matching the constraints
     * was found. If possible, a template will be selected that is available for the given villager count.
     */
    public static RawStructureTemplate getRandomTemplateFor(
            String villageType, String structureType,
            int villagerCount, HashMap<String, ArrayList<BlockPalette>> blockPalettes) {
        ArrayList<RawStructureTemplate> candidates = new ArrayList<>();
        ArrayList<RawStructureTemplate> bestCandidates = new ArrayList<>();
        boolean fits;
        for (RawStructureTemplate template : templatesPerVillageType.get(villageType)) {
            if (template.dataPerStructureType.containsKey(structureType)) {
                candidates.add(template);
                if (template.availableForVillagerCount.get(0)<=villagerCount && villagerCount<= template.availableForVillagerCount.get(1)) {
                    fits = true;
                    for (Map.Entry<String, ArrayList<String>> entry : template.availableForBlockPalettes.entrySet()) {
                        fits = false;
                        for (BlockPalette palette : blockPalettes.get(entry.getKey())) {
                            if (entry.getValue().contains(palette.id.toString())) {
                                fits = true;
                                break;
                            }
                        }
                        if (!fits) {
                            break;
                        }
                    }
                    if (fits) {
                        bestCandidates.add(template);
                    }
                }
            }
        }
        if (!bestCandidates.isEmpty()) {
            return bestCandidates.get(random.nextInt(bestCandidates.size()));
        }
        if (!candidates.isEmpty()) {
            return candidates.get(random.nextInt(candidates.size()));
        }
        return null;
    }

    public static void addAccessPathRoadType(String id, RawRoadType type) {
        accessPathRoadTypes.put(id, type);
    }
    public static RawRoadType getAccessPathRoadType(String id) {
        return accessPathRoadTypes.get(id);
    }

    public static void addRoadType(String id, RawRoadType type) {
        roadTypes.put(id, type);
    }
    public static RawRoadType getRoadType(String id) {
        return roadTypes.get(id);
    }

    /**
     * Selects a random raw road type that matches the given constraints.
     * @param villageType The type of the village.
     * @param villagerCount The villager count of the village.
     * @return A random road type that matches the village type and villager count, if possible.
     */
    public static RawRoadType getRandomRoadTypeFor(String villageType, int villagerCount) {
        ArrayList<RawRoadType> candidates = new ArrayList<>();
        ArrayList<RawRoadType> bestCandidates = new ArrayList<>();
        for (RawRoadType type : roadTypes.values()) {
            candidates.add(type);
            if (type.availableForVillagerCount.get(0)<=villagerCount && villagerCount<=type.availableForVillagerCount.get(1)) {
                bestCandidates.add(type);
            }
        }
        if (!bestCandidates.isEmpty()) {
            return bestCandidates.get(random.nextInt(bestCandidates.size()));
        }
        if (!candidates.isEmpty()) {
            return candidates.get(random.nextInt(candidates.size()));
        }
        return null;
    }

    public static void addVillageNames(ArrayList<String> newNames) {
        villageNames.addAll(newNames);
    }
    public static String getRandomVillageName() {
        return villageNames.get(random.nextInt(villageNames.size()));
    }



    /**
     * Registers all the village stuff like block states, palettes etc.
     */
    public static void registerVillageStuff() {
        registerPointOfInterestSubclasses();
    }

    private static void registerPointOfInterestSubclasses() {
        registerPointOfInterestConstructors("StructureAccessPoint", StructureAccessPoint::new, (reader, abbreviationMap) -> {
            try { return new RawStructureAccessPoint(reader); }
            catch (IOException e) { throw new RuntimeException(e); }
        });
        registerPointOfInterestConstructors("SaplingLocationPoint", SaplingLocationPoint::new, (reader, abbreviationMap) -> {
            try { return new RawSaplingLocationPoint(reader); }
            catch (IOException e) { throw new RuntimeException(e); }
        });
    }

    /**
     * Registers constructors for a subclass of {@link PointOfInterest}.
     * @param key The string representing the subclass in nbt or json files.
     * @param nbtConstructor The constructor used when reading the village from an NBT tag.
     * @param jsonConstructor The constructor used when reading a structure template from a json file during data loading/reloading.
     */
    public static void registerPointOfInterestConstructors(String key, Function<NbtCompound, ? extends PointOfInterest> nbtConstructor, BiFunction<JsonReader, HashMap<String, String>, ? extends RawPointOfInterest> jsonConstructor) {
        pointOfInterestNbtConstructors.put(key, nbtConstructor);
        pointOfInterestJsonConstructors.put(key, jsonConstructor);
    }

    public static Function<NbtCompound, ? extends PointOfInterest> getPointOfInterestNbtConstructor(String key) {
        return pointOfInterestNbtConstructors.get(key);
    }

    public static BiFunction<JsonReader, HashMap<String, String>, ? extends RawPointOfInterest> getPointOfInterestJsonConstructor(String key) {
        return pointOfInterestJsonConstructors.get(key);
    }
}
