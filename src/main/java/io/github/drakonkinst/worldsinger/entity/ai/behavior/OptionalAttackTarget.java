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
package io.github.drakonkinst.worldsinger.entity.ai.behavior;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetAttackTarget;
import net.tslat.smartbrainlib.util.BrainUtils;

// AttackTarget behavior that fails if no valid target is found.
// Useful for FirstApplicableBehavior, as it allows for pass-through.
// For optimization, caches the target when checking. Since this function is a prerequisite,
// there should be no change in behavior.
public class OptionalAttackTarget<E extends LivingEntity> extends SetAttackTarget<E> {

    private LivingEntity cachedTarget;

    public OptionalAttackTarget(boolean usingNearestAttackable) {
        super(usingNearestAttackable);
    }

    @Override
    protected boolean shouldRun(ServerWorld level, E entity) {
        cachedTarget = this.targetFinder.apply(entity);
        return super.shouldRun(level, entity) && cachedTarget != null;
    }

    @Override
    protected void start(E entity) {
        if (cachedTarget != null) {
            BrainUtils.setMemory(entity, MemoryModuleType.ATTACK_TARGET, cachedTarget);
            BrainUtils.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        }
    }
}
