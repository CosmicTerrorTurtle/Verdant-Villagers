package io.github.cosmic_terror_turtle.verdant_villagers;

import io.github.cosmic_terror_turtle.verdant_villagers.block.ModBlocks;
import io.github.cosmic_terror_turtle.verdant_villagers.data.ModResources;
import io.github.cosmic_terror_turtle.verdant_villagers.data.village.DataRegistry;
import io.github.cosmic_terror_turtle.verdant_villagers.item.ModItemGroups;
import io.github.cosmic_terror_turtle.verdant_villagers.util.ModRegistries;
import io.github.cosmic_terror_turtle.verdant_villagers.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.GeckoLib;

public class VerdantVillagers implements ModInitializer {

	public static final String MOD_ID = "verdant_villagers";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModRegistries.registerAll();

		DataRegistry.registerVillageStuff();

		GeckoLib.initialize();

		ModResources.registerResourceReloadListeners();
	}
}