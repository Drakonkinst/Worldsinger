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

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RoseiteSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleManager;
import io.github.drakonkinst.worldsinger.fluid.Fluidlogged;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import io.github.drakonkinst.worldsinger.util.math.Int3;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.EntityType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class RoseiteSporeGrowthEntity extends SporeGrowthEntity {

    public static final int MAX_STAGE = 0;

    private static final int COST_ROSEITE_BLOCK = 9;
    private static final int COST_ROSEITE_STAIRS = 7;
    private static final int COST_ROSEITE_SLAB = 5;
    private static final int COST_ROSEITE_CLUSTER = 4;
    private static final int COST_LARGE_ROSEITE_BUD = 3;
    private static final int COST_MEDIUM_ROSEITE_BUD = 2;
    private static final int COST_SMALL_ROSEITE_BUD = 1;

    private static Direction getRandomHorizontalDirection() {
        return Util.getRandom(ModConstants.HORIZONTAL_DIRECTIONS, RoseiteSporeGrowthEntity.random);
    }

    private static Direction getHorizontalDirectionFromDir(Int3 lastDir) {
        Direction randomDir = RoseiteSporeGrowthEntity.getRandomHorizontalDirection();
        if (lastDir.y() != 0) {
            return randomDir;
        }
        return lastDir.toDirection(randomDir);
    }

    private static int getCost(BlockState blockState) {
        if (blockState.isOf(ModBlocks.ROSEITE_BLOCK)) {
            return COST_ROSEITE_BLOCK;
        }
        if (blockState.isOf(ModBlocks.ROSEITE_STAIRS)) {
            return COST_ROSEITE_STAIRS;
        }
        if (blockState.isOf(ModBlocks.ROSEITE_SLAB)) {
            return COST_ROSEITE_SLAB;
        }
        if (blockState.isOf(ModBlocks.ROSEITE_CLUSTER)) {
            return COST_ROSEITE_CLUSTER;
        }
        if (blockState.isOf(ModBlocks.LARGE_ROSEITE_BUD)) {
            return COST_LARGE_ROSEITE_BUD;
        }
        if (blockState.isOf(ModBlocks.MEDIUM_ROSEITE_BUD)) {
            return COST_MEDIUM_ROSEITE_BUD;
        }
        if (blockState.isOf(ModBlocks.SMALL_ROSEITE_BUD)) {
            return COST_SMALL_ROSEITE_BUD;
        }
        return 0;
    }

    public RoseiteSporeGrowthEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected int getMaxStage() {
        return MAX_STAGE;
    }

    @Override
    protected int getGrowthDelay() {
        if (this.isInitialGrowth()) {
            return 2;
        }
        int water = this.getWater();
        int spores = this.getSpores();

        if (water > spores) {
            return 5;
        }
        if (water == spores) {
            return 10;
        }
        return 15;
    }

    @Override
    protected BlockState getNextBlock() {
        BlockState state = getNextBaseBlock();
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

    private BlockState getNextBaseBlock() {
        BlockPos pos = this.getBlockPos();
        World world = this.getWorld();
        BlockState state = world.getBlockState(pos);
        boolean doubleStep = random.nextInt(5) == 0;

        if (state.isOf(ModBlocks.SMALL_ROSEITE_BUD) && !doubleStep) {
            return ModBlocks.MEDIUM_ROSEITE_BUD.getStateWithProperties(state);
        }
        if ((state.isOf(ModBlocks.MEDIUM_ROSEITE_BUD) && !doubleStep) || (
                state.isOf(ModBlocks.SMALL_ROSEITE_BUD) && doubleStep)) {
            return ModBlocks.LARGE_ROSEITE_BUD.getStateWithProperties(state);
        }
        if ((state.isOf(ModBlocks.LARGE_ROSEITE_BUD) && !doubleStep) || (
                state.isOf(ModBlocks.MEDIUM_ROSEITE_BUD) && doubleStep)) {
            return ModBlocks.ROSEITE_CLUSTER.getStateWithProperties(state);
        }
        if (state.isOf(ModBlocks.ROSEITE_CLUSTER) || state.isOf(ModBlocks.LARGE_ROSEITE_BUD)) {
            Direction facingDir = state.get(Properties.FACING);
            if (!state.isOf(ModBlocks.ROSEITE_CLUSTER) || !doubleStep) {
                // If it should be a slab, attempt to place one
                if (facingDir == Direction.DOWN) {
                    return ModBlocks.ROSEITE_SLAB.getStateWithProperties(state)
                            .with(Properties.SLAB_TYPE, SlabType.TOP);
                }
                if (facingDir == Direction.UP) {
                    return ModBlocks.ROSEITE_SLAB.getStateWithProperties(state)
                            .with(Properties.SLAB_TYPE, SlabType.BOTTOM);
                }
            }
            // Slab placement failed, so place a stair instead
            BlockHalf half;
            Direction horizontalFacing;
            if (facingDir == Direction.DOWN) {
                half = BlockHalf.TOP;
                horizontalFacing = RoseiteSporeGrowthEntity.getRandomHorizontalDirection();
            } else if (facingDir == Direction.UP) {
                half = BlockHalf.BOTTOM;
                horizontalFacing = RoseiteSporeGrowthEntity.getRandomHorizontalDirection();
            } else {
                half = random.nextBoolean() ? BlockHalf.TOP : BlockHalf.BOTTOM;
                horizontalFacing = facingDir.getOpposite();
            }
            return ModBlocks.ROSEITE_STAIRS.getStateWithProperties(state)
                    .with(Properties.BLOCK_HALF, half)
                    .with(Properties.HORIZONTAL_FACING, horizontalFacing);
        }
        if (state.isOf(ModBlocks.ROSEITE_SLAB) && !doubleStep) {
            SlabType slabType = state.get(Properties.SLAB_TYPE);
            if (slabType == SlabType.DOUBLE) {
                return ModBlocks.ROSEITE_BLOCK.getDefaultState();
            }
            BlockHalf blockHalf = (slabType == SlabType.TOP) ? BlockHalf.TOP : BlockHalf.BOTTOM;
            Direction horizontalFacing = RoseiteSporeGrowthEntity.getHorizontalDirectionFromDir(
                    lastDir.opposite());
            return ModBlocks.ROSEITE_STAIRS.getStateWithProperties(state)
                    .with(Properties.BLOCK_HALF, blockHalf)
                    .with(Properties.HORIZONTAL_FACING, horizontalFacing);
        }
        if (state.isOf(ModBlocks.ROSEITE_STAIRS) || (state.isOf(ModBlocks.ROSEITE_SLAB)
                && doubleStep)) {
            return ModBlocks.ROSEITE_BLOCK.getDefaultState();
        }

        // Generate a random block
        if (doubleStep) {
            return ModBlocks.ROSEITE_BLOCK.getDefaultState();
        }

        BlockPos belowPos = pos.down();
        if (world.getBlockState(belowPos).isSideSolidFullSquare(world, belowPos, Direction.UP)) {
            return ModBlocks.ROSEITE_SLAB.getDefaultState()
                    .with(Properties.SLAB_TYPE, SlabType.BOTTOM);
        }
        BlockPos abovePos = pos.up();
        if (world.getBlockState(abovePos).isSideSolidFullSquare(world, abovePos, Direction.DOWN)) {
            return ModBlocks.ROSEITE_SLAB.getDefaultState()
                    .with(Properties.SLAB_TYPE, SlabType.TOP);
        }

        BlockHalf blockHalf;
        if (lastDir.y() > 0) {
            blockHalf = BlockHalf.TOP;
        } else if (lastDir.y() < 0) {
            blockHalf = BlockHalf.BOTTOM;
        } else {
            blockHalf = random.nextBoolean() ? BlockHalf.TOP : BlockHalf.BOTTOM;
        }
        Direction horizontalFacing = RoseiteSporeGrowthEntity.getHorizontalDirectionFromDir(
                lastDir);
        return ModBlocks.ROSEITE_STAIRS.getStateWithProperties(state)
                .with(Properties.BLOCK_HALF, blockHalf)
                .with(Properties.HORIZONTAL_FACING, horizontalFacing);
    }

    @Override
    protected Int3 getNextDirection(boolean allowPassthrough) {
        BlockPos currentPos = this.getBlockPos();
        BlockState currentState = this.getWorld().getBlockState(currentPos);
        if (currentState.isIn(ModBlockTags.ROSEITE_GROWABLE)) {
            return Int3.ZERO;
        }
        return super.getNextDirection(allowPassthrough);
    }

    @Override
    protected boolean canBreakHere(BlockState state) {
        return state.isIn(ModBlockTags.SPORES_CAN_BREAK);
    }

    @Override
    protected boolean canGrowHere(BlockState state) {
        return state.isIn(ModBlockTags.SPORES_CAN_GROW) || state.isIn(
                ModBlockTags.ROSEITE_GROWABLE);
    }

    @Override
    protected int getWeight(World world, BlockPos pos, Int3 direction, boolean allowPassthrough) {
        BlockState state = world.getBlockState(pos);
        int weight = -1;

        if (state.isIn(ModBlockTags.ROSEITE_GROWABLE)) {
            // Prefer to take pre-existing growths
            weight = 125;
        } else if (this.canBreakHere(state)) {
            // Prefer not to break through blocks
            weight = 1;
        } else if (this.canGrowHere(state)) {
            // Can grow through lesser blocks
            weight = 100;
        } else if (allowPassthrough && this.isGrowthBlock(state)) {
            // If allowPassthrough is true, we assume that no actual block will be placed
            weight = 10;
        }

        if (weight < 0) {
            return 0;
        }

        // Heavy penalty for going in the same direction to encourage curved paths
        if (direction.equals(lastDir)) {
            return 5;
        }

        // Prefer to grow downwards, which helps it seal gaps
        weight -= 50 * direction.y();

        // Bonuses based on neighbors
        weight += this.getNeighborBonus(world, pos);

        // Bonus for moving towards origin, keeping it in a clump
        int bonusDistanceFromOrigin =
                this.getDistanceFromOrigin(this.getBlockPos()) - this.getDistanceFromOrigin(pos);
        weight += 50 * bonusDistanceFromOrigin;

        // Massive bonus for going along with external force
        double forceModifier = this.getExternalForceModifier(direction);
        weight += MathHelper.floor(FORCE_MODIFIER_MULTIPLIER * forceModifier);

        // Always have some weight, so it is an options if no other options are good
        weight = Math.max(1, weight);
        return weight;
    }

    private int getNeighborBonus(World world, BlockPos pos) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int weightBonus = 0;
        int numNeighbors = 0;
        boolean hugsBlock = false;
        for (Direction direction : ModConstants.CARDINAL_DIRECTIONS) {
            mutable.set(pos.offset(direction));
            BlockState state = world.getBlockState(mutable);
            if (isGrowthBlock(state)) {
                numNeighbors++;
            } else if (state.isIn(ModBlockTags.AETHER_SPORE_SEA_BLOCKS)) {
                // Prefer to move away from the spore sea
                weightBonus -= 20;
            } else if (state.isSideSolidFullSquare(world, mutable, direction.getOpposite())) {
                // Prefer to wrap around blocks
                hugsBlock = true;
            }
        }

        // Prefer to wrap around itself when possible
        if (numNeighbors > 1) {
            weightBonus += 10 * (numNeighbors - 1);
        }
        if (hugsBlock) {
            weightBonus += 15;
        }
        return weightBonus;
    }

    @Override
    protected boolean isGrowthBlock(BlockState state) {
        return state.isIn(ModBlockTags.ALL_ROSEITE_GROWTH);
    }

    @Override
    protected void onGrowBlock(BlockPos pos, BlockState state, BlockState originalState) {
        int cost = Math.max(
                RoseiteSporeGrowthEntity.getCost(state) - RoseiteSporeGrowthEntity.getCost(
                        originalState), 0);
        boolean drainsWater = state.getOrEmpty(ModProperties.CATALYZED).orElse(false);
        this.doGrowEffects(pos, state, cost, drainsWater, true, true);
        if (state.isOf(ModBlocks.ROSEITE_BLOCK)) {
            this.attemptPlaceDecorators(2);
        }
        SporeParticleManager.damageEntitiesInBlock(this.getWorld(), RoseiteSpores.getInstance(),
                pos);
    }

    @Override
    protected void placeDecorator(BlockPos pos, Direction direction) {
        int randomValue = random.nextInt(4);
        Block block;
        if (randomValue == 0) {
            block = ModBlocks.ROSEITE_CLUSTER;
        } else if (randomValue == 1) {
            block = ModBlocks.LARGE_ROSEITE_BUD;
        } else if (randomValue == 2) {
            block = ModBlocks.MEDIUM_ROSEITE_BUD;
        } else {
            block = ModBlocks.SMALL_ROSEITE_BUD;
        }
        boolean shouldDrainWater = this.shouldDrainWater();
        int fluidloggedIndex = Fluidlogged.getFluidIndex(
                this.getWorld().getFluidState(pos).getFluid());
        BlockState state = block.getDefaultState()
                .with(Properties.FACING, direction)
                .with(ModProperties.CATALYZED, shouldDrainWater)
                .with(ModProperties.FLUIDLOGGED, fluidloggedIndex);
        this.placeBlockWithEffects(pos, state, RoseiteSporeGrowthEntity.getCost(state),
                shouldDrainWater, false, false);
    }
}
