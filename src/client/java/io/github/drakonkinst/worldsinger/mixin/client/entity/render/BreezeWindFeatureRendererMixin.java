package io.github.drakonkinst.worldsinger.mixin.client.entity.render;

import io.github.drakonkinst.worldsinger.entity.render.LivingEntityOverlay;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.BreezeWindFeatureRenderer;
import net.minecraft.client.render.entity.state.BreezeEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BreezeWindFeatureRenderer.class)
public abstract class BreezeWindFeatureRendererMixin {

    @ModifyArgs(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/BreezeEntityRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getBreezeWind(Lnet/minecraft/util/Identifier;FF)Lnet/minecraft/client/render/RenderLayer;"))
    private void replaceWithMidnightOverlay(Args args, MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider, int i,
            BreezeEntityRenderState breezeEntityRenderState, float f, float g) {
        args.set(0, LivingEntityOverlay.applyLivingEntityOverlays(args.get(0),
                breezeEntityRenderState));
    }
}
