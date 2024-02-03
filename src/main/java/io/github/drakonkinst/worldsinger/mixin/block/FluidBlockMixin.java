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

import com.google.common.collect.ImmutableList;
import io.github.drakonkinst.worldsinger.cosmere.WaterReactionManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.fluid.Fluidlogged;
import io.github.drakonkinst.worldsinger.fluid.LivingAetherSporeFluid;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBlock.class)
public abstract class FluidBlockMixin {

    @Shadow
    @Final
    public static ImmutableList<Direction> FLOW_DIRECTIONS;
    @Shadow
    @Final
    protected FlowableFluid fluid;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void registerFluidBlocks(FlowableFluid fluid, AbstractBlock.Settings settings,
            CallbackInfo ci) {
        Fluidlogged.registerFluidBlockForFluid(fluid, (FluidBlock) (Object) this);
    }

    @Inject(method = "receiveNeighborFluids", at = @At("TAIL"), cancellable = true)
    private void addSporeFluidWaterInteraction(World world, BlockPos pos, BlockState state,
            CallbackInfoReturnable<Boolean> cir) {
        if (!(fluid instanceof LivingAetherSporeFluid sporeFluid)) {
            return;
        }

        AetherSpores sporeType = sporeFluid.getSporeType();

        for (Direction direction : FLOW_DIRECTIONS) {
            BlockPos neighborPos = pos.offset(direction.getOpposite());
            if (world.getFluidState(neighborPos).isIn(FluidTags.WATER)) {
                WaterReactionManager.catalyzeAroundWater(world, neighborPos);

                // Replace touching block with a different block if applicable, tends to help
                // with limiting spread.
                BlockState replacingState = sporeType.getFluidCollisionState();
                if (replacingState != null) {
                    world.setBlockState(neighborPos, replacingState);
                }
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
