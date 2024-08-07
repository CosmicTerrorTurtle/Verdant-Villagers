package io.github.cosmic_terror_turtle.ctt_verdant_villagers;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.ModEntities;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.client.VillageHeartRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class VerdantVillagersClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.VILLAGE_HEART_ENTITY_TYPE, VillageHeartRenderer::new);
    }
}
