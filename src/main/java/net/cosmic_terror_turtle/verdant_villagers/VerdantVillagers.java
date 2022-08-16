package net.cosmic_terror_turtle.verdant_villagers;

import net.cosmic_terror_turtle.verdant_villagers.block.ModBlocks;
import net.cosmic_terror_turtle.verdant_villagers.data.ModResources;
import net.cosmic_terror_turtle.verdant_villagers.data.village.DataRegistry;
import net.cosmic_terror_turtle.verdant_villagers.item.ModItems;
import net.cosmic_terror_turtle.verdant_villagers.util.ModRegistries;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.GeckoLib;

public class VerdantVillagers implements ModInitializer {

	public static final String MOD_ID = "verdant_villagers";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		//DataGen.generateBlockPalettes();
		//System.exit(0);

		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModRegistries.registerAll();

		DataRegistry.registerVillageStuff();

		GeckoLib.initialize();

		ModResources.registerResourceReloadListeners();
	}
}