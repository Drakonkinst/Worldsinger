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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.cosmere.WaterReactionManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class LivingAetherSporeBlock extends AetherSporeBlock implements SporeKillable,
        WaterReactiveBlock {

    public static final MapCodec<LivingAetherSporeBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(AetherSpores.CODEC.fieldOf("sporeType")
                                    .forGetter(LivingAetherSporeBlock::getSporeType), Block.CODEC.fieldOf("block")
                                    .forGetter(LivingAetherSporeBlock::getFluidizedBlock),
                            AbstractBlock.createSettingsCodec())
                    .apply(instance, LivingAetherSporeBlock::new));

    public static final int CATALYZE_VALUE = 250;

    public LivingAetherSporeBlock(AetherSpores aetherSporeType, Block fluidized,
            Settings settings) {
        super(aetherSporeType, settings);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockPos waterNeighborPos = LivingVerdantVineBlock.getWaterNeighborPos(world, pos);
        if (waterNeighborPos != null) {
            WaterReactionManager.catalyzeAroundWater(world, waterNeighborPos);
            BlockState replacingState = sporeType.getFluidCollisionState();
            if (replacingState != null) {
                world.setBlockState(pos, replacingState);
            }
            return;
        }
        super.scheduledTick(state, world, pos, random);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        if (world.hasRain(pos.up())) {
            this.reactToWater(world, pos, state, Integer.MAX_VALUE, random);
        }
    }

    @Override
    public boolean reactToWater(World world, BlockPos pos, BlockState state, int waterAmount,
            Random random) {
        world.removeBlock(pos, false);
        sporeType.doReaction(world, pos, CATALYZE_VALUE, waterAmount, random);
        return true;
    }

    @Override
    public boolean canReactToWater(BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction,
            BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        BlockPos waterNeighborPos = LivingVerdantVineBlock.getWaterNeighborPos(world, pos);
        if (waterNeighborPos != null) {
            world.scheduleBlockTick(pos, this, 1);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos,
                neighborPos);
    }

    @Override
    public Block getDeadSporeBlock() {
        return ModBlocks.DEAD_SPORE_BLOCK;
    }

    @Override
    public Type getReactiveType() {
        return AetherSpores.getReactiveTypeFromSpore(sporeType);
    }

    @Override
    protected MapCodec<? extends LivingAetherSporeBlock> getCodec() {
        return CODEC;
    }
}
