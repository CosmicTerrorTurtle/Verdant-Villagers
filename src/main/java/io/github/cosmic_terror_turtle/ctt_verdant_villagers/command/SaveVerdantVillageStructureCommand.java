package io.github.cosmic_terror_turtle.ctt_verdant_villagers.command;

import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.village.BlockStateParsing;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.village.RawStructureTemplate;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SaveVerdantVillageStructureCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("save_verdant_village_structure").then(
                        CommandManager.argument("file_name", StringArgumentType.word()).then(
                                CommandManager.argument("pos1", BlockPosArgumentType.blockPos()).then(
                                        CommandManager.argument("pos2", BlockPosArgumentType.blockPos()).then(
                                                CommandManager.argument("null_block", IdentifierArgumentType.identifier()).then(
                                                        CommandManager.argument("collision_space_block", IdentifierArgumentType.identifier())
                                                        .executes(context -> run(context, true, true))
                                                )
                                                .executes(context -> run(context, true, false))
                                        )
                                        .executes(context -> run(context, false, false))
                                )
                        )
                )
        );
    }

    /**
     * Runs this command.
     * @param context The context in which the command was called.
     * @param nullBlock Whether a null block was provided to the command.
     * @param collisionSpace Whether a collision space block was provided to the command.
     * @return {@link Command#SINGLE_SUCCESS}
     * @throws CommandSyntaxException If fetching the block positions or writing the json file failed.
     */
    public static int run(CommandContext<ServerCommandSource> context, boolean nullBlock, boolean collisionSpace) throws CommandSyntaxException {
        // Fetch command parameters
        String fileName = StringArgumentType.getString(context, "file_name");
        BlockPos pos1 = BlockPosArgumentType.getBlockPos(context, "pos1");
        BlockPos pos2 = BlockPosArgumentType.getBlockPos(context, "pos2");
        Identifier nullBlockId = nullBlock ? IdentifierArgumentType.getIdentifier(context, "null_block") : null;
        Identifier collisionSpaceId = collisionSpace ? IdentifierArgumentType.getIdentifier(context, "collision_space_block") : null;

        // Fetch world and min/max coordinates of the structure
        ServerWorld world = context.getSource().getWorld();
        int lowerX = Math.min(pos1.getX(), pos2.getX());
        int lowerY = Math.min(pos1.getY(), pos2.getY());
        int lowerZ = Math.min(pos1.getZ(), pos2.getZ());
        int upperX = Math.max(pos1.getX(), pos2.getX());
        int upperY = Math.max(pos1.getY(), pos2.getY());
        int upperZ = Math.max(pos1.getZ(), pos2.getZ());

        // Analyze the block states; populate the state strings and the inverse abbreviation map
        BlockState state;
        Identifier stateId;
        String parsedState;
        HashMap<String, String> inverseAbbreviationMap = new HashMap<>();
        inverseAbbreviationMap.put(RawStructureTemplate.NULL_KEY, "nul");
        inverseAbbreviationMap.put(BlockStateParsing.COLLISION_SPACE, "csp");
        int abbreviationName = 0x100;
        ArrayList<ArrayList<ArrayList<String>>> stateStrings = new ArrayList<>();
        ArrayList<ArrayList<String>> stateStrings_;
        ArrayList<String> stateStrings__;
        for (int x=upperX; x>=lowerX; x--) {
            stateStrings_ = new ArrayList<>();
            for (int y=lowerY; y<=upperY; y++) {
                stateStrings__ = new ArrayList<>();
                for (int z=lowerZ; z<=upperZ; z++) {
                    state = world.getBlockState(new BlockPos(x, y, z));
                    stateId = Registries.BLOCK.getId(state.getBlock());
                    // If the state's block id matches the ids given to the command, write null/collision space keywords.
                    if (nullBlockId != null && nullBlockId.equals(stateId)) {
                        stateStrings__.add("nul");
                    } else if(collisionSpaceId != null && collisionSpaceId.equals(stateId)) {
                        stateStrings__.add("csp");
                    } else {
                        parsedState = blockStateToString(state);
                        if (!inverseAbbreviationMap.containsKey(parsedState)) {
                            inverseAbbreviationMap.put(parsedState, Integer.toHexString(abbreviationName++));
                        }
                        stateStrings__.add(inverseAbbreviationMap.get(parsedState));
                    }
                }
                stateStrings_.add(stateStrings__);
            }
            stateStrings.add(stateStrings_);
        }

        // Save structure to a json file
        try (JsonWriter writer = new JsonWriter(new FileWriter(fileName+".json"))) {
            String normalIndent = "  ";
            String compactIndent = "";

            writer.setIndent(normalIndent);
            writer.beginObject();

            writer.name("village_types");
            writer.beginArray();
            writer.setIndent(compactIndent);
            writer.value("default");
            writer.endArray();
            writer.setIndent(normalIndent);

            writer.name("available_for_block_palettes");
            writer.beginObject();
            writer.endObject();

            writer.name("available_for_villager_count");
            writer.beginArray();
            writer.setIndent(compactIndent);
            writer.value(0L);
            writer.value(1000000L);
            writer.endArray();
            writer.setIndent(normalIndent);

            writer.name("available_for_terrain_types_above");
            writer.beginArray();
            writer.endArray();

            writer.name("available_for_terrain_types_below");
            writer.beginArray();
            writer.endArray();

            writer.name("data_per_structure_type");
            writer.beginObject();
            writer.endObject();

            writer.name("abbreviation_map");
            writer.beginObject();
            for (Map.Entry<String, String> entry : inverseAbbreviationMap.entrySet()) {
                writer.name(entry.getValue());
                writer.value(entry.getKey());
            }
            writer.endObject();

            writer.name("block_state_cube");
            writer.beginArray();
            for (ArrayList<ArrayList<String>> innerList1 : stateStrings) {
                writer.beginArray();
                writer.setIndent(compactIndent);
                for (ArrayList<String> innerList2 : innerList1) {
                    writer.beginArray();
                    for (String stateString : innerList2) {
                        writer.value(stateString);
                    }
                    writer.endArray();
                }
                writer.endArray();
                writer.setIndent(normalIndent);
            }
            writer.endArray();

            writer.name("center");
            writer.beginArray();
            writer.setIndent(compactIndent);
            writer.value(0L);
            writer.value(0L);
            writer.value(0L);
            writer.endArray();
            writer.setIndent(normalIndent);

            writer.name("points_of_interest");
            writer.beginArray();
            writer.endArray();

            writer.endObject();

        } catch (IOException e) {
            throw new SimpleCommandExceptionType(Text.literal("Writing to json failed.")).create();
        }

        context.getSource().sendMessage(Text.literal("Successfully saved structure to "+fileName+".json!"));

        return Command.SINGLE_SUCCESS;
    }

    public static String blockStateToString(BlockState state) {
        StringBuilder builder = new StringBuilder("state:");
        builder.append(Registries.BLOCK.getId(state.getBlock()));
        for (Property<?> property : state.getProperties()) {
            builder.append(":").append(property.getName()).append(":").append(getPropertyValueAsString(state, property));
        }
        return builder.toString();
    }
    private static <T extends Comparable<T>> String getPropertyValueAsString(BlockState state, Property<T> property) {
        return property.name(state.get(property));
    }
}
