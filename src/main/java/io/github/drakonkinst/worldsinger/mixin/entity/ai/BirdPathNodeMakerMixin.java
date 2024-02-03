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

import io.github.drakonkinst.worldsinger.mixin.accessor.LandPathNodeMakerInvoker;
import io.github.drakonkinst.worldsinger.util.ModEnums;
import net.minecraft.entity.ai.pathing.BirdPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BirdPathNodeMaker.class)
public abstract class BirdPathNodeMakerMixin {

    @Inject(method = "getDefaultNodeType", at = @At("HEAD"), cancellable = true)
    private void avoidSporeBlocks(BlockView world, int x, int y, int z,
            CallbackInfoReturnable<PathNodeType> cir) {
        BlockPos pos = new BlockPos(x, y, z);
        PathNodeType pathNodeType = LandPathNodeMakerInvoker.worldsinger$getCommonNodeType(world,
                pos);
        if (pathNodeType == PathNodeType.OPEN && y >= world.getBottomY() + 1) {
            PathNodeType pathNodeTypeBelow = LandPathNodeMakerInvoker.worldsinger$getCommonNodeType(
                    world, pos.down());
            if (pathNodeTypeBelow == ModEnums.PathNodeType.AETHER_SPORE_SEA) {
                cir.setReturnValue(ModEnums.PathNodeType.AETHER_SPORE_SEA);
            }
        }
    }
}
