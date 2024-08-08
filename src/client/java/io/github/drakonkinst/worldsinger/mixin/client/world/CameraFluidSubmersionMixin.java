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
package io.github.drakonkinst.worldsinger.mixin.client.world;

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.util.ModEnums;
import io.github.drakonkinst.worldsinger.world.CameraPosAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraFluidSubmersionMixin implements CameraPosAccess {

    @Shadow
    private BlockView area;
    @Shadow
    private Vec3d pos;
    @Shadow
    @Final
    private Mutable blockPos;
    @Shadow
    private boolean ready;

    @Override
    public BlockState worldsinger$getBlockState() {
        return this.area.getBlockState(this.blockPos);
    }

    @Inject(method = "getSubmersionType", at = @At("HEAD"), cancellable = true)
    private void addCustomSubmersionType(CallbackInfoReturnable<CameraSubmersionType> cir) {
        if (!this.ready) {
            return;
        }

        BlockState blockState = this.area.getBlockState(this.blockPos);
        if (blockState.isOf(ModBlocks.SUNLIGHT)) {
            cir.setReturnValue(ModEnums.CameraSubmersionType.SPORE_SEA);
            return;
        }

        FluidState fluidState = this.worldsinger$getSubmersedFluidState();
        if (fluidState.isIn(ModFluidTags.AETHER_SPORES)) {
            cir.setReturnValue(ModEnums.CameraSubmersionType.SPORE_SEA);
        }
    }

    @Override
    @NotNull
    public FluidState worldsinger$getSubmersedFluidState() {
        FluidState fluidState = this.area.getFluidState(this.blockPos);
        float fluidHeight = fluidState.getHeight(this.area, this.blockPos);
        boolean submersedInFluid = this.pos.getY() < this.blockPos.getY() + fluidHeight;
        if (submersedInFluid) {
            return fluidState;
        }
        return Fluids.EMPTY.getDefaultState();
    }
}
