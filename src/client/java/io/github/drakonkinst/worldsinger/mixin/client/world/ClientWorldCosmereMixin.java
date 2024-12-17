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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RainlineManager;
import io.github.drakonkinst.worldsinger.mixin.world.WorldCosmereMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.ClientWorld.Properties;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientWorld.class)
public abstract class ClientWorldCosmereMixin extends WorldCosmereMixin {

    @WrapOperation(method = "setTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld$Properties;setTimeOfDay(J)V"))
    private void setCosmereTimeOfDay(Properties instance, long timeOfDay,
            Operation<Void> original) {
        if (CosmerePlanet.isCosmerePlanet((ClientWorld) (Object) this)) {
            cosmereWorldData.setTimeOfDay(timeOfDay);
        } else {
            original.call(instance, timeOfDay);
        }
    }

    @WrapOperation(method = "tickTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld$Properties;getTimeOfDay()J"))
    private long tickCosmereTime(Properties instance, Operation<Long> original) {
        if (CosmerePlanet.isCosmerePlanet((World) (Object) this)) {
            return cosmereWorldData.getTimeOfDay();
        }
        return original.call(instance);
    }

    @ModifyExpressionValue(method = "getSkyBrightness", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"))
    private float renderRainlines1(float original) {
        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
        if (cameraEntity == null) {
            return original;
        }
        return Math.max(original, RainlineManager.getRainlineGradient((ClientWorld) (Object) this,
                cameraEntity.getPos(), true));
    }

    @ModifyExpressionValue(method = "getSkyColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"))
    private float renderRainlines2(float original, Vec3d cameraPos, float tickDelta) {
        return Math.max(original,
                RainlineManager.getRainlineGradient((ClientWorld) (Object) this, cameraPos, true));
    }

    @ModifyExpressionValue(method = "getCloudsColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"))
    private float renderRainlines3(float original) {
        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
        if (cameraEntity == null) {
            return original;
        }
        return Math.max(original, RainlineManager.getRainlineGradient((ClientWorld) (Object) this,
                cameraEntity.getPos(), true));
    }
}
