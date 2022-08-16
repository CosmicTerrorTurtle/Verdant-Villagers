package net.cosmic_terror_turtle.verdant_villagers;

import net.cosmic_terror_turtle.verdant_villagers.entity.ModEntities;
import net.cosmic_terror_turtle.verdant_villagers.entity.client.VillageHeartRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class VerdantVillagersClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.VILLAGE_HEART_ENTITY_TYPE, VillageHeartRenderer::new);
    }
}
