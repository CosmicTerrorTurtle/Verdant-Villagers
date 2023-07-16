package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import io.github.cosmic_terror_turtle.verdant_villagers.data.village.BlockPalette;
import io.github.cosmic_terror_turtle.verdant_villagers.data.village.DataRegistry;
import io.github.cosmic_terror_turtle.verdant_villagers.data.village.RawStructureTemplate;
import io.github.cosmic_terror_turtle.verdant_villagers.data.village.VillageTypeData;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.VillageHeartEntity;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road.RoadDot;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road.RoadEdge;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road.RoadJunction;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road.RoadType;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.PointOfInterest;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.Structure;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.StructureAccessPoint;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.StructureProvider;
import io.github.cosmic_terror_turtle.verdant_villagers.util.MathUtils;
import io.github.cosmic_terror_turtle.verdant_villagers.util.ModTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LargeEntitySpawnHelper;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ServerVillage extends Village {

    private enum UpdateCyclePhase {PAUSE, STRUCTURES, ROADS}
    public enum SurfaceFluidMode {NONE, AS_GROUND, AS_AIR}


    // If true, the village will immediately place a feature once it is planned.
    public static boolean PLACE_BLOCKS_DIRECTLY = true;
    // If true, the village will speed up its planning process.
    public static boolean PLAN_FAST = true;

    // The side length of an L*L*L volume of blocks called mega block.
    public static final int MEGA_BLOCK_LENGTH = 2;

    // Range of random initial values for searchDistanceRoad (value = avg*(1+offset); offset is in (-fraction, fraction)).
    private static final int SEARCH_DISTANCE_ROAD_AVG = 30;
    private static final double SEARCH_DISTANCE_ROAD_FRACTION = 0.1;
    // Range of random initial values for searchDistanceStructure.
    private static final int SEARCH_DISTANCE_STRUCTURE_AVG = 13;
    private static final double SEARCH_DISTANCE_STRUCTURE_FRACTION = 0.1;
    // If the distance between two positions is lower than this threshold, they are considered to be close to each other.
    private static final int POSITIONS_ARE_CLOSE_DISTANCE = 100;
    // The minimum number of near road junctions needed for a structure position to be valid.
    private static final int MIN_NEAR_ROAD_JUNCTIONS = 6;
    // The minimum space between two road junctions (regardless of whether they are connected or not).
    private static final int ROAD_JUNCTION_BASE_SPACE = 44;
    // The minimum and maximum length that a road edge is allowed to have.
    private static final int ROAD_EDGE_BASE_MIN_LENGTH = ROAD_JUNCTION_BASE_SPACE;
    private static final int ROAD_EDGE_BASE_MAX_LENGTH = 66;
    // The maximum slope angle that a road edge can have.
    private static final double ROAD_EDGE_MAX_Y_SLOPE = 0.45;
    // The maximum length that an access point path can have.
    private static final int ACCESS_POINT_PATH_MAX_LENGTH = 24;


    // Fields deleted when the block gets unloaded.
    private int ticksSinceLastUpdate = 0;
    private final World world;
    private BlockPos pos;
    public final Random random;
    public final StructureProvider structureProvider;
    private UpdateCyclePhase cyclePhase = UpdateCyclePhase.PAUSE;
    private RoadType roadType = null;
    private double needForRoads = 0;

    // Fields persistent when the block gets unloaded.
    private int nextElementID; // The unique id that is given next to a new feature. Always post-increment when assigning the next id.
    private final int searchDistanceRoad; // The distance in positions between different placement attempts in road planning.
    private final int searchDistanceStructure; // The distance in positions between different placement attempts in structure planning.
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
        structureProvider = new StructureProvider(this);

        // Initialize persistent fields

        // Misc values
        nextElementID = 0;
        name = DataRegistry.getRandomVillageName();
        villagerCount = 0;
        searchDistanceRoad = (int) getRand(SEARCH_DISTANCE_ROAD_AVG, SEARCH_DISTANCE_ROAD_FRACTION);
        searchDistanceStructure = (int) getRand(SEARCH_DISTANCE_STRUCTURE_AVG, SEARCH_DISTANCE_STRUCTURE_FRACTION);

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
                        if (world.getBlockState(testPos).isIn(ModTags.Blocks.VILLAGE_GROUND_BLOCKS)) {
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
        } else if (landAbove > 0.5) {
            terrainCategory = "under_ground";
        } else if (fluidAbove > 0.5) {
            terrainCategory = "under_fluid";
        } else {
            if (fluidBelow > 0.15) {
                terrainCategory = "on_coast";
            } else {
                terrainCategory = "on_land";
            }
        }

        // Determine village type
        villageType = null;
        ArrayList<String> villageTypes = new ArrayList<>(DataRegistry.getVillageTypes());
        ArrayList<String> fittingVillageTypes = new ArrayList<>();
        VillageTypeData data;
        boolean dimOkay;
        boolean biomeOkay;
        boolean terrainOkay;
        for (String vType : villageTypes) {
            // Check if terrain category, dimension and biome of each village type are sufficient
            data = DataRegistry.getVillageTypeData(vType);
            dimOkay = data.dimensions.isEmpty() || data.dimensions.contains(world.getDimensionKey().getValue().toString());
            biomeOkay = data.biomes.isEmpty() || data.biomes.contains(world.getBiome(pos).getKey().get().getValue().toString());
            terrainOkay = data.terrainCategory.equals(terrainCategory);
            if (dimOkay && biomeOkay && terrainOkay) {
                fittingVillageTypes.add(vType);
            }
        }
        if (fittingVillageTypes.isEmpty()) {
            villageType = "default";
        } else {
            villageType = fittingVillageTypes.get(random.nextInt(fittingVillageTypes.size()));
        }

        // Maps, lists and block palettes

        blockCounts = new HashMap<>();

        megaChunks = new ArrayList<>();
        addMegaChunksAround(pos);

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

        roadJunctions = new ArrayList<>();
        roadEdges = new ArrayList<>();
        accessPaths = new ArrayList<>();
        structures = new ArrayList<>();
    }

    // Serialize all data that needs to be persistent.
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();

        nbt.putInt("nextElementID", nextElementID);
        nbt.putString("name", name);
        nbt.putInt("villagerCount", villagerCount);
        nbt.putInt("searchDistanceRoad", searchDistanceRoad);
        nbt.putInt("searchDistanceStructure", searchDistanceStructure);
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
        for (String typeKey : DataRegistry.getBlockPaletteTypeKeys()) {
            palettesForTypeNbt = new NbtCompound();
            for (BlockPalette palette : blockPalettes.get(typeKey)) {
                // Boolean value does not get used when reading
                palettesForTypeNbt.putBoolean(palette.id.toString(), true);
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
        structureProvider = new StructureProvider(this);

        // Read the village heart from the nbt tag.

        nextElementID = nbt.getInt("nextElementID");
        name = nbt.getString("name");
        villagerCount = nbt.getInt("villagerCount");
        searchDistanceRoad = nbt.getInt("searchDistanceRoad");
        searchDistanceStructure = nbt.getInt("searchDistanceStructure");
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
        blockPalettes = new HashMap<>();
        for (String typeKey : DataRegistry.getBlockPaletteTypeKeys()) {
            blockPalettes.put(typeKey, new ArrayList<>());
            if (blockPalettesNbt.contains(typeKey)) {
                for (String paletteId : blockPalettesNbt.getCompound(typeKey).getKeys()) {
                    blockPalettes.get(typeKey).add(DataRegistry.getBlockPalette(typeKey, paletteId));
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

    public void increaseVillagerCount() {
        villagerCount += 8;
        villageHeart.markForUpdate();
    }

    /**
     * Determines a random value that is uniformly distributed (inclusive) between {@code avg}*(1-{@code fraction}; 1+{@code fraction}).
     * @param avg The average value.
     * @param fraction The fraction that determines the range.
     * @return The value.
     */
    private double getRand(double avg, double fraction) {
        return avg * (1 + random.nextDouble(-fraction, fraction));
    }

    public String getVillageType() {
        return villageType;
    }

    /**
     * Determines the number of block palettes this village should have depending on the current villager count.
     * @return The number of block palettes.
     */
    private int getUpdatedBlockPaletteLevel() {
        if (villagerCount < 15) {
            return 0;
        } else if (villagerCount < 30) {
            return 1;
        }
        return 2;
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
     * Calculates the time between update cycle steps. It is dependent on the number of villagers (representing the village size;
     * a bigger village will need more frequent updates). The minimum time is 5 seconds at >>1000 villagers, the maximum time 20 seconds at 0 villagers. If
     * {@link ServerVillage#PLAN_FAST} is true, the return value will always be 2 seconds.
     * @return The time in ticks.
     */
    private double getTicksBetweenUpdates() {
        if (PLAN_FAST) {
            return 20 * 2;
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

                // Count iron golems and villagers
                Box box;
                int ironGolemCount = 0;
                //villagerCount = 0;
                for (MegaChunk megaChunk : megaChunks) {
                    box = megaChunk.getBox();
                    ironGolemCount += world.getEntitiesByClass(IronGolemEntity.class, box, entity -> true).size();
                    //villagerCount += world.getEntitiesByClass(XXXEntity.class, box, entity -> true).size();
                }

                // Spawn iron golems if necessary
                if (ironGolemCount * 8 < villagerCount && random.nextDouble() < 0.1) {
                    LargeEntitySpawnHelper.trySpawnAt(EntityType.IRON_GOLEM, SpawnReason.MOB_SUMMONED, (ServerWorld) world, pos,
                            10, 50, 30, LargeEntitySpawnHelper.Requirements.IRON_GOLEM);
                }

                // Spawn villagers if necessary
                if (villagerCount < 5 && random.nextDouble() < 0.1) {
                    // try to spawn villager
                }

                // Add new block palettes if necessary.
                if (getUpdatedBlockPaletteLevel() > blockPaletteLevel) {
                    blockPaletteLevel = getUpdatedBlockPaletteLevel();
                    addBlockPalettesForAllTypes(blockCounts);
                }

                // Update road type
                roadType = getRoadType();
                cyclePhase = UpdateCyclePhase.STRUCTURES;
            }
            case STRUCTURES -> {

                // Plan structures (select one type at random).

                ArrayList<String> structureTypesToBuild = DataRegistry.getVillageTypeData(villageType).structureTypesToBuild;
                String selectedStructureType = structureTypesToBuild.get(random.nextInt(structureTypesToBuild.size()));

                // Determine whether there are enough structures of the selected structure type present
                switch (DataRegistry.getStructureTypeData(selectedStructureType).structureCheckMethod) {
                    default -> {}
                    case "count_villagers" -> {

                        int villagersAccountedFor = 0;
                        for (Structure structure : structures) {
                            if (structure.dataPerStructureType.containsKey(selectedStructureType)) {
                                villagersAccountedFor += Integer.parseInt(structure.dataPerStructureType.get(selectedStructureType).get("villagers_accounted_for"));
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

                // output name and villager count for testing
                for (PlayerEntity player : world.getPlayers()) {
                    player.sendMessage(Text.literal(name+" ("+villagerCount+")"));
                    if (random.nextDouble() < 0.2) {
                        player.sendMessage(Text.literal(
                                "type: "+villageType+", A/B land: "+landAbove+"/"+landBelow+", fluid: "+fluidAbove+"/"+fluidBelow+", air: "+airAbove+"/"+airBelow
                        ));
                    }
                }
                cyclePhase = UpdateCyclePhase.PAUSE;
            }
        }
    }

    /**
     * Adds all mega chunks in a 3x3x3 cube around the given position.
     * @param blockPos A position in the central mega chunk.
     */
    private void addMegaChunksAround(BlockPos blockPos) {
        MegaChunk chunkCandidate;
        boolean chunkIsNew;
        for (int i=-1; i<2; i++) {
            for (int j=-1; j<2; j++) {
                for (int k=-1; k<2; k++) {
                    chunkCandidate = new MegaChunk(nextElementID++, blockPos.add(i*MegaChunk.LENGTH, j*MegaChunk.LENGTH, k*MegaChunk.LENGTH));
                    chunkIsNew = true;
                    for (MegaChunk megaChunk : megaChunks) {
                        if (megaChunk.getLowerTip().equals(chunkCandidate.getLowerTip())) {
                            chunkIsNew = false;
                            break;
                        }
                    }
                    if (chunkIsNew) {
                        chunkCandidate.countBlocks(blockCounts, world);
                        megaChunks.add(chunkCandidate);
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
        return 2+1.1*actualCount;
    }

    /**
     * Fetches the road type (only for major roads) depending on the current villager count.
     * @return The current road type.
     */
    private RoadType getRoadType() {

        if (villagerCount < 5) {
            return RoadType.getSmallPath();
        }
        if (villagerCount < 15) {
            return RoadType.getMediumPath(this);
        }
        if (villagerCount < 25) {
            return RoadType.getBigPath(this);
        }
        if (villagerCount < 35) {
            return RoadType.getSmallStreet(this);
        }
        return RoadType.getMediumStreet(this);
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
        if (isForGeoFeature && random.nextDouble() < 0.3 && (
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

        // Increase starting height for accessing different heights
        int maxStartOffset = 20;
        int minStartHeight = Math.max(minY, startPosition.getY()-maxStartOffset);
        int maxStartHeight = Math.min(maxY, startPosition.getY()+maxStartOffset);
        startPosition = startPosition.withY(MathUtils.nextInt(minStartHeight, maxStartHeight));

        // Locate surface block
        BlockPos result = null;
        if (world != null) {
            for (int yCoord=startPosition.getY(); yCoord>=minY; yCoord--) {
                if (positionIsValidSurfaceLevel(startPosition.withY(yCoord), surfaceFluidMode)) {
                    result = startPosition.withY(yCoord);
                    break;
                }
            }
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
     * @param surfaceFluidMode Whether position, the block above it or none of them can have fluids. {@link SurfaceFluidMode#NONE}
     *                         is for normal positions on land, {@link SurfaceFluidMode#AS_GROUND} is for positions on land
     *                         and on the fluid surface, and {@link SurfaceFluidMode#AS_AIR} is for positions on land and
     *                         on the fluid ground (for example, the sea floor).
     * @return True if the position is an (upwards) surface block.
     */
    private boolean positionIsValidSurfaceLevel(BlockPos position, SurfaceFluidMode surfaceFluidMode) {
        if (world == null) {
            return false;
        } else {
            return switch (surfaceFluidMode) {
                case NONE ->
                        world.getBlockState(position).isIn(ModTags.Blocks.VILLAGE_GROUND_BLOCKS)
                        && !world.getBlockState(position.up()).isIn(ModTags.Blocks.VILLAGE_GROUND_BLOCKS)
                        && world.getFluidState(position.up()).isEmpty();
                case AS_GROUND ->
                        (world.getBlockState(position).isIn(ModTags.Blocks.VILLAGE_GROUND_BLOCKS) || !world.getFluidState(position).isEmpty())
                        && !world.getBlockState(position.up()).isIn(ModTags.Blocks.VILLAGE_GROUND_BLOCKS)
                        && world.getFluidState(position.up()).isEmpty();
                case AS_AIR ->
                        world.getBlockState(position).isIn(ModTags.Blocks.VILLAGE_GROUND_BLOCKS)
                        && !world.getBlockState(position.up()).isIn(ModTags.Blocks.VILLAGE_GROUND_BLOCKS);
            };
        }
    }

    /**
     * Tries to plan new road junctions and edges.
     * @param needForRoads A number indicating the need for roads.
     */
    private void planNewRoads(double needForRoads) {
        // Add new roads (if possible).
        int maxNewRoads = 1;
        int numOfFailures = 0;
        for (int i=0; i<needForRoads && i<maxNewRoads; i++) {
            if (!planSingleJunctionWithEdges()) {
                numOfFailures++;
            }
        }
    }

    /**
     * Attempts to add a single junction to the road network.
     * @return True if the attempt was successful.
     */
    private boolean planSingleJunctionWithEdges() {

        double searchRadius = random.nextDouble(searchDistanceRoad);
        boolean withinBounds;
        boolean foundPositionNearJunctions;
        double startAngle;
        double addAngle;

        BlockPos testPos;
        boolean testPosIsValid;
        RoadJunction newJunction;
        RoadEdge testEdge;
        ArrayList<RoadEdge> newEdges = new ArrayList<>();
        double testSquaredDist;
        boolean edgeCollides;
        do {
            withinBounds = false;
            foundPositionNearJunctions = false;
            startAngle = random.nextDouble(2*Math.PI);
            addAngle = 0;
            do {
                testPos = pos.add((int) (searchRadius*Math.cos(startAngle+addAngle)), -1, (int) (searchRadius*Math.sin(startAngle+addAngle)));
                testPos = getSurfaceBlock(testPos, testPos.getY()-50, testPos.getY()+50, true);
                if (testPos!= null) {
                    testPosIsValid = true;
                    for (MegaChunk megaChunk : megaChunks) {
                        // Find the mega chunk that this position is a part of.
                        if (megaChunk.posIsWithin(testPos)) {
                            withinBounds = true;

                            // Is the test position close enough to existing junctions?
                            for (RoadJunction junction : roadJunctions) {
                                if (testPos.getSquaredDistance(junction.pos) < Math.pow(ROAD_EDGE_BASE_MAX_LENGTH*roadType.edgeMinMaxLengthMultiplier, 2)) {
                                    foundPositionNearJunctions = true;
                                }
                            }

                            // Is the test position too close to existing junctions?
                            for (RoadJunction junction : roadJunctions) {
                                if (testPos.getSquaredDistance(junction.pos) < Math.pow(ROAD_JUNCTION_BASE_SPACE*roadType.edgeMinMaxLengthMultiplier, 2)) {
                                    testPosIsValid = false;
                                    break;
                                }
                            }
                            if (!testPosIsValid) {
                                break;
                            }

                            // Create new junction.
                            newJunction = new RoadJunction(nextElementID++, testPos, roadType);

                            // Does the new junction collide with any existing structures, edges or access paths?
                            for (Structure structure : structures) {
                                if (GeoFeatureCollision.featuresOverlap(newJunction, structure, true)) {
                                    testPosIsValid = false;
                                    break;
                                }
                            }
                            if (!testPosIsValid) {
                                break;
                            }
                            for (RoadEdge edge : roadEdges) {
                                if (GeoFeatureCollision.featuresOverlap(newJunction, edge, true)) {
                                    testPosIsValid = false;
                                    break;
                                }
                            }
                            if (!testPosIsValid) {
                                break;
                            }
                            for (RoadEdge edge : accessPaths) {
                                if (GeoFeatureCollision.featuresOverlap(newJunction, edge, true)) {
                                    testPosIsValid = false;
                                    break;
                                }
                            }
                            if (!testPosIsValid) {
                                break;
                            }
                            // New junction does not collide with anything.

                            // Create between one and two edges (if not, then fail the test position).
                            if (!roadJunctions.isEmpty()) {
                                // Search for existing junctions to connect the new junction to.
                                Collections.shuffle(roadJunctions);
                                for (RoadJunction junction : roadJunctions) {
                                    // Is the edge's length okay?
                                    testSquaredDist = newJunction.pos.getSquaredDistance(junction.pos);
                                    if (testSquaredDist < Math.pow(ROAD_EDGE_BASE_MIN_LENGTH*roadType.edgeMinMaxLengthMultiplier, 2)
                                            || testSquaredDist > Math.pow(ROAD_EDGE_BASE_MAX_LENGTH*roadType.edgeMinMaxLengthMultiplier, 2)) {
                                        continue;
                                    }
                                    // Create new road edge.
                                    testEdge = new RoadEdge(nextElementID++, this, newJunction, junction, false, true, roadType);
                                    // Is the edge's Y-slope okay?
                                    if (Math.abs(testEdge.getYSlope()) > ROAD_EDGE_MAX_Y_SLOPE) {
                                        continue;
                                    }
                                    // Check if the test edge collides with any structures, other edges or junctions.
                                    edgeCollides = false;
                                    for (Structure structure : structures) {
                                        if (GeoFeatureCollision.featuresOverlap(testEdge, structure, true)) {
                                            edgeCollides = true;
                                            break;
                                        }
                                    }
                                    if (edgeCollides) {
                                        continue;
                                    }
                                    for (RoadJunction roadJunction : roadJunctions) {
                                        if (roadJunction != junction && GeoFeatureCollision.featuresOverlap(roadJunction, testEdge, true)) {
                                            edgeCollides = true;
                                            break;
                                        }
                                    }
                                    if (edgeCollides) {
                                        continue;
                                    }
                                    for (RoadEdge edge : roadEdges) {
                                        if (GeoFeatureCollision.edgesOverlap(testEdge, edge)) {
                                            edgeCollides = true;
                                            break;
                                        }
                                    }
                                    if (edgeCollides) {
                                        continue;
                                    }
                                    for (RoadEdge accessPath : accessPaths) {
                                        if (GeoFeatureCollision.featuresOverlap(testEdge, accessPath, true)) {
                                            edgeCollides = true;
                                            break;
                                        }
                                    }
                                    if (edgeCollides) {
                                        continue;
                                    }
                                    for (RoadEdge edge : newEdges) {
                                        if (GeoFeatureCollision.edgesOverlap(testEdge, edge)) {
                                            edgeCollides = true;
                                            break;
                                        }
                                    }
                                    if (edgeCollides) {
                                        continue;
                                    }

                                    // Test edge is okay.
                                    newEdges.add(testEdge);
                                    // Only add a maximum of two edges.
                                    if (newEdges.size() >= 2) {
                                        // Break the loop that searches for junctions to connect to, since the
                                        // new junction is already connected to enough ones.
                                        break;
                                    }

                                }
                                if (newEdges.isEmpty()) {
                                    break;
                                }
                            }

                            // Add the new junction and edges to the network.
                            roadJunctions.add(newJunction);
                            roadEdges.addAll(newEdges);
                            // Add new chunks around the added junction.
                            addMegaChunksAround(megaChunk.getLowerTip());

                            // Build the junction and its edges
                            if (PLACE_BLOCKS_DIRECTLY) {
                                for (GeoFeatureBit bit : newJunction.getBits()) {
                                    attemptToPlace(bit);
                                }
                                for (RoadEdge edge : newEdges) {
                                    for (GeoFeatureBit bit : edge.getBits()) {
                                        attemptToPlace(bit);
                                    }
                                }
                            }

                            return true;
                        }
                    }
                }

                addAngle += searchDistanceRoad/searchRadius;
            } while (addAngle < 2*Math.PI);

            searchRadius += searchDistanceRoad;
        } while (withinBounds && foundPositionNearJunctions);

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
        RawStructureTemplate rawTemplate = DataRegistry.getRandomTemplateFor(villageType, structureType, villagerCount);
        if (rawTemplate == null) {
            return false;
        }

        // Set parameters specific to the structure type.
        double searchDistance = DataRegistry.getStructureTypeData(structureType).searchDistanceMultiplier * searchDistanceStructure;

        // Main body
        double searchRadius = random.nextDouble(searchDistance);
        boolean withinBounds;
        boolean foundPositionNearJunctions;
        int junctionCount;
        double startAngle;
        double addAngle;
        BlockPos testPos;
        Structure newStructure;
        boolean doesNotCollide;
        do {
            withinBounds = false;
            foundPositionNearJunctions = false;
            startAngle = random.nextDouble(2*Math.PI);
            addAngle = 0;
            do {
                testPos = pos.add((int) (searchRadius*Math.cos(startAngle+addAngle)), -1, (int) (searchRadius*Math.sin(startAngle+addAngle)));
                testPos = getSurfaceBlock(testPos, testPos.getY()-50, testPos.getY()+50, true);
                if (testPos != null) {
                    for (MegaChunk megaChunk : megaChunks) {
                        // Find the mega chunk that this position is a part of.
                        if (megaChunk.posIsWithin(testPos)) {
                            withinBounds = true;

                            // Test if enough road junctions are nearby.
                            junctionCount = 0;
                            for (RoadJunction junction : roadJunctions) {
                                if (junction.pos.isWithinDistance(testPos, POSITIONS_ARE_CLOSE_DISTANCE)) {
                                    junctionCount++;
                                    if (junctionCount >= MIN_NEAR_ROAD_JUNCTIONS) {
                                        break;
                                    }
                                }
                            }
                            if (junctionCount < MIN_NEAR_ROAD_JUNCTIONS) {
                                break;
                            }
                            foundPositionNearJunctions = true;

                            // Test if the house collides with any other structures or edges.
                            newStructure = structureProvider.getStructure(nextElementID++, testPos, rawTemplate);
                            if (newStructure == null) {
                                return false;
                            }
                            doesNotCollide = true;
                            for (Structure structure : structures) {
                                if (GeoFeatureCollision.featuresOverlap(newStructure, structure, true)) {
                                    doesNotCollide = false;
                                    break;
                                }
                            }
                            if (!doesNotCollide) {
                                break;
                            }
                            for (RoadEdge edge : roadEdges) {
                                if (GeoFeatureCollision.featuresOverlap(newStructure, edge, true)) {
                                    doesNotCollide = false;
                                    break;
                                }
                            }
                            if (!doesNotCollide) {
                                break;
                            }
                            for (RoadEdge accessPath : accessPaths) {
                                if (GeoFeatureCollision.featuresOverlap(newStructure, accessPath, true)) {
                                    doesNotCollide = false;
                                    break;
                                }
                            }
                            if (!doesNotCollide) {
                                break;
                            }
                            for (RoadJunction junction : roadJunctions) {
                                if (GeoFeatureCollision.featuresOverlap(newStructure, junction, true)) {
                                    doesNotCollide = false;
                                    break;
                                }
                            }
                            if (!doesNotCollide) {
                                break;
                            }
                            // House does not collide with anything.

                            // Try to connect all access points.
                            if (!connectAccessPoints(newStructure)) {
                                break;
                            }

                            // Add the new house.
                            structures.add(newStructure);
                            // Add new chunks around the added house.
                            addMegaChunksAround(megaChunk.getLowerTip());

                            // Build the structure
                            if (PLACE_BLOCKS_DIRECTLY) {
                                for (GeoFeatureBit bit : newStructure.getBits()) {
                                    attemptToPlace(bit);
                                }
                            }

                            return true;
                        }
                    }
                }

                addAngle += searchDistance /searchRadius;
            } while (addAngle < 2*Math.PI);

            searchRadius += searchDistance;
        } while (withinBounds && foundPositionNearJunctions);

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
        ArrayList<RoadDot> nearDots;
        RoadDot roadDot;
        boolean noCollision;

        // Iterate through all access points.
        for (PointOfInterest point : structure.pointsOfInterest) {
            if (point instanceof StructureAccessPoint) {
                accessPoint = (StructureAccessPoint) point;
                pointApproved = false;

                // Attempt to connect the access point.
                // Find all road dots within a distance (and x-z-distance of at least 1).
                nearDots = new ArrayList<>();
                for (RoadEdge edge : roadEdges) {
                    for (RoadDot dot : edge.roadDots) {
                        if (dot.pos.isWithinDistance(accessPoint.pos, ACCESS_POINT_PATH_MAX_LENGTH*roadType.edgeMinMaxLengthMultiplier)
                                && 1 < MathHelper.square(dot.pos.getX()-accessPoint.pos.getX()) + MathHelper.square(dot.pos.getZ()-accessPoint.pos.getZ())) {
                            nearDots.add(dot);
                        }
                    }
                }
                // Try to connect to a road dot and repeat a couple of times in case of failure.
                for (int i=0; i<10 && nearDots.size() > 0; i++) {
                    // Find the nearest dot.
                    roadDot = null;
                    for (RoadDot nearDot : nearDots) {
                        if (roadDot == null || nearDot.pos.getSquaredDistance(accessPoint.pos) < roadDot.pos.getSquaredDistance(accessPoint.pos)) {
                            roadDot = nearDot;
                        }
                    }
                    if (roadDot != null) {
                        // Test the selected dot.

                        // Create test edge.
                        RoadEdge testEdge = new RoadEdge(
                                nextElementID++, this, new RoadJunction(nextElementID++, accessPoint.pos, 0, 0.6),
                                new RoadJunction(nextElementID++, roadDot.pos, 0, 2.0*roadDot.edge.radius),
                                accessPoint.radius, true, true, accessPoint.templateRoadColumn
                        );
                        // Check edge's Y-slope.
                        if (Math.abs(testEdge.getYSlope()) < ROAD_EDGE_MAX_Y_SLOPE) {
                            noCollision = true;
                            // Check if the test edge collides with the road edge it is trying to connect to.
                            if (GeoFeatureCollision.accessPathCollidesWithEdge(testEdge, roadDot.edge)) {
                                noCollision = false;
                            }
                            // Check if the test edge collides with any structures.
                            if (noCollision) {
                                for (Structure collisionTestStructure : structures) {
                                    if (GeoFeatureCollision.featuresOverlap(testEdge, collisionTestStructure, false)) {
                                        noCollision = false;
                                        break;
                                    }
                                }
                                if (GeoFeatureCollision.featuresOverlapIgnoreMatchingBlockStates(testEdge, structure)) {
                                    noCollision = false;
                                }
                            }
                            // Check if the test edge collides with any other paths.
                            if (noCollision) {
                                for (RoadEdge path : accessPaths) {
                                    if (GeoFeatureCollision.featuresOverlap(testEdge, path, false)) {
                                        noCollision = false;
                                        break;
                                    }
                                }
                                for (RoadEdge path : approvedPaths) {
                                    if (GeoFeatureCollision.featuresOverlap(testEdge, path, false)) {
                                        noCollision = false;
                                        break;
                                    }
                                }
                            }
                            // Check if the test edge collides with any junctions.
                            if (noCollision) {
                                for (RoadJunction junction : roadJunctions) {
                                    if (GeoFeatureCollision.featuresOverlap(testEdge, junction, false)) {
                                        noCollision = false;
                                        break;
                                    }
                                }
                            }
                            if (noCollision) {
                                // Test edge is okay.
                                approvedPaths.add(testEdge);
                                pointApproved = true;

                                // Access point successfully connected, no more attempts necessary.
                                break;
                            }
                        }
                    }
                    // If this point is reached, the access point has not been connected to the dot.
                    nearDots.remove(roadDot);
                }
                if (!pointApproved) {
                    return false;
                }
            }
        }

        accessPaths.addAll(approvedPaths);

        // Build the access paths
        if (PLACE_BLOCKS_DIRECTLY) {
            for (RoadEdge approvedPath : approvedPaths) {
                for (GeoFeatureBit bit : approvedPath.getBits()) {
                    attemptToPlace(bit);
                }
            }
        }

        return true;
    }

    /**
     * Tries to place a block in the world. Some blocks remain untouched (see tag JFP_VILLAGE_UNTOUCHED_BLOCKS).
     * @param bit The bit that holds the position and block state that should be used for placing.
     */
    private void attemptToPlace(GeoFeatureBit bit) {
        if (world != null && bit.blockState != null && !world.getBlockState(bit.blockPos).isIn(ModTags.Blocks.VILLAGE_UNTOUCHED_BLOCKS)) {
            world.setBlockState(bit.blockPos, bit.blockState);
        }
    }
}
