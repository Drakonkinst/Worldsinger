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
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManager;
import io.github.drakonkinst.worldsinger.network.packet.CosmereTimeUpdatePayload;
import io.github.drakonkinst.worldsinger.worldgen.CosmereGeneration;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensions;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    private PlayerManager playerManager;

    @Shadow
    public abstract @Nullable ServerWorld getWorld(RegistryKey<World> key);

    @Inject(method = "sendTimeUpdatePackets(Lnet/minecraft/server/world/ServerWorld;)V", at = @At("RETURN"))
    private void sendCosmereTimeUpdatePackets(ServerWorld world, CallbackInfo ci) {
        CosmerePlanet planet = CosmerePlanet.getPlanet(world);
        if (planet != CosmerePlanet.NONE) {
            this.playerManager.sendToDimension(new CustomPayloadS2CPacket(
                            new CosmereTimeUpdatePayload(planet, world.getTime(), world.getTimeOfDay(),
                                    world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE))),
                    world.getRegistryKey());
        }
    }

    @Inject(method = "prepareStartRegion", at = @At(value = "HEAD"))
    private void prepareCosmereWorld(
            WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        ServerWorld lumarWorld = this.getWorld(ModDimensions.WORLD_LUMAR);
        // Make sure that a starting pos is always generated, regardless of starting dimension
        LumarManager.generateOrFetchStartingPos(lumarWorld);
    }

    @WrapOperation(method = "prepareStartRegion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getSpawnPos()Lnet/minecraft/util/math/BlockPos;"))
    private BlockPos calculateLumarSpawn(ServerWorld instance, Operation<BlockPos> original) {
        if (instance.getRegistryKey() == ModDimensions.WORLD_LUMAR) {
            return LumarManager.generateOrFetchStartingPos(instance);
        }
        return original.call(instance);
    }

    @WrapOperation(method = "createWorlds", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setupSpawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/level/ServerWorldProperties;ZZ)V"))
    private void createBonusChestsForAllWorlds(ServerWorld world,
            ServerWorldProperties worldProperties, boolean bonusChest, boolean debugWorld,
            Operation<Void> original) {
        // Overworld
        original.call(world, worldProperties, bonusChest, debugWorld);
        if (bonusChest) {
            // Lumar
            CosmereGeneration.generateBonusChest((MinecraftServer) (Object) this,
                    ModDimensions.WORLD_LUMAR, worldProperties.getSpawnPos());
        }
    }
}
