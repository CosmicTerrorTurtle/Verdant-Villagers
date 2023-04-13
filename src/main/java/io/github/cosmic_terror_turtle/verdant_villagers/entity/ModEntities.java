package io.github.cosmic_terror_turtle.verdant_villagers.entity;

import io.github.cosmic_terror_turtle.verdant_villagers.VerdantVillagers;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.VillageHeartEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final EntityType<VillageHeartEntity> VILLAGE_HEART_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(VerdantVillagers.MOD_ID, "village_heart"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, VillageHeartEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 1.0f))
                    .fireImmune()
                    .trackRangeChunks(32)
                    .build()
    );
}
