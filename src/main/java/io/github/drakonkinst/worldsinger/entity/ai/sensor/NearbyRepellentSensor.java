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

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.util.BrainUtils;

// Now unused, but keeping it around
public class NearbyRepellentSensor<E extends LivingEntity> extends PredicateSensor<BlockState, E> {

    private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(
            MemoryModuleType.NEAREST_REPELLENT);

    private int horizontalRange = 8;
    private int verticalRange = 4;

    public NearbyRepellentSensor<E> setHorizontalRange(int horizontalRange) {
        this.horizontalRange = horizontalRange;
        return this;
    }

    public NearbyRepellentSensor<E> setVerticalRange(int verticalRange) {
        this.verticalRange = verticalRange;
        return this;
    }

    @Override
    public NearbyRepellentSensor<E> setPredicate(BiPredicate<BlockState, E> predicate) {
        super.setPredicate(predicate);
        return this;
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return ModSensors.NEARBY_REPELLENT_SENSOR.get();
    }

    @Override
    protected void sense(ServerWorld world, E entity) {
        Brain<?> brain = entity.getBrain();
        BrainUtils.setMemory(brain, MemoryModuleType.NEAREST_REPELLENT,
                BlockPos.findClosest(entity.getBlockPos(), horizontalRange, verticalRange,
                        pos -> predicate().test(world.getBlockState(pos), entity)).orElse(null));
    }
}
