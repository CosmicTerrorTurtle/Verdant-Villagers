package net.cosmic_terror_turtle.verdant_villagers.item;

import net.cosmic_terror_turtle.verdant_villagers.VerdantVillagers;
import net.cosmic_terror_turtle.verdant_villagers.block.ModBlocks;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    public static final ItemGroup VERDANT_VILLAGE = FabricItemGroupBuilder.build(
            new Identifier(VerdantVillagers.MOD_ID, "verdant_village"),
            () -> new ItemStack(ModBlocks.VILLAGE_ANCHOR_BLOCK)
    );
}
