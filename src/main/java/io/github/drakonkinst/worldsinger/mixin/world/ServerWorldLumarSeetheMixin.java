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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.drakonkinst.worldsinger.cosmere.lumar.ServerLumarSeetheManager;
import io.github.drakonkinst.worldsinger.network.packet.SeetheUpdatePayload;
import java.util.List;
import java.util.concurrent.Executor;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage.Session;
import net.minecraft.world.spawner.SpecialSpawner;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldLumarSeetheMixin extends WorldLumarMixin implements
        StructureWorldAccess {

    @Shadow
    public abstract PersistentStateManager getPersistentStateManager();

    @Shadow
    public abstract @NotNull MinecraftServer getServer();

    @Unique
    private boolean syncedSeething;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeLumar(MinecraftServer server, Executor workerExecutor, Session session,
            ServerWorldProperties properties, RegistryKey<World> worldKey,
            DimensionOptions dimensionOptions,
            WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld,
            long seed, List<SpecialSpawner> spawners, boolean shouldTickTime,
            RandomSequencesState randomSequencesState, CallbackInfo ci) {
        if (isLumar) {
            // Create the seethe manager using PersistentState instead
            seetheManager = this.getPersistentStateManager()
                    .getOrCreate(ServerLumarSeetheManager.getPersistentStateType(),
                            ServerLumarSeetheManager.NAME);
        }
    }

    @Inject(method = "tickWeather", at = @At("TAIL"))
    private void tickSeethe(CallbackInfo ci) {
        if (isLumar) {
            seetheManager.serverTick();

            // Sync seething
            // For now, we only care about whether the seethe state is changed
            boolean isSeething = seetheManager.isSeething();
            if (isSeething != syncedSeething) {
                syncedSeething = isSeething;
                CustomPayload payload = isSeething ? SeetheUpdatePayload.SEETHE_START
                        : SeetheUpdatePayload.SEETHE_STOP;
                this.getServer()
                        .getPlayerManager()
                        .sendToDimension(new CustomPayloadS2CPacket(payload),
                                this.getRegistryKey());
            }
        }
    }

    @ModifyExpressionValue(method = "tickWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean disableRainTickOnLumar(boolean original) {
        return original && !isLumar;
    }

}