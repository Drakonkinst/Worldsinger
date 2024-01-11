package io.github.drakonkinst.worldsinger.event;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.component.PossessionComponent;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable.AttackOrigin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;

public final class ModClientEventHandlers {

    public static void registerEventHandlers() {
        // Possession movement
        // For as little latency as possible, movement commands while possessing are sent on the
        // render thread rather than only 20 times per second. This makes the client view smooth,
        // and the server can catch up.
        WorldRenderEvents.START.register(context -> {
            Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (cameraEntity == null || cameraEntity.isRemoved()) {
                Worldsinger.PROXY.resetRenderViewEntity();
            } else if (player != null
                    && cameraEntity instanceof CameraPossessable cameraPossessable) {
                PossessionComponent possessionData = ModComponents.POSSESSION.get(player);
                if (possessionData.isPossessing()) {
                    float headYaw = player.getHeadYaw();
                    float bodyYaw = player.getBodyYaw();
                    float pitch = player.getPitch();
                    float forwardSpeed = player.input.movementForward;
                    float sidewaysSpeed = player.input.movementSideways;
                    boolean jumping = player.input.jumping;
                    boolean sprinting = MinecraftClient.getInstance().options.sprintKey.isPressed();
                    cameraPossessable.commandMovement(headYaw, bodyYaw, pitch, forwardSpeed,
                            sidewaysSpeed, jumping, sprinting);

                    // Rotation should be wrapped between [-180, 180] on server-side
                    ClientPlayNetworking.send(CameraPossessable.POSSESS_UPDATE_PACKET_ID,
                            CameraPossessable.createSyncPacket(MathHelper.wrapDegrees(headYaw),
                                    MathHelper.wrapDegrees(bodyYaw), MathHelper.wrapDegrees(pitch),
                                    forwardSpeed, sidewaysSpeed, jumping, sprinting));
                } else {
                    Worldsinger.PROXY.resetRenderViewEntity();
                }
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player.isSpectator()) {
                return ActionResult.PASS;
            }

            // Explicitly prevent targeting yourself, which is possible in some possession cases
            if (entity.equals(MinecraftClient.getInstance().player)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player.isSpectator()) {
                return ActionResult.PASS;
            }

            if (MinecraftClient.getInstance()
                    .getCameraEntity() instanceof CameraPossessable cameraPossessable) {
                if (!cameraPossessable.canPerformAttack()) {
                    return ActionResult.FAIL;
                }

                AttackOrigin attackOrigin = cameraPossessable.getEntityAttackOrigin();
                if (attackOrigin == AttackOrigin.DISABLED) {
                    return ActionResult.FAIL;
                } else if (attackOrigin == AttackOrigin.POSSESSED) {
                    // We're on the client side and tryAttack() only works from the server side,
                    // so we need to send a packet
                    ClientPlayNetworking.send(CameraPossessable.POSSESS_ATTACK_PACKET_ID,
                            CameraPossessable.createAttackPacket(entity));
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

            if (MinecraftClient.getInstance()
                    .getCameraEntity() instanceof CameraPossessable cameraPossessable
                    && !cameraPossessable.canBreakBlock()) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

    }

    private ModClientEventHandlers() {}
}
