package io.github.drakonkinst.worldsinger.mixin.entity.ai;

import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LandPathNodeMaker.class)
public abstract class LandPathNodeMakerMixin {

    @Inject(method = "getCommonNodeType", at = @At("TAIL"), cancellable = true)
    private static void avoidSporeBlocks(BlockView world, BlockPos pos,
            CallbackInfoReturnable<PathNodeType> cir) {
        FluidState fluidState = world.getFluidState(pos);
        if (fluidState.isIn(ModFluidTags.AETHER_SPORES)) {
            cir.setReturnValue(PathNodeType.DAMAGE_OTHER);
        }
    }
}