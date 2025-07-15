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

package io.github.drakonkinst.worldsinger.mixin.command;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.CosmereWorldAccess;
import io.github.drakonkinst.worldsinger.cosmere.CosmereWorldData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.SetWorldSpawnCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SetWorldSpawnCommand.class)
public abstract class SetWorldSpawnCommandMixin {

    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;sendError(Lnet/minecraft/text/Text;)V"), cancellable = true)
    private static void setCosmereWorldSpawnPos(ServerCommandSource source, BlockPos pos,
            float angle, CallbackInfoReturnable<Integer> cir) {
        ServerWorld world = source.getWorld();
        if (!CosmerePlanet.isCosmerePlanet(world)) {
            return;
        }
        CosmereWorldData worldAccess = ((CosmereWorldAccess) world).worldsinger$getCosmereWorldData();
        Worldsinger.LOGGER.info("Setting spawn point for cosmere world");
        worldAccess.setSpawnPos(pos);
        worldAccess.setSpawnAngle(angle);
        source.sendFeedback(
                () -> Text.translatable("commands.setworldspawn.success", pos.getX(), pos.getY(),
                        pos.getZ(), angle), true);
        cir.setReturnValue(1);
    }
}
