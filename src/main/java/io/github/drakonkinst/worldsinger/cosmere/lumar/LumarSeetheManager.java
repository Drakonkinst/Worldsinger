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

import io.github.drakonkinst.worldsinger.util.ModConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.intprovider.BiasedToBottomIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.PersistentState;

public class LumarSeetheManager extends PersistentState implements SeetheManager {

    public static final String NAME = "seethe";
    private static final String NBT_TICKS_REMAINING = "ticks_remaining";
    private static final String NBT_CYCLES_UNTIL_NEXT_LONG_STILLING = "cycles_until_next_long_stilling";
    private static final String NBT_IS_SEETHING = "is_seething";

    private static final IntProvider SEETHE_DURATION_PROVIDER = UniformIntProvider.create(
            5 * ModConstants.MINUTES_TO_TICKS, 2 * ModConstants.GAME_DAYS_TO_TICKS);
    private static final IntProvider STILLING_NORMAL_DURATION_PROVIDER = UniformIntProvider.create(
            ModConstants.MINUTES_TO_TICKS, 3 * ModConstants.MINUTES_TO_TICKS);
    private static final IntProvider STILLING_LONG_DURATION_PROVIDER = BiasedToBottomIntProvider.create(
            3 * ModConstants.MINUTES_TO_TICKS, 5 * ModConstants.MINUTES_TO_TICKS);
    private static final IntProvider STILLING_LONG_CYCLE_PROVIDER = BiasedToBottomIntProvider.create(
            3, 5);

    public static PersistentState.Type<LumarSeetheManager> getPersistentStateType() {
        return new PersistentState.Type<>(LumarSeetheManager::new,
                (nbt, registryLookup) -> LumarSeetheManager.fromNbt(nbt), DataFixTypes.LEVEL);
    }

    private static LumarSeetheManager fromNbt(NbtCompound nbt) {
        LumarSeetheManager seetheManager = new LumarSeetheManager();
        seetheManager.isSeething = nbt.getBoolean(NBT_IS_SEETHING);
        seetheManager.ticksRemaining = nbt.getInt(NBT_TICKS_REMAINING);
        seetheManager.cyclesUntilLongStilling = nbt.getInt(NBT_CYCLES_UNTIL_NEXT_LONG_STILLING);
        return seetheManager;
    }

    private final Random random = Random.create();
    private boolean isSeething;
    private int ticksRemaining;
    private int cyclesUntilLongStilling;

    public LumarSeetheManager() {
        // Default values, can be overridden by saved data
        this.startSeetheForRandomDuration();
        this.cyclesUntilLongStilling = STILLING_LONG_CYCLE_PROVIDER.get(this.random);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        nbt.putBoolean(NBT_IS_SEETHING, isSeething);
        nbt.putInt(NBT_TICKS_REMAINING, ticksRemaining);
        nbt.putInt(NBT_CYCLES_UNTIL_NEXT_LONG_STILLING, cyclesUntilLongStilling);
        return nbt;
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
}
