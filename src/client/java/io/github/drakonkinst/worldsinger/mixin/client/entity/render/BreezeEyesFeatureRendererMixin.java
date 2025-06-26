package io.github.drakonkinst.worldsinger.mixin.client.entity.render;

import io.github.drakonkinst.worldsinger.entity.render.state.ExtendedLivingEntityRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.BreezeEyesFeatureRenderer;
import net.minecraft.client.render.entity.state.BreezeEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BreezeEyesFeatureRenderer.class)
public abstract class BreezeEyesFeatureRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/BreezeEntityRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    private void cancelRenderIfMidnightOverlay(MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider, int i,
            BreezeEntityRenderState breezeEntityRenderState, float f, float g, CallbackInfo ci) {

        // Midnight entities should not have this feature rendered!
        if (breezeEntityRenderState instanceof LivingEntityRenderState livingEntityRenderState) {
            if (((ExtendedLivingEntityRenderState) livingEntityRenderState).worldsinger$hasMidnightOverlay()) {
                ci.cancel();
            }
        }
    }
}
