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
import io.github.drakonkinst.worldsinger.util.VoxelShapeUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class TwistingVerdantVineBlock extends AbstractVerticalGrowthBudBlock implements
        Waterloggable, SporeGrowthBlock {

    private static final VoxelShape SHAPE = VoxelShapeUtil.createUpwardsCuboid(4.0, 0.0, 15.0);
    public static final MapCodec<TwistingVerdantVineBlock> CODEC = AbstractBlock.createCodec(
            TwistingVerdantVineBlock::new);

    public static boolean canAttach(BlockState state, BlockState attachCandidate) {
        // Can attach to other branches
        if (attachCandidate.isIn(ModBlockTags.VERDANT_VINE_BRANCH)) {
            return true;
        }
        // Can attach to a snare block if direction matches
        if (attachCandidate.isIn(ModBlockTags.VERDANT_VINE_SNARE)) {
            return attachCandidate.get(Properties.FACING)
                    == AbstractVerticalGrowthComponentBlock.getGrowthDirection(state);
        }
        return false;
    }

    public TwistingVerdantVineBlock(Settings settings) {
        super(settings, SHAPE);
        this.setDefaultState(this.getDefaultState()
                .with(Properties.PERSISTENT, false)
                .with(Properties.WATERLOGGED, false));
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState placementState = super.getPlacementState(ctx);
        if (placementState != null) {
            placementState = placementState.with(Properties.PERSISTENT, true)
                    .with(Properties.WATERLOGGED, ctx.getWorld().isWater(ctx.getBlockPos()));
        }
        return placementState;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false)
                : super.getFluidState(state);
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
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction,
            BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(Properties.WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos,
                neighborPos).with(Properties.PERSISTENT, state.get(Properties.PERSISTENT));
    }

    @Override
    protected boolean canAttachTo(BlockState state, BlockState attachCandidate) {
        return TwistingVerdantVineBlock.canAttach(state, attachCandidate);
    }

    @Override
    protected Block getStem() {
        return ModBlocks.DEAD_TWISTING_VERDANT_VINES_PLANT;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.PERSISTENT, Properties.WATERLOGGED);
        super.appendProperties(builder);
    }

    @Override
    protected MapCodec<? extends TwistingVerdantVineBlock> getCodec() {
        return CODEC;
    }
}
