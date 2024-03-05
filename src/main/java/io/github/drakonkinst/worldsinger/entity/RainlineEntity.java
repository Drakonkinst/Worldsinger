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

import io.github.drakonkinst.worldsinger.Worldsinger;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RainlineEntity extends Entity {

    public static final int RAINLINE_RADIUS = 8;
    private static final int HEIGHT_OFFSET = -1;

    public static List<RainlineEntity> getNearbyRainlineEntities(World world, Vec3d pos,
            int bonusRadius) {
        final double x = pos.getX();
        final double z = pos.getZ();
        final double y = world.getTopY() + HEIGHT_OFFSET;
        final int searchRadius = RAINLINE_RADIUS + bonusRadius;
        final Box box = new Box(x - searchRadius, y - 1, z - searchRadius, x + searchRadius, y + 1,
                z + searchRadius);
        return world.getEntitiesByClass(RainlineEntity.class, box, EntityPredicates.VALID_ENTITY);

    }

    public RainlineEntity(EntityType<? extends RainlineEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void tick() {
        int targetHeight = this.getWorld().getTopY() + HEIGHT_OFFSET;
        if (this.getBlockY() != targetHeight) {
            Worldsinger.LOGGER.info("Correcting entity height");
            Vec3d pos = this.getPos();
            this.setPosition(pos.getX(), targetHeight, pos.getZ());
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
