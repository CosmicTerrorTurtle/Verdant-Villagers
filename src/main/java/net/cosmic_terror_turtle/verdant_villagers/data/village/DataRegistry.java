package net.cosmic_terror_turtle.verdant_villagers.data.village;

import com.google.gson.stream.JsonReader;
import net.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.PointOfInterest;
import net.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.StructureAccessPoint;
import net.minecraft.nbt.NbtCompound;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class DataRegistry {

    private static final Random random = new Random();

    private static final HashMap<String, BlockPaletteType> blockPaletteTypes = new HashMap<>();
    private static final HashMap<String, HashMap<String, BlockPalette>> blockPalettes = new HashMap<>();
    private static final HashMap<String, HashMap<String, BlockPalette>> defaultBlockPalettes = new HashMap<>();
    private static final HashMap<String, VillageTypeData> villageTypes = new HashMap<>();
    private static final HashMap<String, StructureTypeData> structureTypes = new HashMap<>();
    private static final HashMap<String, ArrayList<RawStructureTemplate>> templatesPerVillageType = new HashMap<>();
    private static final ArrayList<String> villageNames = new ArrayList<>();

    private static final HashMap<String, Function<NbtCompound, ? extends PointOfInterest>> pointOfInterestNbtConstructors = new HashMap<>();
    private static final HashMap<String, Function<JsonReader, ? extends RawPointOfInterest>> pointOfInterestJsonConstructors = new HashMap<>();

    public static void clearData() {
        blockPaletteTypes.clear();
        blockPalettes.clear();
        defaultBlockPalettes.clear();
        villageTypes.clear();
        structureTypes.clear();
        templatesPerVillageType.clear();
        villageNames.clear();
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
    public static RawStructureTemplate getRandomTemplateFor(String villageType, String structureType, int villagerCount) {
        ArrayList<RawStructureTemplate> candidates = new ArrayList<>();
        ArrayList<RawStructureTemplate> bestCandidates = new ArrayList<>();
        for (RawStructureTemplate template : templatesPerVillageType.get(villageType)) {
            if (template.dataPerStructureType.containsKey(structureType)) {
                candidates.add(template);
                if (template.availableForVillagerCount.get(0)<=villagerCount && villagerCount<= template.availableForVillagerCount.get(1)) {
                    bestCandidates.add(template);
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
        registerPointOfInterestConstructors("StructureAccessPoint", StructureAccessPoint::new, reader -> {
            try {
                return new RawStructureAccessPoint(reader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Registers constructors for a subclass of {@link PointOfInterest}.
     * @param key The string representing the subclass in nbt or json files.
     * @param nbtConstructor The constructor used when reading the village from an NBT tag.
     * @param jsonConstructor The constructor used when reading a structure template from a json file during data loading/reloading.
     */
    public static void registerPointOfInterestConstructors(String key, Function<NbtCompound, ? extends PointOfInterest> nbtConstructor, Function<JsonReader, ? extends RawPointOfInterest> jsonConstructor) {
        pointOfInterestNbtConstructors.put(key, nbtConstructor);
        pointOfInterestJsonConstructors.put(key, jsonConstructor);
    }

    public static Function<NbtCompound, ? extends PointOfInterest> getPointOfInterestNbtConstructor(String key) {
        return pointOfInterestNbtConstructors.get(key);
    }

    public static Function<JsonReader, ? extends RawPointOfInterest> getPointOfInterestJsonConstructor(String key) {
        return pointOfInterestJsonConstructors.get(key);
    }
}
