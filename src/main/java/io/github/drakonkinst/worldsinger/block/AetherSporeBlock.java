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
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleSpawner;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class AetherSporeBlock extends FallingBlock implements FluidDrainable, SporeEmitting {

    public static final MapCodec<AetherSporeBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(AetherSpores.CODEC.fieldOf("sporeType")
                                    .forGetter(AetherSporeBlock::getSporeType), Registries.BLOCK.getCodec()
                                    .fieldOf("block")
                                    .forGetter(AetherSporeBlock::getFluidizedBlock),
                            AbstractBlock.createSettingsCodec())
                    .apply(instance,
                            (sporeType, fluidizedBlock1, settings1) -> new AetherSporeBlock(
                                    sporeType, settings1)));

    protected final AetherSpores sporeType;

    public AetherSporeBlock(AetherSpores sporeType, Settings settings) {
        super(settings);
        this.sporeType = sporeType;
    }

    @Override
    public void onLanding(World world, BlockPos pos, BlockState fallingBlockState,
            BlockState currentStateInPos, FallingBlockEntity fallingBlockEntity) {

        if (AetherSporeFluidBlock.shouldFluidize(world.getBlockState(pos.down()))) {
            // If it should immediately become a liquid, do not spawn particles
            world.setBlockState(pos, this.getFluidizedBlock().getDefaultState());
            return;
        }

        // Only spawn particles server-side when not submerged in a fluid
        if (world instanceof ServerWorld serverWorld && world.getFluidState(pos)
                .isOf(Fluids.EMPTY)) {
            // Spawn particles based on fall distance
            int fallDistance = fallingBlockEntity.getFallingBlockPos().getY() - pos.getY();
            if (fallDistance >= 4) {
                SporeParticleSpawner.spawnBlockParticles(serverWorld, sporeType, pos, 1.5, 0.45);
            } else {
                SporeParticleSpawner.spawnBlockParticles(serverWorld, sporeType, pos, 0.75, 0.5);
            }
        }
    }

    public Block getFluidizedBlock() {
        return sporeType.getFluidBlock();
    }

    @Override
    public void onDestroyedOnLanding(World world, BlockPos pos, FallingBlockEntity entity) {
        if (world instanceof ServerWorld serverWorld && world.getFluidState(pos)
                .isOf(Fluids.EMPTY)) {
            SporeParticleSpawner.spawnBlockParticles(serverWorld, sporeType, pos, 2.5, 0.45);
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World blockView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        BlockState fluidizedSource = blockView.getBlockState(blockPos.down());
        if (AetherSporeFluidBlock.shouldFluidize(fluidizedSource)) {
            // Become a fluid immediately if it should be fluidized
            return this.getFluidizedBlock().getDefaultState();
        }
        return super.getPlacementState(ctx);
    }

    @Override
    public ItemStack tryDrainFluid(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos,
            BlockState state) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState(),
                Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
        if (!world.isClient()) {
            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
        }
        return sporeType.getBucketItem().getDefaultStack();
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity,
            float fallDistance) {
        // Spawn splash particles on landing
        if (fallDistance > 0.25f && world instanceof ServerWorld serverWorld
                && !(entity instanceof FallingBlockEntity)) {
            SporeParticleSpawner.spawnSplashParticles(serverWorld, sporeType, entity, fallDistance,
                    false);
        }
        super.onLandedUpon(world, state, pos, entity, fallDistance);
    }

    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit,
            ProjectileEntity projectile) {
        // Spawn projectile particles on hit
        if (world instanceof ServerWorld serverWorld) {
            Vec3d pos = projectile.getPos();
            SporeParticleSpawner.spawnProjectileParticles(serverWorld, sporeType, pos);
        }
        super.onProjectileHit(world, state, hit, projectile);
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        // Spawn breaking particles on break
        if (world instanceof ServerWorld serverWorld) {
            SporeParticleSpawner.spawnBlockParticles(serverWorld, sporeType, pos, 0.6, 1.0);
        }
        super.onBroken(world, pos, state);
    }

    @Override
    public Optional<SoundEvent> getBucketFillSound() {
        return Optional.of(ModSoundEvents.ITEM_BUCKET_FILL_AETHER_SPORE);
    }

    @Override
    public int getColor(BlockState state, BlockView world, BlockPos pos) {
        return sporeType.getParticleColor();
    }

    @Override
    public AetherSpores getSporeType() {
        return sporeType;
    }

    @Override
    protected void configureFallingBlockEntity(FallingBlockEntity entity) {
        entity.dropItem = false;
    }

    @Override
    protected MapCodec<? extends AetherSporeBlock> getCodec() {
        return CODEC;
    }
}
