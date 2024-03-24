/*
 * MIT License
 *
 * Copyright (c) 2019-2023 Ladysnake
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
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManagerAccess;
import io.github.drakonkinst.worldsinger.event.PlayerSyncCallback;
import io.github.drakonkinst.worldsinger.network.packet.CosmereTimeUpdatePayload;
import io.github.drakonkinst.worldsinger.network.packet.SeetheUpdatePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

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

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getStatusEffects()Ljava/util/Collection;"))
    private void onPlayerLogIn(ClientConnection connection, ServerPlayerEntity player,
            ConnectedClientData clientData, CallbackInfo ci) {
        PlayerSyncCallback.EVENT.invoker().onPlayerSync(player);
    }

    @Inject(method = "sendPlayerStatus", at = @At("RETURN"))
    private void sendPlayerStatus(ServerPlayerEntity player, CallbackInfo info) {
        PlayerSyncCallback.EVENT.invoker().onPlayerSync(player);
    }

    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    private void respawnPlayer(ServerPlayerEntity player, boolean end,
            CallbackInfoReturnable<ServerPlayerEntity> cir) {
        PlayerSyncCallback.EVENT.invoker().onPlayerSync(cir.getReturnValue());
    }
}
