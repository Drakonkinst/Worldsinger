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
package io.github.drakonkinst.worldsinger.mixin.entity.ai;

import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/*
 * Improves MeleeAttackGoal class to fix https://bugs.mojang.com/browse/MC-198068
 * This fixes the bug where some mobs, especially neutral mobs, tend to lose their pathfinding
 * target or stop attacking at random points or when hit.
 * This doesn't fix all the issues, and warrants further investigation, but seems to improve things
 * a bit.
 */
@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalFixMixin {

    // No benefit to using WrapOperation() here since we are not using the original value
    @Redirect(method = "canStart", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ai/goal/MeleeAttackGoal;lastUpdateTime:J", opcode = Opcodes.GETFIELD))
    private long removeLastCanUseCheck(MeleeAttackGoal instance) {
        // Long overflow happens if you use Long.MIN_VALUE, so this is more than enough
        return Integer.MIN_VALUE;
    }

    // No benefit to using WrapOperation() here since we are not using the original value
    @Redirect(method = "start", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ai/goal/MeleeAttackGoal;updateCountdownTicks:I", opcode = Opcodes.PUTFIELD))
    private void removeResetUpdateCountdownTicks(MeleeAttackGoal instance, int value) {
        // Do nothing
    }

    // No benefit to using WrapOperation() here since we are not using the original value
    @Redirect(method = "shouldContinue", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/ai/goal/MeleeAttackGoal;pauseWhenMobIdle:Z", opcode = Opcodes.GETFIELD))
    private boolean removePauseWhenMobIdleCheck(MeleeAttackGoal instance) {
        return true;
    }
}
