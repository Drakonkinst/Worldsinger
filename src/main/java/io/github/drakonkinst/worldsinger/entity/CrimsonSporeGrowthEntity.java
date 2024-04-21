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
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.block.TallCrimsonSpinesBlock;
import io.github.drakonkinst.worldsinger.cosmere.lumar.CrimsonSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleManager;
import io.github.drakonkinst.worldsinger.fluid.Fluidlogged;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.util.BoxUtil;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import io.github.drakonkinst.worldsinger.util.math.Int3;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class CrimsonSporeGrowthEntity extends SporeGrowthEntity {

    public static final int MAX_STAGE = 2;

    private static final float MIN_ROTATION = 10.0f * MathHelper.RADIANS_PER_DEGREE;
    private static final float MAX_ROTATION = 45.0f * MathHelper.RADIANS_PER_DEGREE;
    private static final int MAX_ATTEMPTS = 10;
    private static final float PARALLEL_THRESHOLD = 0.9f;
    private static final int NEXT_STAGE_WATER_THRESHOLD = 40;
    private static final int NEXT_STAGE_SPORE_THRESHOLD = 70;
    private static final int SPLIT_SPORE_MIN = 140;
    private static final int SPLIT_WATER_MIN = 140;
    private static final int SPLIT_AGE_MAX = 1;

    private static final int COST_CRIMSON_GROWTH = 10;
    private static final int COST_CRIMSON_SPIKE = 7;
    private static final int COST_TALL_CRIMSON_SPINES = 2;
    private static final int COST_CRIMSON_SPINES = 1;

    // Using the given cardinal direction as a basis, rotate up to 45 degrees in yaw or pitch.
    private static Vector3f randomizeDirectionFromCardinalDirection(Int3 cardinalDirection) {
        Vector3f direction = new Vector3f(cardinalDirection.x(), cardinalDirection.y(),
                cardinalDirection.z());
        Vector3f randomUnitVector = CrimsonSporeGrowthEntity.generateRandomUnitVector(direction);
        Vector3f rotationAxis = randomUnitVector.cross(direction);
        float rotationAngle = random.nextFloat() * (MAX_ROTATION - MIN_ROTATION) + MIN_ROTATION;
        Quaternionfc rotation = new Quaternionf(
                new AxisAngle4f(rotationAngle, rotationAxis.x(), rotationAxis.y(),
                        rotationAxis.z()));
        return direction.rotate(rotation);
    }

    // Generate a random unit vector that is not too parallel to the given vector
    private static Vector3f generateRandomUnitVector(Vector3f notParallelWith) {
        float unitX;
        float unitY;
        float unitZ;
        Vector3f vector;
        int attempts = 0;

        do {
            do {
                unitX = random.nextFloat() * 2.0f - 1.0f;
                unitY = random.nextFloat() * 2.0f - 1.0f;
                unitZ = random.nextFloat() * 2.0f - 1.0f;
            } while (unitX * unitX + unitY * unitY + unitZ * unitZ > 1.0f
                    && ++attempts < MAX_ATTEMPTS);
            vector = new Vector3f(unitX, unitY, unitZ).normalize();
        } while (Math.abs(vector.dot(notParallelWith)) >= PARALLEL_THRESHOLD
                && ++attempts < MAX_ATTEMPTS);

        if (attempts >= MAX_ATTEMPTS) {
            Worldsinger.LOGGER.error("Maxed out attempts to generate valid unit vector");
        }

        return new Vector3f(unitX, unitY, unitZ).normalize();
    }

    private final List<Int3> directionCandidates = new ArrayList<>(3);
    private Vector3f targetGrowthDirection = null;
    private Int3 primaryDirection = null;

    public CrimsonSporeGrowthEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    public void setTargetGrowthDirection(Vector3f direction) {
        this.targetGrowthDirection = direction;

        float deltaX = Math.abs(targetGrowthDirection.x());
        float deltaY = Math.abs(targetGrowthDirection.y());
        float deltaZ = Math.abs(targetGrowthDirection.z());

        // Precompute direction candidates
        directionCandidates.clear();
        if (deltaX != 0) {
            int signX = (int) Math.signum(targetGrowthDirection.x());
            directionCandidates.add(new Int3(signX, 0, 0));
        }
        if (deltaY != 0) {
            int signY = (int) Math.signum(targetGrowthDirection.y());
            directionCandidates.add(new Int3(0, signY, 0));
        }
        if (deltaZ != 0) {
            int signZ = (int) Math.signum(targetGrowthDirection.z());
            directionCandidates.add(new Int3(0, 0, signZ));
        }

        if (deltaY >= deltaX && deltaY >= deltaZ) {
            primaryDirection = targetGrowthDirection.y() > 0 ? Int3.UP : Int3.DOWN;
        } else if (deltaX >= deltaY && deltaX >= deltaZ) {
            primaryDirection = targetGrowthDirection.x() > 0 ? Int3.EAST : Int3.WEST;
        } else {
            primaryDirection = targetGrowthDirection.z() > 0 ? Int3.SOUTH : Int3.NORTH;
        }
    }

    @Override
    protected BlockState getNextBlock() {
        BlockState state = null;
        int stage = this.getStage();
        if (stage == 0) {
            state = ModBlocks.CRIMSON_GROWTH.getDefaultState();
        } else if (stage == 1) {
            if (lastDir.equals(primaryDirection)) {
                state = ModBlocks.CRIMSON_SPIKE.getDefaultState()
                        .with(Properties.FACING, lastDir.toDirection(Direction.UP));
            } else {
                state = ModBlocks.CRIMSON_GROWTH.getDefaultState();
            }
        } else if (stage == 2) {
            state = ModBlocks.CRIMSON_SNARE.getDefaultState();
        }

        if (state == null) {
            return null;
        }

        if (state.contains(ModProperties.FLUIDLOGGED)) {
            int fluidloggedIndex = Fluidlogged.getFluidIndex(
                    this.getWorld().getFluidState(this.getBlockPos()).getFluid());
            state = state.with(ModProperties.FLUIDLOGGED, fluidloggedIndex);
        }
        if (this.shouldDrainWater() && state.contains(ModProperties.CATALYZED)) {
            state = state.with(ModProperties.CATALYZED, true);
        }
        return state;
    }

    @Override
    protected Int3 getNextDirection(boolean allowPassthrough) {
        this.updateTargetGrowthDirection();
        if (this.getStage() == 0) {
            return this.getNextDirectionForGrowthBlock(allowPassthrough);
        } else if (this.getStage() == 1) {
            return this.getNextDirectionForSpikeBlock(allowPassthrough);
        }
        return super.getNextDirection(allowPassthrough);
    }

    @Override
    protected boolean shouldRecalculateForces() {
        // Take external forces into account only when the current direction is undecided
        // Cannot change direction once decided
        return targetGrowthDirection == null;
    }

    @Override
    protected int getWeight(World world, BlockPos pos, Int3 direction, boolean allowPassthrough) {
        BlockState state = world.getBlockState(pos);
        int weight = 0;

        if (this.canBreakOrGrow(state, allowPassthrough)) {
            weight = 100;
        }

        if (weight == 0) {
            return 0;
        }

        // Massive bonus for going along with external force
        double forceModifier = this.getExternalForceModifier(direction);
        weight += MathHelper.floor(FORCE_MODIFIER_MULTIPLIER * forceModifier);

        // Always have some weight, so it is an options if no other options are good
        weight = Math.max(1, weight);
        return weight;
    }

    private boolean canBreakOrGrow(BlockState state, boolean allowPassthrough) {
        return this.canBreakHere(state) || this.canGrowHere(state) || (allowPassthrough
                && this.isGrowthBlock(state));
    }

    @Override
    protected boolean canBreakHere(BlockState state) {
        return state.isIn(ModBlockTags.SPORES_CAN_BREAK) || state.isIn(
                ModBlockTags.ALL_VERDANT_GROWTH);
    }

    @Override
    protected boolean canGrowHere(BlockState state) {
        return state.isIn(ModBlockTags.SPORES_CAN_GROW) || state.isIn(ModBlockTags.CRIMSON_SNARE)
                || state.isIn(ModBlockTags.TALL_CRIMSON_SPINES) || state.isIn(
                ModBlockTags.CRIMSON_SPINES) || (this.getStage() == 0 && state.isIn(
                ModBlockTags.CRIMSON_SPIKE)) || (this.getStage() == 2 && state.isIn(
                ModBlockTags.CRIMSON_SPIKE));
    }

    @Override
    protected boolean isGrowthBlock(BlockState state) {
        return state.isIn(ModBlockTags.ALL_CRIMSON_GROWTH);
    }

    @Override
    protected int getGrowthDelay() {
        if (this.isInitialGrowth()) {
            return -3;
        }

        int water = this.getWater();
        int spores = this.getSpores();

        if (water > spores) {
            return 3;
        }
        if (water == spores) {
            return 4;
        }
        return 5;
    }

    private void updateStage() {
        if (this.getStage() == 0) {
            if (this.getWater() < NEXT_STAGE_WATER_THRESHOLD) {
                this.addStage(1);
            } else if (this.getSpores() < NEXT_STAGE_SPORE_THRESHOLD && random.nextInt(3) == 0) {
                this.addStage(1);
            }
        } else if (this.getStage() == 2) {
            // Add one beyond the max, effectively killing it
            this.addStage(1);
        }

        // Split branches
        if (this.getSpores() >= SPLIT_SPORE_MIN && this.getWater() >= SPLIT_WATER_MIN
                && age < SPLIT_AGE_MAX) {
            this.createSplitBranch();
        }
    }

    private void createSplitBranch() {
        float proportion = 0.25f + random.nextFloat() * 0.25f;
        int numSpores = MathHelper.ceil(this.getSpores() * proportion);
        int numWater = MathHelper.ceil(this.getWater() * proportion);
        Vec3d spawnPos = this.getBlockPos().toCenterPos();
        CrimsonSpores.getInstance()
                .spawnSporeGrowth(this.getWorld(), spawnPos, numSpores, numWater,
                        this.isInitialGrowth(), this.getStage() > 0, true, Int3.UP);
        this.drainSpores(numSpores);
        this.drainWater(numWater);
    }

    @Override
    protected int getMaxStage() {
        return MAX_STAGE;
    }

    @Override
    protected void onGrowBlock(BlockPos pos, BlockState state, BlockState originalState) {
        boolean isFullBlock = state.isOf(ModBlocks.CRIMSON_GROWTH);
        int cost = isFullBlock ? COST_CRIMSON_GROWTH : COST_CRIMSON_SPIKE;
        boolean drainsWater = state.getOrEmpty(ModProperties.CATALYZED).orElse(false);
        this.doGrowEffects(pos, state, cost, drainsWater, true, true);

        // Two attempts to place decorators for extra density
        if (isFullBlock) {
            this.attemptPlaceDecorators(2);
        }

        SporeParticleManager.damageEntitiesInBox(this.getWorld(), CrimsonSpores.getInstance(),
                BoxUtil.createBoxAroundBlock(pos, 1.0), true);
        this.updateStage();
    }

    @Override
    protected void placeDecorator(BlockPos pos, Direction direction) {
        if (direction == Direction.UP && random.nextInt(3) == 0 && this.canPlaceDecorator(
                this.getWorld().getBlockState(pos.up()))) {
            this.placeTallSpines(pos);
        } else {
            this.placeSpines(pos, direction);
        }
    }

    private void placeTallSpines(BlockPos pos) {
        boolean shouldDrainWater = this.shouldDrainWater();
        BlockState state = ModBlocks.TALL_CRIMSON_SPINES.getDefaultState()
                .with(ModProperties.CATALYZED, shouldDrainWater);
        // No need to check fluid state since this is handled in placeAt()
        TallCrimsonSpinesBlock.placeAt(this.getWorld(), state, pos, Block.NOTIFY_ALL);
        this.doGrowEffects(pos, state, COST_TALL_CRIMSON_SPINES, shouldDrainWater, false, false);
    }

    private void placeSpines(BlockPos pos, Direction direction) {
        boolean shouldDrainWater = this.shouldDrainWater();
        int fluidloggedIndex = Fluidlogged.getFluidIndex(
                this.getWorld().getFluidState(pos).getFluid());
        BlockState state = ModBlocks.CRIMSON_SPINES.getDefaultState()
                .with(Properties.FACING, direction)
                .with(ModProperties.CATALYZED, shouldDrainWater)
                .with(ModProperties.FLUIDLOGGED, fluidloggedIndex);
        this.placeBlockWithEffects(pos, state, COST_CRIMSON_SPINES, shouldDrainWater, false, true);
    }

    private void resetTargetGrowthDirection() {
        targetGrowthDirection = null;
        directionCandidates.clear();
        primaryDirection = null;
    }

    // https://math.stackexchange.com/a/3757354
    private float getDistanceSqToTargetDir(Vec3i origin, Vec3i pos) {
        int originToCandidateX = pos.getX() - origin.getX();
        int originToCandidateY = pos.getY() - origin.getY();
        int originToCandidateZ = pos.getZ() - origin.getZ();
        Vector3f originToCandidate = new Vector3f(originToCandidateX, originToCandidateY,
                originToCandidateZ);
        float av2 = originToCandidate.dot(originToCandidate);
        float avd = originToCandidate.dot(targetGrowthDirection);
        float distanceSq = av2 - avd * avd;
        return distanceSq;
    }

    private void updateTargetGrowthDirection() {
        if (targetGrowthDirection != null) {
            return;
        }
        Vector3f nextDirection;
        if (!lastDir.isZero()) {
            nextDirection = CrimsonSporeGrowthEntity.randomizeDirectionFromCardinalDirection(
                    lastDir);
        } else {
            nextDirection = CrimsonSporeGrowthEntity.randomizeDirectionFromCardinalDirection(
                    Int3.UP);
        }
        this.setTargetGrowthDirection(nextDirection);
    }

    private Int3 getNextDirectionForGrowthBlock(boolean allowPassthrough) {
        BlockPos currPos = this.getBlockPos();
        float minDistanceSq = Float.MAX_VALUE;
        int minDistanceIndex = 0;
        World world = this.getWorld();
        BlockPos originPos = this.getOrigin();
        BlockPos.Mutable candidatePos = new BlockPos.Mutable();
        for (int i = 0; i < directionCandidates.size(); ++i) {
            Int3 direction = directionCandidates.get(i);
            candidatePos.set(currPos.getX() + direction.x(), currPos.getY() + direction.y(),
                    currPos.getZ() + direction.z());
            float distanceSq = this.getDistanceSqToTargetDir(originPos, candidatePos);
            if (distanceSq < minDistanceSq) {
                minDistanceSq = distanceSq;
                minDistanceIndex = i;
            }
        }

        Int3 minDistanceDirection = directionCandidates.get(minDistanceIndex);
        BlockPos minDistancePos = currPos.add(minDistanceDirection.x(), minDistanceDirection.y(),
                minDistanceDirection.z());
        BlockState minDistanceState = world.getBlockState(minDistancePos);

        // Failed to find a valid direction, so just go randomly
        if (!this.canBreakOrGrow(minDistanceState, true)) {
            this.resetTargetGrowthDirection();
            return super.getNextDirection(allowPassthrough);
        }

        return directionCandidates.get(minDistanceIndex);
    }

    private Int3 getNextDirectionForSpikeBlock(boolean allowPassthrough) {
        BlockPos nextPos = this.getBlockPos()
                .add(primaryDirection.x(), primaryDirection.y(), primaryDirection.z());
        BlockState nextState = this.getWorld().getBlockState(nextPos);
        if (this.canBreakOrGrow(nextState, allowPassthrough)) {
            return primaryDirection;
        } else {
            // If entity cannot go in primary direction, turn into a splinter by advancing the stage
            this.addStage(1);
            return Int3.ZERO;
        }
    }
}
