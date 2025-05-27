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
package io.github.drakonkinst.worldsinger.item;

import io.github.drakonkinst.worldsinger.block.AetherSporeBlock;
import io.github.drakonkinst.worldsinger.block.AetherSporeFluidBlock;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeKillingUtil;
import io.github.drakonkinst.worldsinger.fluid.Fluidlogged;
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class AetherSporeBucketItem extends BlockItem implements FluidModificationItem {

    private final SoundEvent placeSound;
    private final Block fluidBlock;
    private final FlowableFluid fluid;

    public AetherSporeBucketItem(Block block, FlowableFluid fluid, SoundEvent placeSound,
            Settings settings) {
        super(block, settings);
        if (block instanceof AetherSporeBlock aetherSporeBlock) {
            this.fluidBlock = aetherSporeBlock.getFluidizedBlock();
        } else {
            throw new IllegalArgumentException(
                    "Aether Spore Buckets should only be created from Aether Spore Blocks");
        }
        this.fluid = fluid;
        this.placeSound = placeSound;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack handStack = user.getStackInHand(hand);
        BlockHitResult blockHitResult = Item.raycast(world, user, FluidHandling.NONE);
        if (blockHitResult.getType() != Type.BLOCK) {
            return ActionResult.PASS;
        }
        BlockPos blockPos = blockHitResult.getBlockPos();
        Direction direction = blockHitResult.getSide();
        BlockPos adjacentBlock = blockPos.offset(direction);
        if (!world.canEntityModifyAt(user, blockPos) || !user.canPlaceOn(adjacentBlock, direction,
                handStack)) {
            return ActionResult.FAIL;
        }
        BlockState blockState = world.getBlockState(blockPos);

        // Place into a FluidFillable block if possible
        BlockPos placementBlockPos;
        if (blockState.getBlock() instanceof FluidFillable
                && Fluidlogged.getFluidIndex(this.fluid) > 0) {
            placementBlockPos = blockPos;
        } else {
            placementBlockPos = adjacentBlock;
        }

        // Can only place fluid if the block can be fluidized
        if (AetherSporeFluidBlock.shouldFluidize(world.getBlockState(placementBlockPos.down()))
                && this.placeFluid(user, world, placementBlockPos, blockHitResult)) {
            this.onEmptied(user, world, handStack, placementBlockPos);
            if (user instanceof ServerPlayerEntity) {
                Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity) user, placementBlockPos,
                        handStack);
            }

            user.incrementStat(Stats.USED.getOrCreateStat(this));
            // We don't need to exchange the stack here because it gets consumed later
            return ActionResult.SUCCESS;
        }

        // Writing in some hacks to be able to place a bucket in the same fluid, not sure why this isn't working normally
        FluidState fluidState = world.getFluidState(placementBlockPos);
        if (fluidState.isOf(fluid) && fluidState.isStill()) {
            playEmptyingSound(world, user, placementBlockPos);
            return ActionResult.SUCCESS;
        }

        // Try placing as a normal block
        return ActionResult.PASS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return super.useOnBlock(context);
        }

        ActionResult result = attemptUse(context, player);
        if (result.isAccepted() && !player.isInCreativeMode()) {
            Hand hand = context.getHand();
            player.setStackInHand(hand, Items.BUCKET.getDefaultStack());
        }
        return result;
    }

    private ActionResult attemptUse(ItemUsageContext context, PlayerEntity player) {
        ActionResult fluidPlaceResult = this.use(context.getWorld(), player, context.getHand());
        if (fluidPlaceResult.isAccepted()) {
            return fluidPlaceResult;
        }
        return super.useOnBlock(context);
    }

    @Override
    public boolean placeFluid(@Nullable LivingEntity entity, World world, BlockPos pos,
            @Nullable BlockHitResult hitResult) {
        if (!world.isInBuildLimit(pos)) {
            return false;
        }
        boolean fluidized = AetherSporeFluidBlock.shouldFluidize(world.getBlockState(pos.down()));
        Block blockToPlace = fluidized ? fluidBlock : this.getBlock();
        BlockState currentState = world.getBlockState(pos);

        if (currentState.isAir()) {
            if (!world.isClient()) {
                world.setBlockState(pos, blockToPlace.getDefaultState(), Block.NOTIFY_ALL);
            }
            playEmptyingSound(world, entity, pos);
            return true;
        } else if (fluidized && currentState.getBlock() instanceof FluidFillable fluidFillable) {
            FluidState state = fluid.getStill(false);
            if (!state.isOf(ModFluids.DEAD_SPORES) && SporeKillingUtil.isSporeKillingBlockNearby(
                    world, pos)) {
                state = ModFluids.DEAD_SPORES.getStill(false);
            }
            fluidFillable.tryFillWithFluid(world, pos, currentState, state);
            playEmptyingSound(world, entity, pos);
            return true;
        }
        return false;
    }

    private void playEmptyingSound(World world, LivingEntity entity, BlockPos pos) {
        world.playSound(entity, pos, this.placeSound, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.emitGameEvent(entity, GameEvent.FLUID_PLACE, pos);
    }
}
