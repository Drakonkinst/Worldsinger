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
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import io.github.drakonkinst.worldsinger.registry.ModDamageTypes;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import java.util.function.ToIntFunction;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class SunlightBlock extends StillFluidBlock {

    public static final MapCodec<SunlightBlock> CODEC = AbstractBlock.createCodec(
            SunlightBlock::new);
    public static final ToIntFunction<BlockState> STATE_TO_LUMINANCE = (state) -> {
        int level = state.get(ModProperties.SUNLIGHT_LEVEL);
        if (level == 1) {
            // Equal to Magma Block
            return 3;
        }
        if (level == 2) {
            // Half luminance
            return 8;
        }
        // Full luminance
        return 15;
    };

    private static final float DAMAGE_PER_TICK = 4.0f;

    private static boolean isTouchingAnyWater(World world, BlockPos pos) {
        BlockPos.Mutable neighborPos = new BlockPos.Mutable();
        for (Direction direction : ModConstants.CARDINAL_DIRECTIONS) {
            neighborPos.set(pos.add(direction.getOffsetX(), direction.getOffsetY(),
                    direction.getOffsetZ()));
            if (world.isWater(neighborPos)) {
                return true;
            }
        }
        return false;
    }

    public SunlightBlock(Settings settings) {
        super(ModFluids.SUNLIGHT, settings);
        this.setDefaultState(this.getDefaultState().with(ModProperties.SUNLIGHT_LEVEL, 3));
    }

    @Override
    public boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return stateFrom.isOf(this);
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0f;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        state.getFluidState().onRandomTick(world, pos, random);

        int level = state.get(ModProperties.SUNLIGHT_LEVEL);
        if (level > 1) {
            world.setBlockState(pos, state.with(ModProperties.SUNLIGHT_LEVEL, level - 1));
        } else if (!SunlightBlock.isTouchingAnyWater(world, pos)) {
            // Will not decay fully if touching water, to halt unnecessary re-catalyzation
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock,
            BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if (SunlightBlock.isTouchingAnyWater(world, pos)) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            world.syncWorldEvent(WorldEvents.LAVA_EXTINGUISHED, pos, 0);
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        // Same damage and SFX as lava
        if (!entity.isFireImmune()) {
            entity.setOnFireFor(15);
        }

        if (entity.damage(ModDamageTypes.createSource(world, ModDamageTypes.SUNLIGHT),
                DAMAGE_PER_TICK)) {
            entity.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4f,
                    2.0f + world.getRandom().nextFloat() * 0.4f);
        }

        super.onEntityCollision(state, world, pos, entity);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        int sunlightLevel = state.get(ModProperties.SUNLIGHT_LEVEL);
        return super.getFluidState(state).with(ModProperties.SUNLIGHT_LEVEL, sunlightLevel);
    }

    public MapCodec<? extends SunlightBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ModProperties.SUNLIGHT_LEVEL);
    }
}
