package io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.client;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.VerdantVillagers;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.VillageHeartEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class VillageHeartModel extends GeoModel<VillageHeartEntity> {

    @Override
    public Identifier getModelResource(VillageHeartEntity object) {
        return new Identifier(VerdantVillagers.MOD_ID, "geo/village_heart.geo.json");
    }

    @Override
    public Identifier getTextureResource(VillageHeartEntity object) {
        return new Identifier(VerdantVillagers.MOD_ID, "textures/entity/village_heart/village_heart.png");
    }

    @Override
    public Identifier getAnimationResource(VillageHeartEntity animatable) {
        return new Identifier(VerdantVillagers.MOD_ID, "animations/village_heart.animation.json");
    }
}
