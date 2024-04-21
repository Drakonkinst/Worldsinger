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

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.block.VerdantVineBranchBlock;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.VerdantSpores;
import io.github.drakonkinst.worldsinger.fluid.Fluidlogged;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import io.github.drakonkinst.worldsinger.util.math.Int3;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VerdantSporeGrowthEntity extends SporeGrowthEntity {

    public static final int MAX_STAGE = 1;

    private static final int MAX_TWISTING_VINE_DEPTH_UP = 3;
    private static final int MAX_TWISTING_VINE_DEPTH_DOWN = 7;
    private static final int SPORE_BRANCH_THRESHOLD_MIN = 50;
    private static final int SPORE_BRANCH_THRESHOLD_MAX = 100;
    private static final int SPORE_BRANCH_THICK_THRESHOLD = 300;
    private static final int SPORE_SPLIT_MIN = 100;
    private static final int WATER_SPLIT_MIN = 1;
    private static final int SPORE_WATER_THRESHOLD = 25;

    private static final int COST_VERDANT_VINE_BLOCK = 7;
    private static final int COST_VERDANT_VINE_BRANCH = 4;
    private static final int COST_VERDANT_VINE_SNARE = 2;
    private static final int COST_TWISTING_VERDANT_VINES = 1;

    public VerdantSporeGrowthEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected BlockState getNextBlock() {
        BlockState state = null;
        if (this.getStage() == 0) {
            state = ModBlocks.VERDANT_VINE_BLOCK.getDefaultState()
                    .with(Properties.AXIS, this.getPlacementAxis());
        } else if (this.getStage() == 1) {
            VerdantVineBranchBlock block = (VerdantVineBranchBlock) ModBlocks.VERDANT_VINE_BRANCH;
            state = block.withConnectionProperties(this.getWorld(), this.getBlockPos());
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

    private Axis getPlacementAxis() {
        if (lastDir.x() != 0) {
            return Axis.X;
        }
        if (lastDir.z() != 0) {
            return Axis.Z;
        }
        return Axis.Y;
    }

    @Override
    protected int getWeight(World world, BlockPos pos, Int3 direction, boolean allowPassthrough) {
        BlockState state = world.getBlockState(pos);
        int weight = 0;

        // Prefer not to break through blocks
        if (this.canBreakHere(state)) {
            weight = 10;
        } else if (this.canGrowHere(state)) {
            // Can grow through lesser vines
            weight = 200;
        } else if (allowPassthrough && this.isGrowthBlock(state)) {
            // If allowPassthrough is true, we assume that no actual block will be placed
            weight = 10;
        }

        if (weight == 0) {
            return 0;
        }

        // Heavy penalty for going in the same direction to encourage curved paths
        if (direction.equals(lastDir)) {
            return 5;
        }

        // Prefer to grow upwards
        weight += 30 * direction.y();

        // Prefers to go in the same direction away from the origin
        BlockPos originPos = this.getOrigin();
        int dirFromOriginX = Integer.signum(pos.getX() - originPos.getX());
        int dirFromOriginY = Integer.signum(pos.getY() - originPos.getY());
        int dirFromOriginZ = Integer.signum(pos.getZ() - originPos.getZ());
        if (direction.y() == dirFromOriginX || direction.y() == dirFromOriginY
                || direction.z() == dirFromOriginZ) {
            weight += 50;
        }

        // Bonuses based on neighbors
        weight += this.getNeighborBonus(world, pos);

        // Bonus for moving away from origin
        int bonusDistanceFromOrigin =
                this.getDistanceFromOrigin(pos) - this.getDistanceFromOrigin(this.getBlockPos());
        weight += 10 * bonusDistanceFromOrigin;

        // Massive bonus for going along with external force
        double forceModifier = this.getExternalForceModifier(direction);
        weight += MathHelper.floor(FORCE_MODIFIER_MULTIPLIER * forceModifier);

        // Always have some weight, so it is an options if no other options are good
        weight = Math.max(1, weight);
        return weight;
    }

    @Override
    protected boolean canBreakHere(BlockState state) {
        return state.isIn(ModBlockTags.SPORES_CAN_BREAK);
    }

    @Override
    protected boolean canGrowHere(BlockState state) {
        return state.isIn(ModBlockTags.SPORES_CAN_GROW) || state.isIn(
                ModBlockTags.VERDANT_VINE_SNARE) || state.isIn(ModBlockTags.TWISTING_VERDANT_VINES)
                || (state.isIn(ModBlockTags.VERDANT_VINE_BRANCH) && this.getStage() == 0);
    }

    @Override
    protected boolean isGrowthBlock(BlockState state) {
        return state.isIn(ModBlockTags.ALL_VERDANT_GROWTH);
    }

    private int getNeighborBonus(World world, BlockPos pos) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int weightBonus = 0;
        int vineNeighbors = 0;
        boolean hugsBlock = false;
        for (Direction direction : ModConstants.CARDINAL_DIRECTIONS) {
            mutable.set(pos.offset(direction));
            BlockState state = world.getBlockState(mutable);
            if (state.isIn(ModBlockTags.ALL_VERDANT_GROWTH)) {
                // Prefer NOT to be adjacent to too many other of the same block
                if (this.getStage() == 0 && state.isIn(ModBlockTags.VERDANT_VINE_BLOCK)
                        && this.getSpores() > SPORE_BRANCH_THICK_THRESHOLD) {
                    // Allow thick branches
                    continue;
                }
                vineNeighbors++;
            } else if (state.isIn(ModBlockTags.AETHER_SPORE_SEA_BLOCKS)) {
                // Prefer to move away from the spore sea
                weightBonus -= 20;
            } else if (state.isSideSolidFullSquare(world, mutable, direction.getOpposite())) {
                // Prefer to wrap around blocks
                hugsBlock = true;
            }
        }

        if (vineNeighbors > 1) {
            weightBonus -= 150 * (vineNeighbors - 1);
        }
        if (hugsBlock) {
            weightBonus += 100;
        }
        return weightBonus;
    }

    private void updateStage() {
        if (this.getStage() == 0) {
            // Advance stage if low on water
            if (this.getWater() <= SPORE_WATER_THRESHOLD) {
                this.addStage(1);
            } else if (this.getSpores() <= SPORE_BRANCH_THRESHOLD_MIN || (
                    this.getSpores() <= SPORE_BRANCH_THRESHOLD_MAX && random.nextInt(5) == 0)) {
                // Chance to advance stage if low on spores
                this.addStage(1);
            }
        }

        if (this.getSpores() >= SPORE_SPLIT_MIN && this.getWater() >= WATER_SPLIT_MIN
                && random.nextInt(10) == 0) {
            this.createSplitBranch();
        }
    }

    private void createSplitBranch() {
        float proportion = 0.25f + random.nextFloat() * 0.25f;
        int numSpores = MathHelper.ceil(this.getSpores() * proportion);
        int numWater = MathHelper.ceil(this.getWater() * proportion);
        Vec3d spawnPos = this.getBlockPos().toCenterPos();
        VerdantSpores.getInstance()
                .spawnSporeGrowth(this.getWorld(), spawnPos, numSpores, numWater,
                        this.isInitialGrowth(), this.getStage() > 0, true, Int3.ZERO);
        this.drainSpores(numSpores);
        this.drainWater(numWater);
    }

    @Override
    protected void onGrowBlock(BlockPos pos, BlockState state, BlockState originalState) {
        int cost = state.isOf(ModBlocks.VERDANT_VINE_BLOCK) ? COST_VERDANT_VINE_BLOCK
                : COST_VERDANT_VINE_BRANCH;
        boolean drainsWater = state.getOrEmpty(ModProperties.CATALYZED).orElse(false);
        this.doGrowEffects(pos, state, cost, drainsWater, true, true);
        this.attemptPlaceDecorators(1);
        SporeParticleManager.damageEntitiesInBlock(this.getWorld(), VerdantSpores.getInstance(),
                pos);
        this.updateStage();
    }

    @Override
    protected int getMaxStage() {
        return MAX_STAGE;
    }

    @Override
    protected int getGrowthDelay() {
        if (this.isInitialGrowth()) {
            return -3;
        }

        int water = this.getWater();
        int spores = this.getSpores();

        if (water > spores) {
            return 5;
        }
        if (water == spores) {
            return 7;
        }
        return 8;
    }

    @Override
    protected void placeDecorator(BlockPos pos, Direction direction) {
        if ((direction == Direction.UP || direction == Direction.DOWN) && random.nextInt(4) > 0) {
            this.placeTwistingVineChain(pos, direction, 0);
        } else {
            this.placeSnare(pos, direction);
        }
    }

    private void placeTwistingVineChain(BlockPos pos, Direction direction, int depth) {
        World world = this.getWorld();

        boolean shouldDrainWater = this.shouldDrainWater();
        int fluidloggedIndex = Fluidlogged.getFluidIndex(world.getFluidState(pos).getFluid());
        BlockState state = ModBlocks.TWISTING_VERDANT_VINES.getDefaultState()
                .with(Properties.VERTICAL_DIRECTION, direction)
                .with(ModProperties.CATALYZED, shouldDrainWater)
                .with(ModProperties.FLUIDLOGGED, fluidloggedIndex);

        boolean success = this.placeBlockWithEffects(pos, state, COST_TWISTING_VERDANT_VINES,
                shouldDrainWater, false, false);
        if (!success) {
            return;
        }

        // Chance to continue growth
        if (direction == Direction.UP && depth >= MAX_TWISTING_VINE_DEPTH_UP) {
            return;
        }
        if (direction == Direction.DOWN && depth >= MAX_TWISTING_VINE_DEPTH_DOWN) {
            return;
        }
        BlockPos nextPos = pos.offset(direction);
        if (this.getSpores() > 0 && this.canPlaceDecorator(world.getBlockState(nextPos))
                && random.nextInt(5) > 0) {
            this.placeTwistingVineChain(nextPos, direction, depth + 1);
        }
    }

    private void placeSnare(BlockPos pos, Direction direction) {
        boolean shouldDrainWater = this.shouldDrainWater();
        int fluidloggedIndex = Fluidlogged.getFluidIndex(
                this.getWorld().getFluidState(pos).getFluid());
        BlockState state = ModBlocks.VERDANT_VINE_SNARE.getDefaultState()
                .with(Properties.FACING, direction)
                .with(ModProperties.CATALYZED, shouldDrainWater)
                .with(ModProperties.FLUIDLOGGED, fluidloggedIndex);

        this.placeBlockWithEffects(pos, state, COST_VERDANT_VINE_SNARE, shouldDrainWater, false,
                true);
    }

}
