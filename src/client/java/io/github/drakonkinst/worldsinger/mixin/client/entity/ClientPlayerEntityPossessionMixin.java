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
package io.github.drakonkinst.worldsinger.mixin.client.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.PossessionManager;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable.AttackOrigin;
import io.github.drakonkinst.worldsinger.entity.PossessionClientUtil;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityPossessionMixin extends AbstractClientPlayerEntity {

    public ClientPlayerEntityPossessionMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V"))
    private void tickClient(CallbackInfo ci) {
        PossessionManager possessionManager = this.getAttached(ModAttachmentTypes.POSSESSION);
        if (possessionManager != null) {
            possessionManager.clientTick();
        }
    }

    // Player drifts on the client side when they start possessing, since in vanilla cases if
    // the camera entity is not the player, the player doesn't exist in the world
    // But since it does for our purposes, we need to stop their movement.
    @Inject(method = "tickNewAi", at = @At("TAIL"))
    private void stopMovementInputWhenStartPossessing(CallbackInfo ci) {
        // This is a better check than isCamera() because it doesn't interfere with other
        // uses of camera, and also if this is true then the camera should be set properly anyway
        PossessionManager possessionManager = this.getAttached(ModAttachmentTypes.POSSESSION);
        if (possessionManager != null && possessionManager.isPossessing()) {
            this.sidewaysSpeed = 0.0f;
            this.forwardSpeed = 0.0f;
            this.jumping = false;
        }
    }

    @ModifyReturnValue(method = "canStartSprinting", at = @At("RETURN"))
    private boolean preventSprintingIfPossessing(boolean original) {
        CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
        return original && !(possessedEntity != null && !possessedEntity.canMoveSelf());
    }

    @Inject(method = "swingHand", at = @At("HEAD"), cancellable = true)
    private void preventSwingHandIfPossessing(Hand hand, CallbackInfo ci) {
        CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
        if (possessedEntity != null
                && possessedEntity.getEntityAttackOrigin() != AttackOrigin.POSSESSOR) {
            ci.cancel();
        }
    }
}
