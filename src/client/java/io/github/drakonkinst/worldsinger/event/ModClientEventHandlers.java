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
package io.github.drakonkinst.worldsinger.event;

import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.api.ModClientAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.PossessionManager;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable.AttackOrigin;
import io.github.drakonkinst.worldsinger.entity.freelook.FreeLook;
import io.github.drakonkinst.worldsinger.gui.ThirstStatusBar;
import io.github.drakonkinst.worldsinger.network.packet.PossessAttackPayload;
import io.github.drakonkinst.worldsinger.network.packet.PossessUpdatePayload;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;

@SuppressWarnings("UnstableApiUsage")
public final class ModClientEventHandlers {

    public static void registerEventHandlers() {
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
                final float forwardSpeed = player.input.movementForward;
                final float sidewaysSpeed = player.input.movementSideways;
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

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            // FIXME: The thirst meter intermittently doesn't show for some reason?
            MinecraftClient client = MinecraftClient.getInstance();
            assert client.interactionManager != null;
            if (client.interactionManager.hasStatusBars()) {
                assert client.player != null;
                if (ThirstStatusBar.shouldRenderThirstBar(client.player)) {
                    Profiler profiler = Profilers.get();
                    profiler.push("thirst");
                    ThirstStatusBar.renderThirstStatusBar(client, context, client.player);
                    profiler.pop();
                }
            }
        });

        ClientTickEvents.END_WORLD_TICK.register(world -> {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                return;
            }
            world.getAttachedOrCreate(ModClientAttachmentTypes.LUNAGREE_DATA).update(world, player);
            world.getAttachedOrCreate(ModClientAttachmentTypes.RAINLINE_DATA).update(world, player);
        });
        ModClientEventHandlers.registerPreventTargetingSelfEventHandlers();
        ModClientEventHandlers.registerPossessionEventHandlers();

    }

    private static void registerPossessionEventHandlers() {
        UseEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            if (player.isSpectator()) {
                return ActionResult.PASS;
            }
            CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();

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
            CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
            if (possessedEntity != null && !possessedEntity.canInteractWithEntities()) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSpectator()) {
                return ActionResult.PASS;
            }
            CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
            if (possessedEntity != null && !possessedEntity.canInteractWithBlocks()) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player.isSpectator()) {
                return ActionResult.PASS;
            }

            CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
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

            CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
            if (possessedEntity != null && !possessedEntity.canBreakBlock()) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    private static void registerPreventTargetingSelfEventHandlers() {
        // Explicitly prevent targeting yourself, which is possible in some possession cases
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity.equals(MinecraftClient.getInstance().player)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity.equals(MinecraftClient.getInstance().player)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    private ModClientEventHandlers() {}
}
