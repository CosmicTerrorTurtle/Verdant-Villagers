package io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.structure;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.village.RawStructureTemplate;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.ServerVillage;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;

public class StructureProvider {

    private static final ArrayList<StructureProvider> providers = new ArrayList<>();

    public static void resetProviders() {
        providers.forEach(StructureProvider::resetTemplates);
    }



    private final ServerVillage village;
    private final HashMap<RawStructureTemplate, StructureTemplate> templateMap = new HashMap<>();

    public StructureProvider(ServerVillage village) {
        this.village = village;

        providers.add(this);
    }

    /**
     * Sets all templates to null. Call this when the village heart has updated its block palettes.
     */
    public void resetTemplates() {
        templateMap.clear();
    }

    /**
     * Creates a new {@link Structure}. The {@link StructureTemplate} used for it is fetched from {@code templateMap}
     * (if {@code templateMap} does not contain the respective {@link StructureTemplate} before,
     * a new {@link StructureTemplate} is created from {@code rawTemplate} and put into the map).
     * @param elementID The ID used for the new structure.
     * @param anchor The anchor of the new structure.
     * @param rawTemplate The template used for the new structure.
     * @return The new Structure.
     */
    public Structure getStructure(int elementID, BlockPos anchor, RawStructureTemplate rawTemplate) {
        if (!templateMap.containsKey(rawTemplate)) {
            StructureTemplate template = new StructureTemplate(village, rawTemplate);
            templateMap.put(rawTemplate, template);
        }
        return new Structure(village.random, elementID, anchor, templateMap.get(rawTemplate));
    }
}
