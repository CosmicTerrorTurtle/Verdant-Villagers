package io.github.cosmic_terror_turtle.verdant_villagers.block;

import io.github.cosmic_terror_turtle.verdant_villagers.block.custom.VillageAnchorBlock;
import io.github.cosmic_terror_turtle.verdant_villagers.VerdantVillagers;
import io.github.cosmic_terror_turtle.verdant_villagers.block.custom.entity.VillageAnchorBlockEntity;
import io.github.cosmic_terror_turtle.verdant_villagers.item.ModItemGroups;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlocks {

    public static final Block VILLAGE_ANCHOR_BLOCK = registerBlock(
            "village_anchor",
            new VillageAnchorBlock(FabricBlockSettings.of(Material.WOOD).strength(0.5f, 3.0f)),
            ModItemGroups.VERDANT_VILLAGE
    );

    public static BlockEntityType<VillageAnchorBlockEntity> VILLAGE_ANCHOR_BLOCK_ENTITY_TYPE;


    private static Block registerBlock(String name, Block block, ItemGroup group) {
        registerBlockItem(name, block, group);
        return Registry.register(Registry.BLOCK, new Identifier(VerdantVillagers.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block, ItemGroup group) {
        return Registry.register(Registry.ITEM, new Identifier(VerdantVillagers.MOD_ID, name), new BlockItem(block, new FabricItemSettings().group(group)));
    }

    public static void registerModBlocks() {
        VerdantVillagers.LOGGER.info("Registering blocks for " + VerdantVillagers.MOD_ID + ".");

        // Block entity types
        VILLAGE_ANCHOR_BLOCK_ENTITY_TYPE = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier(VerdantVillagers.MOD_ID, "village_anchor"),
                FabricBlockEntityTypeBuilder.create(VillageAnchorBlockEntity::new, VILLAGE_ANCHOR_BLOCK).build(null)
        );
    }
}
