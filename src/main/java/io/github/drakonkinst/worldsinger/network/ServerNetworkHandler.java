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

import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.component.PossessionComponent;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings("UnqualifiedStaticUsage")
public class ServerNetworkHandler {

    public static void registerPacketHandler() {
        registerPossessionPacketHandlers();
    }

    private static void registerPossessionPacketHandlers() {
        // Client requests to set the possessing entity
        ServerPlayNetworking.registerGlobalReceiver(CameraPossessable.POSSESS_SET_PACKET_ID,
                ((server, player, handler, buf, responseSender) -> {
                    final int possessedEntityId = buf.readVarInt();
                    PossessionComponent possessionData = ModComponents.POSSESSION.get(player);

                    // If negative, reset the entity
                    if (possessionData.isPossessing() && possessedEntityId < 0) {
                        ModComponents.POSSESSION.get(player).resetPossessionTarget();
                    }
                }));

        // Client requests to update their possessed entity
        ServerPlayNetworking.registerGlobalReceiver(CameraPossessable.POSSESS_UPDATE_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    // Get corresponding entity
                    PossessionComponent possessionData = ModComponents.POSSESSION.get(player);
                    CameraPossessable possessedEntity = possessionData.getPossessionTarget();
                    if (possessedEntity == null) {
                        // Not possessing anything according to the server
                        possessionData.resetPossessionTarget();
                        return;
                    }

                    // Ensure rotation is wrapped between [-180, 180] on server-side
                    final float yaw = MathHelper.wrapDegrees(buf.readFloat());
                    final float pitch = MathHelper.wrapDegrees(buf.readFloat());
                    final float forwardSpeed = buf.readFloat();
                    final float sidewaysSpeed = buf.readFloat();
                    final boolean jumping = buf.readBoolean();
                    final boolean sprinting = buf.readBoolean();
                    possessedEntity.commandMovement(yaw, pitch, forwardSpeed, sidewaysSpeed,
                            jumping, sprinting);
                });

        // Client requests their entity makes a (melee) attack
        ServerPlayNetworking.registerGlobalReceiver(CameraPossessable.POSSESS_ATTACK_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    // Get corresponding entity
                    PossessionComponent possessionData = ModComponents.POSSESSION.get(player);
                    CameraPossessable possessedEntity = possessionData.getPossessionTarget();
                    if (possessedEntity == null) {
                        // Not possessing anything according to the server
                        return;
                    }

                    // The player can only possess one entity at a time, so no need to check the attacker ID
                    final int targetId = buf.readVarInt();
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
