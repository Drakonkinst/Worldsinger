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

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.api.ClientLunagreeData;
import io.github.drakonkinst.worldsinger.entity.attachments.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.api.sync.SyncableAttachment;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.PossessionManager;
import io.github.drakonkinst.worldsinger.cosmere.ShapeshiftingManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManagerAccess;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.PossessionClientUtil;
import io.github.drakonkinst.worldsinger.entity.Shapeshifter;
import io.github.drakonkinst.worldsinger.entity.attachments.player.PlayerPossessionManager;
import io.github.drakonkinst.worldsinger.network.packet.AttachmentEntitySyncPayload;
import io.github.drakonkinst.worldsinger.network.packet.CosmereTimeUpdatePayload;
import io.github.drakonkinst.worldsinger.network.packet.LunagreeSyncPayload;
import io.github.drakonkinst.worldsinger.network.packet.PossessSetPayload;
import io.github.drakonkinst.worldsinger.network.packet.SeetheUpdatePayload;
import io.github.drakonkinst.worldsinger.network.packet.ShapeshiftAttackPayload;
import io.github.drakonkinst.worldsinger.network.packet.ShapeshiftSyncPayload;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.impl.attachment.AttachmentRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

@SuppressWarnings({ "UnqualifiedStaticUsage", "UnstableApiUsage", "resource" })
public final class ClientNetworkHandler {

    public static void registerPacketHandlers() {
        registerShapeshiftingPacketHandlers();
        registerPossessionPacketHandlers();

        ClientPlayNetworking.registerGlobalReceiver(CosmereTimeUpdatePayload.ID,
                (payload, context) -> {
                    ClientWorld world = context.client().world;
                    if (world == null) {
                        Worldsinger.LOGGER.warn(
                                "Could not process cosmere time update because world is null");
                        return;
                    }
                    if (payload.cosmereWorldId() == CosmerePlanet.NONE.getId()) {
                        Worldsinger.LOGGER.warn(
                                "Cosmere time updates from non-cosmere worlds are ignored");
                        return;
                    }

                    CosmerePlanet currentPlanet = CosmerePlanet.getPlanet(world);
                    if (currentPlanet.getId() == payload.cosmereWorldId()) {
                        world.setTime(payload.time(), payload.timeOfDay(), payload.tickDayTime());
                    }
                });

        ClientPlayNetworking.registerGlobalReceiver(AttachmentEntitySyncPayload.ID,
                (payload, context) -> {
                    AttachmentType<?> attachmentType = AttachmentRegistryImpl.get(
                            payload.attachmentId());
                    if (attachmentType == null) {
                        Worldsinger.LOGGER.warn(
                                "Could not process entity attachment sync packet because attachment type does not exist");
                        return;
                    }
                    // ClientPlayerEntity player = context.player(); // This is null for some reason?
                    ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    if (player == null) {
                        Worldsinger.LOGGER.warn(
                                "Could not process entity attachment sync packet because player is null");
                        return;
                    }
                    Entity entity = player.getWorld().getEntityById(payload.entityId());
                    if (entity == null) {
                        Worldsinger.LOGGER.warn(
                                "Could not process entity attachment sync packet because entity does not exist");
                        return;
                    }
                    Object obj = entity.getAttachedOrCreate(attachmentType);
                    if (obj instanceof SyncableAttachment attachment) {
                        attachment.syncFromNbt(payload.nbt());
                    } else {
                        Worldsinger.LOGGER.warn(
                                "Could not process entity attachment sync because attachment is not syncable");
                    }
                });

        ClientPlayNetworking.registerGlobalReceiver(SeetheUpdatePayload.ID, (payload, context) -> {
            // World world = context.player().getWorld();
            World world = MinecraftClient.getInstance().world;
            if (world == null) {
                return;
            }
            if (CosmerePlanet.isLumar(world)) {
                if (payload.isSeething()) {
                    ((LumarManagerAccess) world).worldsinger$getLumarManager()
                            .getSeetheManager()
                            .startSeethe(-1);
                } else {
                    ((LumarManagerAccess) world).worldsinger$getLumarManager()
                            .getSeetheManager()
                            .stopSeethe(-1);
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(LunagreeSyncPayload.ID, (payload, context) -> {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                Worldsinger.LOGGER.warn(
                        "Could not process lunagree sync packet because player is null");
                return;
            }
            ClientLunagreeData data = ClientLunagreeData.get(player.getWorld());
            data.setLunagreeLocations(payload.locations());
        });
    }

    private static void registerShapeshiftingPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(ShapeshiftSyncPayload.ID,
                (payload, context) -> {
                    final int id = payload.entityId();
                    final String entityType = payload.entityType();
                    final NbtCompound entityData = payload.entityData();

                    // Entity entity = context.player().getWorld().getEntityById(id);
                    World world = MinecraftClient.getInstance().world;
                    if (world == null) {
                        Worldsinger.LOGGER.warn(
                                "Failed to process sync packet since world is null");
                        return;
                    }
                    Entity entity = MinecraftClient.getInstance().world.getEntityById(id);
                    if (entity == null) {
                        Worldsinger.LOGGER.warn(
                                "Failed to process sync packet since entity with ID " + id
                                        + " does not exist");
                        return;
                    }
                    if (!(entity instanceof Shapeshifter shapeshifter)) {
                        Worldsinger.LOGGER.warn(
                                "Failed to process sync packet since entity with ID " + id
                                        + " exists but is not a shapeshifter");
                        return;
                    }

                    if (entityType.equals(ShapeshiftingManager.EMPTY_MORPH)) {
                        shapeshifter.updateMorph(null);
                        return;
                    }

                    if (entityData != null) {
                        entityData.putString(Entity.ID_KEY, entityType);
                        ShapeshiftingManager.createMorphFromNbt(shapeshifter, entityData, true);
                    }
                });
        ClientPlayNetworking.registerGlobalReceiver(ShapeshiftAttackPayload.ID,
                (payload, context) -> {
                    final int id = payload.entityId();

                    // Entity entity = context.player().getWorld().getEntityById(id);
                    World world = MinecraftClient.getInstance().world;
                    if (world == null) {
                        Worldsinger.LOGGER.warn(
                                "Failed to process sync packet since world is null");
                        return;
                    }
                    Entity entity = MinecraftClient.getInstance().world.getEntityById(id);
                    if (entity == null) {
                        Worldsinger.LOGGER.warn(
                                "Failed to process attack packet since entity with ID " + id
                                        + " does not exist");
                        return;
                    }
                    if (!(entity instanceof Shapeshifter shapeshifter)) {
                        Worldsinger.LOGGER.warn(
                                "Failed to process attack packet since entity with ID " + id
                                        + " exists but is not a shapeshifter");
                        return;
                    }
                    ShapeshiftingManager.onAttackClient(shapeshifter);
                });
    }

    private static void registerPossessionPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(PossessSetPayload.ID, (payload, context) -> {
            final int entityIdToPossess = payload.entityId();
            // ClientPlayerEntity player = context.player();
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                return;
            }
            PossessionManager possessionManager = player.getAttachedOrCreate(
                    ModAttachmentTypes.POSSESSION, () -> PlayerPossessionManager.create(player));
            if (entityIdToPossess < 0) {
                possessionManager.resetPossessionTarget();
            } else {
                // Display dismount prompt, regardless of whether entity is already set
                PossessionClientUtil.displayPossessStartText();

                CameraPossessable currentPossessedEntity = possessionManager.getPossessionTarget();
                if (currentPossessedEntity != null
                        && currentPossessedEntity.toEntity().getId() == entityIdToPossess) {
                    // Already set
                    return;
                }
                Entity entity = player.getWorld().getEntityById(entityIdToPossess);
                if (entity instanceof CameraPossessable cameraPossessable) {
                    possessionManager.setPossessionTarget(cameraPossessable);
                }
            }
        });
    }

    private ClientNetworkHandler() {}
}
