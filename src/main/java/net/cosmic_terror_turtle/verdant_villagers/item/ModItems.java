package net.cosmic_terror_turtle.verdant_villagers.item;

import net.cosmic_terror_turtle.verdant_villagers.VerdantVillagers;
import net.cosmic_terror_turtle.verdant_villagers.entity.ModEntities;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModItems {

    public static final Item VILLAGE_HEART_SPAWN_EGG = registerItem("village_heart_spawn_egg", new SpawnEggItem(
            ModEntities.VILLAGE_HEART_ENTITY_TYPE,
            0x00f080,
            0x40ff40,
            new FabricItemSettings().group(ModItemGroups.VERDANT_VILLAGE)
    ));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(VerdantVillagers.MOD_ID, name), item);
    }

    public static void registerModItems() {
        VerdantVillagers.LOGGER.info("Registering items for " + VerdantVillagers.MOD_ID + ".");
    }
}
