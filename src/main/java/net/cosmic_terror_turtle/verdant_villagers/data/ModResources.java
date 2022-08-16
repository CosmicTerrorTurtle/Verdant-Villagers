package net.cosmic_terror_turtle.verdant_villagers.data;

import com.google.gson.stream.JsonReader;
import net.cosmic_terror_turtle.verdant_villagers.VerdantVillagers;
import net.cosmic_terror_turtle.verdant_villagers.data.village.*;
import net.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.StructureProvider;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;

public class ModResources {

    public static void registerResourceReloadListeners() {

        ResourceManagerHelperImpl.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(VerdantVillagers.MOD_ID, "data_reload_listener");
            }
            @Override
            public void reload(ResourceManager manager) {

                // Clear caches
                DataRegistry.clearData();
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

                        DataRegistry.addVillageNames(JsonUtils.readStringArray(reader));

                    } catch (Exception e) {
                        VerdantVillagers.LOGGER.error("Error occurred while loading resource json " + result.getKey(), e);
                    }
                }

            }
        });
    }
}