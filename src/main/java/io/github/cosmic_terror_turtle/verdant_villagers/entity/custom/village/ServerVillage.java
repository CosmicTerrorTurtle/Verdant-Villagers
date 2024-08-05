package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import io.github.cosmic_terror_turtle.verdant_villagers.VerdantVillagers;
import io.github.cosmic_terror_turtle.verdant_villagers.data.village.*;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.VillageHeartEntity;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road.*;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.*;
import io.github.cosmic_terror_turtle.verdant_villagers.util.MathUtils;
import io.github.cosmic_terror_turtle.verdant_villagers.util.ModTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LargeEntitySpawnHelper;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ServerVillage extends Village {

    /**
     * The different phases that the village cycles through when updating.
     */
    private enum UpdateCyclePhase {PAUSE, STRUCTURES, ROADS, VILLAGERS}
    /**
     * Used for defining what block positions are valid ground positions. {@link SurfaceFluidMode#NONE} is for normal
     * positions on land, {@link SurfaceFluidMode#AS_GROUND} is for positions on land and on the fluid surface, and
     * {@link SurfaceFluidMode#AS_AIR} is for positions on land and on the fluid ground (for example, the sea floor).
     */
    public enum SurfaceFluidMode {NONE, AS_GROUND, AS_AIR}


    /**
     * If true, the village will immediately place a feature once it is planned.
     */
    public static final boolean PLACE_BLOCKS_DIRECTLY = true;
    /**
     * If true, the village will speed up its planning process.
     */
    public static final boolean PLAN_FAST = true;
    public static final int VILLAGER_COUNT_DELTA = 5;

    /**
     * The maximum number of junctions that a village can have. If this number is reached, no more roads will be planned.
     */
    private static final int JUNCTION_LIMIT = 50;

    /**
     * The distance in positions between different placement attempts in road planning.
     */
    private static final int SEARCH_DISTANCE_ROAD = 30;
    /**
     * The distance in positions between different placement attempts in structure planning.
     */
    private static final int SEARCH_DISTANCE_STRUCTURE = 15;
    /**
     * If the distance between two positions is lower than this threshold, they are considered to be close to each other.
     */
    private static final int POSITIONS_ARE_CLOSE_DISTANCE = 80;
    /**
     * The minimum number of near road junctions needed for a structure position to be valid.
     */
    private static final int MIN_NEAR_ROAD_JUNCTIONS = 6;
    /**
     * The basic minimum space between two road junctions (regardless of whether they are connected or not).
     */
    private static final int ROAD_JUNCTION_BASE_SPACE = 44;
    /**
     * The basic minimum length that a road edge is allowed to have.
     */
    private static final int ROAD_EDGE_BASE_MIN_LENGTH = ROAD_JUNCTION_BASE_SPACE;
    /**
     * The basic maximum length that a road edge is allowed to have.
     */
    private static final int ROAD_EDGE_BASE_MAX_LENGTH = 66;
    /**
     * The maximum slope angle that a road edge can have.
     */
    public static final double ROAD_EDGE_MAX_Y_SLOPE = 0.39;
    /**
     * The basic maximum length that an access path can have.
     */
    private static final int ACCESS_PATH_BASE_MAX_LENGTH = 20;
    public static final int ROAD_PILLAR_EXTENSION_LENGTH = 20;

    /**
     * If true, the villagers get counted normally. If false, the count is incremented every {@link ServerVillage#update()}
     * call and can be manually adjusted by the player.
     */
    public static boolean countVillagers = false;


    // Fields deleted when the entity gets unloaded.
    private int ticksSinceLastUpdate = 0;
    private final World world;
    private BlockPos pos;
    public final Random random;
    public final RoadTypeProvider roadTypeProvider;
    public final StructureProvider structureProvider;
    private UpdateCyclePhase cyclePhase = UpdateCyclePhase.PAUSE;
    private RoadType roadType = null;
    private double needForRoads = 0;
    private final ArrayList<RoadEdge> accessPathsToPlace = new ArrayList<>();

    // Fields persistent when the entity gets unloaded.
    private int nextElementID; // The unique id that is given next to a new feature. Always post-increment when assigning the next id.
    // Village type parameters
    private final float landAbove;
    private final float landBelow;
    private final float airAbove;
    private final float airBelow;
    private final float fluidAbove;
    private final float fluidBelow;
    private String villageType;
    public final HashMap<Identifier, Integer> blockCounts; // Map that holds the number of blocks contained in the mega chunks for each type.
    private final ArrayList<MegaChunk> megaChunks;
    private final HashMap<String, ArrayList<BlockPalette>> blockPalettes; // Map between block palette type and the list of palettes this village uses for that type.
    private int blockPaletteLevel; // Current level for the block palettes.
    private final ArrayList<RoadJunction> roadJunctions;
    private final ArrayList<RoadEdge> roadEdges;
    private final ArrayList<RoadEdge> accessPaths;
    private final ArrayList<Structure> structures;


    /**
     * Creates a new Village.
     * @param villageHeart The village heart that this Village belongs to.
     */
    public ServerVillage(VillageHeartEntity villageHeart) {
        super(villageHeart);

        world = villageHeart.world;
        pos = villageHeart.getBlockPos();
        random = new Random();
        roadTypeProvider = new RoadTypeProvider(this);
        structureProvider = new StructureProvider(this);

        // Initialize persistent fields

        // Misc values
        nextElementID = 0;
        villageHeart.setCustomName(Text.literal(DataRegistry.getRandomVillageName()));
        villagerCount = 0;

        // Analyze terrain
        int xzTerrainRadius = 64;
        int yTerrainRadius = 25;
        BlockPos testPos;
        // Start total at 1 to avoid zero division
        int totalA = 1;
        int totalB = 1;
        int landA = 0;
        int airA = 0;
        int fluidA = 0;
        int landB = 0;
        int airB = 0;
        int fluidB = 0;
        for (int i=-xzTerrainRadius; i<xzTerrainRadius; i++) {
            for (int j=-yTerrainRadius; j<yTerrainRadius; j++) {
                for (int k=-xzTerrainRadius; k<xzTerrainRadius; k++) {
                    testPos = pos.add(i, j, k);
                    // Ignore positions outside the height limit
                    if (world.getBlockState(testPos).isOf(Blocks.VOID_AIR)) {
                        continue;
                    }
                    if (j<0) totalB++; else totalA++;
                    if (world.getFluidState(testPos).isEmpty()) {
                        if (world.getBlockState(testPos).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS)) {
                            // Ground
                            if (j<0) landB++; else landA++;
                        } else {
                            // Neither ground nor fluid
                            if (j<0) airB++; else airA++;
                        }
                    } else {
                        // Fluid
                        if (j<0) fluidB++; else fluidA++;
                    }
                }
            }
        }
        landAbove = (float) landA / totalA;
        landBelow = (float) landB / totalB;
        airAbove = (float) airA / totalA;
        airBelow = (float) airB / totalB;
        fluidAbove = (float) fluidA / totalA;
        fluidBelow = (float) fluidB / totalB;

        // Determine terrain category
        String terrainCategory;
        if (airBelow > 0.7) {
            terrainCategory = "sky";
        } else if (landAbove > 0.55) {
            terrainCategory = "under_ground";
        } else if (fluidAbove > 0.30) {
            terrainCategory = "under_fluid";
        } else if (fluidBelow > 0.15) {
            terrainCategory = "on_coast";
        } else {
            terrainCategory = "on_land";
        }

        // Determine village type
        villageType = null;
        ArrayList<String> villageTypes = new ArrayList<>(DataRegistry.getVillageTypes());
        ArrayList<String> fittingVillageTypes = new ArrayList<>();
        VillageTypeData data;
        RegistryEntry<Biome> biomeEntry = world.getBiome(pos);
        String biomeID = biomeEntry.getKey().get().getValue().toString();
        boolean biomeOkay;
        for (String vType : villageTypes) {
            // Check if terrain category, dimension and biome of each village type are sufficient
            data = DataRegistry.getVillageTypeData(vType);
            if (!data.dimensions.isEmpty() && !data.dimensions.contains(world.getDimensionKey().getValue().toString())) {
                continue;
            }
            if (!data.terrainCategory.equals(terrainCategory)) {
                continue;
            }

            biomeOkay = false;
            if (data.biomes.isEmpty()) {
                biomeOkay = true;
            } else {
                for (String biome : data.biomes) {
                    if (biome.startsWith("#")) {
                        // Test biome tag
                        if (biomeEntry.isIn(TagKey.of(RegistryKeys.BIOME, new Identifier(biome.substring(1))))) {
                            biomeOkay = true;
                            break;
                        }
                    } else {
                        // Test biome
                        if (biome.equals(biomeID)) {
                            biomeOkay = true;
                            break;
                        }
                    }
                }
            }
            if (biomeOkay) {
                // The village type fits.
                fittingVillageTypes.add(vType);
            }
        }
        if (fittingVillageTypes.isEmpty()) {
            villageType = "default";
        } else {
            villageType = fittingVillageTypes.get(random.nextInt(fittingVillageTypes.size()));
        }

        // Maps, lists and block palettes

        blockPaletteLevel = 0;
        blockPalettes = new HashMap<>();
        for (String typeKey : DataRegistry.getBlockPaletteTypeKeys()) {
            blockPalettes.put(typeKey, new ArrayList<>());
        }
        HashMap<Identifier, Integer> tmpBlockCounts = new HashMap<>(); // Map used for initial terrain scan close to the center
        Identifier blockId;
        int xzPaletteRadius = 80;
        int yPaletteRadius = 15;
        for (int i=-xzPaletteRadius; i<xzPaletteRadius; i++) {
            for (int j=-yPaletteRadius; j<yPaletteRadius; j++) {
                for (int k=-xzPaletteRadius; k<xzPaletteRadius; k++) {
                    blockId = Registries.BLOCK.getId(world.getBlockState(pos.add(i, j, k)).getBlock());
                    tmpBlockCounts.put(blockId, tmpBlockCounts.getOrDefault(blockId, 0)+1);
                }
            }
        }
        addBlockPalettesForAllTypes(tmpBlockCounts);

        blockCounts = new HashMap<>();
        megaChunks = new ArrayList<>();
        addMegaChunksAround(pos);

        roadJunctions = new ArrayList<>();
        roadEdges = new ArrayList<>();
        accessPaths = new ArrayList<>();
        structures = new ArrayList<>();

        // output name, type and terrain stats for testing
        for (PlayerEntity player : world.getPlayers()) {
            player.sendMessage(Text.literal(villageHeart.getName().getString()+" ("+villageType+")"));
            player.sendMessage(Text.literal(
                    "A/B land: " + landAbove + "/" + landBelow + ", fluid: " + fluidAbove + "/" + fluidBelow + ", air: " + airAbove + "/" + airBelow
            ));
        }
    }

    // Serialize all data that needs to be persistent.
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();

        nbt.putInt("nextElementID", nextElementID);
        nbt.putInt("villagerCount", villagerCount);
        nbt.putFloat("landAbove", landAbove);
        nbt.putFloat("landBelow", landBelow);
        nbt.putFloat("airAbove", airAbove);
        nbt.putFloat("airBelow", airBelow);
        nbt.putFloat("fluidAbove", fluidAbove);
        nbt.putFloat("fluidBelow", fluidBelow);
        nbt.putString("villageType", villageType);

        // Block counts
        NbtCompound blockCountsNbt = new NbtCompound();
        for (Map.Entry<Identifier, Integer> entry : blockCounts.entrySet()) {
            blockCountsNbt.putInt(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("blockCounts", blockCountsNbt);

        // Mega chunks
        NbtCompound megaChunksNbt = new NbtCompound();
        for (int i=0; i<megaChunks.size(); i++) {
            megaChunksNbt.put(Integer.toString(i), megaChunks.get(i).toNbt());
        }
        nbt.put("megaChunks", megaChunksNbt);

        // Block palettes
        NbtCompound blockPalettesNbt = new NbtCompound();
        NbtCompound palettesForTypeNbt;
        ArrayList<BlockPalette> palettesPerType;
        for (String typeKey : DataRegistry.getBlockPaletteTypeKeys()) {
            palettesForTypeNbt = new NbtCompound();
            palettesPerType = blockPalettes.get(typeKey);
            for (int i=0; i<palettesPerType.size(); i++) {
                palettesForTypeNbt.putString(String.valueOf(i), palettesPerType.get(i).id.toString());
            }
            blockPalettesNbt.put(typeKey, palettesForTypeNbt);
        }
        nbt.put("blockPalettes", blockPalettesNbt);
        nbt.putInt("blockPaletteLevel", blockPaletteLevel);

        // Road junctions
        NbtCompound roadJunctionsNbt = new NbtCompound();
        for (int i=0; i<roadJunctions.size(); i++) {
            roadJunctionsNbt.put(Integer.toString(i), roadJunctions.get(i).toNbt());
        }
        nbt.put("roadJunctions", roadJunctionsNbt);

        // Road edges
        NbtCompound roadEdgesNbt = new NbtCompound();
        for (int i=0; i<roadEdges.size(); i++) {
            roadEdgesNbt.put(Integer.toString(i), roadEdges.get(i).toNbt(true));
        }
        nbt.put("roadEdges", roadEdgesNbt);

        // Access paths
        NbtCompound accessPathsNbt = new NbtCompound();
        for (int i=0; i<accessPaths.size(); i++) {
            accessPathsNbt.put(Integer.toString(i), accessPaths.get(i).toNbt(false));
        }
        nbt.put("accessPaths", accessPathsNbt);

        // Structures
        NbtCompound structuresNbt = new NbtCompound();
        for (int i=0; i<structures.size(); i++) {
            structuresNbt.put(Integer.toString(i), structures.get(i).toNbt());
        }
        nbt.put("structures", structuresNbt);

        return nbt;
    }

    /**
     * Reads a Village from an NbtCompound.
     * @param villageHeart The village heart that this Village belongs to.
     * @param nbt The compound as returned by Village.toNbt().
     */
    public ServerVillage(VillageHeartEntity villageHeart, @NotNull NbtCompound nbt) {
        super(villageHeart);

        world = villageHeart.world;
        pos = villageHeart.getBlockPos();
        random = new Random();
        roadTypeProvider = new RoadTypeProvider(this);
        structureProvider = new StructureProvider(this);

        // Read the village heart from the nbt tag.

        nextElementID = nbt.getInt("nextElementID");
        villagerCount = nbt.getInt("villagerCount");
        landAbove = nbt.getFloat("landAbove");
        landBelow = nbt.getFloat("landBelow");
        airAbove = nbt.getFloat("airAbove");
        airBelow = nbt.getFloat("airBelow");
        fluidAbove = nbt.getFloat("fluidAbove");
        fluidBelow = nbt.getFloat("fluidBelow");
        villageType = nbt.getString("villageType");

        // Block counts
        NbtCompound blockCountsNbt = nbt.getCompound("blockCounts");
        blockCounts = new HashMap<>();
        for (String key : blockCountsNbt.getKeys()) {
            blockCounts.put(new Identifier(key), blockCountsNbt.getInt(key));
        }

        // Mega chunks
        NbtCompound megaChunksNbt = nbt.getCompound("megaChunks");
        megaChunks = new ArrayList<>();
        for (String key : megaChunksNbt.getKeys()) {
            megaChunks.add(new MegaChunk(megaChunksNbt.getCompound(key)));
        }

        // Block palettes
        NbtCompound blockPalettesNbt = nbt.getCompound("blockPalettes");
        NbtCompound palettesForTypeNbt;
        int palettesPerTypeCount;
        blockPalettes = new HashMap<>();
        for (String typeKey : DataRegistry.getBlockPaletteTypeKeys()) {
            blockPalettes.put(typeKey, new ArrayList<>());
            if (blockPalettesNbt.contains(typeKey)) {
                palettesForTypeNbt = blockPalettesNbt.getCompound(typeKey);
                palettesPerTypeCount = palettesForTypeNbt.getKeys().size();
                for (int i=0; i<palettesPerTypeCount; i++) {
                    blockPalettes.get(typeKey).add(DataRegistry.getBlockPalette(typeKey, palettesForTypeNbt.getString(String.valueOf(i))));
                }
            } else {
                addBlockPaletteFor(typeKey, blockCounts);
            }
        }
        blockPaletteLevel = nbt.getInt("blockPaletteLevel");

        // Road junctions
        NbtCompound roadJunctionsNbt = nbt.getCompound("roadJunctions");
        roadJunctions = new ArrayList<>();
        for (String key : roadJunctionsNbt.getKeys()) {
            roadJunctions.add(new RoadJunction(roadJunctionsNbt.getCompound(key)));
        }

        // Road edges
        NbtCompound roadEdgesNbt = nbt.getCompound("roadEdges");
        roadEdges = new ArrayList<>();
        for (String key : roadEdgesNbt.getKeys()) {
            roadEdges.add(new RoadEdge(roadEdgesNbt.getCompound(key), roadJunctions));
        }

        // Access paths
        NbtCompound accessPathsNbt = nbt.getCompound("accessPaths");
        accessPaths = new ArrayList<>();
        for (String key : accessPathsNbt.getKeys()) {
            accessPaths.add(new RoadEdge(accessPathsNbt.getCompound(key), null));
        }

        // Structures
        NbtCompound structuresNbt = nbt.getCompound("structures");
        structures = new ArrayList<>();
        for (String key : structuresNbt.getKeys()) {
            structures.add(new Structure(structuresNbt.getCompound(key)));
        }
    }

    public World getWorld() {
        return world;
    }

    public void changeVillagerCount(int amount) {
        villagerCount += amount;
        if (villagerCount < 0) {
            villagerCount = 0;
        }
        villageHeart.markForUpdate();
    }

    /**
     * Determines the number of block palettes this village should have depending on the current villager count.
     * @return The number of block palettes.
     */
    private int getBlockPaletteLevel() {
        if (villagerCount < 10) {
            return 0;
        } else if (villagerCount < 20) {
            return 1;
        } else if (villagerCount < 40) {
            return 2;
        } else if (villagerCount < 60) {
            return 3;
        }
        return 4;
    }

    /**
     * Calls addBlockPaletteFor() for all palette types and resets the template manager afterwards.
     * @param blockCountMap The block count map that shall be used for evaluating a palette.
     */
    private void addBlockPalettesForAllTypes(HashMap<Identifier, Integer> blockCountMap) {
        for (String typeKey : DataRegistry.getBlockPaletteTypeKeys()) {
            addBlockPaletteFor(typeKey, blockCountMap);
        }
        structureProvider.resetTemplates();
    }

    /**
     * Adds a new(!) block palette to the list. This takes into account the list of indicator blocks
     * of each palette and adds the block palette that matches the village terrain the most. If no new palette
     * fits the terrain sufficiently, a random default palette is added.
     * @param typeKey The palette type.
     * @param blockCountMap The block count map that shall be used for evaluating a palette.
     */
    private void addBlockPaletteFor(String typeKey, HashMap<Identifier, Integer> blockCountMap) {
        // Get all palettes of the given type and add them to the list of candidates if the palette is not in the village's list.
        ArrayList<BlockPalette> newPalettes = new ArrayList<>();
        boolean paletteIsNew;
        for (BlockPalette registeredPalette : DataRegistry.getBlockPalettesOfType(typeKey).values()) {
            paletteIsNew = true;
            for (BlockPalette oldPalette : blockPalettes.get(typeKey)) {
                if (oldPalette.id.equals(registeredPalette.id)) {
                    paletteIsNew = false;
                    break;
                }
            }
            if (paletteIsNew) {
                newPalettes.add(registeredPalette);
            }
        }
        // If no new palettes are available, just return.
        if (newPalettes.isEmpty()) {
            return;
        }
        // Count points for all new palettes.
        HashMap<Identifier, Integer> pointsPerPalette = new HashMap<>();
        for (Map.Entry<Identifier, Integer> entry : blockCountMap.entrySet()) {
            for (BlockPalette newPalette : newPalettes) {
                for (Block block : newPalette.indicatorBlocks) {
                    if (entry.getKey().equals(Registries.BLOCK.getId(block))) {
                        pointsPerPalette.put(newPalette.id, pointsPerPalette.getOrDefault(newPalette.id, 0) + entry.getValue());
                    }
                }
            }
        }
        // Find the palette with the highest score.
        int highscore = -1;
        BlockPalette bestNewPalette = newPalettes.get(0);
        for (BlockPalette newPalette : newPalettes) {
            if (pointsPerPalette.getOrDefault(newPalette.id, 0) > highscore) {
                highscore = pointsPerPalette.getOrDefault(newPalette.id, 0);
                bestNewPalette = newPalette;
            }
        }

        // Add palette with the highest points or a random default palette, if the highscore was not higher than the threshold.
        int threshold = 25;
        if (highscore > threshold) {
            blockPalettes.get(typeKey).add(bestNewPalette);
        } else {
            addRandomDefaultBlockPaletteOf(typeKey);
        }
    }

    /**
     * Adds a random default palette to the list that is not already in the list.
     * @param typeKey The palette type key of the list.
     */
    private void addRandomDefaultBlockPaletteOf(String typeKey) {
        BlockPalette palette = DataRegistry.getRandomBlockPalette(true, typeKey, blockPalettes.get(typeKey), random);
        if (palette != null) {
            blockPalettes.get(typeKey).add(palette);
        }
    }

    /**
     * Gets a block palette.
     * @param typeKey The type that the palette should be of.
     * @param paletteIndex The index of the palette indicating whether the primary (index=0), secondary(index=1),
     *                     tertiary(index=2) etc. palette for the given type shall be returned.
     * @return The palette of the given type and index.
     */
    public BlockPalette getBlockPaletteOf(Identifier typeKey, int paletteIndex) {
        ArrayList<BlockPalette> list = blockPalettes.get(typeKey.toString());
        if (paletteIndex >= list.size() || paletteIndex < 0) {
            paletteIndex = 0;
        }
        return list.get(paletteIndex);
    }

    /**
     * Calculates the time between update cycle steps. It is dependent on the number of villagers (representing the
     * village size; a bigger village will need more frequent updates). The minimum time is 5 seconds at >>1000 villagers,
     * the maximum time 20 seconds at 0 villagers. If {@link ServerVillage#PLAN_FAST} is true, the return value will
     * always be 1.5 seconds.
     * @return The time in ticks.
     */
    private double getTicksBetweenUpdates() {
        if (PLAN_FAST) {
            return 20 * 1.5;
        }
        return 20 * (5 + 15 * Math.pow(1.03, -villagerCount));
    }

    @Override
    public void tick() {
        ticksSinceLastUpdate++;
        if (ticksSinceLastUpdate > getTicksBetweenUpdates()) {
            ticksSinceLastUpdate = 0;
            update();
        }
    }

    /**
     * Determines the need for more structures, roads etc. and updates the building plan of the village.
     */
    private void update() {
        switch (cyclePhase) {
            case PAUSE -> {
                // Reset variables
                needForRoads = 0;

                // Update position
                pos = villageHeart.getBlockPos();

                // Update road type
                roadType = roadTypeProvider.getRoadType(DataRegistry.getRandomRoadTypeFor(villageType, villagerCount));

                // Add new block palettes if necessary.
                if (getBlockPaletteLevel() > blockPaletteLevel) {
                    blockPaletteLevel = getBlockPaletteLevel();
                    addBlockPalettesForAllTypes(blockCounts);
                }

                // Count iron golems and villagers
                Box box;
                int ironGolemCount = 0;
                if (countVillagers) {
                    villagerCount = 0;
                } else if (villagerCount < 150 && random.nextDouble() < 0.3) {
                    villagerCount++;
                }
                for (MegaChunk megaChunk : megaChunks) {
                    box = megaChunk.getBox();
                    ironGolemCount += world.getEntitiesByClass(IronGolemEntity.class, box, entity -> true).size();
                    if (countVillagers) {
                        villagerCount += world.getEntitiesByClass(VillagerEntity.class, box, entity -> true).size();
                    }
                }

                // Spawn iron golems if necessary
                if (ironGolemCount * 10 < villagerCount && random.nextDouble() < 0.08) {
                    LargeEntitySpawnHelper.trySpawnAt(EntityType.IRON_GOLEM, SpawnReason.MOB_SUMMONED, (ServerWorld) world, pos,
                            10, (int)(roadType.scale*50), 35, LargeEntitySpawnHelper.Requirements.IRON_GOLEM);
                }

                // Spawn villagers if necessary
                if (villagerCount < 5 && random.nextDouble() < 0.1) {
                    //<try to spawn villager>
                }

                cyclePhase = UpdateCyclePhase.STRUCTURES;
            }
            case STRUCTURES -> {
                // Plan structures (select one type at random).
                String selectedStructureType = DataRegistry.getVillageTypeData(villageType).getRandomStructureTypeToBuild(random);
                // Determine whether there are enough structures of the selected structure type present
                switch (DataRegistry.getStructureTypeData(selectedStructureType).structureCheckMethod) {
                    default -> {}
                    case "count_villagers" -> {

                        float villagersAccountedFor = 0;
                        for (Structure structure : structures) {
                            if (structure.dataPerStructureType.containsKey(selectedStructureType)) {
                                villagersAccountedFor += Float.parseFloat(structure.dataPerStructureType.get(selectedStructureType).get("villagers_accounted_for"));
                            }
                        }
                        if (villagersAccountedFor < getInflatedVillagerCount(villagerCount)) {
                            if (!planSingleStructure(selectedStructureType)) {
                                needForRoads++;
                            }
                        }

                    }
                }
                cyclePhase = UpdateCyclePhase.ROADS;
            }
            case ROADS -> {
                // Based on the need for roads, attempt to plan new road junctions and edges.
                planNewRoads(needForRoads);
                cyclePhase = UpdateCyclePhase.VILLAGERS;
            }
            case VILLAGERS -> {
                // Plant saplings
                ArrayList<Integer> groupCounts = new ArrayList<>();
                ArrayList<BlockPalette> woodBlockPalettes = blockPalettes.get(VerdantVillagers.MOD_ID+":wood");
                BlockState saplingState;
                SaplingData saplingData;
                int requiredTreeDiameter;
                for (Structure structure : structures) {
                    if (structure.dataPerStructureType.containsKey("tree_farm_1")) {
                        groupCounts.add(1);
                    }
                    if (structure.dataPerStructureType.containsKey("tree_farm_4")) {
                        groupCounts.add(4);
                    }
                    for (Integer saplingGroupCount : groupCounts) {
                        // For each group count, select a random sapling block from the wood block palettes.
                        saplingState = woodBlockPalettes.get(random.nextInt(0, woodBlockPalettes.size())).getBlockState(Blocks.OAK_SAPLING.getDefaultState());
                        saplingData = DataRegistry.getSaplingData(Registries.BLOCK.getId(saplingState.getBlock()).toString());
                        if (saplingData.diametersPerTreeType.containsKey(saplingGroupCount)) {
                            requiredTreeDiameter = saplingData.diametersPerTreeType.get(saplingGroupCount);
                            for (PointOfInterest poi : structure.pointsOfInterest) {
                                if (poi instanceof SaplingLocationPoint saplingLocationPoint
                                        && saplingLocationPoint.saplings == saplingGroupCount
                                        && saplingLocationPoint.treeDiameter >= requiredTreeDiameter
                                        && world.getBlockState(saplingLocationPoint.pos).isOf(Blocks.AIR)) {
                                    if (PLACE_BLOCKS_DIRECTLY) {
                                        attemptToPlace(new GeoFeatureBit(saplingState, saplingLocationPoint.pos));
                                    } else {
                                        // Task villagers with planting the saplings (all at the same time for the same sapling group count!!!)
                                    }
                                }
                            }
                        }
                    }
                }

                //output name and villager count for testing
                for (PlayerEntity player : world.getPlayers()) {
                    player.sendMessage(Text.literal(villageHeart.getName().getString()+" ("+villagerCount+")"));
                }//

                cyclePhase = UpdateCyclePhase.PAUSE;
            }
        }
    }

    /**
     * Adds all mega chunks in an n*3*n cube around the given position, where n is 3 for small villages and 5 for big
     * villages.
     * @param blockPos A position in the central mega chunk.
     */
    private void addMegaChunksAround(BlockPos blockPos) {
        int ikMin = villagerCount < 100 ? -1 : -2;
        int ikMax = villagerCount < 100 ? 2 : 3;
        MegaChunk chunkCandidate;
        boolean chunkIsNew;
        for (int i=ikMin; i<ikMax; i++) {
            for (int j=-1; j<2; j++) {
                for (int k=ikMin; k<ikMax; k++) {
                    chunkCandidate = new MegaChunk(nextElementID++, blockPos.add(i*MegaChunk.LENGTH, j*MegaChunk.LENGTH, k*MegaChunk.LENGTH));
                    chunkIsNew = true;
                    for (MegaChunk megaChunk : megaChunks) {
                        if (megaChunk.getLowerTip().equals(chunkCandidate.getLowerTip())) {
                            chunkIsNew = false;
                            break;
                        }
                    }
                    if (chunkIsNew) {
                        // Add new chunk, count blocks and determine features to be removed.
                        megaChunks.add(chunkCandidate);
                        chunkCandidate.scanBlocks(world, blockCounts, true);
                    }
                }
            }
        }
    }

    /**
     * Calculates an inflated villager count for future-safe village planning.
     * @param actualCount The actual villager count.
     * @return The inflated villager count.
     */
    private static double getInflatedVillagerCount(double actualCount) {
        return 5+1.1*actualCount;
    }

    /**
     * Determines the block position of the surface block closest to the given position.
     * @param startPosition The position of the block from which the search should start (+/- a small random offset).
     * @param minY The minimum Y value checked.
     * @param maxY The maximum Y value checked.
     * @param isForGeoFeature Whether the return value will be used for determining the center of a {@link GeoFeature}.
     *                        For road edge terrain adjustments, select false.
     * @return The block position of the surface block or null if no surface block was found.
     */
    public BlockPos getSurfaceBlock(BlockPos startPosition, int minY, int maxY, boolean isForGeoFeature) {
        // For underground or sky terrain categories, sometimes return random position in the given range.
        String terrainCategory = DataRegistry.getVillageTypeData(villageType).terrainCategory;
        if (isForGeoFeature && random.nextDouble() < 0.5 && (
                terrainCategory.equals("under_ground")
                || terrainCategory.equals("sky")
        )) {
            return startPosition.withY(MathUtils.nextInt(minY, maxY));
        }

        // Determine the surface fluid mode depending on the village's terrain category.
        SurfaceFluidMode surfaceFluidMode = SurfaceFluidMode.NONE;
        if (terrainCategory.equals("on_coast")) {
            surfaceFluidMode = SurfaceFluidMode.AS_GROUND;
        } else if (terrainCategory.equals("under_fluid")) {
            surfaceFluidMode = SurfaceFluidMode.AS_AIR;
        }

        // Randomly alter starting Y for accessing different heights.
        startPosition = startPosition.withY(MathUtils.nextInt((minY+startPosition.getY())/2, (maxY+startPosition.getY())/2));

        // Locate surface block.
        BlockPos result = null;
        if (world != null) {
            // Check blocks below.
            for (int yCoord=startPosition.getY(); yCoord>=minY; yCoord--) {
                if (positionIsValidSurfaceLevel(startPosition.withY(yCoord), surfaceFluidMode)) {
                    result = startPosition.withY(yCoord);
                    break;
                }
            }
            // Check blocks above.
            for (int yCoord=startPosition.getY(); yCoord<=maxY; yCoord++) {
                if (positionIsValidSurfaceLevel(startPosition.withY(yCoord), surfaceFluidMode)) {
                    if (result!=null && yCoord-startPosition.getY() > startPosition.getY()-result.getY()) {
                        return result;
                    } else {
                        return startPosition.withY(yCoord);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Checks if a position is valid.
     * @param position The position that should be checked.
     * @param surfaceFluidMode Whether position, the block above it or none of them can have fluids.
     * @return True if the position is an (upwards) surface block.
     */
    private boolean positionIsValidSurfaceLevel(BlockPos position, SurfaceFluidMode surfaceFluidMode) {
        if (world == null) {
            return false;
        } else {
            return switch (surfaceFluidMode) {
                case NONE ->
                        world.getBlockState(position).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS)
                        && !world.getBlockState(position.up()).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS)
                        && world.getFluidState(position.up()).isEmpty();
                case AS_GROUND ->
                        (world.getBlockState(position).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS) || !world.getFluidState(position).isEmpty())
                        && !world.getBlockState(position.up()).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS)
                        && world.getFluidState(position.up()).isEmpty();
                case AS_AIR ->
                        world.getBlockState(position).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS)
                        && !world.getBlockState(position.up()).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS);
            };
        }
    }

    /**
     * Tries to plan new road junctions and edges.
     * @param needForRoads A number indicating the need for roads.
     */
    private void planNewRoads(double needForRoads) {
        // Attempt to plan new roads if the village has not reached the maximum number of road junctions.
        int maxNewRoads = 1;
        for (int i=0; i<needForRoads && i<maxNewRoads && roadJunctions.size() < JUNCTION_LIMIT; i++) {
            planSingleJunctionWithEdges();
        }
    }

    /**
     * Attempts to add a single junction to the road network.
     */
    private void planSingleJunctionWithEdges() {
        boolean withinBounds = true;
        boolean foundPositionNearJunctions = true;
        double startAngle;

        BlockPos testPos;
        int surfaceBlockMaxYOffset = (int)(50*roadType.scale);
        MegaChunk testPosMegaChunk;
        RoadJunction newJunction;
        RoadEdge testEdge;
        ArrayList<RoadEdge> newEdges = new ArrayList<>();
        double testSquaredDist;

        // Loop through different search radii.
        for (double searchRadius = random.nextDouble(SEARCH_DISTANCE_ROAD);
             withinBounds && foundPositionNearJunctions;
             searchRadius += SEARCH_DISTANCE_ROAD
        ) {
            withinBounds = false;
            foundPositionNearJunctions = false;
            startAngle = random.nextDouble(2*Math.PI);

            // Loop through different points on the circle.
            angleFor: for (double addAngle = 0;
                 addAngle < 2*Math.PI;
                 addAngle += SEARCH_DISTANCE_ROAD/searchRadius
            ) {
                // Determine the test position.
                testPos = pos.add((int) (searchRadius*Math.cos(startAngle+addAngle)), 0, (int) (searchRadius*Math.sin(startAngle+addAngle)));
                testPos = getSurfaceBlock(testPos, testPos.getY()-surfaceBlockMaxYOffset, testPos.getY()+surfaceBlockMaxYOffset, true);
                if (testPos == null) {
                    continue;
                }
                // Find the mega chunk that this position is a part of.
                testPosMegaChunk = null;
                for (MegaChunk megaChunk : megaChunks) {
                    if (MathUtils.posIsInChunklikeCube(testPos, MegaChunk.LENGTH, megaChunk.getLowerTip())) {
                        testPosMegaChunk = megaChunk;
                        withinBounds = true;
                        break;
                    }
                }
                if (testPosMegaChunk == null) {
                    continue;
                }
                // Is the test position close enough to existing junctions? (Relevant for outer loop)
                if (!foundPositionNearJunctions) {
                    for (RoadJunction junction : roadJunctions) {
                        if (testPos.getSquaredDistance(junction.pos) < Math.pow(ROAD_EDGE_BASE_MAX_LENGTH*roadType.scale, 2)) {
                            foundPositionNearJunctions = true;
                            break;
                        }
                    }
                }
                // Is the test position too close to existing junctions?
                for (RoadJunction junction : roadJunctions) {
                    if (testPos.getSquaredDistance(junction.pos) < Math.pow(ROAD_JUNCTION_BASE_SPACE*roadType.scale, 2)) {
                        continue angleFor;
                    }
                }

                // Create new junction.
                newJunction = new RoadJunction(nextElementID++, world, testPos, roadType);
                // Does the new junction collide with any existing structures, edges or access paths?
                for (Structure structure : structures) {
                    if (GeoFeatureCollision.featuresOverlap(newJunction, structure)) {
                        continue angleFor;
                    }
                }
                for (RoadEdge edge : roadEdges) {
                    if (GeoFeatureCollision.featuresOverlap(newJunction, edge)) {
                        continue angleFor;
                    }
                }
                for (RoadEdge edge : accessPaths) {
                    if (GeoFeatureCollision.featuresOverlap(newJunction, edge)) {
                        continue angleFor;
                    }
                }

                // Create between one and two edges connecting the new junction to the road network. The first junction
                // does not require any edges.
                if (!roadJunctions.isEmpty()) {
                    // Search for existing junctions to connect the new junction to.
                    Collections.shuffle(roadJunctions);
                    junctionCollisionFor: for (RoadJunction junction : roadJunctions) {
                        // Will the test edge's length be okay?
                        testSquaredDist = Math.pow(newJunction.pos.getX()-junction.pos.getX(), 2)
                                + Math.pow(newJunction.pos.getZ()-junction.pos.getZ(), 2);
                        if (testSquaredDist < Math.pow(ROAD_EDGE_BASE_MIN_LENGTH*roadType.scale, 2)
                                || testSquaredDist > Math.pow(ROAD_EDGE_BASE_MAX_LENGTH*roadType.scale, 2)) {
                            continue;
                        }
                        // Create new road edge.
                        testEdge = new RoadEdge(
                                nextElementID++,
                                this,
                                newJunction,
                                junction,
                                true,
                                roadType,
                                false,
                                false
                        );
                        // Is the edge's Y-slope okay?
                        if (Math.abs(testEdge.getYSlope()) > ROAD_EDGE_MAX_Y_SLOPE) {
                            // Sometimes recreate the test edge with spiral ramp mode enabled, if the number of spirals
                            // will not be larger than the allowed amount.
                            if (Math.abs((junction.pos.getY()-newJunction.pos.getY())/(RoadEdge.SPIRAL_BASE_Y_DIFF*roadType.scale)) <= RoadEdge.MAX_SPIRALS
                                    && random.nextFloat() < 0.15) {
                                testEdge = new RoadEdge(
                                        nextElementID++,
                                        this,
                                        newJunction,
                                        junction,
                                        false,
                                        roadType,
                                        false,
                                        true
                                );
                            } else {
                                continue;
                            }
                        }
                        // Check if the test edge collides with any structures, other edges or junctions.
                        for (Structure structure : structures) {
                            if (GeoFeatureCollision.featuresOverlap(testEdge, structure)) {
                                continue junctionCollisionFor;
                            }
                        }
                        for (RoadJunction roadJunction : roadJunctions) {
                            if (roadJunction != junction && GeoFeatureCollision.featuresOverlap(roadJunction, testEdge)) {
                                continue junctionCollisionFor;
                            }
                        }
                        for (RoadEdge edge : roadEdges) {
                            if (GeoFeatureCollision.edgesOverlap(testEdge, edge)) {
                                continue junctionCollisionFor;
                            }
                        }
                        for (RoadEdge edge : newEdges) {
                            if (GeoFeatureCollision.edgesOverlap(testEdge, edge)) {
                                continue junctionCollisionFor;
                            }
                        }
                        for (RoadEdge accessPath : accessPaths) {
                            if (GeoFeatureCollision.featuresOverlap(testEdge, accessPath)) {
                                continue junctionCollisionFor;
                            }
                        }

                        // Add edge to accepted edges list.
                        newEdges.add(testEdge);
                        // Only add a maximum of two edges.
                        if (newEdges.size() >= 2) {
                            // Break the loop that searches for junctions to connect to, since the new junction
                            // is already connected to a sufficient number.
                            break;
                        }

                    }
                    // Has at least one edge been created to connect the new junction to the road network?
                    if (newEdges.isEmpty()) {
                        continue ;
                    }
                }

                // Add the new junction and edges to the network.
                roadJunctions.add(newJunction);
                roadEdges.addAll(newEdges);
                // Extend pillars to the ground.
                extendPillars(newJunction, newEdges);
                // Add new chunks around the added junction.
                addMegaChunksAround(testPosMegaChunk.getLowerTip());
                // Place the junction and its edges into the world.
                if (PLACE_BLOCKS_DIRECTLY) {
                    attemptToPlace(newJunction);
                    for (RoadEdge edge : newEdges) {
                        attemptToPlace(edge);
                    }
                }
                return;
            }
        }
    }

    /**
     * Attempts to extend pillars below newly planned road features.
     * @param newJunction The newly planned junction.
     * @param newEdges A list of newly planned edges.
     */
    private void extendPillars(RoadJunction newJunction, ArrayList<RoadEdge> newEdges) {
        ArrayList<RoadFeature> features = new ArrayList<>();
        features.add(newJunction);
        features.addAll(newEdges);
        ArrayList<GeoFeatureBit> pillarBits;
        BlockPos testPos;
        for (RoadFeature feature : features) {
            pillarBits = new ArrayList<>();
            // Attempt to extend pillar while the maximum extension is not reached, the ground is not reached and no
            // position downwards is part of an existing feature.
            for (GeoFeatureBit startBit : feature.pillarStartBits) {
                for (int i=1; i<ROAD_PILLAR_EXTENSION_LENGTH; i++) {
                    testPos = startBit.blockPos.down(i);
                    if (posIsPartOfFeature(testPos) || (i>1 && world.getBlockState(testPos.up()).isIn(ModTags.Blocks.NATURAL_GROUND_BLOCKS))) {
                        break;
                    }
                    pillarBits.add(new GeoFeatureBit(startBit.blockState, testPos));
                }
            }
            feature.addBits(pillarBits);
        }
    }

    /**
     * Scans the entire village for features which overlap a position.
     * @param testPos The block position to check.
     * @return True when there is an overlap, false otherwise.
     */
    private boolean posIsPartOfFeature(BlockPos testPos) {
        for (RoadJunction junction : roadJunctions) {
            if (junction.bitsCollideWith(testPos)) {
                return true;
            }
        }
        for (RoadEdge edge : roadEdges) {
            if (edge.bitsCollideWith(testPos)) {
                return true;
            }
        }
        for (RoadEdge path : accessPaths) {
            if (path.bitsCollideWith(testPos)) {
                return true;
            }
        }
        for (Structure structure : structures) {
            if (structure.bitsCollideWith(testPos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to add a single structure.
     * @param structureType The arguments for the given structure type.
     *             STRUCTURE_XXX: {description of the parameters}
     * @return True if the attempt was successful.
     */
    private boolean planSingleStructure(String structureType) {
        // Randomly select the structure template for this attempt.
        RawStructureTemplate rawTemplate = DataRegistry.getRandomTemplateFor(villageType, structureType, villagerCount, blockPalettes);
        if (rawTemplate == null) {
            return false;
        }
        // Set parameter specific to the structure type.
        double searchDistance = DataRegistry.getStructureTypeData(structureType).searchDistanceMultiplier*SEARCH_DISTANCE_STRUCTURE;

        boolean withinBounds = true;
        boolean foundPositionNearJunctions = true;

        int junctionCount;
        double startAngle;
        BlockPos testPos;
        int surfaceBlockMaxYOffset = (int)(50*roadType.scale);
        MegaChunk testPosMegaChunk;
        Structure newStructure;

        // Loop through different search radii.
        for (double searchRadius = random.nextDouble(searchDistance);
             withinBounds && foundPositionNearJunctions;
             searchRadius += searchDistance
        ) {
            withinBounds = false;
            foundPositionNearJunctions = false;
            startAngle = random.nextDouble(2*Math.PI);

            // Loop through different points on the circle.
            angleFor: for (double addAngle = 0;
                 addAngle < 2*Math.PI;
                 addAngle += searchDistance /searchRadius
            ) {
                // Determine the test position.
                testPos = pos.add((int) (searchRadius*Math.cos(startAngle+addAngle)), 0, (int) (searchRadius*Math.sin(startAngle+addAngle)));
                testPos = getSurfaceBlock(testPos, testPos.getY()-surfaceBlockMaxYOffset, testPos.getY()+surfaceBlockMaxYOffset, true);
                if (testPos == null) {
                    continue;
                }
                // Find the mega chunk that this position is a part of.
                testPosMegaChunk = null;
                for (MegaChunk megaChunk : megaChunks) {
                    if (MathUtils.posIsInChunklikeCube(testPos, MegaChunk.LENGTH, megaChunk.getLowerTip())) {
                        testPosMegaChunk = megaChunk;
                        withinBounds = true;
                        break;
                    }
                }
                if (testPosMegaChunk == null) {
                    continue;
                }
                // Test if enough road junctions are nearby.
                junctionCount = 0;
                for (RoadJunction junction : roadJunctions) {
                    if (junction.pos.isWithinDistance(testPos, roadType.scale * POSITIONS_ARE_CLOSE_DISTANCE)) {
                        junctionCount++;
                        if (junctionCount >= MIN_NEAR_ROAD_JUNCTIONS) {
                            break;
                        }
                    }
                }
                if (junctionCount < MIN_NEAR_ROAD_JUNCTIONS) {
                    continue ;
                }
                foundPositionNearJunctions = true;

                // Create new structure.
                newStructure = structureProvider.getStructure(nextElementID++, testPos, rawTemplate);
                if (newStructure == null) {
                    return false;
                }
                // Test if the structure collides with any existing features.
                for (Structure structure : structures) {
                    if (GeoFeatureCollision.featuresOverlap(newStructure, structure)) {
                        continue angleFor;
                    }
                }
                for (RoadEdge edge : roadEdges) {
                    if (GeoFeatureCollision.featuresOverlap(newStructure, edge)) {
                        continue angleFor;
                    }
                }
                for (RoadEdge accessPath : accessPaths) {
                    if (GeoFeatureCollision.featuresOverlap(newStructure, accessPath)) {
                        continue angleFor;
                    }
                }
                for (RoadJunction junction : roadJunctions) {
                    if (GeoFeatureCollision.featuresOverlap(newStructure, junction)) {
                        continue angleFor;
                    }
                }
                // Try to connect all access points.
                accessPathsToPlace.clear();
                if (!connectAccessPoints(newStructure)) {
                    continue;
                }

                // Add the new structure.
                structures.add(newStructure);
                // Add new chunks around the added structure.
                addMegaChunksAround(testPosMegaChunk.getLowerTip());
                // Place the structure and the access paths in the world.
                if (PLACE_BLOCKS_DIRECTLY) {
                    attemptToPlace(newStructure);
                    for (RoadEdge approvedPath : accessPathsToPlace) {
                        attemptToPlace(approvedPath);
                    }
                }
                accessPathsToPlace.clear();
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to connect all access points of a structure and adds the resulting paths to the village plan
     * (call this method as the last step before adding the structure to the village plan).
     * @param structure The structure that is getting planned.
     * @return True if all access points have been connected.
     */
    private boolean connectAccessPoints(Structure structure) {
        ArrayList<RoadEdge> approvedPaths = new ArrayList<>();

        StructureAccessPoint accessPoint;
        boolean pointApproved;
        ArrayList<RoadDot> nearDotsToTest;
        RoadDot roadDot;

        // Iterate through all access points.
        for (PointOfInterest point : structure.pointsOfInterest) {
            if (!(point instanceof StructureAccessPoint)) {
                continue;
            }
            accessPoint = (StructureAccessPoint) point;
            pointApproved = false;

            // Attempt to connect the access point.
            // Find all road dots within a distance (and x-z-distance of more than 1).
            nearDotsToTest = new ArrayList<>();
            for (RoadEdge edge : roadEdges) {
                for (RoadDot dot : edge.roadDots) {
                    if (dot.pos.isWithinDistance(accessPoint.pos, ACCESS_PATH_BASE_MAX_LENGTH*roadType.scale)
                            && 1 < MathHelper.square(dot.pos.getX()-accessPoint.pos.getX()) + MathHelper.square(dot.pos.getZ()-accessPoint.pos.getZ())) {
                        nearDotsToTest.add(dot);
                    }
                }
            }
            // Try to connect to a road dot and repeat a couple of times in case of failure.
            connectDotFor: for (int i=0; i<10 && nearDotsToTest.size() > 0; i++) {
                // Find the nearest dot.
                roadDot = null;
                for (RoadDot nearDot : nearDotsToTest) {
                    if (roadDot == null || nearDot.pos.getSquaredDistance(accessPoint.pos) < roadDot.pos.getSquaredDistance(accessPoint.pos)) {
                        roadDot = nearDot;
                    }
                }
                if (roadDot == null) {
                    continue;
                }
                nearDotsToTest.remove(roadDot);
                // Create test edge connecting the selected dot.
                RoadEdge testEdge = new RoadEdge(
                        nextElementID++,
                        this,
                        new RoadJunction(nextElementID++, world, accessPoint.pos, 0.6),
                        new RoadJunction(nextElementID++, world, roadDot.pos, 2.5*roadDot.edge.radius),
                        true,
                        roadTypeProvider.getRoadType(DataRegistry.getAccessPathRoadType(accessPoint.accessPathRoadType)),
                        true,
                        false
                );
                // Check edge's Y-slope.
                if (Math.abs(testEdge.getYSlope()) > ROAD_EDGE_MAX_Y_SLOPE) {
                    continue;
                }
                // Check if the test edge collides with the road edge it is trying to connect to.
                if (GeoFeatureCollision.accessPathCollidesWithEdge(testEdge, roadDot)) {
                    continue;
                }
                // Check if the test edge collides with any structures.
                if (GeoFeatureCollision.accessPathCollidesWithItsStructure(structure, accessPoint, testEdge)) {
                    continue;
                }
                for (Structure collisionTestStructure : structures) {
                    if (GeoFeatureCollision.featuresOverlap(testEdge, collisionTestStructure)) {
                        continue connectDotFor;
                    }
                }
                // Check if the test edge collides with any other paths.
                for (RoadEdge path : accessPaths) {
                    if (GeoFeatureCollision.featuresOverlap(testEdge, path)) {
                        continue connectDotFor;
                    }
                }
                for (RoadEdge path : approvedPaths) {
                    if (GeoFeatureCollision.featuresOverlap(testEdge, path)) {
                        continue connectDotFor;
                    }
                }
                // Check if the test edge collides with any junctions.
                for (RoadJunction junction : roadJunctions) {
                    if (GeoFeatureCollision.featuresOverlap(testEdge, junction)) {
                        continue connectDotFor;
                    }
                }
                // Access point successfully connected, no more attempts necessary.
                approvedPaths.add(testEdge);
                pointApproved = true;
                break;
            }
            if (!pointApproved) {
                return false;
            }
        }

        // Add the new access paths to the network.
        accessPaths.addAll(approvedPaths);
        accessPathsToPlace.addAll(approvedPaths);
        return true;
    }

    /**
     * Tries to place all blocks of a {@link GeoFeature} in the world. Some blocks remain untouched (see tag
     * {@link ModTags.Blocks#VILLAGE_UNTOUCHED_BLOCKS}).
     * @param feature The feature that should be placed.
     */
    private void attemptToPlace(GeoFeature feature) {
        for (GeoFeatureBit bit: feature.getBits()) {
            attemptToPlace(bit);
        }
    }
    /**
     * Tries to place a {@link GeoFeatureBit} in the world. Some blocks remain untouched (see tag
     * {@link ModTags.Blocks#VILLAGE_UNTOUCHED_BLOCKS}).
     * @param bit The bit that should be placed.
     */
    private void attemptToPlace(GeoFeatureBit bit) {
        if (world != null && bit.blockState != null && !world.getBlockState(bit.blockPos).isIn(ModTags.Blocks.VILLAGE_UNTOUCHED_BLOCKS)) {
            world.setBlockState(bit.blockPos, bit.blockState);
        }
    }
}
