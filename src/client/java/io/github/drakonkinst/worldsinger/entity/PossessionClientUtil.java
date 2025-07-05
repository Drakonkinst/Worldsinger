/*
 * MIT License
 *
 * Copyright (c) 2024 Drakonkinst
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
package io.github.drakonkinst.worldsinger.entity;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.entity.attachments.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.PossessionManager;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable.AttackOrigin;
import io.github.drakonkinst.worldsinger.network.packet.PossessAttackPayload;
import io.github.drakonkinst.worldsinger.network.packet.PossessUpdatePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings("UnstableApiUsage")
public final class PossessionClientUtil {

    private static final String ON_POSSESS_START_TRANSLATION_KEY = Util.createTranslationKey(
            "action", Worldsinger.id("possess.on_start"));

    public static void registerPossessionEventHandlers() {
        // Possession movement
        // For as little latency as possible, movement commands while possessing are sent on the
        // render thread rather than only 20 times per second. This makes the client view smooth,
        // and the server can catch up.
        WorldRenderEvents.START.register(context -> {
            Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                return;
            }

            FreeLook freeLookData = (FreeLook) player;
            PossessionManager possessionManager = player.getAttached(ModAttachmentTypes.POSSESSION);
            if (cameraEntity instanceof CameraPossessable cameraPossessable
                    && !cameraEntity.isRemoved() && possessionManager != null
                    && possessionManager.isPossessing()) {
                float yaw;
                float pitch;
                if (freeLookData.worldsinger$isFreeLookEnabled()) {
                    yaw = freeLookData.worldsinger$getFreeLookYaw();
                    pitch = freeLookData.worldsinger$getFreeLookPitch();
                } else {
                    yaw = player.getHeadYaw();
                    pitch = player.getPitch();
                }
                final float forwardSpeed = player.input.getMovementInput().y;
                final float sidewaysSpeed = player.input.getMovementInput().x;
                final boolean jumping = player.input.playerInput.jump();
                final boolean sprinting = MinecraftClient.getInstance().options.sprintKey.isPressed();
                cameraPossessable.commandMovement(yaw, pitch, forwardSpeed, sidewaysSpeed, jumping,
                        sprinting);

                // Rotation should be wrapped between [-180, 180] on server-side
                ClientPlayNetworking.send(new PossessUpdatePayload(MathHelper.wrapDegrees(yaw),
                        MathHelper.wrapDegrees(pitch), forwardSpeed, sidewaysSpeed, jumping,
                        sprinting));
            }
        });

        UseEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            if (player.isSpectator()) {
                return ActionResult.PASS;
            }
            CameraPossessable possessedEntity = getPossessedEntity();

            // Allow interactions targeting the possessed entity
            // This fixes issues where it can prevent the packet from being sent, causing a desync
            // on client/server side
            if (entity.equals(possessedEntity)) {
                return ActionResult.PASS;
            }
            if (possessedEntity != null && !possessedEntity.canInteractWithEntities()) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        }));

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (player.isSpectator()) {
                return ActionResult.PASS;
            }
            CameraPossessable possessedEntity = getPossessedEntity();
            if (possessedEntity != null && !possessedEntity.canInteractWithEntities()) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSpectator()) {
                return ActionResult.PASS;
            }
            CameraPossessable possessedEntity = getPossessedEntity();
            if (possessedEntity != null && !possessedEntity.canInteractWithBlocks()) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player.isSpectator()) {
                return ActionResult.PASS;
            }

            CameraPossessable possessedEntity = getPossessedEntity();
            if (possessedEntity != null) {
                if (!possessedEntity.canPerformAttack()) {
                    return ActionResult.FAIL;
                }

                AttackOrigin attackOrigin = possessedEntity.getEntityAttackOrigin();
                if (attackOrigin == AttackOrigin.DISABLED) {
                    return ActionResult.FAIL;
                } else if (attackOrigin == AttackOrigin.POSSESSED) {
                    // We're on the client side and tryAttack() only works from the server side,
                    // so we need to send a packet
                    ClientPlayNetworking.send(new PossessAttackPayload(entity.getId()));
                    // Don't send the original packet
                    return ActionResult.FAIL;
                }
            }
            return ActionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player.isSpectator()) {
                return ActionResult.PASS;
            }

            CameraPossessable possessedEntity = getPossessedEntity();
            if (possessedEntity != null && !possessedEntity.canBreakBlock()) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    // Don't need to check camera entity directly since we can trust possessionData to hold the
    // right value, if the camera entity is set due to possession.
    public static CameraPossessable getPossessedEntity() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return null;
        }
        PossessionManager possessionManager = player.getAttached(ModAttachmentTypes.POSSESSION);
        if (possessionManager == null) {
            return null;
        }
        return possessionManager.getPossessionTarget();
    }

    public static void displayPossessStartText() {
        MinecraftClient client = MinecraftClient.getInstance();
        Text text = Text.translatable(ON_POSSESS_START_TRANSLATION_KEY,
                client.options.sneakKey.getBoundKeyLocalizedText());
        client.inGameHud.setOverlayMessage(text, false);
        client.getNarratorManager().narrate(text);
    }

    private PossessionClientUtil() {}
}
