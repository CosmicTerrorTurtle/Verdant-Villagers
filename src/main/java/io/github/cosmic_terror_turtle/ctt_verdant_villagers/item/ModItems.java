package io.github.cosmic_terror_turtle.ctt_verdant_villagers.item;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.VerdantVillagers;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.ModEntities;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item VILLAGE_HEART_SPAWN_EGG = registerItem(
            "village_heart_spawn_egg",
            new SpawnEggItem(ModEntities.VILLAGE_HEART_ENTITY_TYPE, 0x00f080, 0x40ff40, new FabricItemSettings()),
            ModItemGroups.VERDANT_VILLAGE);

    private static Item registerItem(String name, Item item, ItemGroup group) {
        Registry.register(Registries.ITEM, new Identifier(VerdantVillagers.MOD_ID, name), item);
        ItemGroupEvents.modifyEntriesEvent(group).register(entries -> entries.add(item));
        return item;
    }

    public static void registerModItems() {
        VerdantVillagers.LOGGER.info("Registering items for " + VerdantVillagers.MOD_ID + ".");
    }
}
