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
import io.github.drakonkinst.worldsinger.api.ClientLunagreeData;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarLunagreeGenerator;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarSeetheManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeGenerator;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeLocation;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RainlineSpawner;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleManager;
import io.github.drakonkinst.worldsinger.mixin.world.WorldLumarMixin;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.ClientWorld.Properties;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldLumarMixin extends WorldLumarMixin {

    @Shadow
    public abstract void addParticle(ParticleEffect parameters, double x, double y, double z,
            double velocityX, double velocityY, double velocityZ);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initialize(ClientPlayNetworkHandler networkHandler, Properties properties,
            RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimensionType,
            int loadDistance, int simulationDistance, WorldRenderer worldRenderer,
            boolean debugWorld, long seed, int seaLevel, CallbackInfo ci) {
        if (CosmerePlanet.getPlanetFromKey(registryRef).equals(CosmerePlanet.LUMAR)) {
            lumarManager = new LumarManager(new LumarSeetheManager(), LunagreeGenerator.NULL,
                    RainlineSpawner.NULL);
        }
    }

    @Inject(method = "randomBlockDisplayTick", at = @At("TAIL"))
    private void addLumarLunagreeParticles(int centerX, int centerY, int centerZ, int radius,
            Random random, Block block, Mutable pos, CallbackInfo ci,
            @Local BlockState blockState) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ClientWorld world = (ClientWorld) (Object) this;
        // Only occurs on Lumar
        if (player == null || !CosmerePlanet.isLumar(world)) {
            return;
        }

        ClientLunagreeData data = ClientLunagreeData.get(world);
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
                LumarLunagreeGenerator.SPORE_FALL_RADIUS);
        if (location == null) {
            return;
        }

        double spawnX = (double) x + random.nextDouble();
        double spawnY = (double) y + 1.0 + random.nextDouble();
        double spawnZ = (double) z + random.nextDouble();
        SporeParticleManager.addClientDisplayParticle(world,
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
