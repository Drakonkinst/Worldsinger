package io.github.drakonkinst.worldsinger.network;

import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.component.PossessionComponent;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.network.packet.PossessAttackPayload;
import io.github.drakonkinst.worldsinger.network.packet.PossessSetPayload;
import io.github.drakonkinst.worldsinger.network.packet.PossessUpdatePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings("UnqualifiedStaticUsage")
public class ServerNetworkHandler {

    public static void initialize() {
        registerPossessionPacketHandlers();
    }

    private static void registerPossessionPacketHandlers() {
        // Client requests to set the possessing entity
        ServerPlayNetworking.registerGlobalReceiver(PossessSetPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            final int possessedEntityId = payload.entityId();
            PossessionComponent possessionData = ModComponents.POSSESSION.get(player);

            // If negative, reset the entity
            if (possessionData.isPossessing() && possessedEntityId < 0) {
                ModComponents.POSSESSION.get(player).resetPossessionTarget();
            }
        });

        // Client requests to update their possessed entity
        ServerPlayNetworking.registerGlobalReceiver(PossessUpdatePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            // Get corresponding entity
            PossessionComponent possessionData = ModComponents.POSSESSION.get(player);
            CameraPossessable possessedEntity = possessionData.getPossessionTarget();
            if (possessedEntity == null) {
                // Not possessing anything according to the server
                possessionData.resetPossessionTarget();
                return;
            }

            // Ensure rotation is wrapped between [-180, 180] on server-side
            final float yaw = MathHelper.wrapDegrees(payload.yaw());
            final float pitch = MathHelper.wrapDegrees(payload.pitch());
            possessedEntity.commandMovement(yaw, pitch, payload.forwardSpeed(),
                    payload.sidewaysSpeed(), payload.jumping(), payload.sprinting());
        });

        // Client requests to set the possessing entity
        ServerPlayNetworking.registerGlobalReceiver(PossessAttackPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            // Get corresponding entity
            PossessionComponent possessionData = ModComponents.POSSESSION.get(player);
            CameraPossessable possessedEntity = possessionData.getPossessionTarget();
            if (possessedEntity == null) {
                // Not possessing anything according to the server
                return;
            }

            // The player can only possess one entity at a time, so no need to check the attacker ID
            final int targetId = payload.targetEntityId();
            LivingEntity attacker = possessedEntity.toEntity();
            Entity target = player.getWorld().getEntityById(targetId);

            if (target == null) {
                // Target does not exist
                return;
            }

            boolean success = attacker.tryAttack(target);
            // Set player's attacking target so other pets respond
            if (success) {
                player.onAttacking(target);
            }
        });
    }
}
