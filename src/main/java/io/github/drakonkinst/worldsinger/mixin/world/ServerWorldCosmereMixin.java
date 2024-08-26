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
import io.github.drakonkinst.worldsinger.cosmere.CosmereWorldData;
import java.util.List;
import java.util.concurrent.Executor;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage.Session;
import net.minecraft.world.spawner.SpecialSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldCosmereMixin extends WorldCosmereMixin {

    @Shadow
    public abstract PersistentStateManager getPersistentStateManager();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeCosmereData(MinecraftServer server, Executor workerExecutor,
            Session session, ServerWorldProperties properties, RegistryKey<World> worldKey,
            DimensionOptions dimensionOptions,
            WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld,
            long seed, List<SpecialSpawner> spawners, boolean shouldTickTime,
            RandomSequencesState randomSequencesState, CallbackInfo ci) {
        if (CosmerePlanet.getPlanetFromKey(worldKey) != CosmerePlanet.NONE) {
            cosmereWorldData = this.getPersistentStateManager()
                    .getOrCreate(CosmereWorldData.getPersistentStateType(), CosmereWorldData.NAME);
        }
    }

    @ModifyConstant(method = "tick", constant = @Constant(longValue = 24000L), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/SleepManager;canSkipNight(I)Z"), to = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;calculateAmbientDarkness()V")))
    private long fixSleepSkipLength(long constant) {
        if (CosmerePlanet.isCosmerePlanet((World) (Object) this)) {
            return this.worldsinger$getPlanet().getDayLength();
        }
        return constant;
    }

    @Inject(method = "setTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void setCosmereTimeOfDay(long timeOfDay, CallbackInfo ci) {
        if (CosmerePlanet.isCosmerePlanet((World) (Object) this)) {
            cosmereWorldData.setTimeOfDay(timeOfDay);
            cosmereWorldData.markDirty();
            ci.cancel();
        }
    }

    @Inject(method = "tickTime", at = @At("RETURN"))
    private void tickCosmereTime(CallbackInfo ci) {
        if (this.properties.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)
                && CosmerePlanet.isCosmerePlanet((World) (Object) this)) {
            cosmereWorldData.setTimeOfDay(cosmereWorldData.getTimeOfDay() + 1L);
            cosmereWorldData.markDirty();
        }
    }
}
