package io.github.drakonkinst.worldsinger.network;

import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.component.PossessionComponent;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class ServerNetworkHandler {

    public static void registerPacketHandler() {
        ServerPlayNetworking.registerGlobalReceiver(CameraPossessable.POSSESS_UPDATE_PACKET_ID,
                ((server, player, handler, buf, responseSender) -> {
                    // Get corresponding entity
                    PossessionComponent possessionData = ModComponents.POSSESSION.get(player);
                    CameraPossessable possessedEntity = possessionData.getPossessedEntity();
                    if (possessedEntity == null) {
                        // Not possessing anything according to the server
                        return;
                    }

                    // Ensure rotation is wrapped between [-180, 180] on server-side
                    float headYaw = MathHelper.wrapDegrees(buf.readFloat());
                    float bodyYaw = MathHelper.wrapDegrees(buf.readFloat());
                    float pitch = MathHelper.wrapDegrees(buf.readFloat());
                    float forwardSpeed = buf.readFloat();
                    float sidewaysSpeed = buf.readFloat();
                    boolean jumping = buf.readBoolean();
                    boolean sprinting = buf.readBoolean();
                    possessedEntity.commandMovement(headYaw, bodyYaw, pitch, forwardSpeed,
                            sidewaysSpeed, jumping, sprinting);
                }));
        ServerPlayNetworking.registerGlobalReceiver(CameraPossessable.POSSESS_ATTACK_PACKET_ID,
                ((server, player, handler, buf, responseSender) -> {
                    // Get corresponding entity
                    PossessionComponent possessionData = ModComponents.POSSESSION.get(player);
                    CameraPossessable possessedEntity = possessionData.getPossessedEntity();
                    if (possessedEntity == null) {
                        // Not possessing anything according to the server
                        return;
                    }

                    // The player can only possess one entity at a time, so no need to check the attacker ID
                    int targetId = buf.readVarInt();
                    LivingEntity attacker = possessedEntity.toEntity();
                    Entity target = player.getWorld().getEntityById(targetId);

                    if (target == null) {
                        // Target does not exist
                        return;
                    }

                    // TODO: Do we need to check attack range here?
                    attacker.tryAttack(target);
                }));
    }
}
