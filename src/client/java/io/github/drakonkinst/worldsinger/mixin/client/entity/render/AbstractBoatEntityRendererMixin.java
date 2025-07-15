/*
 * MIT License
 *
 * Copyright (c) 2024 Drakonkinst
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
package io.github.drakonkinst.worldsinger.mixin.client.entity.render;

import io.github.drakonkinst.worldsinger.entity.render.BoatSilverLiningFeatureRenderer;
import io.github.drakonkinst.worldsinger.entity.render.CustomFeatureRendererContext;
import io.github.drakonkinst.worldsinger.entity.render.state.BoatEntityRenderStateSilverLining;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.AbstractBoatEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.AbstractBoatEntityModel;
import net.minecraft.client.render.entity.state.BoatEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoatEntityRenderer.class)
public abstract class AbstractBoatEntityRendererMixin extends
        EntityRenderer<AbstractBoatEntity, BoatEntityRenderState> implements
        CustomFeatureRendererContext<BoatEntityRenderState, AbstractBoatEntityModel> {

    @Unique
    private FeatureRenderer<BoatEntityRenderState, AbstractBoatEntityModel> silverLiningFeatureRenderer;

    protected AbstractBoatEntityRendererMixin(Context context) {
        super(context);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addSilverLiningFeature(Context context, CallbackInfo ci) {
        silverLiningFeatureRenderer = new BoatSilverLiningFeatureRenderer(this);
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/vehicle/AbstractBoatEntity;Lnet/minecraft/client/render/entity/state/BoatEntityRenderState;F)V", at = @At("TAIL"))
    private void addSilverLiningData(AbstractBoatEntity abstractBoatEntity,
            BoatEntityRenderState boatEntityRenderState, float f, CallbackInfo ci) {
        ((BoatEntityRenderStateSilverLining) boatEntityRenderState).worldsinger$setSilverLiningVariant(
                BoatEntityRenderStateSilverLining.encodeBoatVariant(abstractBoatEntity));
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/BoatEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void renderSilverLining(BoatEntityRenderState boatEntityRenderState,
            MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i,
            CallbackInfo ci) {
        silverLiningFeatureRenderer.render(matrixStack, vertexConsumerProvider, i,
                boatEntityRenderState, 0.0f, 0.0f);
    }
}
