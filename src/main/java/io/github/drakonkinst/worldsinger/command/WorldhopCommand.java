package io.github.drakonkinst.worldsinger.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.drakonkinst.worldsinger.command.argument.CosmerePlanetArgumentType;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.dialog.ModDialogs;
import java.util.Set;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WorldhopCommand {

    private static final SimpleCommandExceptionType MUST_BE_PLAYER_EXCEPTION = new SimpleCommandExceptionType(
            Text.translatable("commands.worldhop.teleport.must_be_player"));
    private static final SimpleCommandExceptionType TELEPORT_FAILED = new SimpleCommandExceptionType(
            Text.translatable("commands.worldhop.teleport.failed_teleport"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("worldhop").requires(
                        source -> source.hasPermissionLevel(ModCommands.PERMISSION_LEVEL_GAMEMASTER))
                .then(argument("planet", CosmerePlanetArgumentType.cosmerePlanet()).executes(
                        WorldhopCommand::worldhopToPlanet))
                .executes(WorldhopCommand::showDialog));
    }

    private static Vec3d getWorldSpawnPos(ServerWorld world) {
        BlockPos worldSpawnPos = world.getSpawnPos();
        Vec3d centerPos = worldSpawnPos.toCenterPos();
        int safeYLevel = world.getWorldChunk(worldSpawnPos)
                .sampleHeightmap(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, worldSpawnPos.getX(),
                        worldSpawnPos.getZ()) + 1;
        BlockPos safeSpawnPos = BlockPos.ofFloored(centerPos.getX(), safeYLevel, centerPos.getZ());
        return safeSpawnPos.toBottomCenterPos();
    }

    @Nullable
    private static TeleportTarget createTeleportTarget(ServerWorld world,
            RegistryKey<World> targetWorldKey) {
        ServerWorld targetWorld = world.getServer().getWorld(targetWorldKey);
        if (targetWorld == null) {
            return null;
        }

        float spawnAngle = targetWorld.getSpawnAngle();
        Vec3d spawnPos = getWorldSpawnPos(targetWorld);
        Set<PositionFlag> positionFlags = PositionFlag.combine(PositionFlag.DELTA,
                PositionFlag.ROT);
        return new TeleportTarget(targetWorld, spawnPos, Vec3d.ZERO, spawnAngle, 0.0f,
                positionFlags, TeleportTarget.NO_OP);
    }

    private static int worldhopToPlanet(CommandContext<ServerCommandSource> commandContext)
            throws CommandSyntaxException {
        ServerCommandSource source = commandContext.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            throw MUST_BE_PLAYER_EXCEPTION.create();
        }
        CosmerePlanet planet = CosmerePlanetArgumentType.getCosmerePlanet(commandContext, "planet");
        if (planet == null) {
            planet = CosmerePlanet.NONE;
        }
        TeleportTarget teleportTarget = createTeleportTarget(player.getWorld(),
                planet.getRegistryKey());
        if (teleportTarget == null) {
            throw TELEPORT_FAILED.create();
        }
        player.teleportTo(teleportTarget);
        Text planetName = Text.translatable("planet.worldsinger." + planet.getTranslationKey());
        commandContext.getSource()
                .sendFeedback(
                        () -> Text.translatable("commands.worldhop.teleport.success", planetName),
                        true);
        return 1;
    }

    private static int showDialog(CommandContext<ServerCommandSource> commandContext)
            throws CommandSyntaxException {
        ServerCommandSource source = commandContext.getSource();
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            throw MUST_BE_PLAYER_EXCEPTION.create();
        }
        player.openDialog(player.getWorld()
                .getRegistryManager()
                .getOrThrow(RegistryKeys.DIALOG)
                .getOrThrow(ModDialogs.WORLDHOP));
        return 1;
    }

}
