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

import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldCosmereMixin extends WorldCosmereMixin {

    @Inject(method = "setTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void setCosmereTimeOfDay(long timeOfDay, CallbackInfo ci) {
        if (CosmerePlanet.isCosmerePlanet((World) (Object) this)) {
            cosmereWorldData.setTimeOfDay(timeOfDay);
            ci.cancel();
        }
    }

    // TODO: Also need to fix sleeping
    @Inject(method = "tickTime", at = @At("RETURN"))
    private void tickCosmereTime(CallbackInfo ci) {
        if (this.properties.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)
                && CosmerePlanet.isCosmerePlanet((World) (Object) this)) {
            cosmereWorldData.setTimeOfDay(cosmereWorldData.getTimeOfDay() + 1L);
        }
    }
}
