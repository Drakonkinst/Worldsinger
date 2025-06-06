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
package io.github.drakonkinst.worldsinger.entity.render;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.entity.render.state.BoatEntityRenderStateSilverLining;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.AbstractBoatEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.BoatEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class BoatSilverLiningFeatureRenderer extends
        FeatureRenderer<BoatEntityRenderState, AbstractBoatEntityModel> {

    private static final Identifier[] TEXTURE_MAP = BoatSilverLiningFeatureRenderer.generateTextureMap();

    private static Identifier[] generateTextureMap() {
        String[] entityTypes = { "boat", "chest_boat" };
        String[] boatVariants = { "boat", "raft" };
        String[] silverLevels = { "low", "medium", "high", "perfect" };
        Identifier[] textureMap = new Identifier[entityTypes.length * boatVariants.length
                * silverLevels.length];

        int i = 0;
        for (String entityType : entityTypes) {
            for (String boatVariant : boatVariants) {
                for (String silverLevel : silverLevels) {
                    textureMap[i++] = Worldsinger.id(
                            "textures/entity/" + entityType + "/" + boatVariant + "_silver_lining_"
                                    + silverLevel + ".png");
                }
            }
        }

        return textureMap;
    }

    protected static <T extends EntityRenderState> void renderModel(EntityModel<T> model,
            Identifier texture, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
            int light) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(texture));
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, -1);
    }

    public BoatSilverLiningFeatureRenderer(
            FeatureRendererContext<BoatEntityRenderState, AbstractBoatEntityModel> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            BoatEntityRenderState state, float limbAngle, float limbDistance) {
        int variant = ((BoatEntityRenderStateSilverLining) state).worldsinger$getSilverLiningVariant();
        if (variant < 0) {
            return;
        }

        Identifier texture = TEXTURE_MAP[variant];
        renderModel(this.getContextModel(), texture, matrices, vertexConsumers, light);
    }
}
