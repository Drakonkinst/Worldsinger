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

import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.component.SeetheComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.intprovider.BiasedToBottomIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LumarSeethe implements SeetheComponent {

    private static final String NBT_TICKS_REMAINING = "ticksRemaining";
    private static final String NBT_CYCLES_UNTIL_NEXT_LONG_STILLING = "cyclesUntilNextLongStilling";
    private static final String NBT_IS_SEETHING = "isSeething";

    private static final int SECONDS_TO_TICKS = 20;
    private static final int MINUTES_TO_SECONDS = 60;
    private static final int GAME_DAYS_TO_MINUTES = 20;
    private static final int MINUTES_TO_TICKS = MINUTES_TO_SECONDS * SECONDS_TO_TICKS;
    private static final int GAME_DAYS_TO_TICKS = GAME_DAYS_TO_MINUTES * MINUTES_TO_TICKS;
    private static final IntProvider SEETHE_DURATION_PROVIDER = UniformIntProvider.create(
            5 * MINUTES_TO_TICKS, 2 * GAME_DAYS_TO_TICKS);
    private static final IntProvider STILLING_NORMAL_DURATION_PROVIDER = UniformIntProvider.create(
            2 * MINUTES_TO_TICKS, 5 * MINUTES_TO_TICKS);
    private static final IntProvider STILLING_LONG_DURATION_PROVIDER = BiasedToBottomIntProvider.create(
            10 * MINUTES_TO_TICKS, 30 * MINUTES_TO_TICKS);
    private static final IntProvider STILLING_LONG_CYCLE_PROVIDER = BiasedToBottomIntProvider.create(
            2, 5);

    public static boolean areSporesFluidized(World world) {
        return LumarSeethe.areSporesFluidized(world.getScoreboard());
    }

    public static boolean areSporesFluidized(Scoreboard scoreboard) {
        return ModComponents.LUMAR_SEETHE.get(scoreboard).isSeething();
    }

    private final Random random = Random.create();
    private final Object provider;
    private boolean isSeething;
    private int ticksRemaining;
    private int cyclesUntilLongStilling;

    public LumarSeethe(Scoreboard scoreboard, @Nullable MinecraftServer server) {
        this.provider = scoreboard;

        // Default values
        this.startSeethe();
        this.cyclesUntilLongStilling = STILLING_LONG_CYCLE_PROVIDER.get(this.random);
    }

    @Override
    public void startSeethe() {
        startSeethe(SEETHE_DURATION_PROVIDER.get(this.random));
    }

    @Override
    public void startSeethe(int ticks) {
        isSeething = true;
        ticksRemaining = ticks;
    }

    @Override
    public void serverTick() {
        if (ticksRemaining > 0) {
            --ticksRemaining;
        } else {
            if (isSeething) {
                this.stopSeethe();
            } else {
                this.startSeethe();
            }
        }
        ModComponents.LUMAR_SEETHE.sync(this.provider);
    }

    @Override
    public void stopSeethe() {
        int stillingTime;
        if (cyclesUntilLongStilling <= 0) {
            stillingTime = STILLING_LONG_DURATION_PROVIDER.get(this.random);
        } else {
            stillingTime = STILLING_NORMAL_DURATION_PROVIDER.get(this.random);
        }
        stopSeethe(stillingTime);
    }

    @Override
    public void stopSeethe(int ticks) {
        isSeething = false;
        if (cyclesUntilLongStilling <= 0) {
            cyclesUntilLongStilling = STILLING_LONG_CYCLE_PROVIDER.get(this.random);
        } else {
            --cyclesUntilLongStilling;
        }
        ticksRemaining = ticks;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        isSeething = tag.getBoolean(NBT_IS_SEETHING);
        ticksRemaining = tag.getInt(NBT_TICKS_REMAINING);
        cyclesUntilLongStilling = tag.getInt(NBT_CYCLES_UNTIL_NEXT_LONG_STILLING);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean(NBT_IS_SEETHING, isSeething);
        tag.putInt(NBT_TICKS_REMAINING, ticksRemaining);
        tag.putInt(NBT_CYCLES_UNTIL_NEXT_LONG_STILLING, cyclesUntilLongStilling);
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
