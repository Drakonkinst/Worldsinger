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
package io.github.drakonkinst.worldsinger.entity.ai.sensor;

import java.util.Collections;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

// GenericAttackTargetSensor doesn't properly support custom predicates, so we're making our own
public class NearestAttackableSensor<E extends LivingEntity> extends
        PredicateSensor<LivingEntity, E> {

    @Override
    protected void sense(ServerWorld level, E entity) {
        BrainUtils.setMemory(entity, MemoryModuleType.NEAREST_ATTACKABLE, testForEntity(entity));
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return Collections.singletonList(MemoryModuleType.NEAREST_ATTACKABLE);
    }

    protected LivingEntity testForEntity(E entity) {
        LivingTargetCache matcher = BrainUtils.getMemory(entity, MemoryModuleType.VISIBLE_MOBS);

        if (matcher == null) {
            return null;
        }

        return findMatches(entity, matcher);
    }

    @Nullable
    protected LivingEntity findMatches(E entity, LivingTargetCache matcher) {
        return matcher.findFirst(target -> predicate().test(target, entity)).orElse(null);
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return SBLSensors.GENERIC_ATTACK_TARGET.get();
    }
}
