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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarSeetheManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.NullSeetheManager;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensions;
import java.util.function.Supplier;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ServerWorld.class)
public abstract class ServerWorldLumarSeetheMixin extends WorldLumarMixin implements
        StructureWorldAccess {

    @Shadow
    public abstract PersistentStateManager getPersistentStateManager();

    @Override
    protected void worldsinger$initializeLumar(MutableWorldProperties properties,
            RegistryKey<World> registryRef, DynamicRegistryManager registryManager,
            RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler,
            boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates,
            CallbackInfo ci) {
        isLumar = registryRef.equals(ModDimensions.WORLD_LUMAR);
        if (isLumar) {
            // Create the seethe manager using PersistentState instead
            seetheManager = this.getPersistentStateManager()
                    .getOrCreate(LumarSeetheManager.getPersistentStateType(),
                            LumarSeetheManager.NAME);
        } else {
            seetheManager = new NullSeetheManager();
        }
    }

    @Inject(method = "tickWeather", at = @At("TAIL"))
    private void tickSeethe(CallbackInfo ci) {
        if (isLumar) {
            seetheManager.serverTick();
        }
    }

    @ModifyReturnValue(method = "tickWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean disableRainTickOnLumar(boolean original) {
        return original && !isLumar;
    }

}
