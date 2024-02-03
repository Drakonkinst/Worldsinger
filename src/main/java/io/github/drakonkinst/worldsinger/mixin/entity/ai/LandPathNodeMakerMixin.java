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
package io.github.drakonkinst.worldsinger.mixin.entity.ai;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.drakonkinst.worldsinger.block.ModBlockTags;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.mixin.accessor.LandPathNodeMakerInvoker;
import io.github.drakonkinst.worldsinger.util.ModEnums;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LandPathNodeMaker.class)
public abstract class LandPathNodeMakerMixin extends PathNodeMaker {

    // Modded logic takes precedence for modded blocks, so inject from head and cancel
    // is more efficient (if less syntactically nice) than @ModifyReturnValue
    @Inject(method = "getCommonNodeType", at = @At("HEAD"), cancellable = true)
    private static void addModBlocksNodeTypes(BlockView world, BlockPos pos,
            CallbackInfoReturnable<PathNodeType> cir) {
        BlockState state = world.getBlockState(pos);

        if (state.isOf(ModBlocks.SUNLIGHT)) {
            cir.setReturnValue(PathNodeType.LAVA);
            return;
        }

        if (state.isIn(ModBlockTags.CRIMSON_SPIKE) || state.isIn(ModBlockTags.TALL_CRIMSON_SPINES)
                || state.isIn(ModBlockTags.AETHER_SPORE_BLOCKS)) {
            cir.setReturnValue(PathNodeType.DAMAGE_CAUTIOUS);
            return;
        }

        if (state.isIn(ModBlockTags.CRIMSON_SPINES) || state.isIn(ModBlockTags.CRIMSON_SNARE)) {
            cir.setReturnValue(PathNodeType.DAMAGE_OTHER);
            return;
        }

        // Any silver blocks that cannot be walked through
        if (state.isIn(ModBlockTags.SILVER_WALKABLE)) {
            cir.setReturnValue(ModEnums.PathNodeType.BLOCKING_SILVER);
            return;
        }

        // Any silver blocks that can be walked through
        if (state.isIn(ModBlockTags.HAS_SILVER)) {
            cir.setReturnValue(ModEnums.PathNodeType.DAMAGE_SILVER);
            return;
        }

        FluidState fluidState = world.getFluidState(pos);
        if (fluidState.isIn(ModFluidTags.AETHER_SPORES)) {
            cir.setReturnValue(ModEnums.PathNodeType.AETHER_SPORE_SEA);
        }
    }

    // Specifically runs after DANGER_OTHER and DANGER_FIRE, which should take precedence
    @Inject(method = "getNodeTypeFromNeighbors", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/BlockView;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"), cancellable = true)
    private static void addSilverNodeTypes(BlockView world, Mutable pos, PathNodeType nodeType,
            CallbackInfoReturnable<PathNodeType> cir, @Local BlockState blockState) {
        if (blockState.isIn(ModBlockTags.HAS_SILVER)) {
            cir.setReturnValue(ModEnums.PathNodeType.DANGER_SILVER);
        }
    }

    @Inject(method = "getLandNodeType", at = @At("HEAD"), cancellable = true)
    private static void avoidSporeBlocks(BlockView world, BlockPos.Mutable pos,
            CallbackInfoReturnable<PathNodeType> cir) {
        int y = pos.getY();
        PathNodeType pathNodeType = LandPathNodeMakerInvoker.worldsinger$getCommonNodeType(world,
                pos);
        if (pathNodeType == PathNodeType.OPEN && y >= world.getBottomY() + 1) {
            PathNodeType pathNodeTypeBelow = LandPathNodeMakerInvoker.worldsinger$getCommonNodeType(
                    world, pos.down());
            if (pathNodeTypeBelow == ModEnums.PathNodeType.AETHER_SPORE_SEA) {
                cir.setReturnValue(ModEnums.PathNodeType.AETHER_SPORE_SEA);
            }
            if (pathNodeTypeBelow == ModEnums.PathNodeType.BLOCKING_SILVER) {
                cir.setReturnValue(ModEnums.PathNodeType.DAMAGE_SILVER);
            }
        }
    }
}
