package io.github.drakonkinst.worldsinger.mixin.block;

import io.github.drakonkinst.worldsinger.util.ModProperties;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net/minecraft/block/AbstractBlock$AbstractBlockState$ShapeCache")
public abstract class ShapeCacheMixin {

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getCollisionShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"), index = 0)
    private BlockState injectCustomFluidCollisionShape(BlockState state) {
        if (state.contains(ModProperties.FLUIDLOGGED)) {
            return state.with(ModProperties.FLUIDLOGGED, 0);
        }
        return state;
    }
}
