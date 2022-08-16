package net.cosmic_terror_turtle.verdant_villagers.util;

import net.cosmic_terror_turtle.verdant_villagers.entity.ModEntities;
import net.cosmic_terror_turtle.verdant_villagers.entity.custom.VillageHeartEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class ModRegistries {

    public static void registerAll() {
        registerEntityAttributes();
    }

    private static void registerEntityAttributes() {
        FabricDefaultAttributeRegistry.register(ModEntities.VILLAGE_HEART_ENTITY_TYPE, VillageHeartEntity.createVillageHeartAttributes());
    }
}
