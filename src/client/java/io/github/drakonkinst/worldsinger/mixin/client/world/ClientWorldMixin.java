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

import com.llamalad7.mixinextras.sugar.Local;
import io.github.drakonkinst.worldsinger.cosmere.ClientLunagreeData;
import io.github.drakonkinst.worldsinger.cosmere.CosmereWorldUtil;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarLunagreeManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeLocation;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleManager;
import io.github.drakonkinst.worldsinger.entity.ClientLunagreeDataAccess;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

    @Shadow
    public abstract void addParticle(ParticleEffect parameters, double x, double y, double z,
            double velocityX, double velocityY, double velocityZ);

    protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef,
            DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry,
            Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess,
            int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient,
                debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(method = "randomBlockDisplayTick", at = @At("TAIL"))
    private void addLumarLunagreeParticles(int centerX, int centerY, int centerZ, int radius,
            Random random, Block block, Mutable pos, CallbackInfo ci,
            @Local BlockState blockState) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        // Only occurs on Lumar
        if (player == null || !CosmereWorldUtil.isLumar(this)) {
            return;
        }

        ClientLunagreeData data = ((ClientLunagreeDataAccess) player).worldsinger$getLunagreeData();
        // We want a larger radius than the biome particles if not under lunagree
        // But keep particle spawn rates proportional
        int radiusMultiplier = data.isUnderLunagree() ? ClientLunagreeData.SPORE_FALL_RADIUS_CLOSE
                : ClientLunagreeData.SPORE_FALL_RADIUS_FAR;
        // This divides the chance by 4, instead of 2 -- making the particles more sparse while within the lunagree
        double spawnChanceMultiplier =
                data.isUnderLunagree() ? (1.0 / ClientLunagreeData.SPORE_FALL_RADIUS_FAR) : 1.0;
        setRandomLocation(pos, centerX, centerY, centerZ, radius, radiusMultiplier);

        if (blockState.isFullCube(this, pos) || random.nextFloat()
                > ClientLunagreeData.SPORE_FALL_PARTICLE_CHANCE * spawnChanceMultiplier
                || !this.isSkyVisible(pos)) {
            return;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int topY = this.getTopY(Type.MOTION_BLOCKING, x, z);
        if (y <= topY) {
            return;
        }

        LunagreeLocation location = data.getNearestLunagreeLocation(x, z,
                LumarLunagreeManager.SPORE_FALL_RADIUS);
        if (location == null) {
            return;
        }

        double spawnX = (double) x + random.nextDouble();
        double spawnY = (double) y + 1.0 + random.nextDouble();
        double spawnZ = (double) z + random.nextDouble();
        SporeParticleManager.addClientDisplayParticle(this,
                AetherSpores.getAetherSporeTypeById(location.sporeId()), spawnX, spawnY, spawnZ,
                ClientLunagreeData.SPORE_FALL_PARTICLE_SIZE, false, random);
    }

    @Unique
    private void setRandomLocation(BlockPos.Mutable pos, int centerX, int centerY, int centerZ,
            int radius, int radiusMultiplier) {
        final int extendedRadius = radius * radiusMultiplier;
        pos.setX(centerX + random.nextInt(extendedRadius) - random.nextInt(extendedRadius));
        pos.setY(centerY + random.nextInt(extendedRadius) - random.nextInt(radius));
        pos.setZ(centerZ + random.nextInt(extendedRadius) - random.nextInt(extendedRadius));
    }
}
