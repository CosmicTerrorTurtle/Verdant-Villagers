package io.github.cosmic_terror_turtle.verdant_villagers.item;

import io.github.cosmic_terror_turtle.verdant_villagers.VerdantVillagers;
import io.github.cosmic_terror_turtle.verdant_villagers.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    public static ItemGroup VERDANT_VILLAGE;

    public static void registerItemGroups() {
        VERDANT_VILLAGE = FabricItemGroup.builder(new Identifier(VerdantVillagers.MOD_ID, "verdant_village"))
                .displayName(Text.literal("Verdant Village"))
                .icon(() -> new ItemStack(ModBlocks.VILLAGE_ANCHOR_BLOCK))
                .build();
    }
}
