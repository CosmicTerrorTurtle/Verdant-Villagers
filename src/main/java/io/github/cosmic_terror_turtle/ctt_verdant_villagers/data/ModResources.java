package io.github.cosmic_terror_turtle.ctt_verdant_villagers.data;

import com.google.gson.stream.JsonReader;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.VerdantVillagers;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.village.*;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.geo_feature.road.RoadTypeProvider;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.geo_feature.structure.StructureProvider;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class ModResources {

    public static void registerResourceReloadListeners() {

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(VerdantVillagers.MOD_ID, "data_reload_listener");
            }
            @Override
            public void reload(ResourceManager manager) {

                // Clear caches
                DataRegistry.clearData();
                RoadTypeProvider.resetProviders();
                StructureProvider.resetProviders();


                // Initial loading and runtime-reloading

                // Block palette types
                for (Map.Entry<Identifier, Resource> result :
                        manager.findResources("verdant_village"+File.separator+"block_palette_types", id -> id.getPath().endsWith(".json")).entrySet()
                ) {
                    try (JsonReader reader = new JsonReader(new InputStreamReader(result.getValue().getInputStream()))) {

                        DataRegistry.addBlockPaletteType(new BlockPaletteType(reader));

                    } catch (Exception e) {
                        VerdantVillagers.LOGGER.error("Error occurred while loading resource json " + result.getKey(), e);
                    }
                }

                // Block palettes
                for (Map.Entry<Identifier, Resource> result :
                        manager.findResources("verdant_village"+File.separator+"block_palettes", id -> id.getPath().endsWith(".json")).entrySet()
                ) {
                    try (JsonReader reader = new JsonReader(new InputStreamReader(result.getValue().getInputStream()))) {

                        BlockPalette.createNew(reader);

                    } catch (Exception e) {
                        VerdantVillagers.LOGGER.error("Error occurred while loading resource json " + result.getKey(), e);
                    }
                }

                // Sapling data
                for (Map.Entry<Identifier, Resource> result :
                        manager.findResources("verdant_village"+File.separator+"sapling_info", id -> id.getPath().endsWith(".json")).entrySet()
                ) {
                    try (JsonReader reader = new JsonReader(new InputStreamReader(result.getValue().getInputStream()))) {

                        DataRegistry.addSaplingData(SaplingData.readSaplingInfo(reader));

                    } catch (Exception e) {
                        VerdantVillagers.LOGGER.error("Error occurred while loading resource json " + result.getKey(), e);
                    }
                }

                // Sapling soil types
                for (Map.Entry<Identifier, Resource> result :
                        manager.findResources("verdant_village"+File.separator+"sapling_soil_types", id -> id.getPath().endsWith(".json")).entrySet()
                ) {
                    try (JsonReader reader = new JsonReader(new InputStreamReader(result.getValue().getInputStream()))) {

                        reader.beginObject();
                        while (reader.hasNext()) {
                            switch (reader.nextName()) {
                                default -> throw new IOException();
                                case "wood_block_palette_type_id"
                                        -> DataRegistry.setSaplingSoilTypeWoodBlockPaletteTypeId(reader.nextString());
                                case "sapling_soil_types_per_palette_id" -> DataRegistry.addSaplingSoilTypes(
                                        JsonUtils.readMap(reader, reader1 -> JsonUtils.readList(reader1, JsonReader::nextString))
                                );
                            }
                        }
                        reader.endObject();

                    } catch (Exception e) {
                        VerdantVillagers.LOGGER.error("Error occurred while loading resource json " + result.getKey(), e);
                    }
                }

                // Village types
                for (Map.Entry<Identifier, Resource> result :
                        manager.findResources("verdant_village"+File.separator+"village_types", id -> id.getPath().endsWith(".json")).entrySet()
                ) {
                    try (JsonReader reader = new JsonReader(new InputStreamReader(result.getValue().getInputStream()))) {

                        DataRegistry.addVillageTypes(VillageTypeData.readVillageTypes(reader));

                    } catch (Exception e) {
                        VerdantVillagers.LOGGER.error("Error occurred while loading resource json " + result.getKey(), e);
                    }
                }

                // Road types
                String tmp;
                for (Map.Entry<Identifier, Resource> result :
                        manager.findResources("verdant_village"+File.separator+"road_types"+File.separator+"roads", id -> id.getPath().endsWith(".json")).entrySet()) {
                    try (JsonReader reader = new JsonReader(new InputStreamReader(result.getValue().getInputStream()))) {

                        tmp = new File(result.getKey().getPath()).getName();
                        RawRoadType.createNew(reader, result.getKey().getNamespace()+":"+tmp.substring(0, tmp.length()-5), false);

                    } catch (Exception e) {
                        VerdantVillagers.LOGGER.error("Error occurred while loading resource json " + result.getKey(), e);
                    }
                }
                // Access path road types
                for (Map.Entry<Identifier, Resource> result :
                        manager.findResources("verdant_village"+File.separator+"road_types"+File.separator+"access_paths", id -> id.getPath().endsWith(".json")).entrySet()) {
                    try (JsonReader reader = new JsonReader(new InputStreamReader(result.getValue().getInputStream()))) {

                        tmp = new File(result.getKey().getPath()).getName();
                        RawRoadType.createNew(reader, result.getKey().getNamespace()+":"+tmp.substring(0, tmp.length()-5), true);

                    } catch (Exception e) {
                        VerdantVillagers.LOGGER.error("Error occurred while loading resource json " + result.getKey(), e);
                    }
                }

                // Structure types
                for (Map.Entry<Identifier, Resource> result :
                        manager.findResources("verdant_village"+File.separator+"structure_types", id -> id.getPath().endsWith(".json")).entrySet()
                ) {
                    try (JsonReader reader = new JsonReader(new InputStreamReader(result.getValue().getInputStream()))) {

                        DataRegistry.addStructureTypes(StructureTypeData.readStructureTypes(reader));

                    } catch (Exception e) {
                        VerdantVillagers.LOGGER.error("Error occurred while loading resource json " + result.getKey(), e);
                    }
                }

                // Tree farm structure types
                for (Map.Entry<Identifier, Resource> result :
                        manager.findResources("verdant_village"+File.separator+"tree_farm_structure_types", id -> id.getPath().endsWith(".json")).entrySet()
                ) {
                    try (JsonReader reader = new JsonReader(new InputStreamReader(result.getValue().getInputStream()))) {

                        DataRegistry.addTreeFarmStructureTypes(TreeFarmStructureTypeData.readTreeFarmStructureTypes(reader));

                    } catch (Exception e) {
                        VerdantVillagers.LOGGER.error("Error occurred while loading resource json " + result.getKey(), e);
                    }
                }

                // Structure templates
                for (Map.Entry<Identifier, Resource> result :
                        manager.findResources("verdant_village"+File.separator+"structure_templates", id -> id.getPath().endsWith(".json")).entrySet()
                ) {
                    try (JsonReader reader = new JsonReader(new InputStreamReader(result.getValue().getInputStream()))) {

                        RawStructureTemplate.createNew(reader);

                    } catch (Exception e) {
                        VerdantVillagers.LOGGER.error("Error occurred while loading resource json " + result.getKey(), e);
                    }
                }

                // Village names
                for (Map.Entry<Identifier, Resource> result :
                        manager.findResources("verdant_village"+File.separator+"village_names", id -> id.getPath().endsWith(".json")).entrySet()
                ) {
                    try (JsonReader reader = new JsonReader(new InputStreamReader(result.getValue().getInputStream()))) {

                        DataRegistry.addVillageNames(JsonUtils.readList(reader, JsonReader::nextString));

                    } catch (Exception e) {
                        VerdantVillagers.LOGGER.error("Error occurred while loading resource json " + result.getKey(), e);
                    }
                }

                // Check if the loaded data contains errors.
                DataRegistry.checkData();
            }
        });
    }
}