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

package io.github.drakonkinst.worldsinger.entity.rainline;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RainlinePath;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator.SporeSeaEntry;
import it.unimi.dsi.fastutil.longs.LongByteImmutablePair;
import it.unimi.dsi.fastutil.longs.LongBytePair;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

public class RainlineFollowPathBehavior implements RainlineBehavior {

    private static final String KEY_RAINLINE_ID = "rainline_id";
    private static final String KEY_RAINLINE_INDEX = "rainline_index";

    public static @Nullable RainlineFollowPathBehavior readFromNbt(LumarManager manager,
            NbtCompound nbt) {
        if (!nbt.contains(KEY_RAINLINE_ID) || !nbt.contains(KEY_RAINLINE_INDEX)) {
            return null;
        }
        long rainlineId = nbt.getLong(KEY_RAINLINE_ID, -999);
        byte rainlineIndex = nbt.getByte(KEY_RAINLINE_INDEX, (byte) 0);
        RainlinePath rainlinePath = manager.getRainlineManager().getRainlinePathById(rainlineId);
        if (rainlinePath == null) {
            return null;
        }
        return new RainlineFollowPathBehavior(rainlinePath, rainlineId, rainlineIndex);
    }

    private final RainlinePath rainlinePath;
    private final LongBytePair pathId;
    private final int stepOffset;

    public RainlineFollowPathBehavior(RainlinePath rainlinePath, long id, byte index) {
        this.rainlinePath = rainlinePath;
        this.pathId = new LongByteImmutablePair(id, index);
        this.stepOffset = rainlinePath.getStepOffset(index);
    }

    @Override
    public void serverTick(ServerWorld world, RainlineEntity entity) {
        SporeSeaEntry entry = LumarChunkGenerator.getSporeSeaEntryAtPos(
                world.getChunkManager().getNoiseConfig(), entity.getBlockX(), entity.getBlockZ());
        if (stepOffset < 0 || !AetherSpores.hasRainlinePathsInSea(entry.id())) {
            // If step offset is invalid or does not support rainlines in sea, start wandering
            Worldsinger.LOGGER.info("Rainline following path is now wandering");
            entity.setRainlineBehavior(new RainlineWanderBehavior(entity.getRandom()));
            return;
        }
        Vec2f newPos = rainlinePath.getRainlinePosition(world, stepOffset);
        entity.setVelocity(0, 0, 0);
        entity.setPos(newPos.x, RainlineEntity.getTargetHeight(world), newPos.y);
    }

    @Override
    public boolean isFollowingPath() {
        return true;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putLong(KEY_RAINLINE_ID, pathId.firstLong());
        nbt.putByte(KEY_RAINLINE_INDEX, pathId.secondByte());
    }

    public LongBytePair getPathId() {
        return pathId;
    }

}
