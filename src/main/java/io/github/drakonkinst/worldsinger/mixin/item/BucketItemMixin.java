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
package io.github.drakonkinst.worldsinger.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.drakonkinst.worldsinger.fluid.Fluidlogged;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {

    @ModifyExpressionValue(method = "use", at = @At(value = "FIELD", target =
            "Lnet/minecraft/item/BucketItem;"
                    + "fluid:Lnet/minecraft/fluid/Fluid;", opcode = Opcodes.GETFIELD, ordinal = 2))
    private Fluid allowAnyFluid(Fluid original) {
        return Fluids.WATER;
    }

    @ModifyExpressionValue(method = "placeFluid", at = @At(value = "FIELD", target = "Lnet/minecraft/item/BucketItem;fluid:Lnet/minecraft/fluid/Fluid;", opcode = Opcodes.GETFIELD, ordinal = 0), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"), to = @At(value = "INVOKE", target = "Lnet/minecraft/block/FluidFillable;tryFillWithFluid(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;)Z")))
    private Fluid allowAnyFluid2(Fluid original) {
        return Fluids.WATER;
    }

    // play the right sound when draining a fluidlogged block
    @Inject(method = "use", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/block/FluidDrainable;"
                    + "getBucketFillSound()Ljava/util/Optional;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void playRightSound(World world, PlayerEntity user, Hand hand,
            CallbackInfoReturnable<TypedActionResult<ItemStack>> cir, ItemStack itemStack,
            BlockHitResult blockHitResult, BlockPos blockPos, Direction direction,
            BlockPos blockPos2, BlockState blockState, FluidDrainable fluidDrainable,
            ItemStack itemStack2) {
        Fluid fluid = Fluidlogged.getFluid(blockState);
        if (fluid != null) {
            fluid.getBucketFillSound()
                    .ifPresentOrElse(sound -> user.playSound(sound, 1.0F, 1.0F),
                            () -> user.playSound(Fluids.WATER.getBucketFillSound().orElseThrow(),
                                    1.0F, 1.0F));
        }
    }
}
