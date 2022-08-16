package net.cosmic_terror_turtle.verdant_villagers.entity.client;

import net.cosmic_terror_turtle.verdant_villagers.VerdantVillagers;
import net.cosmic_terror_turtle.verdant_villagers.entity.custom.VillageHeartEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class VillageHeartModel extends AnimatedGeoModel<VillageHeartEntity> {

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
