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
package io.github.drakonkinst.worldsinger.mixin.client.network;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.freelook.FreeLook;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Unique
    private static final float CHANGE_LOOK_MULTIPLIER = 0.15f;
    @Unique
    private static final float MIN_PITCH = -90.0f;
    @Unique
    private static final float MAX_PITCH = 90.0f;

    @WrapWithCondition(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;setSelectedSlot(I)V"))
    private boolean preventHotbarSwitchWithScrollIfPossessing(PlayerInventory instance, int slot) {
        CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
        return possessedEntity == null || possessedEntity.canModifyInventory();
    }

    @WrapOperation(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
    private void changeFreeLookDirection(ClientPlayerEntity instance, double cursorDeltaX,
            double cursorDeltaY, Operation<Void> original) {
        FreeLook freeLookData = (FreeLook) instance;
        if (freeLookData.worldsinger$isFreeLookEnabled()) {
            // Set free look data instead
            float deltaYaw = (float) cursorDeltaX * CHANGE_LOOK_MULTIPLIER;
            float deltaPitch = (float) cursorDeltaY * CHANGE_LOOK_MULTIPLIER;
            freeLookData.worldsinger$setFreeLookYaw(
                    freeLookData.worldsinger$getFreeLookYaw() + deltaYaw);
            freeLookData.worldsinger$setFreeLookPitch(
                    MathHelper.clamp(freeLookData.worldsinger$getFreeLookPitch() + deltaPitch,
                            MIN_PITCH, MAX_PITCH));
        } else {
            original.call(instance, cursorDeltaX, cursorDeltaY);
            // Also sync free look, so it always starts from the current look direction when you
            // start free looking
            freeLookData.worldsinger$setFreeLookYaw(instance.getYaw());
            freeLookData.worldsinger$setFreeLookPitch(instance.getPitch());
        }

    }

}
