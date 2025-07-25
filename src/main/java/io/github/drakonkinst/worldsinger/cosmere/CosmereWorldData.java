/*
 * MIT License
 *
 * Copyright (c) 2023-2025 Drakonkinst
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

package io.github.drakonkinst.worldsinger.cosmere;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import org.jetbrains.annotations.Nullable;

public class CosmereWorldData extends PersistentState {

    private static final String KEY_TIME = "time";
    private static final String KEY_SPAWN_POS = "spawn_pos";
    private static final String KEY_SPAWN_ANGLE = "spawn_angle";

    public static final String NAME = "cosmere";

    private final static Codec<CosmereWorldData> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(
                            Codec.LONG.fieldOf(KEY_TIME).forGetter(CosmereWorldData::getTimeOfDay),
                            net.minecraft.util.math.BlockPos.CODEC.optionalFieldOf(KEY_SPAWN_POS)
                                    .forGetter(cosmereWorldData -> cosmereWorldData.spawnPos),
                            Codec.FLOAT.fieldOf(KEY_SPAWN_ANGLE).forGetter(CosmereWorldData::getSpawnAngle))
                    .apply(builder, CosmereWorldData::new));
    public static PersistentStateType<CosmereWorldData> STATE_TYPE = new PersistentStateType<>(NAME,
            CosmereWorldData::new, CODEC, DataFixTypes.LEVEL);

    private long timeOfDay;
    private Optional<BlockPos> spawnPos;
    private float spawnAngle;

    public CosmereWorldData() {
        this(-1, Optional.empty(), 0.0f);
    }

    public CosmereWorldData(long timeOfDay, Optional<BlockPos> spawnPos, float spawnAngle) {
        this.timeOfDay = timeOfDay;
        this.spawnPos = spawnPos;
        this.spawnAngle = spawnAngle;
    }

    public void setTimeOfDay(long timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public void setSpawnPos(@Nullable BlockPos pos) {
        this.spawnPos = Optional.ofNullable(pos);
    }

    public void setSpawnAngle(float spawnAngle) {
        this.spawnAngle = spawnAngle;
    }

    public long getTimeOfDay() {
        return timeOfDay;
    }

    public @Nullable BlockPos getSpawnPos() {
        return spawnPos.orElse(null);
    }

    public float getSpawnAngle() {
        return spawnAngle;
    }
}
