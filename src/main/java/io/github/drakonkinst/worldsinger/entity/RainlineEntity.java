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
import io.github.drakonkinst.worldsinger.block.ModBlockTags;
import io.github.drakonkinst.worldsinger.block.WaterReactiveBlock;
import io.github.drakonkinst.worldsinger.cosmere.lumar.CrimsonSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeLocation;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeManagerAccess;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RainlinePath;
import io.github.drakonkinst.worldsinger.fluid.WaterReactiveFluid;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator.SporeSeaEntry;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.Precipitation;
import org.joml.Vector2d;

public class RainlineEntity extends Entity {

    public static final int RAINLINE_RADIUS = 6;
    private static final int HEIGHT_OFFSET = -1;
    private static final int RANDOM_TICK_INTERVAL = 10;
    private static final int UPDATE_PATH_TICK_INTERVAL = 10;
    private static final String KEY_FOLLOWING_PATH = "following_path";

    // Steering behaviors
    private static final float SPEED_BLOCKS_PER_TICK = 0.4f;
    private static final float WANDER_CIRCLE_RADIUS = 20.0f;
    private static final float WANDER_CIRCLE_DISTANCE = 20.0f;
    private static final float WANDER_ANGLE_CHANGE = 0.1f;
    private static final int LOOK_AHEAD_TICKS = 0;
    private static final float STEERING_SCALE = 0.5f;
    private static final float CLOSE_ENOUGH_DISTANCE = 100.0f;

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

    private static Vec3d truncate(Vec3d vec3d, float maxValue) {
        if (vec3d.lengthSquared() > maxValue * maxValue) {
            return vec3d.normalize().multiply(maxValue);
        } else {
            return vec3d;
        }
    }

    private static int getTargetHeight(World world) {
        return world.getTopY() + HEIGHT_OFFSET;
    }

    private RainlinePath rainlinePath = null;
    private LunagreeLocation lunagreeLocation = null;
    private final Vector2d steeringForce = new Vector2d();
    private float wanderAngle = 0.0f;
    private Vec2f targetPathPos = null;

    public RainlineEntity(EntityType<? extends RainlineEntity> type, World world) {
        super(type, world);
    }

    public void setRainlinePath(LunagreeLocation lunagreeLocation, RainlinePath rainlinePath) {
        this.lunagreeLocation = lunagreeLocation;
        this.rainlinePath = rainlinePath;
    }

    @Override
    public void tick() {
        fixHeight();
        World world = this.getWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            doAdditionalWaterReactiveTicks(serverWorld);
            if (targetPathPos == null || isCloseEnoughToTarget()) {
                updatePath();
            }
            calculateSteeringForce();
            validatePosition(serverWorld);
        }

        this.setVelocity(RainlineEntity.truncate(this.getNextVelocity(), SPEED_BLOCKS_PER_TICK));
        move();
    }

    private boolean isCloseEnoughToTarget() {
        double deltaX = targetPathPos.x - this.getX();
        double deltaZ = targetPathPos.y - this.getZ();
        double distSq = deltaX * deltaX + deltaZ * deltaZ;
        return distSq < CLOSE_ENOUGH_DISTANCE * CLOSE_ENOUGH_DISTANCE;
    }

    private void fixHeight() {
        int targetHeight = this.getWorld().getTopY() + HEIGHT_OFFSET;
        if (this.getBlockY() != targetHeight) {
            Vec3d pos = this.getPos();
            this.setPosition(pos.getX(), targetHeight, pos.getZ());
        }
    }

    private void updatePath() {
        if (rainlinePath != null && (this.age % UPDATE_PATH_TICK_INTERVAL == 0
                || targetPathPos == null)) {
            targetPathPos = rainlinePath.getPositionAtTime(
                    this.getWorld().getTimeOfDay() + LOOK_AHEAD_TICKS, SPEED_BLOCKS_PER_TICK);
        }
    }

    private boolean shouldHaveRandomMovement(ServerWorld world) {
        SporeSeaEntry entry = LumarChunkGenerator.getSporeSeaEntryAtPos(
                world.getChunkManager().getNoiseConfig(), this.getBlockX(), this.getBlockZ());
        return entry.id() == CrimsonSpores.ID;
    }

    private void validatePosition(ServerWorld world) {
        boolean followingPath = rainlinePath != null;
        boolean shouldBeRandom = shouldHaveRandomMovement(world);
        if (followingPath == shouldBeRandom) {
            Worldsinger.LOGGER.info(
                    "followingPath = " + followingPath + " and shouldBeRandom = " + shouldBeRandom
                            + ", discarding");
            this.discard();
        }
    }

    private void calculateSteeringForce() {
        Vec3d pos = this.getPos();
        Vec3d velocity = this.getVelocity();

        // If following a path, continue to follow it
        if (rainlinePath != null) {
            steeringForce.set(targetPathPos.x - pos.getX(), targetPathPos.y - pos.getZ());
            steeringForce.normalize()
                    .mul(SPEED_BLOCKS_PER_TICK)
                    .sub(velocity.getX(), velocity.getZ())
                    .mul(STEERING_SCALE);
        } else {
            // Wander randomly
            if (velocity.equals(Vec3d.ZERO)) {
                // If no velocity, go in a random direction
                float randomAngle = this.random.nextFloat() * MathHelper.TAU;
                float velocityX = MathHelper.cos(randomAngle) * SPEED_BLOCKS_PER_TICK;
                float velocityZ = MathHelper.sin(randomAngle) * SPEED_BLOCKS_PER_TICK;
                steeringForce.set(velocityX, velocityZ);
            } else {
                // If current velocity, use wander steering behavior
                steeringForce.set(velocity.getX(), velocity.getZ())
                        .normalize()
                        .mul(WANDER_CIRCLE_DISTANCE);
                float displacementX = MathHelper.cos(wanderAngle) * WANDER_CIRCLE_RADIUS;
                float displacementZ = MathHelper.sin(wanderAngle) * WANDER_CIRCLE_RADIUS;
                steeringForce.add(displacementX, displacementZ).mul(STEERING_SCALE);
                wanderAngle +=
                        random.nextFloat() * WANDER_ANGLE_CHANGE * 2.0f - WANDER_ANGLE_CHANGE;
                if (wanderAngle < 0) {
                    wanderAngle += MathHelper.TAU;
                }
                if (wanderAngle > MathHelper.TAU) {
                    wanderAngle -= MathHelper.TAU;
                }
            }
            // TODO While wandering randomly, avoid lunagrees
        }
    }

    private Vec3d getNextVelocity() {
        Vec3d velocity = this.getVelocity();
        calculateSteeringForce();
        return new Vec3d(velocity.getX() + steeringForce.x(), 0.0,
                velocity.getZ() + steeringForce.y());
    }

    private void move() {
        Vec3d velocity = this.getVelocity();
        Vec3d position = this.getPos();
        this.setPosition(position.getX() + velocity.getX(), position.getY(),
                position.getZ() + velocity.getZ());
    }

    // Cause additional random ticks in place for WaterReactive blocks
    private void doAdditionalWaterReactiveTicks(ServerWorld world) {
        if (this.age % RANDOM_TICK_INTERVAL == 0) {
            doWaterReactiveTick(world);
        }
    }

    private void doWaterReactiveTick(ServerWorld world) {
        Profiler profiler = world.getProfiler();
        BlockPos.Mutable mutable = new Mutable();
        // Note: This is a square radius rather than the circular radius used for rendering,
        // which will be slightly larger
        int x = this.getBlockX() - RAINLINE_RADIUS + this.random.nextInt(RAINLINE_RADIUS * 2);
        int z = this.getBlockZ() - RAINLINE_RADIUS + this.random.nextInt(RAINLINE_RADIUS * 2);
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
        World world = this.getWorld();
        Worldsinger.LOGGER.info("Reading NBT");
        if (nbt.contains(KEY_FOLLOWING_PATH, NbtElement.BYTE_TYPE) && nbt.getBoolean(
                KEY_FOLLOWING_PATH)) {
            LunagreeManager lunagreeManager = ((LunagreeManagerAccess) world).worldsinger$getLunagreeManager();
            lunagreeLocation = lunagreeManager.getNearestLunagree(this.getBlockX(),
                    this.getBlockZ(), RainlinePath.MAX_RADIUS).orElse(null);
            if (lunagreeLocation != null) {
                rainlinePath = lunagreeManager.getNearestRainlinePathAt(lunagreeLocation.blockX(),
                        lunagreeLocation.blockZ());
                Worldsinger.LOGGER.info("Set rainline path to " + rainlinePath);
            }
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (rainlinePath != null) {
            nbt.putBoolean(KEY_FOLLOWING_PATH, true);
        }
    }
}
