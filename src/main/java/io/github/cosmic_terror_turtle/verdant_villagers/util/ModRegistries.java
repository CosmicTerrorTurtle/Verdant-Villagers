package io.github.cosmic_terror_turtle.verdant_villagers.util;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.ModEntities;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.VillageHeartEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class ModRegistries {

    public static void registerAll() {
        registerEntityAttributes();
    }

    private static void registerEntityAttributes() {
        FabricDefaultAttributeRegistry.register(ModEntities.VILLAGE_HEART_ENTITY_TYPE, VillageHeartEntity.createVillageHeartAttributes());
    }
}