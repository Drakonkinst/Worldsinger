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
package io.github.drakonkinst.worldsinger.entity.spore_growth;

import io.github.drakonkinst.worldsinger.cosmere.WaterReactionManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeGrowthMovement;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeKillingUtil;
import io.github.drakonkinst.worldsinger.entity.ServerSideEntity;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import io.github.drakonkinst.worldsinger.util.math.Int3;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.Vector3d;

public abstract class SporeGrowthEntity extends ServerSideEntity {

    protected static final Random random = Random.create();
    protected static final int FORCE_MODIFIER_MULTIPLIER = 20;

    private static final String WATER_REMAINING_KEY = "WaterRemaining";
    private static final String SPORES_REMAINING_KEY = "SporesRemaining";
    private static final String STAGE_KEY = "Stage";
    private static final String ORIGIN_X_KEY = "OriginX";
    private static final String ORIGIN_Y_KEY = "OriginY";
    private static final String ORIGIN_Z_KEY = "OriginZ";
    private static final String INITIAL_GROWTH_KEY = "InitialGrowth";

    private static final int DIRECTION_ARRAY_SIZE = 6;
    private static final int MAX_PLACE_ATTEMPTS = 3;
    private static final int MAX_AGE_TICKS = 20 * 10;
    private static final int SPORE_DRAIN_NEAR_SPORE_KILLABLE = 50;

    // Break a block as if it was broken by a spore growth entity, with a 2 in 3 chance to drop loot
    public static void breakBlockFromSporeGrowth(World world, BlockPos pos, Entity breakingEntity) {
        boolean shouldDropLoot = random.nextInt(3) > 0;
        world.breakBlock(pos, shouldDropLoot, breakingEntity);
    }

    // Play a block's placing sound effect to simulate placement
    public static void playPlaceSoundEffect(World world, BlockPos pos, BlockState state) {
        Vec3d centerPos = pos.toCenterPos();
        world.playSound(null, centerPos.getX(), centerPos.getY(), centerPos.getZ(),
                state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1.0f,
                0.8f + 0.4f * random.nextFloat(), random.nextLong());
    }

    // Safely sets a block's CATALYZED state to false
    private static void resetCatalyzed(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.contains(ModProperties.CATALYZED) && state.get(ModProperties.CATALYZED)) {
            world.setBlockState(pos, state.with(ModProperties.CATALYZED, false));
        }
    }

    // NBT data
    private int waterRemaining;
    private int sporesRemaining;
    private short stage = 0;
    private boolean isInitialGrowth;
    private BlockPos origin;

    // Volatile data
    private final Vector3d currentForceDir = new Vector3d();
    protected Int3 lastDir = Int3.ZERO;
    private int placeAttempts = 0;
    private BlockPos lastPos = null;
    private double currentForceMagnitude = 0.0;

    public SporeGrowthEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    /* Spore-Specific Methods */

    // Track the current growth stage they are at
    // If past the maximum stage, kills the Spore Growth Entity
    protected abstract int getMaxStage();

    // Return the next block to grow in the current location
    protected abstract BlockState getNextBlock();

    // Return if the entity can grow here, but must break the block
    protected abstract boolean canBreakHere(BlockState state);

    // Return if the entity can grow here, overriding the block
    protected abstract boolean canGrowHere(BlockState state);

    // Returns the weight of a certain candidate neighbor
    protected abstract int getWeight(World world, BlockPos pos, Int3 direction,
            boolean allowPassthrough);

    // Returns whether the given block is a growth block generated by this entity.
    protected abstract boolean isGrowthBlock(BlockState state);

    // Called after a block is placed by the main entity (non-decorator)
    protected abstract void onGrowBlock(BlockPos pos, BlockState state, BlockState originalState);

    // Called to place a decorator
    protected abstract void placeDecorator(BlockPos pos, Direction direction);

    // Returns the number of ticks between growth updates. 0 stops growth entirely, while
    // negative values indicate the entity should update multiple times per tick
    protected abstract int getGrowthDelay();

    // Returns whether external forces should be recalculated
    protected boolean shouldRecalculateForces() {
        return true;
    }

    /* Internal Methods */

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.waterRemaining = nbt.getInt(WATER_REMAINING_KEY, 0);
        this.sporesRemaining = nbt.getInt(SPORES_REMAINING_KEY, 0);
        this.isInitialGrowth = nbt.getBoolean(INITIAL_GROWTH_KEY, false);
        this.stage = nbt.getShort(STAGE_KEY, (short) 0);
        if (nbt.contains(ORIGIN_X_KEY)) {
            int x = nbt.getInt(ORIGIN_X_KEY, 0);
            int y = nbt.getInt(ORIGIN_Y_KEY, 0);
            int z = nbt.getInt(ORIGIN_Z_KEY, 0);
            this.origin = new BlockPos(x, y, z);
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt(WATER_REMAINING_KEY, this.waterRemaining);
        nbt.putInt(SPORES_REMAINING_KEY, this.sporesRemaining);
        nbt.putBoolean(INITIAL_GROWTH_KEY, this.isInitialGrowth);
        nbt.putShort(STAGE_KEY, this.stage);
        if (this.origin != null) {
            nbt.putInt(ORIGIN_X_KEY, this.origin.getX());
            nbt.putInt(ORIGIN_Y_KEY, this.origin.getY());
            nbt.putInt(ORIGIN_Z_KEY, this.origin.getZ());
        }
    }

    @Override
    public void tick() {
        if (this.getOrigin() == null) {
            this.setOrigin(this.getBlockPos());
        }

        if (!this.getWorld().isClient()) {
            if (this.shouldBeDead()) {
                if (this.getSpores() > 0) {
                    this.onEarlyDiscard();
                }
                this.discard();
            } else {
                this.grow();
            }
        }
    }

    @Override
    public final boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    // Called if entity was killed but still has spores remaining
    private void onEarlyDiscard() {
        BlockPos pos = this.getBlockPos();
        BlockPos.Mutable mutable = pos.mutableCopy();
        World world = this.getWorld();
        // Reset CATALYZED state for self and all neighboring blocks
        SporeGrowthEntity.resetCatalyzed(world, pos);
        for (Direction direction : ModConstants.CARDINAL_DIRECTIONS) {
            SporeGrowthEntity.resetCatalyzed(world, mutable.set(pos.offset(direction)));
        }
    }

    // Performs a tick's worth of growth.
    private void grow() {
        int growthDelay = this.getGrowthDelay();
        if (growthDelay > 0) {
            // Slower growths update once every few ticks
            if ((age + this.getId()) % growthDelay == 0) {
                if (this.shouldRecalculateForces()) {
                    this.recalculateForces();
                }
                this.doGrowStep();
            }
        } else {
            // Faster growths update multiple times in the same tick
            // Only recalculate forces once per tick, leading to less precision when moving quickly
            if (this.shouldRecalculateForces()) {
                this.recalculateForces();
            }
            for (int i = 0; i < -growthDelay; ++i) {
                this.doGrowStep();
            }
        }
    }

    // Spore Growth Entities should die if past the maximum stage, if out of spores or water, if
    // maximum age is reached, and if it fails to place a block too many times in a row
    private boolean shouldBeDead() {
        return this.getStage() > this.getMaxStage() || this.getSpores() <= 0 || age > MAX_AGE_TICKS
                || placeAttempts >= MAX_PLACE_ATTEMPTS || this.getWater() <= 0;
    }

    // Calculates external forces like Steel and Iron
    private void recalculateForces() {
        if (lastPos == null || !lastPos.equals(this.getBlockPos())) {
            BlockPos pos = this.getBlockPos();
            SporeGrowthMovement.calcExternalForce(this.getWorld(), pos, currentForceDir);
            currentForceMagnitude = currentForceDir.length();
            if (currentForceMagnitude > 0.0) {
                currentForceDir.mul(1.0 / currentForceMagnitude);
            }
            lastPos = pos;
        }
    }

    // Performs a single grow step, which usually places a single block
    private void doGrowStep() {
        World world = this.getWorld();
        BlockPos pos = this.getBlockPos();

        // Drain spores rapidly if near a spore-killing block
        if (SporeKillingUtil.isSporeKillingBlockNearby(world, pos)) {
            this.drainSpores(SPORE_DRAIN_NEAR_SPORE_KILLABLE);
        }

        // Absorb nearby water
        if (this.getWater() < this.getSpores() && world.getFluidState(pos).isIn(FluidTags.WATER)) {
            int waterAbsorbed = WaterReactionManager.absorbWaterAtBlock(world, pos);
            if (waterAbsorbed > 0) {
                this.setWater(this.getWater() + waterAbsorbed);
            }
        }

        // Attempts to place a block. If it initially fails, allows itself to pass through other
        // growth blocks to reach another position.
        boolean result = this.attemptGrowBlock(this.getNextBlock());
        if (result) {
            this.shiftBlock(this.getNextDirection(false));
            placeAttempts = 0;
        } else {
            Int3 direction = this.getNextDirection(true);
            if (direction.isZero()) {
                placeAttempts++;
            } else {
                this.shiftBlock(direction);
            }
        }
    }

    private boolean attemptGrowBlock(BlockState state) {
        if (state == null) {
            return false;
        }
        BlockPos blockPos = this.getBlockPos();
        BlockState originalState = this.getWorld().getBlockState(blockPos);
        if (this.canBreakHere(originalState)) {
            SporeGrowthEntity.breakBlockFromSporeGrowth(this.getWorld(), blockPos, this);
            return this.growBlock(state, originalState);
        } else if (this.canGrowHere(originalState)) {
            return this.growBlock(state, originalState);
        } else if (originalState.isIn(ModBlockTags.AETHER_SPORE_SEA_BLOCKS)) {
            return this.growBlock(state, originalState);
        }
        return false;
    }

    private void shiftBlock(Int3 direction) {
        if (direction.isZero()) {
            return;
        }
        Vec3d pos = this.getPos();
        this.setPosition(pos.add(direction.x(), direction.y(), direction.z()));
        this.setLastDir(direction);
    }

    // Set the block only, let each block handle its own grow effects in onGrowBlock()
    private boolean growBlock(BlockState state, BlockState originalState) {
        BlockPos pos = this.getBlockPos();
        boolean success = this.getWorld().setBlockState(pos, state);
        if (success) {
            this.onGrowBlock(pos, state, originalState);
        }
        return success;
    }

    private Int3 chooseWeighted(List<Int3> candidates, IntList weights, int weightSum) {
        if (candidates.isEmpty()) {
            return Int3.ZERO;
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        int currentWeight = 0;
        int targetWeight = random.nextInt(weightSum);
        for (int i = 0; i < candidates.size(); ++i) {
            currentWeight += weights.getInt(i);
            if (currentWeight >= targetWeight) {
                return candidates.get(i);
            }
        }
        return Int3.ZERO;
    }

    private void spawnParticles(Vec3d centerPos, BlockState state) {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
                    centerPos.getX(), centerPos.getY(), centerPos.getZ(), 100, 0.0, 0.0, 0.0,
                    0.15f);
        }
    }

    /* External Methods */

    public void setWater(int water) {
        if (this.waterRemaining < Integer.MAX_VALUE) {
            this.waterRemaining = Math.max(0, water);
        }
    }

    public void setSpores(int spores) {
        if (this.sporesRemaining < Integer.MAX_VALUE) {
            this.sporesRemaining = Math.max(0, spores);
        }
    }

    public void setInitialGrowth(boolean flag) {
        this.isInitialGrowth = flag;
    }

    // Allows initial stage to be set outside the entity
    // Only works if entity has not progressed a stage yet
    public void setInitialStage(int stage) {
        if (this.getStage() == 0) {
            this.addStage(stage);
        }
    }

    public void setLastDir(Int3 lastDir) {
        // lastDir is initialized at zero, but cannot become zero again.
        if (!lastDir.isZero()) {
            this.lastDir = lastDir;
        }
    }

    /* Helper methods to be used by subclasses */

    // Chooses next direction based on weighted probability
    // Can be overridden to skip this logic entirely
    protected Int3 getNextDirection(boolean allowPassthrough) {
        World world = this.getWorld();
        BlockPos pos = this.getBlockPos();
        Mutable mutable = new Mutable();
        List<Int3> candidates = new ArrayList<>(DIRECTION_ARRAY_SIZE);
        IntList weights = new IntArrayList(DIRECTION_ARRAY_SIZE);
        int weightSum = 0;
        for (Int3 direction : Int3.CARDINAL_3D) {
            if (direction.isZero() || direction.equals(lastDir.opposite())) {
                continue;
            }
            mutable.set(pos.getX() + direction.x(), pos.getY() + direction.y(),
                    pos.getZ() + direction.z());
            int weight = this.getWeight(world, mutable, direction, allowPassthrough);
            if (weight > 0) {
                candidates.add(direction);
                weights.add(weight);
                weightSum += weight;
            }
        }

        Int3 nextDirection = chooseWeighted(candidates, weights, weightSum);
        return nextDirection;
    }

    protected void drainSpores(int cost) {
        this.setSpores(this.getSpores() - cost);
    }

    protected void drainWater(int cost) {
        this.setWater(this.getWater() - cost);
    }

    protected void addStage(int stageIncrement) {
        if (stageIncrement > 0) {
            this.stage += (short) stageIncrement;
        }
    }

    protected void setOrigin(BlockPos pos) {
        origin = pos;
    }

    protected boolean shouldDrainWater() {
        int spores = this.getSpores();
        int water = this.getWater();
        if (water >= spores) {
            return true;
        }
        if (water <= 0) {
            return false;
        }
        // Want water to last as long as possible, so higher proportion of spores means lower chance
        float chanceToCatalyze = (float) spores / water;
        return random.nextFloat() < chanceToCatalyze;
    }

    // Returns [-currentForceMagnitude, +currentForceMagnitude]
    protected double getExternalForceModifier(Int3 direction) {
        // Dot product returns [-1, 1] based on how well direction matches currentForceDir
        double dot = direction.x() * currentForceDir.x() + direction.y() * currentForceDir.y()
                + direction.z() * currentForceDir.z();
        return dot * currentForceMagnitude;
    }

    // Helper method to place decorators using placeDecorator() on adjacent blocks, provided
    // that the block can be placed there.
    protected void attemptPlaceDecorators(int numIterations) {
        World world = this.getWorld();
        if (this.getSpores() <= 0 || random.nextInt(5) == 0) {
            return;
        }

        List<Direction> validDirections = new ArrayList<>(6);
        BlockPos pos = this.getBlockPos();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Direction direction : ModConstants.CARDINAL_DIRECTIONS) {
            mutable.set(pos.offset(direction));
            if (this.canPlaceDecorator(world.getBlockState(mutable))) {
                validDirections.add(direction);
            }
        }

        int numSpawned = 0;
        while (!validDirections.isEmpty() && numSpawned < numIterations) {
            int index = random.nextInt(validDirections.size());
            Direction direction = validDirections.remove(index);
            this.placeDecorator(pos.offset(direction), direction);
            numSpawned += 1;
        }
    }

    // Helper method to determine if any growth can spawn in this block
    protected boolean canPlaceDecorator(BlockState state) {
        return state.isIn(ModBlockTags.SPORES_CAN_GROW);
    }

    protected boolean placeBlockWithEffects(BlockPos pos, BlockState state, int cost,
            boolean drainsWater, boolean showParticles, boolean playSound) {
        boolean success = this.getWorld().setBlockState(pos, state);

        if (success) {
            this.doGrowEffects(pos, state, cost, drainsWater, showParticles, playSound);
        }

        return success;
    }

    protected void doGrowEffects(BlockPos pos, BlockState state, int cost, boolean drainsWater,
            boolean showParticles, boolean playSound) {
        if (drainsWater) {
            this.drainWater(cost);
        }
        this.drainSpores(cost);

        if (showParticles) {
            this.spawnParticles(pos.toCenterPos(), state);
        }

        if (playSound) {
            SporeGrowthEntity.playPlaceSoundEffect(this.getWorld(), pos, state);
        }
    }

    protected int getDistanceFromOrigin(BlockPos pos) {
        return pos.getManhattanDistance(this.getOrigin());
    }

    /* Getters */

    public int getWater() {
        return waterRemaining;
    }

    public int getSpores() {
        return sporesRemaining;
    }

    public int getStage() {
        return stage;
    }

    public boolean isInitialGrowth() {
        return isInitialGrowth;
    }

    public BlockPos getOrigin() {
        return origin;
    }
}
