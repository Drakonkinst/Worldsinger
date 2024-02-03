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
package io.github.drakonkinst.worldsinger.block;

import com.mojang.serialization.MapCodec;
import io.github.drakonkinst.worldsinger.fluid.Fluidlogged;
import io.github.drakonkinst.worldsinger.registry.ModDamageTypes;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import io.github.drakonkinst.worldsinger.util.VoxelShapeUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class TallCrimsonSpinesBlock extends Block implements Waterloggable, SporeGrowthBlock {

    public static final MapCodec<TallCrimsonSpinesBlock> CODEC = AbstractBlock.createCodec(
            TallCrimsonSpinesBlock::new);

    private static final double OFFSET = 3.0;
    private static final VoxelShape SHAPE_LOWER = VoxelShapeUtil.createOffsetCuboid(OFFSET, 0.0);
    private static final VoxelShape SHAPE_UPPER = VoxelShapeUtil.createUpwardsCuboid(OFFSET, 0.0,
            15.0);
    private static final VoxelShape DAMAGE_SHAPE = VoxelShapeUtil.createUpwardsCuboid(OFFSET, 15.0,
            16.0);
    private static final VoxelShape SAFE_SHAPE = VoxelShapeUtil.createUpwardsCuboid(0.0, 0.0, 15.0);

    public static void placeAt(WorldAccess world, BlockState state, BlockPos pos, int flags) {
        BlockPos abovePos = pos.up();
        world.setBlockState(pos, TallCrimsonSpinesBlock.withFluidState(world, pos,
                state.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)), flags);
        world.setBlockState(abovePos, TallCrimsonSpinesBlock.withFluidState(world, abovePos,
                state.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER)), flags);
    }

    private static BlockState withFluidState(WorldView world, BlockPos pos, BlockState state) {
        state = state.with(Properties.WATERLOGGED, world.isWater(pos));
        int fluidIndex = Fluidlogged.getFluidIndex(world.getFluidState(pos).getFluid());
        if (fluidIndex > -1) {
            state = state.with(ModProperties.FLUIDLOGGED, fluidIndex);
        }
        return state;
    }

    public TallCrimsonSpinesBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(Properties.PERSISTENT, false)
                .with(Properties.WATERLOGGED, false)
                .with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction,
            BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(Properties.WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        DoubleBlockHalf half = state.get(Properties.DOUBLE_BLOCK_HALF);
        if (!(direction.getAxis() != Direction.Axis.Y || half == DoubleBlockHalf.LOWER != (direction
                == Direction.UP) || neighborState.isIn(ModBlockTags.TALL_CRIMSON_SPINES)
                && neighborState.get(Properties.DOUBLE_BLOCK_HALF) != half)) {
            return Blocks.AIR.getDefaultState();
        }
        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world,
                pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos,
                neighborPos);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos,
            ShapeContext context) {
        return switch (state.get(Properties.DOUBLE_BLOCK_HALF)) {
            case UPPER -> SHAPE_UPPER;
            case LOWER -> SHAPE_LOWER;
        };
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        // Spikes do not destroy items
        if (entity instanceof ItemEntity) {
            return;
        }

        // Only spike tips can damage entities
        if (state.get(Properties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.UPPER) {
            return;
        }

        if (!CrimsonSpikeBlock.isMoving(entity)) {
            return;
        }

        VoxelShape entityShape = VoxelShapes.cuboid(
                entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ()));

        // Only damage if entity is at the tip of the spike, not the sides
        if (VoxelShapes.matchesAnywhere(entityShape, SAFE_SHAPE, BooleanBiFunction.AND)
                || !VoxelShapes.matchesAnywhere(entityShape, DAMAGE_SHAPE, BooleanBiFunction.AND)) {
            return;
        }
        entity.damage(ModDamageTypes.createSource(world, ModDamageTypes.SPIKE), 2.0f);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();
        if (pos.getY() >= world.getTopY() - 1 || !world.getBlockState(pos.up()).canReplace(ctx)) {
            return null;
        }
        return this.getDefaultState()
                .with(Properties.PERSISTENT, true)
                .with(Properties.WATERLOGGED, world.isWater(pos));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack itemStack) {
        BlockPos abovePos = pos.up();
        BlockState aboveState = this.getStateWithProperties(state)
                .with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
        world.setBlockState(abovePos,
                TallCrimsonSpinesBlock.withFluidState(world, abovePos, aboveState));
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos belowPos = pos.down();
        BlockState belowState = world.getBlockState(belowPos);
        if (state.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            return belowState.isIn(ModBlockTags.TALL_CRIMSON_SPINES)
                    && belowState.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
        }
        return belowState.isSideSolidFullSquare(world, pos, Direction.UP);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient()) {
            if (player.isCreative()) {
                // Destroy bottom half without dropping an item
                BlockPos belowPos = pos.down();
                BlockState belowState = world.getBlockState(belowPos);
                DoubleBlockHalf half = state.get(Properties.DOUBLE_BLOCK_HALF);
                if (half == DoubleBlockHalf.UPPER && belowState.isIn(
                        ModBlockTags.TALL_CRIMSON_SPINES)
                        && belowState.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                    Fluid fluidAtPos = Fluidlogged.getFluid(belowState);
                    BlockState nextBlockState;
                    if (fluidAtPos == null) {
                        nextBlockState = Blocks.AIR.getDefaultState();
                    } else {
                        nextBlockState = fluidAtPos.getDefaultState().getBlockState();
                    }
                    world.setBlockState(belowPos, nextBlockState,
                            Block.NOTIFY_ALL | Block.SKIP_DROPS);
                    world.syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, belowPos,
                            Block.getRawIdFromState(belowState));
                }
            } else {
                Block.dropStacks(state, world, pos, null, player, player.getMainHandStack());
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state,
            @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.afterBreak(world, player, pos, Blocks.AIR.getDefaultState(), blockEntity, tool);
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity,
            float fallDistance) {
        if (state.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            entity.handleFallDamage(fallDistance + 1.0f, 1.5f,
                    ModDamageTypes.createSource(world, ModDamageTypes.SPIKE_FALL));
        } else {
            super.onLandedUpon(world, state, pos, entity, fallDistance);
        }
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return !state.get(Properties.PERSISTENT);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        // Decay over time
        if (SporeGrowthBlock.canDecay(world, pos, state, random)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false)
                : super.getFluidState(state);
    }

    @Override
    public float getMaxHorizontalModelOffset() {
        return 0.125f;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.PERSISTENT, Properties.WATERLOGGED, Properties.DOUBLE_BLOCK_HALF);
    }

    @Override
    protected MapCodec<? extends TallCrimsonSpinesBlock> getCodec() {
        return CODEC;
    }
}
