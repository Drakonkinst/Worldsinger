package io.github.drakonkinst.worldsinger.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import io.github.drakonkinst.worldsinger.entity.attachments.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.entity.attachments.player.PlayerOrigin;
import java.util.Collection;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ResetOriginCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("resetorigin").requires(
                        CommandManager.requirePermissionLevel(ModCommands.PERMISSION_LEVEL_GAMEMASTER))
                .then(argument("target", EntityArgumentType.players()).executes(
                        context -> ResetOriginCommand.resetOrigin(context,
                                EntityArgumentType.getPlayers(context, "target")))));
    }

    private static int resetOrigin(CommandContext<ServerCommandSource> context,
            Collection<ServerPlayerEntity> targets) {
        int i = 0;

        for (ServerPlayerEntity serverPlayerEntity : targets) {
            if (resetOrigin(context.getSource(), serverPlayerEntity)) {
                i++;
            }
        }

        return i;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static boolean resetOrigin(ServerCommandSource source, ServerPlayerEntity target) {
        target.setAttached(ModAttachmentTypes.PLAYER_ORIGIN, PlayerOrigin.DEFAULT);
        source.sendFeedback(
                () -> Text.translatable("commands.resetorigin.success", target.getDisplayName()),
                true);
        target.sendMessage(Text.translatable("commands.resetorigin.info"));
        return true;
    }
}
