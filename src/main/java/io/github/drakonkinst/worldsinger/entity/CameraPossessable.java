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
package io.github.drakonkinst.worldsinger.entity;

import io.github.drakonkinst.worldsinger.network.packet.PossessSetPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

// Indicates a mob that can be possessed by the player, which changes the camera client-side only.
// Should use PossessionComponent
public interface CameraPossessable {

    PossessSetPayload POSSESS_RESET = new PossessSetPayload(-1);
    int FIRST_PERSON = 0;
    int THIRD_PERSON_BACK = 1;
    int THIRD_PERSON_FRONT = 2;

    float DEFAULT_MAX_POSSESSION_DISTANCE = 64.0f;

    enum AttackOrigin {
        POSSESSOR, POSSESSED, DISABLED
    }

    // Can be sent by client or server
    static PossessSetPayload createSetPacket(@Nullable CameraPossessable cameraPossessable) {
        if (cameraPossessable == null) {
            return POSSESS_RESET;
        }
        return new PossessSetPayload(cameraPossessable.toEntity().getId());
    }

    static PacketByteBuf createUpdatePacket(float yaw, float pitch, float forwardSpeed,
            float sidewaysSpeed, boolean jumping, boolean sprinting) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeFloat(forwardSpeed);
        buf.writeFloat(sidewaysSpeed);
        buf.writeBoolean(jumping);
        buf.writeBoolean(sprinting);
        return buf;
    }

    void commandMovement(float yaw, float pitch, float forwardSpeed, float sidewaysSpeed,
            boolean jumping, boolean sprinting);

    void onStartPossessing(PlayerEntity possessor);

    void onStopPossessing(PlayerEntity possessor);

    boolean shouldKeepPossessing(PlayerEntity possessor);

    boolean isBeingPossessed();

    LivingEntity toEntity();

    default int getDefaultPerspective() {
        return FIRST_PERSON;
    }

    // Enables any left-click actions, including attacking entities and breaking blocks
    default boolean canPerformAttack() {
        return false;
    }

    default AttackOrigin getEntityAttackOrigin() {
        return AttackOrigin.DISABLED;
    }

    default boolean canPickBlock() {
        return false;
    }

    // Includes all left-click block interaction
    default boolean canBreakBlock() {
        return false;
    }

    default boolean canInteractWithEntities() {
        return false;
    }

    default boolean canModifyInventory() {
        return false;
    }

    default boolean canMoveSelf() {
        return false;
    }

    default boolean canInteractWithBlocks() {
        return false;
    }

    default boolean canSwitchPerspectives() {
        return false;
    }

    default boolean canFreeLook() {
        return false;
    }

    default boolean shouldPossessorLookAt() {
        return false;
    }

    default float getMaxPossessionDistance() {
        return DEFAULT_MAX_POSSESSION_DISTANCE;
    }

    default boolean redirectWorldEvents() {
        return true;
    }
}
