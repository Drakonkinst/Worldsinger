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
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleSpawner;
import io.github.drakonkinst.worldsinger.fluid.AetherSporeFluid;
import io.github.drakonkinst.worldsinger.fluid.FluidShapes;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.mixin.accessor.FluidBlockAccessor;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class AetherSporeFluidBlock extends FluidBlock implements SporeEmitting {

    // Unused Codec
    public static final MapCodec<AetherSporeFluidBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(FluidBlockAccessor.worldsinger$getFluidCodec()
                                    .fieldOf("fluid")
                                    .forGetter(block -> block.fluid), AetherSpores.CODEC.fieldOf("sporeType")
                                    .forGetter(AetherSporeFluidBlock::getSporeType),
                            AbstractBlock.createSettingsCodec())
                    .apply(instance,
                            (fluid1, sporeType, settings1) -> new AetherSporeFluidBlock(sporeType,
                                    settings1)));

    // Update fluidization from a source of spores
    public static void update(WorldAccess world, BlockPos pos, BlockState blockState,
            BlockState fluidizeSource) {
        if (!AetherSporeFluidBlock.isAnySporeSource(blockState)) {
            return;
        }

        BlockPos.Mutable mutable = pos.mutableCopy();
        boolean fluidized = AetherSporeFluidBlock.shouldFluidize(fluidizeSource);

        // Iterate upwards and update fluidization
        while (AetherSporeFluidBlock.updateFluidizationForBlock(world, mutable,
                world.getBlockState(mutable), fluidized)
                && mutable.getY() < world.getTopYInclusive()) {
            mutable.move(Direction.UP);
        }
    }

    private static boolean isAnySporeSource(BlockState state) {
        return AetherSporeFluidBlock.isSolidSpores(state)
                || AetherSporeFluidBlock.isFluidSourceSpores(state)
                || AetherSporeFluidBlock.isFluidloggedInSpores(state);
    }

    // Returns whether a block should fluidize based on fluidizeSource, which is
    // generally the block underneath it
    public static boolean shouldFluidize(BlockState fluidizeSource) {
        return fluidizeSource.isOf(Blocks.MAGMA_BLOCK) || fluidizeSource.isOf(ModBlocks.MAGMA_VENT)
                || AetherSporeFluidBlock.isFluidSourceSpores(fluidizeSource)
                || AetherSporeFluidBlock.isFluidloggedInSpores(fluidizeSource);
    }

    // Update fluidization for a single block. Returns false if obstructed
    public static boolean updateFluidizationForBlock(WorldAccess world, BlockPos pos,
            BlockState blockState, boolean fluidized) {
        // Fix blocks getting overridden where they shouldn't be
        if (!world.getBlockState(pos).isOf(blockState.getBlock())) {
            return false;
        }

        if (fluidized) {
            return AetherSporeFluidBlock.fluidizeBlock(world, pos, blockState);
        } else {
            return AetherSporeFluidBlock.solidifyBlock(world, pos, blockState);
        }
    }

    private static boolean isSolidSpores(BlockState state) {
        return state.isIn(ModBlockTags.AETHER_SPORE_BLOCKS);
    }

    private static boolean isFluidSourceSpores(BlockState state) {
        return state.isIn(ModBlockTags.AETHER_SPORE_SEA_BLOCKS)
                && state.getFluidState().getLevel() >= 8 && state.getFluidState().isStill();
    }

    private static boolean isFluidloggedInSpores(BlockState state) {
        return state.getBlock() instanceof Waterloggable && state.getFluidState()
                .isIn(ModFluidTags.AETHER_SPORES);
    }

    // Ensure block is fluidized. Returns false if obstructed
    private static boolean fluidizeBlock(WorldAccess world, BlockPos pos, BlockState blockState) {
        Block block = blockState.getBlock();

        // No need to modify fluid blocks
        if (block instanceof AetherSporeFluidBlock || AetherSporeFluidBlock.isFluidloggedInSpores(
                blockState)) {
            return true;
        }

        // Turn solid into fluid
        if (block instanceof AetherSporeBlock aetherSporeBlock) {
            BlockState newBlockState = aetherSporeBlock.getFluidizedBlock().getDefaultState();
            return world.setBlockState(pos, newBlockState, Block.NOTIFY_LISTENERS);
        }

        return false;
    }

    private static boolean solidifyBlock(WorldAccess world, BlockPos pos, BlockState blockState) {
        Block block = blockState.getBlock();

        // No need to modify solid blocks
        if (block instanceof AetherSporeBlock) {
            return true;
        }

        // Turn fluid into solid
        if (block instanceof AetherSporeFluidBlock aetherSporeFluidBlock) {
            BlockState newBlockState = aetherSporeFluidBlock.getSolidBlock().getDefaultState();
            return world.setBlockState(pos, newBlockState, Block.NOTIFY_ALL);
        }

        // Dissolve fluidlogged fluid into particles
        if (blockState.getBlock() instanceof Waterloggable waterloggable
                && AetherSporeFluidBlock.isFluidloggedInSpores(blockState)) {
            Fluid fluid = blockState.getFluidState().getFluid();
            // Spawn dissolving particles
            if (fluid instanceof AetherSporeFluid sporeFluid
                    && world instanceof ServerWorld serverWorld) {
                SporeParticleSpawner.spawnBlockParticles(serverWorld, sporeFluid.getSporeType(),
                        pos, 0.6, 1.0);
            }
            waterloggable.tryDrainFluid(null, world, pos, blockState);
            return true;
        }

        return false;
    }

    protected final AetherSpores sporeType;

    public AetherSporeFluidBlock(AetherSpores sporeType, Settings settings) {
        super(sporeType.getFluid(), settings);
        this.sporeType = sporeType;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, WorldView world,
            ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos,
            BlockState neighborState, Random random) {
        if (direction == Direction.DOWN) {
            // If the block beneath is changed, update fluidization
            tickView.scheduleBlockTick(pos, this, 5);
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos,
                neighborState, random);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        AetherSporeFluidBlock.update(world, pos, state, world.getBlockState(pos.down()));
    }

    public Block getSolidBlock() {
        return this.sporeType.getSolidBlock();
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity,
            float fallDistance) {
        // Spawn splash particles upon landing (during stilling)
        if (fallDistance > 0.25f && world instanceof ServerWorld serverWorld) {
            SporeParticleSpawner.spawnSplashParticles(serverWorld, sporeType, entity, fallDistance,
                    false);
        }
        super.onLandedUpon(world, state, pos, entity, fallDistance);
    }

    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit,
            ProjectileEntity projectile) {
        // Spawn projectile particles upon landing (during stilling)
        if (world instanceof ServerWorld serverWorld) {
            SporeParticleSpawner.spawnProjectileParticles(serverWorld, sporeType,
                    projectile.getPos());
        }
        super.onProjectileHit(world, state, hit, projectile);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos,
            ShapeContext context) {
        FluidState fluidState = state.getFluidState();

        // If ShapeContext implements WorldShapeContext, use it to make the block solid
        // during stillings (mainly for entities). Otherwise, assume it is transparent.
        if (fluidState.isStill() && context instanceof WorldShapeContextAccess shapeContext) {
            World realWorld = shapeContext.worldsinger$getWorld();
            if (realWorld != null && !SeetheManager.areSporesFluidized(realWorld)) {
                return FluidShapes.VOXEL_SHAPES[fluidState.getLevel()];
            }
        }

        return super.getCollisionShape(state, world, pos, context);
    }

    public AetherSpores getSporeType() {
        return sporeType;
    }

    // Due to how FluidBlock is implemented, can't return the right type here.
    // @Override
    // public MapCodec<? extends AetherSporeFluidBlock> getCodec() {
    //     return CODEC;
    // }
}
