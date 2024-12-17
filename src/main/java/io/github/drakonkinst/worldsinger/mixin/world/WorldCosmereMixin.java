/*
 * MIT License
 *
 * Copyright (c) 2024 Drakonkinst
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
import io.github.drakonkinst.worldsinger.cosmere.CosmereWorldAccess;
import io.github.drakonkinst.worldsinger.cosmere.CosmereWorldData;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldCosmereMixin implements WorldAccess, AutoCloseable, CosmereWorldAccess {

    @Shadow
    @Final
    protected MutableWorldProperties properties;

    @Shadow
    public abstract DimensionType getDimension();

    @Unique
    protected CosmerePlanet planet;

    @Unique
    protected CosmereWorldData cosmereWorldData;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeCosmereWorld(MutableWorldProperties properties,
            RegistryKey<World> registryRef, DynamicRegistryManager registryManager,
            RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld,
            long seed, int maxChainedNeighborUpdates, CallbackInfo ci) {
        planet = CosmerePlanet.getPlanetFromKey(registryRef);
        cosmereWorldData = new CosmereWorldData();
    }

    @Override
    public CosmerePlanet worldsinger$getPlanet() {
        return planet;
    }

    @Override
    public CosmereWorldData worldsinger$getCosmereWorldData() {
        return cosmereWorldData;
    }

    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void getCosmereTimeOfDay(CallbackInfoReturnable<Long> cir) {
        if (CosmerePlanet.isCosmerePlanet((World) (Object) this)) {
            cir.setReturnValue(cosmereWorldData.getTimeOfDay());
        }
    }

    @Inject(method = "getSpawnPos", at = @At("HEAD"), cancellable = true)
    private void getCosmereSpawnPos(CallbackInfoReturnable<BlockPos> cir) {
        if (CosmerePlanet.isCosmerePlanet((World) (Object) this)) {
            BlockPos spawnPos = cosmereWorldData.getSpawnPos();
            if (spawnPos != null) {
                cir.setReturnValue(spawnPos);
            }
        }
    }

    @Override
    public long getLunarTime() {
        if (CosmerePlanet.isCosmerePlanet((World) (Object) this)) {
            return cosmereWorldData.getTimeOfDay();
        }
        return WorldAccess.super.getLunarTime();
    }

    @Override
    public float getSkyAngle(float deltaTime) {
        CosmerePlanet planet = CosmerePlanet.getPlanet((World) (Object) this);
        if (this.getDimension().hasFixedTime() || planet == CosmerePlanet.NONE) {
            return WorldAccess.super.getSkyAngle(deltaTime);
        }
        float fractionalPart = MathHelper.fractionalPart(
                cosmereWorldData.getTimeOfDay() * 1.0f / planet.getDayLength() - 0.25f);
        float offset = 0.5f - MathHelper.cos(fractionalPart * MathHelper.PI) * 0.5f;
        return (fractionalPart * 2.0f + offset) / 3.0f;
    }
}
