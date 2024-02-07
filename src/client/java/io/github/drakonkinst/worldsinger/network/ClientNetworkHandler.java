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
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.PossessionManager;
import io.github.drakonkinst.worldsinger.cosmere.ShapeshiftingManager;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.Shapeshifter;
import io.github.drakonkinst.worldsinger.entity.data.PlayerPossessionManager;
import io.github.drakonkinst.worldsinger.network.packet.PossessSetPayload;
import io.github.drakonkinst.worldsinger.network.packet.ShapeshiftAttackPayload;
import io.github.drakonkinst.worldsinger.network.packet.ShapeshiftSyncPayload;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;

@SuppressWarnings({ "UnqualifiedStaticUsage", "UnstableApiUsage" })
public final class ClientNetworkHandler {

    public static void registerPacketHandlers() {
        registerShapeshiftingPacketHandlers();
        registerPossessionPacketHandlers();
    }

    private static void registerShapeshiftingPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(ShapeshiftSyncPayload.ID,
                (payload, context) -> {
                    final int id = payload.entityId();
                    final String entityType = payload.entityType();
                    final NbtCompound entityData = payload.entityData();

                    Entity entity = context.player().getWorld().getEntityById(id);
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

                    Entity entity = context.player().getWorld().getEntityById(id);
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
            PossessionManager possessionManager = context.player()
                    .getAttachedOrCreate(ModAttachmentTypes.POSSESSION,
                            () -> PlayerPossessionManager.create(context.player()));
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
                Entity entity = context.player().getWorld().getEntityById(entityIdToPossess);
                if (entity instanceof CameraPossessable cameraPossessable) {
                    possessionManager.setPossessionTarget(cameraPossessable);
                }
            }
        });
    }

    private ClientNetworkHandler() {}
}
