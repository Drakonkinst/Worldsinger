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

package io.github.drakonkinst.worldsinger.mixin.command;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.brigadier.context.CommandContext;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TimeCommand;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TimeCommand.class)
public abstract class TimeCommandMixin {

    // Only modify time for worlds that are on the same planet
    // Vanilla worlds are all CosmerePlanet.NONE, so they set time together
    // Each Cosmere planet is treated separately
    @WrapWithCondition(method = {
            "executeSet", "executeAdd"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
    private static boolean setTimePerCosmereWorld(ServerWorld instance, long timeOfDay,
            ServerCommandSource source, int time) {
        return CosmerePlanet.getPlanet(instance) == CosmerePlanet.getPlanet(source.getWorld());
    }

    @ModifyConstant(method = "method_13795", constant = @Constant(longValue = 24000L))
    private static long adjustDayCountForPlanet(long constant,
            CommandContext<ServerCommandSource> context) {
        return CosmerePlanet.getDayLengthOrDefault(context.getSource().getWorld(), constant);
    }

    @ModifyConstant(method = "method_13792", constant = @Constant(intValue = 1000))
    private static int adjustPresetTimeDay(int constant,
            CommandContext<ServerCommandSource> context) {
        return Math.round(
                constant * CosmerePlanet.getDayLengthMultiplier(context.getSource().getWorld()));
    }

    @ModifyConstant(method = "method_13794", constant = @Constant(intValue = 6000))
    private static int adjustPresetTimeNoon(int constant,
            CommandContext<ServerCommandSource> context) {
        return Math.round(
                constant * CosmerePlanet.getDayLengthMultiplier(context.getSource().getWorld()));
    }

    @ModifyConstant(method = "method_13797", constant = @Constant(intValue = 13000))
    private static int adjustPresetTimeNight(int constant,
            CommandContext<ServerCommandSource> context) {
        return Math.round(
                constant * CosmerePlanet.getDayLengthMultiplier(context.getSource().getWorld()));
    }

    @ModifyConstant(method = "method_13785", constant = @Constant(intValue = 18000))
    private static int adjustPresetTimeMidnight(int constant,
            CommandContext<ServerCommandSource> context) {
        return Math.round(
                constant * CosmerePlanet.getDayLengthMultiplier(context.getSource().getWorld()));
    }

    @ModifyConstant(method = "getDayTime", constant = @Constant(longValue = 24000L))
    private static long adjustRelativeDayTimeForPlanet(long constant, ServerWorld world) {
        return CosmerePlanet.getDayLengthOrDefault(world, constant);
    }
}
