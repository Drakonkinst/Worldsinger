/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Drakonkinst
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.drakonkinst.worldsinger.command;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManagerAccess;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class SeetheCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("seethe").requires(
                        source -> source.hasPermissionLevel(ModCommands.PERMISSION_LEVEL_GAMEMASTER))
                .then(literal("on").executes(SeetheCommand::activateNoArgs)
                        .then(argument("duration", TimeArgumentType.time(0)).executes(
                                SeetheCommand::activateWithArgs)))
                .then(literal("off").executes(SeetheCommand::deactivateNoArgs)
                        .then(argument("duration", TimeArgumentType.time(0)).executes(
                                SeetheCommand::deactivateWithArgs)))
                .executes(SeetheCommand::getStatus));
    }

    private static int activateNoArgs(CommandContext<ServerCommandSource> context) {
        SeetheManager seethe = SeetheCommand.getSeethe(context);
        seethe.startSeethe(-1);
        // TODO: Make these translatable
        context.getSource().sendFeedback(() -> Text.literal("Set seethe to ACTIVE"), true);
        SeetheCommand.getSeethe(context).sync();
        return 1;
    }

    private static int activateWithArgs(CommandContext<ServerCommandSource> context) {
        SeetheManager seethe = SeetheCommand.getSeethe(context);
        seethe.startSeethe(getInteger(context, "duration"));
        // TODO: Make these translatable
        context.getSource()
                .sendFeedback(() -> Text.literal(
                                "Set seethe to ACTIVE for " + seethe.getTicksUntilNextCycle() + " ticks"),
                        true);
        SeetheCommand.getSeethe(context).sync();
        return 1;
    }

    private static int deactivateNoArgs(CommandContext<ServerCommandSource> context) {
        SeetheManager seethe = SeetheCommand.getSeethe(context);
        seethe.stopSeethe(-1);
        // TODO: Make these translatable
        context.getSource().sendFeedback(() -> Text.literal("Set seethe to INACTIVE"), true);
        SeetheCommand.getSeethe(context).sync();
        return 1;
    }

    private static int deactivateWithArgs(CommandContext<ServerCommandSource> context) {
        SeetheManager seethe = SeetheCommand.getSeethe(context);
        seethe.stopSeethe(getInteger(context, "duration"));
        // TODO: Make these translatable
        context.getSource()
                .sendFeedback(() -> Text.literal(
                                "Set seethe to INACTIVE for " + seethe.getTicksUntilNextCycle() + " ticks"),
                        true);
        SeetheCommand.getSeethe(context).sync();
        return 1;
    }

    private static int getStatus(CommandContext<ServerCommandSource> context) {
        SeetheManager seethe = SeetheCommand.getSeethe(context);
        boolean isSeething = seethe.isSeething();
        int cycleTicks = seethe.getTicksUntilNextCycle();
        // TODO: Make these translatable
        context.getSource()
                .sendMessage(Text.literal(
                        "Seethe is " + (isSeething ? "ACTIVE" : "INACTIVE") + " for the next "
                                + cycleTicks + " ticks, or " + (MathHelper.floor(
                                cycleTicks * ModConstants.TICKS_TO_SECONDS)) + " seconds"));
        SeetheCommand.getSeethe(context).sync();
        return 1;
    }

    private static SeetheManager getSeethe(CommandContext<ServerCommandSource> context) {
        return ((SeetheManagerAccess) context.getSource()
                .getWorld()).worldsinger$getSeetheManager();
    }
}
