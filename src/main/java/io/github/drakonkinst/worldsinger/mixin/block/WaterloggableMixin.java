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
package io.github.drakonkinst.worldsinger.mixin.block;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.fluid.Fluidlogged;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Waterloggable.class)
public interface WaterloggableMixin {

    @Inject(method = "canFillWithFluid", at = @At("HEAD"), cancellable = true)
    private void canFillWithAnyFluid(@Nullable PlayerEntity player, BlockView world, BlockPos pos,
            BlockState state, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        if (state.contains(ModProperties.FLUIDLOGGED)) {
            cir.setReturnValue(
                    state.get(ModProperties.FLUIDLOGGED) == 0 && !state.get(Properties.WATERLOGGED)
                            && (fluid.equals(Fluids.WATER)
                            || Fluidlogged.WATERLOGGABLE_FLUIDS.contains(
                            Registries.FLUID.getId(fluid))));
        } else {
            cir.setReturnValue(!state.get(Properties.WATERLOGGED) && (fluid.equals(Fluids.WATER)));
        }
    }

    @Inject(method = "tryFillWithFluid", at = @At("HEAD"), cancellable = true)
    private void tryFillWithAnyFluid(WorldAccess world, BlockPos pos, BlockState state,
            FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        Fluid fluid = fluidState.getFluid();
        if (state.contains(ModProperties.FLUIDLOGGED) && !state.get(Properties.WATERLOGGED)
                && state.get(ModProperties.FLUIDLOGGED) == 0) {
            if (!world.isClient()) {
                BlockState newState = state;
                if (fluid.equals(Fluids.WATER)) {
                    newState = newState.with(Properties.WATERLOGGED, true);
                }
                int index = Fluidlogged.getFluidIndex(fluid);
                if (index == -1) {
                    Worldsinger.LOGGER.warn("Tried to fill a block with a not loggable fluid!");
                    cir.setReturnValue(false);
                    return;
                }
                world.setBlockState(pos, newState.with(ModProperties.FLUIDLOGGED, index),
                        Block.NOTIFY_ALL);
                world.scheduleFluidTick(pos, fluid, fluid.getTickRate(world));
            }
            cir.setReturnValue(true);
        } else if (!state.get(Properties.WATERLOGGED)) {
            if (!world.isClient()) {
                world.setBlockState(pos, state.with(Properties.WATERLOGGED, true),
                        Block.NOTIFY_ALL);
                world.scheduleFluidTick(pos, fluid, fluid.getTickRate(world));
            }
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tryDrainFluid", at = @At("HEAD"), cancellable = true)
    private void tryDrainAnyFluid(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos,
            BlockState state, CallbackInfoReturnable<ItemStack> cir) {
        if (state.get(Properties.WATERLOGGED) || (state.contains(ModProperties.FLUIDLOGGED)
                && state.get(ModProperties.FLUIDLOGGED) > 0)) {
            Fluid fluid = Fluidlogged.getFluid(state);
            if (state.get(Properties.WATERLOGGED)) {
                fluid = Fluids.WATER;
            }
            if (state.contains(ModProperties.FLUIDLOGGED)) {
                state = state.with(ModProperties.FLUIDLOGGED, 0);
            }
            world.setBlockState(pos, state.with(Properties.WATERLOGGED, false), Block.NOTIFY_ALL);
            if (!state.canPlaceAt(world, pos)) {
                world.breakBlock(pos, true);
            }
            if (fluid == null) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
            cir.setReturnValue(new ItemStack(fluid.getBucketItem()));
            return;
        }
        cir.setReturnValue(ItemStack.EMPTY);
    }

    @Inject(method = "getBucketFillSound", at = @At("HEAD"), cancellable = true)
    private void removeDefaultBucketFillSound(CallbackInfoReturnable<Optional<SoundEvent>> cir) {
        cir.setReturnValue(Optional.empty());
    }
}
