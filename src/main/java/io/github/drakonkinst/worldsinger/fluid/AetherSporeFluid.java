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

import io.github.drakonkinst.worldsinger.block.AetherSporeFluidBlock;
import io.github.drakonkinst.worldsinger.block.SporeEmitting;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleManager;
import io.github.drakonkinst.worldsinger.mixin.accessor.FlowableFluidInvoker;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import io.github.drakonkinst.worldsinger.util.ColorUtil;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class AetherSporeFluid extends FlowableFluid implements SporeEmitting {

    public static final int MAX_LEVEL = 8;
    public static final float FOG_START = 0.25f;
    public static final float FOG_END = 2.0f;
    public static final float HORIZONTAL_DRAG_MULTIPLIER = 0.7f;
    public static final float VERTICAL_DRAG_MULTIPLIER = 0.8f;

    // How fast this fluid pushes entities.
    // Water uses the value 0.014, and lava uses 0.007 in the Nether and 0.0023 otherwise
    public static final double FLUID_SPEED = 0.012;

    private final AetherSpores sporeType;
    private final float fogRed;
    private final float fogGreen;
    private final float fogBlue;

    public AetherSporeFluid(AetherSpores sporeType) {
        super();
        this.sporeType = sporeType;

        int color = sporeType.getColor();
        this.fogRed = ColorUtil.getNormalizedRed(color);
        this.fogGreen = ColorUtil.getNormalizedGreen(color);
        this.fogBlue = ColorUtil.getNormalizedBlue(color);
    }

    @Override
    public int getLevel(FluidState state) {
        return state.getLevel();
    }

    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == getStill() || fluid == getFlowing();
    }

    @Override
    public void onScheduledTick(ServerWorld world, BlockPos pos, BlockState blockState,
            FluidState fluidState) {
        if (this.isStill(fluidState) && !AetherSporeFluidBlock.shouldFluidize(
                world.getBlockState(pos.down()))) {
            AetherSporeFluidBlock.updateFluidizationForBlock(world, pos, blockState, false);
        }
        super.onScheduledTick(world, pos, blockState, fluidState);
    }

    @Override
    public Optional<SoundEvent> getBucketFillSound() {
        return Optional.of(ModSoundEvents.ITEM_BUCKET_FILL_AETHER_SPORE);
    }

    @Override
    public int getTickRate(WorldView worldView) {
        return 5;
    }

    public int getFogColor() {
        return sporeType.getColor();
    }

    public AetherSpores getSporeType() {
        return sporeType;
    }

    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        // Drop stacks of blocks when breaking them with fluid
        final BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }

    @Override
    protected void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random) {
        if (!SeetheManager.areSporesFluidized(world)) {
            return;
        }

        // Play particles and sounds during seething only
        BlockPos posAbove = pos.up();
        if (world.getBlockState(posAbove).isAir() && !world.getBlockState(posAbove)
                .isOpaqueFullCube()) {
            if (random.nextInt(100) == 0) {
                double spawnX = (double) pos.getX() + random.nextDouble();
                double spawnY = (double) pos.getY() + 1.0 + random.nextDouble();
                double spawnZ = (double) pos.getZ() + random.nextDouble();
                SporeParticleManager.addClientDisplayParticle(world, sporeType, spawnX, spawnY,
                        spawnZ, 1.0f, true, random);
            }
            if (random.nextInt(200) == 0) {
                world.playSoundClient(pos.getX(), pos.getY(), pos.getZ(),
                        ModSoundEvents.BLOCK_SPORE_SEA_AMBIENT, SoundCategory.BLOCKS,
                        0.2f + random.nextFloat() * 0.2f, 0.9f + random.nextFloat() * 0.15f, false);
            }
        }
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos,
            Fluid fluid, Direction direction) {
        return state.isIn(ModFluidTags.AETHER_SPORES) && !state.isStill();
    }

    @Override
    protected FluidState getUpdatedState(ServerWorld world, BlockPos pos, BlockState state) {
        int maxNeighboringFluidLevel = 0;
        int neighboringSourceBlocks = 0;
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos currPos = pos.offset(direction);
            BlockState currBlockState = world.getBlockState(currPos);
            FluidState currFluidState = currBlockState.getFluidState();
            // noinspection ConstantValue
            if (!(currFluidState.getFluid() instanceof AetherSporeFluid)
                    || !FlowableFluidInvoker.worldsinger$receivesFlow(direction, world, pos, state,
                    currPos, currBlockState)) {
                continue;
            }
            if (currFluidState.isStill()) {
                ++neighboringSourceBlocks;
            }
            maxNeighboringFluidLevel = Math.max(maxNeighboringFluidLevel,
                    currFluidState.getLevel());
        }

        if (this.isInfinite(world) && neighboringSourceBlocks >= 2) {
            BlockState belowBlockState = world.getBlockState(pos.down());
            FluidState belowFluidState = belowBlockState.getFluidState();
            if (belowBlockState.isSolid()
                    || ((FlowableFluidInvoker) this).worldsinger$isMatchingAndStill(
                    belowFluidState)) {
                return this.getStill(false);
            }
        }

        BlockPos posAbove = pos.up();
        BlockState stateAbove = world.getBlockState(posAbove);
        FluidState aboveFluidState = stateAbove.getFluidState();
        // noinspection ConstantValue
        if (!aboveFluidState.isEmpty() && aboveFluidState.getFluid() instanceof AetherSporeFluid
                && FlowableFluidInvoker.worldsinger$receivesFlow(Direction.UP, world, pos, state,
                posAbove, stateAbove)) {
            return this.getFlowing(AetherSporeFluid.MAX_LEVEL, true);
        }
        int updatedFluidLevel = maxNeighboringFluidLevel - this.getLevelDecreasePerBlock(world);
        if (updatedFluidLevel <= 0) {
            return Fluids.EMPTY.getDefaultState();
        }
        return this.getFlowing(updatedFluidLevel, false);
    }

    @Override
    protected boolean isInfinite(ServerWorld world) {
        return true;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView worldView) {
        return 2;
    }

    @Override
    // This is used when the fluid "pathfinds" to see which way it should flow
    // Since it only flows as far as lava in the Overworld, match the flow speed
    protected int getMaxFlowDistance(WorldView worldView) {
        return 2;
    }

    @Override
    protected float getBlastResistance() {
        return 100.0F;
    }
}
