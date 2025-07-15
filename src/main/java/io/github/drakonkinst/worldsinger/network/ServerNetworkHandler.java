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
package io.github.drakonkinst.worldsinger.network;

import io.github.drakonkinst.worldsinger.command.ModCommands;
import io.github.drakonkinst.worldsinger.command.WorldhopCommand;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.PossessionManager;
import io.github.drakonkinst.worldsinger.dialog.ModDialogs;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.attachments.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.entity.attachments.player.PlayerOrigin;
import io.github.drakonkinst.worldsinger.event.CustomClickActionCallback;
import io.github.drakonkinst.worldsinger.event.CustomClickConfigActionCallback;
import io.github.drakonkinst.worldsinger.mixin.accessor.PlayerManagerAccessor;
import io.github.drakonkinst.worldsinger.network.packet.PossessAttackPayload;
import io.github.drakonkinst.worldsinger.network.packet.PossessSetPayload;
import io.github.drakonkinst.worldsinger.network.packet.PossessUpdatePayload;
import io.github.drakonkinst.worldsinger.world.ExtendedPlayerSaveHandler;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.PlayerSaveHandler;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({ "UnqualifiedStaticUsage", "UnstableApiUsage" })
public final class ServerNetworkHandler {

    public static void initialize() {
        registerConfigurationHandlers();
        registerPossessionPacketHandlers();
    }

    private static void registerConfigurationHandlers() {
        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            UUID uuid = ((UUIDProvider) handler).worldsinger$getUuid();
            if (isFirstPlayerJoin(uuid, server)) {
                handler.addTask(new SelectWorldOriginTask(server));
            }
        });
        CustomClickActionCallback.EVENT.register((player, id, payload) -> {
            if (id.equals(ModDialogs.WORLDHOP_ID) && payload.isPresent()) {
                handleWorldhopAction(player, payload.get().asCompound().orElse(null));
            }
        });
        CustomClickConfigActionCallback.EVENT.register(((handler, configActions, id, payload) -> {
            if (id.equals(ModDialogs.WORLDHOP_ID) && payload.isPresent()) {
                configActions.put(ModDialogs.WORLDHOP_ID, (player) -> {
                    handleWorldhopAction(player, payload.get().asCompound().orElse(null));
                });
                handler.completeTask(SelectWorldOriginTask.KEY);
            }
        }));
    }

    // TODO: Move all this logic to a library
    private static boolean isFirstPlayerJoin(UUID uuid, MinecraftServer server) {
        PlayerSaveHandler playerSaveHandler = ((PlayerManagerAccessor) server.getPlayerManager()).worldsinger$getSaveHandler();
        return !((ExtendedPlayerSaveHandler) playerSaveHandler).worldsinger$existsInSaveData(uuid);
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void handleWorldhopAction(ServerPlayerEntity player,
            @Nullable NbtCompound compound) {
        if (compound == null) {
            return;
        }
        compound.getString(ModDialogs.WORLDHOP_PAYLOAD_KEY).ifPresent(planetId -> {
            // TODO: At some point we might want to prevent players from sending this action more than once
            boolean force = player.hasPermissionLevel(ModCommands.PERMISSION_LEVEL_GAMEMASTER);
            PlayerOrigin playerOrigin = player.getAttachedOrCreate(
                    ModAttachmentTypes.PLAYER_ORIGIN);
            if (playerOrigin.hasPlayerSelectedOrigin() && !force) {
                player.sendMessage(Text.translatable(
                        "action.worldsinger.choose_origin_planet.already_selected"));
                return;
            }
            CosmerePlanet targetPlanet = null;
            for (CosmerePlanet planet : CosmerePlanet.VALUES) {
                if (planet.getTranslationKey().equals(planetId)) {
                    targetPlanet = planet;
                }
            }
            if (targetPlanet != null) {
                // It's valid
                TeleportTarget target = WorldhopCommand.createTeleportTarget(player.getWorld(),
                        targetPlanet.getRegistryKey());
                if (target != null) {
                    player.teleportTo(target);
                }
            }
            if (!playerOrigin.hasPlayerSelectedOrigin()) {
                player.setAttached(ModAttachmentTypes.PLAYER_ORIGIN,
                        playerOrigin.setStartingPlanet(targetPlanet));
            }
        });
    }

    private static void registerPossessionPacketHandlers() {
        // Client requests to set the possessing entity
        ServerPlayNetworking.registerGlobalReceiver(PossessSetPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            final int possessedEntityId = payload.entityId();
            PossessionManager possessionManager = player.getAttached(ModAttachmentTypes.POSSESSION);

            // If negative, reset the entity
            if (possessionManager != null && possessionManager.isPossessing()
                    && possessedEntityId < 0) {
                possessionManager.resetPossessionTarget();
            }
        });

        // Client requests to update their possessed entity
        ServerPlayNetworking.registerGlobalReceiver(PossessUpdatePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            // Get corresponding entity
            PossessionManager possessionManager = player.getAttached(ModAttachmentTypes.POSSESSION);
            if (possessionManager == null) {
                return;
            }
            CameraPossessable possessedEntity = possessionManager.getPossessionTarget();
            if (possessedEntity == null) {
                // Not possessing anything according to the server
                possessionManager.resetPossessionTarget();
                return;
            }

            // Ensure rotation is wrapped between [-180, 180] on server-side
            final float yaw = MathHelper.wrapDegrees(payload.yaw());
            final float pitch = MathHelper.wrapDegrees(payload.pitch());
            possessedEntity.commandMovement(yaw, pitch, payload.forwardSpeed(),
                    payload.sidewaysSpeed(), payload.jumping(), payload.sprinting());
        });

        // Client requests to attack as the possessing entity
        ServerPlayNetworking.registerGlobalReceiver(PossessAttackPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            // Get corresponding entity
            PossessionManager possessionManager = player.getAttached(ModAttachmentTypes.POSSESSION);
            if (possessionManager == null) {
                return;
            }
            CameraPossessable possessedEntity = possessionManager.getPossessionTarget();
            if (possessedEntity == null) {
                // Not possessing anything according to the server
                return;
            }
            if (!(player.getWorld() instanceof ServerWorld serverWorld)) {
                return;
            }

            // The player can only possess one entity at a time, so no need to check the attacker ID
            final int targetId = payload.targetEntityId();
            LivingEntity attacker = possessedEntity.toEntity();
            Entity target = serverWorld.getEntityById(targetId);

            if (target == null) {
                // Target does not exist
                return;
            }

            boolean success = attacker.tryAttack(serverWorld, target);
            // Set player's attacking target so other pets respond
            if (success) {
                attacker.onAttacking(target);
                player.onAttacking(target);
            }
        });
    }

    private ServerNetworkHandler() {}
}
