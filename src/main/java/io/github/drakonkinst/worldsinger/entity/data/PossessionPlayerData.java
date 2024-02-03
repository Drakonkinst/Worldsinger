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
package io.github.drakonkinst.worldsinger.entity.data;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.component.PossessionComponent;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.mixin.accessor.PlayerEntityInvoker;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class PossessionPlayerData implements PossessionComponent {

    private final PlayerEntity player;
    private CameraPossessable possessionTarget;
    private boolean shouldResetCamera = false;

    public PossessionPlayerData(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void setPossessionTarget(CameraPossessable entity) {
        if (!entity.equals(possessionTarget)) {
            entity.onStartPossessing(player);
        }
        this.possessionTarget = entity;
        // Send packet to client
        if (player instanceof ServerPlayerEntity serverPlayerEntity) {
            ServerPlayNetworking.send(serverPlayerEntity, CameraPossessable.POSSESS_SET_PACKET_ID,
                    CameraPossessable.createSetPacket(possessionTarget));
        }
    }

    @Override
    public void resetPossessionTarget() {
        if (possessionTarget != null) {
            possessionTarget.onStopPossessing(player);
        }
        this.possessionTarget = null;

        // Send packet to client
        if (player instanceof ServerPlayerEntity serverPlayerEntity) {
            ServerPlayNetworking.send(serverPlayerEntity, CameraPossessable.POSSESS_SET_PACKET_ID,
                    CameraPossessable.createSetPacket(null));
        } else if (player.getWorld().isClient()) {
            // Since this method can be called from events, need to defer camera changes to the render thread
            shouldResetCamera = true;
        }
    }

    @Override
    @Nullable
    public CameraPossessable getPossessionTarget() {
        return possessionTarget;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        // Intentionally empty
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        // Intentionally empty
    }

    @Override
    public void clientTick() {
        // Force the camera to follow the possessed entity
        if (possessionTarget != null && !possessionTarget.toEntity().isRemoved()) {
            Worldsinger.PROXY.setRenderViewEntity(possessionTarget.toEntity());
        }

        // Reset the camera
        if (shouldResetCamera) {
            Worldsinger.PROXY.resetRenderViewEntity();
            shouldResetCamera = false;
        }
    }

    @Override
    public void serverTick() {
        if (!isPossessing() || possessionTarget == null) {
            return;
        }
        LivingEntity possessedEntity = possessionTarget.toEntity();

        // Look at possession target
        if (possessionTarget.shouldPossessorLookAt()) {
            player.lookAt(EntityAnchor.EYES, possessedEntity.getPos());
        }

        // Reset if conditions are not met
        if (possessedEntity.isDead() || doesPlayerWantToExit() || !isInRange(possessedEntity)
                || !possessionTarget.shouldKeepPossessing(player)) {
            resetPossessionTarget();
        }
    }

    private boolean doesPlayerWantToExit() {
        return ((PlayerEntityInvoker) player).worldsinger$shouldDismount();
    }

    private boolean isInRange(LivingEntity entity) {
        final float maxPossessionDistance = possessionTarget.getMaxPossessionDistance();
        return entity.squaredDistanceTo(player) <= maxPossessionDistance * maxPossessionDistance;
    }

    @Override
    public void tick() {

    }
}
