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
package io.github.drakonkinst.worldsinger.mixin.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.PossessionManager;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Makes the server send packets to the player as if they were in the possessed entity's location
// This helps fix sounds, etc. that they wouldn't normally hear.
@SuppressWarnings("UnstableApiUsage")
@Mixin(PlayerManager.class)
public abstract class PlayerManagerPossessionMixin {

    @WrapOperation(method = "sendToAround", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getX()D"))
    private double modifyXCoordinateIfPossessing(ServerPlayerEntity instance,
            Operation<Double> original) {
        PossessionManager possessionManager = instance.getAttached(ModAttachmentTypes.POSSESSION);
        if (possessionManager != null) {
            CameraPossessable possessionTarget = possessionManager.getPossessionTarget();
            if (possessionTarget != null && possessionTarget.redirectWorldEvents()) {
                return possessionTarget.toEntity().getX();
            }
        }
        return original.call(instance);
    }

    @WrapOperation(method = "sendToAround", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getY()D"))
    private double modifyYCoordinateIfPossessing(ServerPlayerEntity instance,
            Operation<Double> original) {
        PossessionManager possessionManager = instance.getAttached(ModAttachmentTypes.POSSESSION);
        if (possessionManager != null) {
            CameraPossessable possessionTarget = possessionManager.getPossessionTarget();
            if (possessionTarget != null && possessionTarget.redirectWorldEvents()) {
                return possessionTarget.toEntity().getY();
            }
        }
        return original.call(instance);
    }

    @WrapOperation(method = "sendToAround", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getZ()D"))
    private double modifyZCoordinateIfPossessing(ServerPlayerEntity instance,
            Operation<Double> original) {
        PossessionManager possessionManager = instance.getAttached(ModAttachmentTypes.POSSESSION);
        if (possessionManager != null) {
            CameraPossessable possessionTarget = possessionManager.getPossessionTarget();
            if (possessionTarget != null && possessionTarget.redirectWorldEvents()) {
                return possessionTarget.toEntity().getZ();
            }
        }
        return original.call(instance);
    }
}
