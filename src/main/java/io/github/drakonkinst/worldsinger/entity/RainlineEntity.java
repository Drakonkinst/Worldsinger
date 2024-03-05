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

package io.github.drakonkinst.worldsinger.entity;

import io.github.drakonkinst.worldsinger.cosmere.WaterReactive;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.World;

public class RainlineEntity extends Entity {

    public static final int RAINLINE_RADIUS = 8;
    private static final int HEIGHT_OFFSET = -1;

    private static int getTargetHeight(World world) {
        return world.getTopY() + HEIGHT_OFFSET;
    }

    public static List<RainlineEntity> getNearbyRainlineEntities(World world, Vec3d pos,
            int bonusRadius) {
        final double x = pos.getX();
        final double z = pos.getZ();
        final double y = RainlineEntity.getTargetHeight(world);
        final int searchRadius = RAINLINE_RADIUS + bonusRadius;
        final Box box = new Box(x - searchRadius, y - 1, z - searchRadius, x + searchRadius, y + 1,
                z + searchRadius);
        return world.getEntitiesByClass(RainlineEntity.class, box, EntityPredicates.VALID_ENTITY);
    }

    public static boolean isRainlineOver(World world, Vec3d pos) {
        return !RainlineEntity.getNearbyRainlineEntities(world, pos, 0).isEmpty();
    }

    public RainlineEntity(EntityType<? extends RainlineEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void tick() {
        fixHeight();
        World world = this.getWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            doAdditionalWaterReactiveTicks(serverWorld);
        }
    }

    private void fixHeight() {
        int targetHeight = this.getWorld().getTopY() + HEIGHT_OFFSET;
        if (this.getBlockY() != targetHeight) {
            Vec3d pos = this.getPos();
            this.setPosition(pos.getX(), targetHeight, pos.getZ());
        }
    }

    // Cause additional random ticks in place for WaterReactive blocks
    private void doAdditionalWaterReactiveTicks(ServerWorld world) {
        int randomTickSpeed = this.getWorld().getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
        if (randomTickSpeed <= 0) {
            return;
        }

        Profiler profiler = world.getProfiler();
        int centerX = this.getBlockX();
        int centerZ = this.getBlockZ();
        BlockPos.Mutable mutable = new Mutable();
        // Note: This is a square radius rather than the circular radius used for rendering,
        // which will be slightly larger
        for (int i = 0; i < randomTickSpeed; ++i) {
            int x = centerX - RAINLINE_RADIUS + this.random.nextInt(RAINLINE_RADIUS * 2);
            int z = centerZ - RAINLINE_RADIUS + this.random.nextInt(RAINLINE_RADIUS * 2);
            int y = world.getTopY(Type.MOTION_BLOCKING, x, z);
            mutable.set(x, y, z);
            BlockState blockState = world.getBlockState(mutable);
            if (blockState.hasRandomTicks() && blockState.getBlock() instanceof WaterReactive) {
                profiler.push("randomTick");
                blockState.randomTick(world, mutable, this.random);
                profiler.pop();
            }
        }
    }

    @Override
    protected void initDataTracker(Builder builder) {
        // Do nothing
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        // Do nothing
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        // Do nothing
    }
}
