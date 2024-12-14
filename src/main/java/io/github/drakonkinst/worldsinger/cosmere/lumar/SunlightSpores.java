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
package io.github.drakonkinst.worldsinger.cosmere.lumar;

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.effect.ModStatusEffects;
import io.github.drakonkinst.worldsinger.entity.cannonball.CannonballBehavior;
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import io.github.drakonkinst.worldsinger.util.BoxUtil;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidDrainable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

public class SunlightSpores extends AetherSpores {

    public static final String NAME = "sunlight";
    public static final int ID = 3;

    private static final SunlightSpores INSTANCE = new SunlightSpores();
    private static final int COLOR = 0xf4bd52;
    private static final int PARTICLE_COLOR = 0xf4bd52;

    private static final int MAX_SUNLIGHT_SPREAD_DEPTH = 7;
    private static final int MAX_BLOCKS_AFFECTED = 65;
    private static final int WATER_PER_BLOCK = 12;
    private static final int EFFECT_RADIUS = 2;
    private static final int MIN_FIRE_WAVE_RADIUS = 2;
    private static final int MAX_FIRE_WAVE_RADIUS = 7;
    private static final int FIRE_WAVE_RADIUS_DIVISOR = 10;
    private static final int MAX_BLOCKS_PROCESSED = 1000;

    public static SunlightSpores getInstance() {
        return INSTANCE;
    }

    private SunlightSpores() {}

    @Override
    public void doReaction(World world, Vec3d pos, int spores, int water, Random random) {
        this.doSunlightSporeReaction(world, BlockPosUtil.toBlockPos(pos), water, random, true, 0);
    }

    private BlockPos getValidStartingBlock(World world, BlockPos center) {
        if (this.canFirstSunlightBlockReplace(world.getBlockState(center))) {
            return center;
        }
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Direction offset : ModConstants.CARDINAL_DIRECTIONS) {
            mutable.set(center).move(offset);
            if (this.canFirstSunlightBlockReplace(world.getBlockState(mutable))) {
                return mutable;
            }
        }
        return center;
    }

    public void doSunlightSporeReaction(World world, BlockPos pos, int water, Random random,
            boolean shouldSpreadSunlightBlocks, int fireWaveRadiusBonus) {
        // Adjust position to look for a good starting block
        pos = getValidStartingBlock(world, pos);

        // Number of blocks of Sunlight generated depends on amount of water
        int maxBlocks = Math.min(water / WATER_PER_BLOCK, MAX_BLOCKS_AFFECTED);
        if (maxBlocks <= 0) {
            return;
        }

        int fireWaveRadius = MIN_FIRE_WAVE_RADIUS + fireWaveRadiusBonus;

        if (shouldSpreadSunlightBlocks) {
            Set<BlockPos> affectedBlocks = this.spreadSunlightBlocks(world, pos, maxBlocks, random);

            // Set blocks on fire, evaporate water, deal damage
            if (!affectedBlocks.isEmpty()) {
                this.doReactionEffects(world, pos, affectedBlocks, random);
            }

            fireWaveRadius += affectedBlocks.size() / FIRE_WAVE_RADIUS_DIVISOR;
        }

        fireWaveRadius = Math.min(fireWaveRadius, MAX_FIRE_WAVE_RADIUS);
        this.doFireExplosion(world, pos, fireWaveRadius);

        // Show particles
        if (world instanceof ServerWorld serverWorld) {
            Vec3d center = pos.toCenterPos();
            serverWorld.spawnParticles(ParticleTypes.FLAME, center.getX(), center.getY(),
                    center.getZ(), 25, 0.0, 0.0, 0.0, 0.25);
        }

        // Play sound
        world.playSound(null, pos, ModSoundEvents.BLOCK_SUNLIGHT_SPORE_BLOCK_CATALYZE,
                SoundCategory.BLOCKS, 1.0f,
                (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f);
    }

    // Spread Sunlight blocks using BFS, replacing Sunlight Spore Sea / Sunlight Spore Blocks
    // Places at least one Sunlight block
    // Should place a consistent amount of Sunlight Blocks (if available)
    private Set<BlockPos> spreadSunlightBlocks(World world, BlockPos startPos, int maxBlocks,
            Random random) {
        Set<BlockPos> affectedBlocks = new HashSet<>();

        // Always generate one block of Sunlight at the point of interaction
        BlockState blockState = world.getBlockState(startPos);
        if (this.canFirstSunlightBlockReplace(blockState)) {
            // Not a waterlogged block
            world.setBlockState(startPos, ModBlocks.SUNLIGHT.getDefaultState());
            affectedBlocks.add(startPos);
        }
        if (affectedBlocks.size() >= maxBlocks) {
            return affectedBlocks;
        }

        LongSet visited = new LongOpenHashSet();
        Queue<IntObjectPair<BlockPos>> queue = new ArrayDeque<>();
        queue.add(new IntObjectImmutablePair<>(0, startPos));
        visited.add(startPos.asLong());

        while (!queue.isEmpty() && affectedBlocks.size() < maxBlocks) {
            IntObjectPair<BlockPos> nextPair = queue.remove();
            int depth = nextPair.leftInt();
            BlockPos nextPos = nextPair.right();
            BlockState state = world.getBlockState(nextPos);
            if (this.canSunlightReplace(state)) {
                // Replace with Sunlight
                world.setBlockState(nextPos, ModBlocks.SUNLIGHT.getDefaultState());
                affectedBlocks.add(nextPos);
            } else if (state.getFluidState().isOf(ModFluids.SUNLIGHT_SPORES)
                    && state.getBlock() instanceof FluidDrainable fluidDrainable) {
                fluidDrainable.tryDrainFluid(null, world, nextPos, state);
            } else if (!this.canSunlightPassThrough(state)) {
                continue;
            }

            if (depth >= MAX_SUNLIGHT_SPREAD_DEPTH) {
                continue;
            }

            // Add neighbors
            for (Direction direction : Direction.shuffle(random)) {
                BlockPos neighborPos = nextPos.add(direction.getOffsetX(), direction.getOffsetY(),
                        direction.getOffsetZ());
                long encodedNeighborPos = neighborPos.asLong();
                if (visited.add(encodedNeighborPos)) {
                    queue.add(new IntObjectImmutablePair<>(depth + 1, neighborPos));
                }
            }
        }
        return affectedBlocks;
    }

    private void doReactionEffects(World world, BlockPos originPos, Set<BlockPos> affectedBlocks,
            Random random) {
        LongSet blocksProcessed = new LongOpenHashSet();
        Mutable mutable = new Mutable();
        MutableBoolean anyEvaporated = new MutableBoolean();

        for (BlockPos pos : affectedBlocks) {
            if (blocksProcessed.size() >= MAX_BLOCKS_PROCESSED) {
                break;
            }
            this.doReactionEffectsForBlock(world, pos, blocksProcessed, mutable, anyEvaporated,
                    random);
        }
        if (anyEvaporated.booleanValue()) {
            world.playSound(null, originPos, ModSoundEvents.BLOCK_SUNLIGHT_EVAPORATE,
                    SoundCategory.BLOCKS, 1.0f,
                    (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f);
        }
    }

    private void doFireExplosion(World world, BlockPos pos, int radius) {
        Box box = BoxUtil.createBoxAroundBlock(pos, radius);
        List<LivingEntity> affectedEntities = world.getNonSpectatingEntities(LivingEntity.class,
                box);
        for (LivingEntity entity : affectedEntities) {
            if (world instanceof ServerWorld serverWorld) {
                entity.damage(serverWorld, entity.getDamageSources().inFire(), 3.0f);
            }
            entity.setOnFireFor(5);
        }
    }

    private boolean canSunlightReplace(BlockState state) {
        return state.isOf(ModBlocks.SUNLIGHT_SPORE_SEA) || state.isOf(
                ModBlocks.SUNLIGHT_SPORE_BLOCK);
    }

    private boolean canFirstSunlightBlockReplace(BlockState state) {
        return canSunlightReplace(state) || state.isIn(BlockTags.AIR);
    }

    private boolean canSunlightPassThrough(BlockState state) {
        return state.isOf(ModBlocks.SUNLIGHT) || state.getFluidState()
                .isOf(ModFluids.SUNLIGHT_SPORES);
    }

    private void doReactionEffectsForBlock(World world, BlockPos centerPos, LongSet blocksProcessed,
            Mutable mutable, MutableBoolean anyEvaporated, Random random) {
        blocksProcessed.add(centerPos.asLong());
        for (int offsetX = -EFFECT_RADIUS; offsetX <= EFFECT_RADIUS; ++offsetX) {
            for (int offsetY = -EFFECT_RADIUS; offsetY <= EFFECT_RADIUS; ++offsetY) {
                for (int offsetZ = -EFFECT_RADIUS; offsetZ <= EFFECT_RADIUS; ++offsetZ) {
                    mutable.set(centerPos.getX() + offsetX, centerPos.getY() + offsetY,
                            centerPos.getZ() + offsetZ);
                    if (!blocksProcessed.add(mutable.asLong())) {
                        continue;
                    }
                    this.processBlock(world, mutable, anyEvaporated, random);
                }
            }
        }
    }

    private void processBlock(World world, BlockPos mutable, MutableBoolean anyEvaporated,
            Random random) {
        BlockState state = world.getBlockState(mutable);
        if (state.getBlock() instanceof FluidDrainable fluidDrainable && state.getFluidState()
                .isOf(Fluids.WATER)) {
            // Evaporate water
            fluidDrainable.tryDrainFluid(null, world, mutable, state);
            anyEvaporated.setTrue();
        }

        if (random.nextInt(3) == 0 && state.isAir()) {
            // Set fire
            world.setBlockState(mutable, Blocks.FIRE.getDefaultState());
        }
    }

    @Override
    public void doReactionFromFluidContainer(World world, BlockPos fluidContainerPos, int spores,
            int water, Random random) {
        this.doSunlightSporeReaction(world, fluidContainerPos, water, random, false, 2);
    }

    @Override
    public void doReactionFromParticle(World world, Vec3d pos, int spores, int water, Random random,
            boolean affectingFluidContainer) {
        this.doSunlightSporeReaction(world, BlockPosUtil.toBlockPos(pos), water, random,
                spores >= CannonballBehavior.SPORE_AMOUNT, 0);
    }

    @Override
    public void onDeathFromStatusEffect(World world, LivingEntity entity, BlockPos pos, int water) {
        this.doSunlightSporeReaction(world, pos, water, world.getRandom(), false, 0);
    }

    @Override
    public Item getBottledItem() {
        return ModItems.SUNLIGHT_SPORES_BOTTLE;
    }

    @Override
    public Item getBucketItem() {
        return ModItems.SUNLIGHT_SPORES_BUCKET;
    }

    @Override
    public Block getFluidBlock() {
        return ModBlocks.SUNLIGHT_SPORE_SEA;
    }

    @Override
    public Block getSolidBlock() {
        return ModBlocks.SUNLIGHT_SPORE_BLOCK;
    }

    @Override
    public FlowableFluid getFluid() {
        return ModFluids.SUNLIGHT_SPORES;
    }

    @Override
    public RegistryEntry<StatusEffect> getStatusEffect() {
        return ModStatusEffects.SUNLIGHT_SPORES;
    }

    @Override
    public int getColor() {
        return COLOR;
    }

    @Override
    public int getParticleColor() {
        return PARTICLE_COLOR;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public @Nullable BlockState getFluidCollisionState() {
        return ModBlocks.SUNLIGHT.getDefaultState();
    }
}
