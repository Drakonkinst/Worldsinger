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

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import io.github.drakonkinst.worldsinger.util.VoxelShapeUtil;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public abstract class MultifaceBlock extends Block {

    private static final VoxelShape[] SHAPES = VoxelShapeUtil.createDirectionAlignedShapes(0.0,
            15.0, 16.0);

    private static VoxelShape getShapeForState(BlockState state) {
        VoxelShape voxelShape = VoxelShapes.empty();

        for (Direction direction : ModConstants.CARDINAL_DIRECTIONS) {
            if (MultifaceGrowthBlock.hasDirection(state, direction)) {
                voxelShape = VoxelShapes.union(voxelShape, SHAPES[direction.ordinal()]);
            }
        }

        return voxelShape.isEmpty() ? VoxelShapes.fullCube() : voxelShape;
    }

    private static BlockState disableDirection(BlockState state, BooleanProperty direction) {
        BlockState blockState = state.with(direction, Boolean.FALSE);
        return MultifaceBlock.hasAnyDirection(blockState) ? blockState
                : Blocks.AIR.getDefaultState();
    }

    private static BlockState withAllDirections(StateManager<Block, BlockState> stateManager) {
        BlockState blockState = stateManager.getDefaultState();

        for (BooleanProperty booleanProperty : ConnectingBlock.FACING_PROPERTIES.values()) {
            if (blockState.contains(booleanProperty)) {
                blockState = blockState.with(booleanProperty, Boolean.FALSE);
            }
        }

        return blockState;
    }

    protected static boolean hasAnyDirection(BlockState state) {
        return Arrays.stream(ModConstants.CARDINAL_DIRECTIONS)
                .anyMatch(direction -> MultifaceGrowthBlock.hasDirection(state, direction));
    }

    private static boolean isNotFullBlock(BlockState state) {
        return Arrays.stream(ModConstants.CARDINAL_DIRECTIONS)
                .anyMatch(direction -> !MultifaceGrowthBlock.hasDirection(state, direction));
    }

    private final ImmutableMap<BlockState, VoxelShape> stateToShapeMap;
    private final boolean hasAllHorizontalDirections;
    private final boolean canMirrorX;
    private final boolean canMirrorZ;

    public MultifaceBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(MultifaceBlock.withAllDirections(this.stateManager));
        this.stateToShapeMap = this.getShapesForStates(MultifaceBlock::getShapeForState);
        this.hasAllHorizontalDirections = Direction.Type.HORIZONTAL.stream()
                .allMatch(this::canHaveDirection);
        this.canMirrorX = Direction.Type.HORIZONTAL.stream()
                .filter(Direction.Axis.X)
                .filter(this::canHaveDirection)
                .count() % 2L == 0L;
        this.canMirrorZ = Direction.Type.HORIZONTAL.stream()
                .filter(Direction.Axis.Z)
                .filter(this::canHaveDirection)
                .count() % 2L == 0L;
    }

    @Override
    protected abstract MapCodec<? extends MultifaceBlock> getCodec();

    protected boolean canHaveDirection(Direction direction) {
        return true;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        for (Direction direction : DIRECTIONS) {
            if (this.canHaveDirection(direction)) {
                builder.add(MultifaceGrowthBlock.getProperty(direction));
            }
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockState nextState = state;
        int numChanged = 0;
        for (Direction direction : ModConstants.CARDINAL_DIRECTIONS) {
            if (MultifaceGrowthBlock.hasDirection(state, direction)) {
                BlockPos neighborPos = pos.offset(direction);
                BlockState neighborState = world.getBlockState(neighborPos);
                if (!MultifaceGrowthBlock.canGrowOn(world, direction, neighborPos, neighborState)) {
                    nextState = MultifaceBlock.disableDirection(state,
                            MultifaceGrowthBlock.getProperty(direction));
                    ++numChanged;
                }
            }
        }

        if (numChanged > 0) {
            Block.dropStack(world, pos, new ItemStack(this, numChanged));
            world.setBlockState(pos, nextState);
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction,
            BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!MultifaceBlock.hasAnyDirection(state)) {
            return Blocks.AIR.getDefaultState();
        } else {
            world.scheduleBlockTick(pos, this, 1);
            return state;
        }
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return context.getStack().isOf(this.asItem()) && MultifaceBlock.isNotFullBlock(state);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos,
            ShapeContext context) {
        return this.stateToShapeMap.get(state);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        boolean result = false;
        for (Direction direction : DIRECTIONS) {
            if (MultifaceGrowthBlock.hasDirection(state, direction)) {
                BlockPos blockPos = pos.offset(direction);
                if (!MultifaceGrowthBlock.canGrowOn(world, direction, blockPos,
                        world.getBlockState(blockPos))) {
                    return false;
                }
                result = true;
            }
        }
        return result;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        return Arrays.stream(ctx.getPlacementDirections())
                .map(direction -> this.withDirection(blockState, world, blockPos, direction))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public boolean canGrowWithDirection(BlockView world, BlockState state, BlockPos pos,
            Direction direction) {
        if (this.canHaveDirection(direction) && (!state.isOf(this)
                || !MultifaceGrowthBlock.hasDirection(state, direction))) {
            BlockPos blockPos = pos.offset(direction);
            return MultifaceGrowthBlock.canGrowOn(world, direction, blockPos,
                    world.getBlockState(blockPos));
        } else {
            return false;
        }
    }

    @Nullable
    public BlockState withDirection(BlockState state, BlockView world, BlockPos pos,
            Direction direction) {
        if (!this.canGrowWithDirection(world, state, pos, direction)) {
            return null;
        } else {
            BlockState blockState;
            if (state.isOf(this)) {
                blockState = state;
            } else if (this.isWaterlogged() && state.getFluidState()
                    .isEqualAndStill(Fluids.WATER)) {
                blockState = this.getDefaultState().with(Properties.WATERLOGGED, Boolean.TRUE);
            } else {
                blockState = this.getDefaultState();
            }

            return blockState.with(MultifaceGrowthBlock.getProperty(direction), Boolean.TRUE);
        }
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return !this.hasAllHorizontalDirections ? state : this.mirror(state, rotation::rotate);
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        if (mirror == BlockMirror.FRONT_BACK && !this.canMirrorX) {
            return state;
        } else {
            return mirror == BlockMirror.LEFT_RIGHT && !this.canMirrorZ ? state
                    : this.mirror(state, mirror::apply);
        }
    }

    private BlockState mirror(BlockState state, Function<Direction, Direction> mirror) {
        BlockState blockState = state;

        for (Direction direction : DIRECTIONS) {
            if (this.canHaveDirection(direction)) {
                blockState = blockState.with(
                        MultifaceGrowthBlock.getProperty(mirror.apply(direction)),
                        state.get(MultifaceGrowthBlock.getProperty(direction)));
            }
        }

        return blockState;
    }

    private boolean isWaterlogged() {
        return this.stateManager.getProperties().contains(Properties.WATERLOGGED);
    }
}
