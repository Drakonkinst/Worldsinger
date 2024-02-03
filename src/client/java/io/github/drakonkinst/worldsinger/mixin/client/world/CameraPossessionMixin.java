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
package io.github.drakonkinst.worldsinger.mixin.client.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.freelook.FreeLook;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import io.github.drakonkinst.worldsinger.world.CameraPosAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraPossessionMixin implements CameraPosAccess {

    @Shadow
    private Entity focusedEntity;

    @Shadow
    private float lastCameraY;

    @Shadow
    private float cameraY;

    @Inject(method = "update", at = @At("HEAD"))
    private void moveCameraInstantly(BlockView area, Entity focusedEntity, boolean thirdPerson,
            boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (focusedEntity == null || this.focusedEntity == null || this.focusedEntity.equals(
                focusedEntity)) {
            return;
        }

        if (focusedEntity instanceof CameraPossessable
                || this.focusedEntity instanceof CameraPossessable) {
            this.cameraY = focusedEntity.getStandingEyeHeight();
            this.lastCameraY = this.cameraY;
        }
    }

    // Target the first use of setRotation()
    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"), slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V")))
    private void useFreeLookRotationIfPossessing(Camera instance, float yaw, float pitch,
            Operation<Void> original) {
        CameraPossessable possessionTarget = PossessionClientUtil.getPossessedEntity();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && possessionTarget != null && possessionTarget.canFreeLook()) {
            FreeLook freeLookData = (FreeLook) player;
            original.call(instance, freeLookData.worldsinger$getFreeLookYaw(),
                    freeLookData.worldsinger$getFreeLookPitch());
        } else if (focusedEntity instanceof FreeLook freeLookData
                && freeLookData.worldsinger$isFreeLookEnabled()) {
            original.call(instance, freeLookData.worldsinger$getFreeLookYaw(),
                    freeLookData.worldsinger$getFreeLookPitch());
        }
        original.call(instance, yaw, pitch);
    }
}
