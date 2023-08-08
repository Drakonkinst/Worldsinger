package io.github.drakonkinst.examplemod.command;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.drakonkinst.examplemod.world.LumarSeetheAccess;
import io.github.drakonkinst.examplemod.world.LumarSeetheData;
import io.github.drakonkinst.examplemod.world.LumarSeetheManager;
import io.github.drakonkinst.examplemod.world.LumarSeetheManagerAccess;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class ModCommands {

    private static final int PERMISSION_LEVEL_GAMEMASTER = 2;
    private static final float TICKS_TO_SECONDS = 1.0f / 20.0f;

    private ModCommands() {}

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> createCommands(dispatcher));
    }

    private static void createCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("seethe")
                .requires(source -> source.hasPermissionLevel(PERMISSION_LEVEL_GAMEMASTER))
                .then(literal("on")
                        .executes(ModCommands::runCommandSeetheOnNoArgs)
                        .then(argument("duration", TimeArgumentType.time(0))
                                .executes(ModCommands::runCommandSeetheOnWithArgs)))
                .then(literal("off")
                        .executes(ModCommands::runCommandSeetheOffNoArgs)
                        .then(argument("duration", TimeArgumentType.time(0))
                                .executes(ModCommands::runCommandSeetheOffWithArgs)))
                .executes(ModCommands::runCommandSeetheGet));
    }

    private static int runCommandSeetheGet(CommandContext<ServerCommandSource> context) {
        LumarSeetheData lumarSeetheData = ((LumarSeetheAccess) context.getSource()
                .getWorld()).examplemod$getLumarSeetheData();
        boolean isSeething = lumarSeetheData.isSeething();
        int cycleTicks = lumarSeetheData.getCycleTicks();
        int cyclesUntilNextLongStilling = lumarSeetheData.getCyclesUntilNextLongStilling();
        context.getSource().sendMessage(Text.literal(
                "Seethe is " + (isSeething ? "ACTIVE" : "INACTIVE") + " for the next " + cycleTicks
                        + " ticks, or " + ((int) Math.floor(cycleTicks * TICKS_TO_SECONDS))
                        + " seconds\nThere are " + cyclesUntilNextLongStilling
                        + " cycles until the next long stilling"));
        return 1;
    }

    private static int runCommandSeetheOnNoArgs(CommandContext<ServerCommandSource> context) {
        LumarSeetheManager lumarSeetheManager = ((LumarSeetheManagerAccess) context.getSource()
                .getWorld()).examplemod$getLumarSeetheManager();
        lumarSeetheManager.startSeething();
        context.getSource().sendMessage(Text.literal("Set seethe to ACTIVE"));
        return 1;
    }

    private static int runCommandSeetheOffNoArgs(CommandContext<ServerCommandSource> context) {
        LumarSeetheManager lumarSeetheManager = ((LumarSeetheManagerAccess) context.getSource()
                .getWorld()).examplemod$getLumarSeetheManager();
        lumarSeetheManager.startStilling();
        context.getSource().sendMessage(Text.literal("Set seethe to INACTIVE"));
        return 1;
    }

    private static int runCommandSeetheOnWithArgs(CommandContext<ServerCommandSource> context) {
        LumarSeetheManager lumarSeetheManager = ((LumarSeetheManagerAccess) context.getSource()
                .getWorld()).examplemod$getLumarSeetheManager();
        lumarSeetheManager.startSeething(getInteger(context, "duration"));
        context.getSource().sendMessage(Text.literal("Set seethe to ACTIVE"));
        return 1;
    }

    private static int runCommandSeetheOffWithArgs(CommandContext<ServerCommandSource> context) {
        LumarSeetheManager lumarSeetheManager = ((LumarSeetheManagerAccess) context.getSource()
                .getWorld()).examplemod$getLumarSeetheManager();
        lumarSeetheManager.startStilling(getInteger(context, "duration"));
        context.getSource().sendMessage(Text.literal("Set seethe to INACTIVE"));
        return 1;
    }
}
