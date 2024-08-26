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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.CosmereWorldAccess;
import io.github.drakonkinst.worldsinger.cosmere.CosmereWorldData;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManagerAccess;
import io.github.drakonkinst.worldsinger.event.PlayerSyncCallback;
import io.github.drakonkinst.worldsinger.network.packet.CosmereTimeUpdatePayload;
import io.github.drakonkinst.worldsinger.network.packet.SeetheUpdatePayload;
import io.github.drakonkinst.worldsinger.registry.ModGameRules;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.network.ClientConnection;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "sendWorldInfo", at = @At("RETURN"))
    private void syncAdditionalWorldData(ServerPlayerEntity player, ServerWorld world,
            CallbackInfo ci) {
        CosmerePlanet planet = CosmerePlanet.getPlanet(world);
        if (planet == CosmerePlanet.LUMAR) {
            if (((LumarManagerAccess) world).worldsinger$getLumarManager()
                    .getSeetheManager()
                    .isSeething()) {
                ServerPlayNetworking.send(player, SeetheUpdatePayload.SEETHE_START);
            } else {
                ServerPlayNetworking.send(player, SeetheUpdatePayload.SEETHE_STOP);
            }
        }
        if (planet != CosmerePlanet.NONE) {
            ServerPlayNetworking.send(player,
                    new CosmereTimeUpdatePayload(planet, world.getTimeOfDay()));
        }
    }

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendStatusEffects(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    private void onPlayerLogIn(ClientConnection connection, ServerPlayerEntity player,
            ConnectedClientData clientData, CallbackInfo ci) {
        PlayerSyncCallback.EVENT.invoker().onPlayerSync(player);
    }

    @Inject(method = "sendPlayerStatus", at = @At("RETURN"))
    private void sendPlayerStatus(ServerPlayerEntity player, CallbackInfo info) {
        PlayerSyncCallback.EVENT.invoker().onPlayerSync(player);
    }

    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    private void respawnPlayer(ServerPlayerEntity player, boolean alive,
            RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        PlayerSyncCallback.EVENT.invoker().onPlayerSync(cir.getReturnValue());
    }

    @ModifyExpressionValue(method = "onPlayerConnect", at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/world/World;OVERWORLD:Lnet/minecraft/registry/RegistryKey;"))
    private RegistryKey<World> changeSpawnDimension(RegistryKey<World> original,
            ClientConnection connection, ServerPlayerEntity player,
            ConnectedClientData clientData) {
        boolean isNewPlayer =
                player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) == 0;
        if (isNewPlayer) {
            if (this.server.getGameRules().getBoolean(ModGameRules.START_ON_LUMAR)) {
                return ModDimensions.WORLD_LUMAR;
            }
        }
        return original;
    }

    @WrapOperation(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setServerWorld(Lnet/minecraft/server/world/ServerWorld;)V"))
    private void changeSpawnCoordinates(ServerPlayerEntity instance, ServerWorld world,
            Operation<Void> original) {
        original.call(instance, world);

        // After the function is called
        boolean isNewPlayer =
                instance.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME))
                        == 0;
        if (isNewPlayer) {
            if (this.server.getGameRules().getBoolean(ModGameRules.START_ON_LUMAR)) {
                CosmereWorldData cosmereWorldData = ((CosmereWorldAccess) world).worldsinger$getCosmereWorldData();
                BlockPos spawnPos = cosmereWorldData.getSpawnPos();
                if (spawnPos != null) {
                    Worldsinger.LOGGER.info("Teleporting new player to Lumar spawn at " + spawnPos);
                    // Does NOT use teleport() since we are moving the player before most player data is loaded
                    instance.refreshPositionAndAngles(spawnPos, 0, 0);
                }
            }
        }
    }

}
