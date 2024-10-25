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

import io.github.drakonkinst.worldsinger.block.WaterReactiveBlock;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManagerAccess;
import io.github.drakonkinst.worldsinger.fluid.WaterReactiveFluid;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.Precipitation;

public class RainlineEntity extends Entity {

    public static final int RAINLINE_RADIUS = 6;
    public static final int RAINLINE_EFFECT_RADIUS = 2;
    private static final int HEIGHT_OFFSET = -1;
    private static final int RANDOM_TICK_INTERVAL = 3;
    private static final String KEY_FOLLOWING_PATH = "following_path";

    // Particles
    private static final int NUM_PARTICLES_PER_TICK = 25;
    private static final double PARTICLE_VERTICAL_DISTANCE = 4.0;
    private static final double PARTICLE_HORIZONTAL_DISTANCE = 16.0;

    // Mainly used for rendering
    public static List<RainlineEntity> getNearbyRainlineEntities(World world, Vec3d pos,
            double bonusRadius) {
        final double x = pos.getX();
        final double z = pos.getZ();
        final double searchRadius = RAINLINE_RADIUS + bonusRadius;
        final Box box = new Box(x - searchRadius, world.getBottomY(), z - searchRadius,
                x + searchRadius, world.getTopY(), z + searchRadius);
        return world.getEntitiesByClass(RainlineEntity.class, box, EntityPredicates.VALID_ENTITY);
    }

    public static boolean isRainlineOver(ServerWorld world, Vec3d pos) {
        final double x = pos.getX();
        final double z = pos.getZ();
        // final Box box = new Box(x - RAINLINE_SEARCH_RADIUS, world.getBottomY(),
        //         z - RAINLINE_SEARCH_RADIUS, x + RAINLINE_SEARCH_RADIUS, world.getTopY(),
        //         z + RAINLINE_SEARCH_RADIUS);
        // List<RainlineEntity> nearbyRainlines = world.getEntitiesByClass(RainlineEntity.class, box,
        //         EntityPredicates.VALID_ENTITY);

        // For some reason there is a bug where the rainline, even though nearby, doesn't
        List<RainlineEntity> nearbyRainlines = new ArrayList<>();
        world.collectEntitiesByType(TypeFilter.instanceOf(RainlineEntity.class),
                EntityPredicates.VALID_ENTITY, nearbyRainlines);
        for (RainlineEntity entity : nearbyRainlines) {
            double deltaX = entity.getX() - pos.getX();
            double deltaZ = entity.getZ() - pos.getZ();
            double distSq = deltaX * deltaX + deltaZ * deltaZ;
            if (distSq <= RAINLINE_EFFECT_RADIUS * RAINLINE_EFFECT_RADIUS) {
                return true;
            }
        }
        return false;
    }

    private static int getTargetHeight(World world) {
        return world.getTopY() + HEIGHT_OFFSET;
    }

    public RainlineEntity(EntityType<? extends RainlineEntity> type, World world) {
        super(type, world);
    }

    private RainlineBehavior rainlineBehavior;

    @Override
    public void tick() {
        if (this.getWorld().isClient()) {
            doClientTick();
        } else {
            doServerTick();
        }
    }

    private void doClientTick() {
        World world = this.getWorld();
        // TODO: Replace with an actual storm cloud at some point?
        for (int i = 0; i < NUM_PARTICLES_PER_TICK; ++i) {
            double x = this.getX() + random.nextDouble() * PARTICLE_HORIZONTAL_DISTANCE * 2
                    - PARTICLE_HORIZONTAL_DISTANCE;
            double y = RainlineEntity.getTargetHeight(world)
                    + random.nextDouble() * PARTICLE_VERTICAL_DISTANCE * 2
                    - PARTICLE_VERTICAL_DISTANCE;
            double z = this.getZ() + random.nextDouble() * PARTICLE_HORIZONTAL_DISTANCE * 2
                    - PARTICLE_HORIZONTAL_DISTANCE;
            world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0.0f, 0.0f, 0.0f);
        }
    }

    private void doServerTick() {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            doWaterReactiveTicks(serverWorld);
            rainlineBehavior.serverTick(serverWorld, this);
        }
    }

    // Cause additional random ticks in place for WaterReactive blocks
    private void doWaterReactiveTicks(ServerWorld world) {
        if (this.age % RANDOM_TICK_INTERVAL == 0) {
            doWaterReactiveTick(world);
        }
    }

    private void doWaterReactiveTick(ServerWorld world) {
        Profiler profiler = world.getProfiler();
        BlockPos.Mutable mutable = new Mutable();
        // Note: This is a square radius rather than the circular radius used for rendering,
        // which will be slightly larger
        int x = this.getBlockX() - RAINLINE_EFFECT_RADIUS + this.random.nextInt(
                RAINLINE_EFFECT_RADIUS * 2);
        int z = this.getBlockZ() - RAINLINE_EFFECT_RADIUS + this.random.nextInt(
                RAINLINE_EFFECT_RADIUS * 2);
        int y = world.getTopY(Type.MOTION_BLOCKING, x, z) - 1;
        mutable.set(x, y, z);
        BlockState blockState = world.getBlockState(mutable);
        Block block = blockState.getBlock();
        FluidState fluidState = blockState.getFluidState();
        Fluid fluid = fluidState.getFluid();

        // Okay, there are a lot of ways random ticks can happen...
        // Cauldrons use Block#precipitationTick
        // Fluids use FluidState#doRandomTick
        // Blocks use BlockState#randomTick
        profiler.push("randomTick");
        block.precipitationTick(blockState, world, mutable, Precipitation.RAIN);
        if (fluidState.hasRandomTicks() && fluid instanceof WaterReactiveFluid) {
            fluidState.onRandomTick(world, mutable, this.random);
        } else if (blockState.hasRandomTicks() && (block instanceof WaterReactiveBlock
                || blockState.isIn(ModBlockTags.AFFECTED_BY_RAIN))) {
            blockState.randomTick(world, mutable, this.random);
        }
        profiler.pop();
    }

    @Override
    protected void initDataTracker(Builder builder) {
        // Do nothing
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (!(this.getWorld() instanceof ServerWorld world)) {
            return;
        }

        LumarManager lumarManager = ((LumarManagerAccess) world).worldsinger$getLumarManager();
        boolean isFollowingPath = nbt.getBoolean(KEY_FOLLOWING_PATH);
        if (isFollowingPath) {
            rainlineBehavior = RainlineFollowPathBehavior.readFromNbt(lumarManager, nbt);
        }
        if (rainlineBehavior == null) {
            rainlineBehavior = RainlineWanderBehavior.readFromNbt(nbt);
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putBoolean(KEY_FOLLOWING_PATH, rainlineBehavior.isFollowingPath());
        rainlineBehavior.writeCustomDataToNbt(nbt);
    }
}
