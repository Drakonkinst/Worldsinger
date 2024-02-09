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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.api.fluid.VariantApi;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CauldronBehavior.class)
public interface CauldronBehaviorMixin {

    @WrapOperation(method = "fillCauldron", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
    private static boolean fillCauldronVariant(World instance, BlockPos pos, BlockState state,
            Operation<Boolean> original) {

        Block interactedBlock = instance.getBlockState(pos).getBlock();
        Optional<Block> newBlock = VariantApi.getBlockVariant(interactedBlock, state.getBlock());
        if (newBlock.isPresent()) {
            return original.call(instance, pos, newBlock.get().getStateWithProperties(state));
        }
        return original.call(instance, pos, state);
    }

    @WrapOperation(method = "emptyCauldron", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z"))
    private static boolean emptyCauldronVariant(World instance, BlockPos pos, BlockState newState,
            Operation<Boolean> original, BlockState originalState, World world,
            BlockPos originalPos, PlayerEntity player, Hand hand, ItemStack stack, ItemStack output,
            Predicate<BlockState> fullPredicate, SoundEvent soundEvent) {
        Block interactedBlock = originalState.getBlock();
        Optional<Block> variantBlock = VariantApi.getBlockVariant(interactedBlock, Blocks.CAULDRON);
        if (variantBlock.isPresent()) {
            return original.call(instance, pos, variantBlock.get().getDefaultState());
        }
        return original.call(instance, pos, newState);
    }
}
