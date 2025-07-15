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

package io.github.drakonkinst.worldsinger.cosmere.lumar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.util.math.intprovider.BiasedToBottomIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class LumarSeetheManager extends PersistentState implements SeetheManager {

    private static final String NAME = "seethe";
    public static final Codec<LumarSeetheManager> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(Codec.BOOL.optionalFieldOf("is_seething", true)
                                    .forGetter(LumarSeetheManager::isSeething),
                            Codec.INT.optionalFieldOf("ticks_remaining", 0)
                                    .forGetter(LumarSeetheManager::getTicksUntilNextCycle),
                            Codec.INT.optionalFieldOf("cycles_until_next_long_stilling", 0)
                                    .forGetter(LumarSeetheManager::getCyclesUntilLongStilling))
                    .apply(instance, LumarSeetheManager::new));
    public static final PersistentStateType<LumarSeetheManager> STATE_TYPE = new PersistentStateType<>(
            NAME, LumarSeetheManager::new, CODEC, DataFixTypes.LEVEL);

    private static final IntProvider SEETHE_DURATION_PROVIDER = UniformIntProvider.create(
            5 * ModConstants.MINUTES_TO_TICKS, 2 * ModConstants.GAME_DAYS_TO_TICKS);
    private static final IntProvider STILLING_NORMAL_DURATION_PROVIDER = UniformIntProvider.create(
            ModConstants.MINUTES_TO_TICKS, 3 * ModConstants.MINUTES_TO_TICKS);
    private static final IntProvider STILLING_LONG_DURATION_PROVIDER = BiasedToBottomIntProvider.create(
            3 * ModConstants.MINUTES_TO_TICKS, 5 * ModConstants.MINUTES_TO_TICKS);
    private static final IntProvider STILLING_LONG_CYCLE_PROVIDER = BiasedToBottomIntProvider.create(
            3, 5);

    private final Random random = Random.create();
    private boolean isSeething;
    private int ticksRemaining;
    private int cyclesUntilLongStilling;

    public LumarSeetheManager(boolean isSeething, int ticksRemaining, int cyclesUntilLongStilling) {
        this.isSeething = isSeething;
        this.ticksRemaining = ticksRemaining;
        this.cyclesUntilLongStilling = cyclesUntilLongStilling;
    }

    public LumarSeetheManager() {
        // Default values, can be overridden by saved data
        this.startSeetheForRandomDuration();
        this.cyclesUntilLongStilling = STILLING_LONG_CYCLE_PROVIDER.get(this.random);
    }

    @Override
    public void startSeethe(int ticks) {
        isSeething = true;
        ticksRemaining = ticks;
    }

    @Override
    public void startSeetheForRandomDuration() {
        startSeethe(SEETHE_DURATION_PROVIDER.get(this.random));
    }

    @Override
    public void stopSeethe(int ticks) {
        isSeething = false;
        ticksRemaining = ticks;
        if (cyclesUntilLongStilling <= 0) {
            cyclesUntilLongStilling = STILLING_LONG_CYCLE_PROVIDER.get(this.random);
        } else {
            --cyclesUntilLongStilling;
        }
    }

    @Override
    public void stopSeetheForRandomDuration() {
        int ticks;
        if (cyclesUntilLongStilling <= 0) {
            ticks = STILLING_LONG_DURATION_PROVIDER.get(this.random);
        } else {
            ticks = STILLING_NORMAL_DURATION_PROVIDER.get(this.random);
        }
        stopSeethe(ticks);
    }

    @Override
    public void serverTickWeather() {
        if (ticksRemaining > 0) {
            --ticksRemaining;
        } else {
            if (isSeething) {
                this.stopSeetheForRandomDuration();
            } else {
                this.startSeetheForRandomDuration();
            }
        }
        this.markDirty();
    }

    @Override
    public boolean isSeething() {
        return isSeething;
    }

    @Override
    public int getTicksUntilNextCycle() {
        return ticksRemaining;
    }

    public int getCyclesUntilLongStilling() {
        return cyclesUntilLongStilling;
    }
}
