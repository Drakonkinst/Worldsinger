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
import io.github.drakonkinst.worldsinger.mixin.accessor.AbstractBlockAccessor;
import io.github.drakonkinst.worldsinger.registry.ModDamageTypes;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import io.github.drakonkinst.worldsinger.util.VoxelShapeUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.Thickness;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class CrimsonSpikeBlock extends Block implements Waterloggable, SporeGrowthBlock {

    public static final MapCodec<CrimsonSpikeBlock> CODEC = AbstractBlock.createCodec(
            CrimsonSpikeBlock::new);

    private static final float MOVEMENT_THRESHOLD = 0.0003f;
    private static final double TIP_OFFSET = 5.0;
    private static final double TIP_HEIGHT = 15.0;
    private static final VoxelShape[] TIP_SHAPES = VoxelShapeUtil.createDirectionAlignedShapes(
            TIP_OFFSET, 0.0, TIP_HEIGHT);
    private static final VoxelShape[] TIP_SAFE_SHAPES = VoxelShapeUtil.createDirectionAlignedShapes(
            0.0, 0.0, TIP_HEIGHT);
    private static final VoxelShape[] TIP_DAMAGE_SHAPES = VoxelShapeUtil.createDirectionAlignedShapes(
            TIP_OFFSET, TIP_HEIGHT, 16.0);
    private static final VoxelShape[] FRUSTUM_SHAPES = VoxelShapeUtil.createAxisAlignedShapes(4.0,
            0.0);
    private static final VoxelShape[] MIDDLE_SHAPES = VoxelShapeUtil.createAxisAlignedShapes(3.0,
            0.0);
    private static final VoxelShape[] BASE_SHAPES = VoxelShapeUtil.createAxisAlignedShapes(2.0,
            0.0);

    // Conditional XZ offsetter that acts normally when on Y-axis, and is disabled on other axes.
    public static Offsetter getOffsetter() {
        return (state, world, pos) -> {
            if (state.get(Properties.FACING).getAxis() != Axis.Y) {
                return Vec3d.ZERO;
            }

            Block block = state.getBlock();
            long hashCode = MathHelper.hashCode(pos.getX(), 0, pos.getZ());
            float maxOffset = ((AbstractBlockAccessor) block).worldsinger$getMaxHorizontalModelOffset();
            double xOffset = MathHelper.clamp(
                    ((double) ((float) (hashCode & 0xFL) / 15.0f) - 0.5) * 0.5, -maxOffset,
                    maxOffset);
            double zOffset = MathHelper.clamp(
                    ((double) ((float) (hashCode >> 8 & 0xFL) / 15.0f) - 0.5) * 0.5, -maxOffset,
                    maxOffset);
            return new Vec3d(xOffset, 0.0, zOffset);
        };
    }

    @Nullable
    private static Direction getDirectionToPlaceAt(WorldView world, BlockPos pos,
            Direction direction) {
        if (CrimsonSpikeBlock.canPlaceAtWithDirection(world, pos, direction)) {
            return direction;
        } else if (CrimsonSpikeBlock.canPlaceAtWithDirection(world, pos, direction.getOpposite())) {
            return direction.getOpposite();
        } else {
            return null;
        }
    }

    private static Thickness getThickness(WorldView world, BlockPos pos,
            Direction attachDirection) {
        Direction nextDirection = attachDirection.getOpposite();
        BlockState blockState = world.getBlockState(pos.offset(attachDirection));
        if (CrimsonSpikeBlock.isCrimsonSpikeFacingDirection(blockState, nextDirection)) {
            return Thickness.TIP;
        }
        if (!CrimsonSpikeBlock.isCrimsonSpikeFacingDirection(blockState, attachDirection)) {
            return Thickness.TIP;
        }
        Thickness thickness = blockState.get(ModProperties.DISCRETE_THICKNESS);
        if (thickness == Thickness.TIP) {
            return Thickness.FRUSTUM;
        }
        BlockState blockState2 = world.getBlockState(pos.offset(nextDirection));
        if (!CrimsonSpikeBlock.isCrimsonSpikeFacingDirection(blockState2, attachDirection)) {
            return Thickness.BASE;
        }
        return Thickness.MIDDLE;
    }

    private static boolean canPlaceAtWithDirection(WorldView world, BlockPos pos,
            Direction direction) {
        BlockPos anchorPos = pos.offset(direction.getOpposite());
        BlockState anchorState = world.getBlockState(anchorPos);
        return anchorState.isSideSolidFullSquare(world, anchorPos, direction)
                || CrimsonSpikeBlock.isCrimsonSpikeFacingDirection(anchorState, direction);
    }

    private static boolean isCrimsonSpikeFacingDirection(BlockState state, Direction direction) {
        return (state.isIn(ModBlockTags.CRIMSON_SPIKE)
                && state.get(Properties.FACING) == direction);
    }

    public static boolean isMoving(Entity entity) {
        boolean isMoving =
                entity.lastRenderX != entity.getX() || entity.lastRenderY != entity.getY()
                        || entity.lastRenderZ != entity.getZ();
        if (isMoving) {
            double deltaX = Math.abs(entity.getX() - entity.lastRenderX);
            double deltaY = Math.abs(entity.getY() - entity.lastRenderY);
            double deltaZ = Math.abs(entity.getZ() - entity.lastRenderZ);
            return deltaX >= MOVEMENT_THRESHOLD || deltaY >= MOVEMENT_THRESHOLD
                    || deltaZ >= MOVEMENT_THRESHOLD;
        }
        return false;
    }

    public CrimsonSpikeBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState()
                .with(Properties.FACING, Direction.UP)
                .with(Properties.PERSISTENT, false)
                .with(Properties.WATERLOGGED, false)
                .with(ModProperties.DISCRETE_THICKNESS, Thickness.TIP));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        Direction direction = ctx.getSide();
        Direction placeDirection = CrimsonSpikeBlock.getDirectionToPlaceAt(world, pos,
                direction.getOpposite());

        if (placeDirection == null) {
            return null;
        }

        Thickness thickness = CrimsonSpikeBlock.getThickness(world, pos, placeDirection);
        if (thickness == null) {
            return null;
        }

        return this.getDefaultState()
                .with(Properties.FACING, placeDirection)
                .with(ModProperties.DISCRETE_THICKNESS, thickness)
                .with(Properties.PERSISTENT, true)
                .with(Properties.WATERLOGGED, world.isWater(pos));
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!this.canPlaceAt(state, world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return CrimsonSpikeBlock.canPlaceAtWithDirection(world, pos, state.get(Properties.FACING));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction,
            BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(Properties.WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        // if (direction != Direction.UP && direction != Direction.DOWN) {
        //     return state;
        // }
        Direction attachDirection = state.get(Properties.FACING);
        if (attachDirection == Direction.DOWN && world.getBlockTickScheduler()
                .isQueued(pos, this)) {
            return state;
        }
        if (direction == attachDirection.getOpposite() && !this.canPlaceAt(state, world, pos)) {
            world.scheduleBlockTick(pos, this, 1);
            return state;
        }
        Thickness thickness = CrimsonSpikeBlock.getThickness(world, pos, attachDirection);
        return state.with(ModProperties.DISCRETE_THICKNESS, thickness);
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
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity,
            float fallDistance) {
        if (state.get(Properties.FACING) == Direction.UP
                && state.get(ModProperties.DISCRETE_THICKNESS) == Thickness.TIP) {
            entity.handleFallDamage(fallDistance + 2.0f, 2.0f,
                    ModDamageTypes.createSource(world, ModDamageTypes.SPIKE_FALL));
        } else {
            super.onLandedUpon(world, state, pos, entity, fallDistance);
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        // Spikes do not destroy items
        if (entity instanceof ItemEntity) {
            return;
        }

        // Only spike tips can damage entities
        Thickness thickness = state.get(ModProperties.DISCRETE_THICKNESS);
        if (thickness != Thickness.TIP) {
            return;
        }

        if (!CrimsonSpikeBlock.isMoving(entity)) {
            return;
        }

        Direction facingDirection = state.get(Properties.FACING);
        VoxelShape entityShape = VoxelShapes.cuboid(
                entity.getBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ()));
        VoxelShape safeShape = TIP_SAFE_SHAPES[facingDirection.ordinal()];
        VoxelShape damageShape = TIP_DAMAGE_SHAPES[facingDirection.ordinal()];

        // Only damage if entity is at the tip of the spike, not the sides
        if (VoxelShapes.matchesAnywhere(entityShape, safeShape, BooleanBiFunction.AND)
                || !VoxelShapes.matchesAnywhere(entityShape, damageShape, BooleanBiFunction.AND)) {
            return;
        }
        entity.damage(ModDamageTypes.createSource(world, ModDamageTypes.SPIKE), 2.0f);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false)
                : super.getFluidState(state);
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos,
            ShapeContext context) {
        Thickness thickness = state.get(ModProperties.DISCRETE_THICKNESS);
        Direction direction = state.get(Properties.FACING);
        Axis axis = direction.getAxis();
        VoxelShape voxelShape;
        if (thickness == Thickness.TIP) {
            voxelShape = TIP_SHAPES[direction.ordinal()];
        } else if (thickness == Thickness.FRUSTUM) {
            voxelShape = FRUSTUM_SHAPES[axis.ordinal()];
        } else if (thickness == Thickness.MIDDLE) {
            voxelShape = MIDDLE_SHAPES[axis.ordinal()];
        } else {
            // Base
            voxelShape = BASE_SHAPES[axis.ordinal()];
        }
        if (axis == Axis.Y) {
            Vec3d modelOffset = state.getModelOffset(world, pos);
            return voxelShape.offset(modelOffset.x, 0.0, modelOffset.z);
        }
        return voxelShape;
    }

    @Override
    public boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    public float getMaxHorizontalModelOffset() {
        return 0.125f;
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return !state.get(Properties.PERSISTENT);
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.FACING, Properties.PERSISTENT, Properties.WATERLOGGED,
                ModProperties.DISCRETE_THICKNESS);
    }

    @Override
    protected MapCodec<? extends CrimsonSpikeBlock> getCodec() {
        return CODEC;
    }
}
