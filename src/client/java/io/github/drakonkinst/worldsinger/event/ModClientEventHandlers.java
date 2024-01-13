package io.github.drakonkinst.worldsinger.event;

import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.component.PossessionComponent;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable.AttackOrigin;
import io.github.drakonkinst.worldsinger.entity.freelook.FreeLook;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockApplyCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
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
            if (player == null) {
                return;
            }

            FreeLook freeLookData = (FreeLook) player;
            PossessionComponent possessionData = ModComponents.POSSESSION.get(player);
            if (cameraEntity instanceof CameraPossessable cameraPossessable
                    && !cameraEntity.isRemoved() && possessionData.isPossessing()) {
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
                final boolean jumping = player.input.jumping;
                final boolean sprinting = MinecraftClient.getInstance().options.sprintKey.isPressed();
                cameraPossessable.commandMovement(yaw, pitch, forwardSpeed, sidewaysSpeed, jumping,
                        sprinting);

                // Rotation should be wrapped between [-180, 180] on server-side
                ClientPlayNetworking.send(CameraPossessable.POSSESS_UPDATE_PACKET_ID,
                        CameraPossessable.createUpdatePacket(MathHelper.wrapDegrees(yaw),
                                MathHelper.wrapDegrees(pitch), forwardSpeed, sidewaysSpeed, jumping,
                                sprinting));
            }
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
            ItemStack stack = player.getStackInHand(hand);
            if (player.isSpectator()) {
                return TypedActionResult.pass(stack);
            }
            CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
            if (possessedEntity != null && !possessedEntity.canInteractWithEntities()) {
                return TypedActionResult.fail(stack);
            }
            return TypedActionResult.pass(stack);
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

            CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
            if (possessedEntity != null && !possessedEntity.canBreakBlock()) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        ClientPickBlockApplyCallback.EVENT.register((player, result, stack) -> {
            CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
            if (possessedEntity != null && !possessedEntity.canPickBlock()) {
                return ItemStack.EMPTY;
            }
            return stack;
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
