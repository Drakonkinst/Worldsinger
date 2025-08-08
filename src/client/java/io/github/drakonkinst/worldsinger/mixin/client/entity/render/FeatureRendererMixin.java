package io.github.drakonkinst.worldsinger.mixin.client.entity.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.entity.render.LivingEntityOverlay;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FeatureRenderer.class)
public abstract class FeatureRendererMixin {

    @WrapOperation(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getEntityCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"))
    private static RenderLayer replaceWithMidnightOverlay(Identifier texture,
            Operation<RenderLayer> original, EntityModel<?> model, Identifier texture2,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            LivingEntityRenderState state, int color) {
        return original.call(LivingEntityOverlay.applyLivingEntityOverlays(texture, state));
    }
}
