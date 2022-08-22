package io.github.cosmic_terror_turtle.verdant_villagers.command;

import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.io.FileWriter;
import java.io.IOException;

public class SaveVillageStructureCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("save_village_structure").then(
                        CommandManager.argument("file_name", StringArgumentType.word()).then(
                                CommandManager.argument("lower_pos", BlockPosArgumentType.blockPos()).then(
                                        CommandManager.argument("upper_pos", BlockPosArgumentType.blockPos()).then(
                                                CommandManager.argument("null_block", IdentifierArgumentType.identifier())
                                                .executes(context -> run(context, IdentifierArgumentType.getIdentifier(context, "null_block")))
                                        )
                                        .executes(context -> run(context, null))
                                )
                        )
                )
        );
    }

    public static int run(CommandContext<ServerCommandSource> context, Identifier nullBlockId) throws CommandSyntaxException {
        ServerWorld world = context.getSource().getWorld();
        String fileName = StringArgumentType.getString(context, "file_name");
        BlockPos lower = BlockPosArgumentType.getBlockPos(context, "lower_pos");
        BlockPos upper = BlockPosArgumentType.getBlockPos(context, "upper_pos");
        int lowerX = lower.getX();
        int lowerY = lower.getY();
        int lowerZ = lower.getZ();
        int upperX = upper.getX();
        int upperY = upper.getY();
        int upperZ = upper.getZ();

        // Save to json file
        try (JsonWriter writer = new JsonWriter(new FileWriter(fileName+".json"))) {
            String normalIndent = "  ";
            String compactIndent = "";

            writer.setIndent(normalIndent);
            writer.beginObject();

            writer.name("village_types");
            writer.beginArray();
            writer.setIndent(compactIndent);
            writer.value("standard");
            writer.endArray();
            writer.setIndent(normalIndent);

            writer.name("available_for_villager_count");
            writer.beginArray();
            writer.setIndent(compactIndent);
            writer.value(0);
            writer.value(1000);
            writer.endArray();
            writer.setIndent(normalIndent);

            writer.name("data_per_structure_type");
            writer.beginObject();
            writer.endObject();

            writer.name("abbreviation_map");
            writer.beginObject();
            writer.name("nul");
            writer.value("null");
            writer.endObject();

            BlockState state;
            writer.name("block_state_cube");
            writer.beginArray();
            for (int x=upperX; x>=lowerX; x--) {
                writer.beginArray();
                writer.setIndent(compactIndent);
                for (int y=lowerY; y<=upperY; y++) {
                    writer.beginArray();
                    for (int z=lowerZ; z<=upperZ; z++) {
                        state = world.getBlockState(new BlockPos(x, y, z));
                        // If the state's block id matches, write null keyword
                        if (nullBlockId != null && Registry.BLOCK.getId(state.getBlock()).equals(nullBlockId)) {
                            writer.value("nul");
                        } else {
                            writer.value(blockStateToString(state));
                        }
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
            writer.value(0);
            writer.value(0);
            writer.value(0);
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
        builder.append(Registry.BLOCK.getId(state.getBlock()));
        for (Property<?> property : state.getProperties()) {
            builder.append(":").append(property.getName()).append(":").append(getPropertyValueAsString(state, property));
        }
        return builder.toString();
    }
    private static <T extends Comparable<T>> String getPropertyValueAsString(BlockState state, Property<T> property) {
        return property.name(state.get(property));
    }
}
