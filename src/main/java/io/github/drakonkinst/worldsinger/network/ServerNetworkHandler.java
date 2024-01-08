package io.github.drakonkinst.worldsinger.network;

import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.component.PossessionComponent;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.math.MathHelper;

public class ServerNetworkHandler {

    public static void registerPacketHandler() {
        ServerPlayNetworking.registerGlobalReceiver(CameraPossessable.POSSESS_UPDATE_PACKET_ID,
                ((server, player, handler, buf, responseSender) -> {
                    // Get corresponding entity
                    PossessionComponent possessionData = ModComponents.POSSESSION.get(player);
                    CameraPossessable possessedEntity = possessionData.getPossessedEntity();
                    if (possessedEntity != null) {
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
                    }
                }));
    }
}
