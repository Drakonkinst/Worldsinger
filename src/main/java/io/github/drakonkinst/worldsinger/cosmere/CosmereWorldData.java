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

package io.github.drakonkinst.worldsinger.cosmere;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

public class CosmereWorldData extends PersistentState {

    private static final String KEY_TIME = "time";
    private static final String KEY_SPAWN_POS = "spawn_pos";

    public static final String NAME = "cosmere";

    public static PersistentState.Type<CosmereWorldData> getPersistentStateType() {
        return new PersistentState.Type<>(CosmereWorldData::new,
                (nbt, registryLookup) -> CosmereWorldData.fromNbt(nbt), DataFixTypes.LEVEL);
    }

    private static CosmereWorldData fromNbt(NbtCompound nbt) {
        CosmereWorldData cosmereWorldData = new CosmereWorldData();
        cosmereWorldData.setTimeOfDay(nbt.getLong(KEY_TIME));
        if (nbt.contains(KEY_SPAWN_POS, NbtElement.LONG_TYPE)) {
            cosmereWorldData.setSpawnPos(BlockPos.fromLong(nbt.getLong(KEY_SPAWN_POS)));
        }
        return cosmereWorldData;
    }

    private long timeOfDay;
    private @Nullable BlockPos spawnPos = null;

    public CosmereWorldData() {

    }

    public void setTimeOfDay(long timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public void setSpawnPos(@Nullable BlockPos pos) {
        this.spawnPos = pos;
    }

    public long getTimeOfDay() {
        return timeOfDay;
    }

    public @Nullable BlockPos getSpawnPos() {
        return spawnPos;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        nbt.putLong(KEY_TIME, timeOfDay);
        if (spawnPos != null) {
            nbt.putLong(KEY_SPAWN_POS, spawnPos.asLong());
        }
        return nbt;
    }
}
