package io.github.drakonkinst.worldsinger.mixin.client;

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.client.CameraPosAccess;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.util.ModEnums;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
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

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public abstract class CameraMixin implements CameraPosAccess {

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

    @Override
    public BlockState worldsinger$getBlockState() {
        return this.area.getBlockState(this.blockPos);
    }

    @Shadow
    private BlockView area;
    @Shadow
    private Vec3d pos;
    @Shadow
    @Final
    private Mutable blockPos;
    @Shadow
    private boolean ready;
}