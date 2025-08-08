package io.github.drakonkinst.worldsinger.mixin.client.entity.render;

import io.github.drakonkinst.worldsinger.entity.render.LivingEntityOverlay;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.EmissiveFeatureRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(EmissiveFeatureRenderer.class)
public abstract class EmissiveFeatureRendererMixin {

    @ModifyArgs(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/LivingEntityRenderState;FF)V", at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"))
    private void replaceWithMidnightOverlay(Args args, MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider, int i,
            LivingEntityRenderState livingEntityRenderState, float f, float g) {
        args.set(0, LivingEntityOverlay.applyLivingEntityOverlays(args.get(0),
                livingEntityRenderState));
    }
}
