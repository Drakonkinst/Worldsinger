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

import net.minecraft.client.render.entity.AbstractBoatEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.state.BoatEntityRenderState;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractBoatEntityRenderer.class)
public abstract class AbstractBoatEntityRendererMixin extends
        EntityRenderer<AbstractBoatEntity, BoatEntityRenderState> {

    @Unique
    private FeatureRenderer<BoatEntityRenderState, BoatEntityModel> silverLiningFeatureRenderer;

    protected AbstractBoatEntityRendererMixin(Context context) {
        super(context);
    }

    // TODO: RESTORE
    // @Inject(method = "<init>", at = @At("TAIL"))
    // private void addSilverLiningFeature(Context context, CallbackInfo ci) {
    //     silverLiningFeatureRenderer = new BoatSilverLiningFeatureRenderer(this, texturesAndModels);
    // }

    // @Inject(method = "render(Lnet/minecraft/entity/vehicle/BoatEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    // private void renderSilverLining(BoatEntity boatEntity, float f, float g,
    //         MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i,
    //         CallbackInfo ci) {
    //     silverLiningFeatureRenderer.render();
    //     for (FeatureRenderer<BoatEntity, BoatEntityModel> featureRenderer : this.features) {
    //         featureRenderer.render(matrixStack, vertexConsumerProvider, i, boatEntity, 0.0f, 0.0f,
    //                 0.0f, 0.0f, 0.0f, 0.0f);
    //     }
    // }
}
