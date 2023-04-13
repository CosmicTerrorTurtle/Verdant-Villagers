package io.github.cosmic_terror_turtle.verdant_villagers.entity.client;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.VillageHeartEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VillageHeartRenderer extends GeoEntityRenderer<VillageHeartEntity> {

    public VillageHeartRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new VillageHeartModel());
    }
}
