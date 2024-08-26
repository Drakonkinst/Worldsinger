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
import io.github.drakonkinst.worldsinger.registry.ModGameRules;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensions;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    private PlayerManager playerManager;

    @Inject(method = "sendTimeUpdatePackets(Lnet/minecraft/server/world/ServerWorld;)V", at = @At("RETURN"))
    private void sendCosmereTimeUpdatePackets(ServerWorld world, CallbackInfo ci) {
        CosmerePlanet planet = CosmerePlanet.getPlanet(world);
        if (planet != CosmerePlanet.NONE) {
            this.playerManager.sendToDimension(new CustomPayloadS2CPacket(
                            new CosmereTimeUpdatePayload(planet, world.getTimeOfDay())),
                    world.getRegistryKey());
        }
    }

    @WrapOperation(method = "prepareStartRegion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getOverworld()Lnet/minecraft/server/world/ServerWorld;"))
    private ServerWorld prepareCosmereWorld(MinecraftServer instance,
            Operation<ServerWorld> original) {
        if (instance.getGameRules().getBoolean(ModGameRules.START_ON_LUMAR)) {
            return instance.getWorld(ModDimensions.WORLD_LUMAR);
        }
        return original.call(instance);
    }

    @WrapOperation(method = "prepareStartRegion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getSpawnPos()Lnet/minecraft/util/math/BlockPos;"))
    private BlockPos calculateLumarSpawn(ServerWorld instance, Operation<BlockPos> original) {
        if (instance.getRegistryKey() == ModDimensions.WORLD_LUMAR) {
            return LumarManager.generateOrFetchStartingPos(instance);
        }
        return original.call(instance);
    }
}
