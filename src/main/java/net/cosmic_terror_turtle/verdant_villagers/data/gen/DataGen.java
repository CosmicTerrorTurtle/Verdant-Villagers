package net.cosmic_terror_turtle.verdant_villagers.data.gen;

import com.google.gson.stream.JsonWriter;
import net.cosmic_terror_turtle.verdant_villagers.VerdantVillagers;
import net.minecraft.block.*;
import net.minecraft.state.property.Property;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataGen {

    public static <T extends Comparable<T>> ArrayList<String> getPropertyNameAndValueList(String del, Property<T> property) {
        return new ArrayList<>(property.getValues().stream().map(t -> del+property.getName()+del+property.name(t)).toList());
    }

    public static void addStairBlockStates(ArrayList<String> list, String mod_id, String paletteId, String del) {
        for (String nameAndValue1 : getPropertyNameAndValueList(del, StairsBlock.WATERLOGGED)) {
            for (String nameAndValue2 : getPropertyNameAndValueList(del, StairsBlock.HALF)) {
                for (String nameAndValue3 : getPropertyNameAndValueList(del, StairsBlock.FACING)) {
                    for (String nameAndValue4 : getPropertyNameAndValueList(del, StairsBlock.SHAPE)) {
                        list.add(mod_id+paletteId+"stairs"+nameAndValue1+nameAndValue2+nameAndValue3+nameAndValue4);
                    }
                }
            }
        }
    }

    public static void addSlabBlockStates(ArrayList<String> list, String modId, String paletteId, String del) {
        for (String nameAndValue1 : getPropertyNameAndValueList(del, SlabBlock.WATERLOGGED)) {
            for (String nameAndValue2 : getPropertyNameAndValueList(del, SlabBlock.TYPE)) {
                list.add(modId+paletteId+"slab"+nameAndValue1+nameAndValue2);
            }
        }
    }

    public static void addButtonBlockStates(ArrayList<String> list, String modId, String paletteId, String del) {
        for (String nameAndValue1 : getPropertyNameAndValueList(del, WallMountedBlock.FACE)) {
            for (String nameAndValue2 : getPropertyNameAndValueList(del, HorizontalFacingBlock.FACING)) {
                list.add(modId+paletteId+"button"+nameAndValue1+nameAndValue2);
            }
        }
    }

    /**
     * Generates a json file for a palette type, overwriting existing files.
     * @param id The palette identifier.
     * @param elements The palette elements.
     * @param filePath The path (relative to the 'run' folder) where the file will be written to.
     */
    public static void generateJsonForPaletteType(String id, ArrayList<String> elements, String filePath) {
        try (JsonWriter writer = new JsonWriter(new FileWriter(filePath))) {
            writer.setIndent("  ");

            writer.beginObject();
            writer.name("id");
            writer.value(id);
            writer.name("elements");
            writer.beginArray();
            for (String element : elements) {
                writer.value(element);
            }
            writer.endArray();
            writer.endObject();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void generateJsonForPalette(String paletteTypeId, String paletteId, ArrayList<String> indicatorBlocks,
                                              ArrayList<String> elements, String filePath, boolean addToDefaults) {
        try (JsonWriter writer = new JsonWriter(new FileWriter(filePath))) {
            writer.setIndent("  ");

            writer.beginObject();
            writer.name("palette_type_id");
            writer.value(paletteTypeId);
            writer.name("palette_id");
            writer.value(paletteId);
            writer.name("add_to_defaults");
            writer.value(addToDefaults);
            writer.name("indicator_blocks");
            writer.beginArray();
            for (String indicator : indicatorBlocks) {
                writer.value(indicator);
            }
            writer.endArray();
            writer.name("elements");
            writer.beginArray();
            for (String element : elements) {
                writer.value(element);
            }
            writer.endArray();
            writer.endObject();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void generateBlockPalettes() {
        VerdantVillagers.LOGGER.info("Generating block palettes.");

        // Wood
        generateWoodBlockPaletteType();
        generateWoodBlockPalette(VerdantVillagers.MOD_ID, "dark_oak", "minecraft", "dark_oak", true);
        generateWoodBlockPalette(VerdantVillagers.MOD_ID, "spruce", "minecraft", "spruce", true);
        generateWoodBlockPalette(VerdantVillagers.MOD_ID, "oak", "minecraft", "oak", true);
        generateWoodBlockPalette(VerdantVillagers.MOD_ID, "jungle", "minecraft", "jungle", true);
        generateWoodBlockPalette(VerdantVillagers.MOD_ID, "birch", "minecraft", "birch", true);
        generateWoodBlockPalette(VerdantVillagers.MOD_ID, "acacia", "minecraft", "acacia", true);

        // Stone
        generateStoneBlockPaletteType();
        generateStoneBlockPalette(VerdantVillagers.MOD_ID, "cobblestone", "minecraft", "cobblestone",
                new ArrayList<>(List.of("minecraft:stone", "minecraft:cobblestone")),
                new ArrayList<>(List.of("cobblestone", "cobblestone")), true);
        generateStoneBlockPalette(VerdantVillagers.MOD_ID, "stone_bricks", "minecraft", "stone_brick",
                new ArrayList<>(List.of("minecraft:stone", "minecraft:cobblestone", "minecraft:stone_bricks")),
                new ArrayList<>(List.of("stone_bricks", "chiseled_stone_bricks")), true);

    }

    private static void generateWoodBlockPaletteType() {
        generateJsonForPaletteType(VerdantVillagers.MOD_ID+":wood", generateListForWood("", "", "_"), "wood.json");
    }

    /**
     * Generates a wood block palette.
     * @param paletteModId The namespace that the new palette will be registered under (for example: 'my_mod').
     * @param paletteName The name of the new palette (for example: 'some_oak_palette').
     * @param woodBlockModId The namespace of the wood block (for example: 'minecraft').
     * @param woodBlockPath The path of the wood block (for example: 'oak').
     * @param addToDefaults If the block palette will be registered as a default for its palette type.
     */
    public static void generateWoodBlockPalette(String paletteModId, String paletteName, String woodBlockModId, String woodBlockPath, boolean addToDefaults) {
        ArrayList<String> indicatorBlocks = new ArrayList<>();
        indicatorBlocks.add(woodBlockModId+":"+woodBlockPath+"_wood");
        indicatorBlocks.add(woodBlockModId+":"+woodBlockPath+"_log");
        indicatorBlocks.add(woodBlockModId+":"+woodBlockPath+"_planks");

        generateJsonForPalette(VerdantVillagers.MOD_ID+":wood", paletteModId+":"+paletteName, indicatorBlocks,
                generateListForWood(woodBlockModId+":", woodBlockPath+"_", ":"), paletteName+".json", addToDefaults);
    }

    private static ArrayList<String> generateListForWood(String modId, String paletteId, String del) {
        ArrayList<String> elements = new ArrayList<>();
        // Greenery
        elements.add(modId+paletteId+"sapling");
        elements.add(modId+"potted_"+paletteId+"sapling");
        elements.add(modId+paletteId+"leaves"+del+"persistent"+del+"true");
        // Woods and logs
        for (String nameAndValue : getPropertyNameAndValueList(del, PillarBlock.AXIS)) {
            elements.add(modId+paletteId+"wood"+nameAndValue);
        }
        for (String nameAndValue : getPropertyNameAndValueList(del, PillarBlock.AXIS)) {
            elements.add(modId+"stripped_"+paletteId+"wood"+nameAndValue);
        }
        for (String nameAndValue : getPropertyNameAndValueList(del, PillarBlock.AXIS)) {
            elements.add(modId+paletteId+"log"+nameAndValue);
        }
        for (String nameAndValue : getPropertyNameAndValueList(del, PillarBlock.AXIS)) {
            elements.add(modId+"stripped_"+paletteId+"log"+nameAndValue);
        }
        // Planks, stairs, slabs
        elements.add(modId+paletteId+"planks");
        addStairBlockStates(elements, modId, paletteId, del);
        addSlabBlockStates(elements, modId, paletteId, del);
        // Signs
        for (String nameAndValue1 : getPropertyNameAndValueList(del, AbstractSignBlock.WATERLOGGED)) {
            for (String nameAndValue2 : getPropertyNameAndValueList(del, SignBlock.ROTATION)) {
                elements.add(modId+paletteId+"sign"+nameAndValue1+nameAndValue2);
            }
        }
        for (String nameAndValue1 : getPropertyNameAndValueList(del, AbstractSignBlock.WATERLOGGED)) {
            for (String nameAndValue2 : getPropertyNameAndValueList(del, WallSignBlock.FACING)) {
                elements.add(modId+paletteId+"wall_sign"+nameAndValue1+nameAndValue2);
            }
        }
        // Fences, fence gates
        for (String nameAndValue1 : getPropertyNameAndValueList(del, HorizontalConnectingBlock.WATERLOGGED)) {
            for (String nameAndValue2 : getPropertyNameAndValueList(del, HorizontalConnectingBlock.NORTH)) {
                for (String nameAndValue3 : getPropertyNameAndValueList(del, HorizontalConnectingBlock.SOUTH)) {
                    for (String nameAndValue4 : getPropertyNameAndValueList(del, HorizontalConnectingBlock.WEST)) {
                        for (String nameAndValue5 : getPropertyNameAndValueList(del, HorizontalConnectingBlock.EAST)) {
                            elements.add(modId+paletteId+"fence"+nameAndValue1+nameAndValue2+nameAndValue3+nameAndValue4+nameAndValue5);
                        }
                    }
                }
            }
        }
        for (String nameAndValue1 : getPropertyNameAndValueList(del, HorizontalFacingBlock.FACING)) {
            for (String nameAndValue2 : getPropertyNameAndValueList(del, FenceGateBlock.OPEN)) {
                elements.add(modId+paletteId+"fence_gate"+nameAndValue1+nameAndValue2);
            }
        }
        // Doors, trapdoors
        for (String nameAndValue1 : getPropertyNameAndValueList(del, DoorBlock.HALF)) {
            for (String nameAndValue2 : getPropertyNameAndValueList(del, DoorBlock.FACING)) {
                for (String nameAndValue3 : getPropertyNameAndValueList(del, DoorBlock.HINGE)) {
                    for (String nameAndValue4 : getPropertyNameAndValueList(del, DoorBlock.OPEN)) {
                        elements.add(modId+paletteId+"door"+nameAndValue1+nameAndValue2+nameAndValue3+nameAndValue4);
                    }
                }
            }
        }
        for (String nameAndValue1 : getPropertyNameAndValueList(del, TrapdoorBlock.WATERLOGGED)) {
            for (String nameAndValue2 : getPropertyNameAndValueList(del, TrapdoorBlock.HALF)) {
                for (String nameAndValue3 : getPropertyNameAndValueList(del, HorizontalFacingBlock.FACING)) {
                    for (String nameAndValue4 : getPropertyNameAndValueList(del, TrapdoorBlock.OPEN)) {
                        elements.add(modId+paletteId+"trapdoor"+nameAndValue1+nameAndValue2+nameAndValue3+nameAndValue4);
                    }
                }
            }
        }
        // Pressure plates, buttons
        elements.add(modId+paletteId+"pressure_plate");
        addButtonBlockStates(elements, modId, paletteId, del);

        return elements;
    }

    private static void generateStoneBlockPaletteType() {
        generateJsonForPaletteType(
                VerdantVillagers.MOD_ID+":stone",
                generateListForStone("", "", new ArrayList<>(List.of(new String[]{"full", "full_variant"})), "_"),
                "stone.json");
    }

    /**
     * Generates a stone block palette.
     * @param paletteModId The namespace that the new palette will be registered under (for example: 'my_mod').
     * @param paletteName The name of the new palette (for example: 'some_cobble_palette').
     * @param stoneBlockModId The namespace of the wood block (for example: 'minecraft').
     * @param stoneBlockPath The path of the wood block (for example: 'cobblestone').
     * @param addToDefaults If the block palette will be registered as a default for its palette type.
     */
    public static void generateStoneBlockPalette(String paletteModId, String paletteName, String stoneBlockModId, String stoneBlockPath,
                                                 ArrayList<String> indicatorBlocks, ArrayList<String> fullVariants, boolean addToDefaults) {
        generateJsonForPalette(
                VerdantVillagers.MOD_ID+":stone",
                paletteModId+":"+paletteName,
                indicatorBlocks,
                generateListForStone(stoneBlockModId+":", stoneBlockPath+"_", fullVariants,":"),
                paletteName+".json",
                addToDefaults);
    }

    private static ArrayList<String> generateListForStone(String modId, String paletteId, ArrayList<String> fullVariants, String del) {
        ArrayList<String> elements = new ArrayList<>();

        // Full variants
        fullVariants.forEach(var -> elements.add(modId+var));

        // Stairs and slabs
        addStairBlockStates(elements, modId, paletteId, del);
        addSlabBlockStates(elements, modId, paletteId, del);

        // Walls
        for (String nameAndValue1 : getPropertyNameAndValueList(del, WallBlock.WATERLOGGED)) {
            for (String nameAndValue2 : getPropertyNameAndValueList(del, WallBlock.UP)) {
                for (String nameAndValue3 : getPropertyNameAndValueList(del, WallBlock.NORTH_SHAPE)) {
                    for (String nameAndValue4 : getPropertyNameAndValueList(del, WallBlock.SOUTH_SHAPE)) {
                        for (String nameAndValue5 : getPropertyNameAndValueList(del, WallBlock.WEST_SHAPE)) {
                            for (String nameAndValue6 : getPropertyNameAndValueList(del, WallBlock.EAST_SHAPE)) {
                                elements.add(modId+paletteId+"wall"+nameAndValue1+nameAndValue2+nameAndValue3+nameAndValue4+nameAndValue5+nameAndValue6);
                            }
                        }
                    }
                }
            }
        }

        return elements;
    }
}
