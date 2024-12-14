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

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.ThirstManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightSpores;
import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import io.github.drakonkinst.worldsinger.particle.ModParticleTypes;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class MidnightEssenceBlock extends Block {

    private static final int WATER_COST = 4;

    public MidnightEssenceBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getDefaultState().with(Properties.PERSISTENT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.PERSISTENT);
        super.appendProperties(builder);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState placementState = super.getPlacementState(ctx);
        if (placementState != null) {
            placementState = placementState.with(Properties.PERSISTENT, true);
        }
        return placementState;
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return !state.get(Properties.PERSISTENT);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        // Decay over time
        if (random.nextInt(5) == 0) {
            world.breakBlock(pos, true);
            Vec3d centerPos = pos.toCenterPos();

            // randomTick() is always called server-side, so use spawnParticles()
            world.spawnParticles(ModParticleTypes.MIDNIGHT_ESSENCE, centerPos.getX(),
                    centerPos.getY(), centerPos.getZ(), 5, 0.5, 0.5, 0.5, 0.0);
        }
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        super.onBroken(world, pos, state);
        MidnightSpores.addMidnightParticles(world, new Box(pos), world.getRandom(), 0.1, 5);
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0f;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,
            BlockHitResult hit) {
        Difficulty difficulty = world.getDifficulty();
        // Cannot be interacted with on peaceful
        if (difficulty == Difficulty.PEACEFUL) {
            // Still swing hand to indicate it can be interacted with, but not on this difficulty
            return ActionResult.SUCCESS;
        }
        ThirstManager thirstManager = player.getAttachedOrCreate(ModAttachmentTypes.THIRST);
        if (!player.isCreative() && thirstManager.get() < WATER_COST) {
            // Not enough water to summon anything, but should still swing hand
            return ActionResult.SUCCESS;
        }
        if (!player.isCreative()) {
            thirstManager.remove(WATER_COST);
        }
        world.removeBlock(pos, false);
        MidnightCreatureEntity entity = new MidnightCreatureEntity(world);
        entity.setMidnightEssenceAmount(1);
        entity.setPosition(new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
        entity.setController(player);
        entity.acceptWaterBribe(player, MidnightCreatureEntity.INITIAL_BRIBE);
        if (!world.isClient()) {
            boolean success = world.spawnEntity(entity);
            if (!success) {
                Worldsinger.LOGGER.warn("Failed to spawn Midnight Creature Entity");
            }
        }
        world.playSound(null, pos, ModSoundEvents.ENTITY_MIDNIGHT_CREATURE_AMBIENT,
                SoundCategory.BLOCKS, 1.0f, 1.0f);
        return ActionResult.SUCCESS;
    }
}
