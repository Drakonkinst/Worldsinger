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
package io.github.drakonkinst.worldsinger.fluid;

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.cosmere.WaterReactionManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeKillingManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public abstract class LivingAetherSporeFluid extends AetherSporeFluid implements
        WaterReactiveFluid {

    public static final int CATALYZE_VALUE_STILL = 250;
    public static final int CATALYZE_VALUE_FLOWING = 25;
    private static final int NUM_RANDOM_SPREAD_PER_RANDOM_TICK = 2;

    public LivingAetherSporeFluid(AetherSpores sporeType) {
        super(sporeType);
    }

    @Override
    protected boolean hasRandomTicks() {
        return true;
    }

    @Override
    protected void onRandomTick(World world, BlockPos pos, FluidState state, Random random) {
        super.onRandomTick(world, pos, state, random);

        if (world.hasRain(pos.up())) {
            this.reactToWater(world, pos, state, Integer.MAX_VALUE, random);
        }

        if (!SeetheManager.areSporesFluidized(world)) {
            return;
        }

        // Spread to nearby dead spore sea blocks, regenerating them
        BlockState blockState = this.toBlockState(state);
        for (int i = 0; i < NUM_RANDOM_SPREAD_PER_RANDOM_TICK; ++i) {
            int offsetX = random.nextInt(3) - 1;
            int offsetY = random.nextInt(3) - 1;
            int offsetZ = random.nextInt(3) - 1;

            BlockPos blockPos = pos.add(offsetX, offsetY, offsetZ);
            if (world.getBlockState(blockPos).isOf(ModBlocks.DEAD_SPORE_SEA) && world.getFluidState(
                    blockPos).isStill() && !SporeKillingManager.isSporeKillingBlockNearby(world,
                    blockPos)) {
                world.setBlockState(blockPos, blockState);
            }
        }
    }

    @Override
    public boolean reactToWater(World world, BlockPos pos, FluidState fluidState, int waterAmount,
            Random random) {
        // Water reaction
        int sporeAmount = this.isStill(fluidState) ? CATALYZE_VALUE_STILL : CATALYZE_VALUE_FLOWING;
        this.getSporeType().doReaction(world, pos, sporeAmount, waterAmount, random);

        // Remove the spores
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (block instanceof FluidDrainable fluidDrainable) {
            ItemStack itemStack = fluidDrainable.tryDrainFluid(null, world, pos, blockState);
            if (itemStack.isEmpty() && block instanceof FluidBlock) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
        return true;
    }

    @Override
    protected void flow(WorldAccess world, BlockPos pos, BlockState state, Direction direction,
            FluidState fluidState) {
        if (direction == Direction.DOWN) {
            FluidState neighborState = world.getFluidState(pos);
            if (neighborState.isIn(FluidTags.WATER)) {
                if (state.getBlock() instanceof FluidBlock && world instanceof World realWorld) {
                    WaterReactionManager.catalyzeAroundWater(realWorld, pos);
                    BlockState replacingState = this.getSporeType().getFluidCollisionState();
                    if (replacingState != null) {
                        world.setBlockState(pos, replacingState, Block.NOTIFY_ALL);
                    }
                    return;
                }
            }
        }
        super.flow(world, pos, state, direction, fluidState);
    }
}
