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

import io.github.drakonkinst.worldsinger.entity.model.RainlineEntityModel;
import io.github.drakonkinst.worldsinger.entity.rainline.RainlineEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RainlineEntityRenderer extends GeoEntityRenderer<RainlineEntity> {

    public RainlineEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new RainlineEntityModel());
    }

    // TODO: RESTORE
    // @Override
    // public void render(EntityRenderState entityRenderState, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
    //     // My feeble attempt to make interpolation work has gone horribly wrong
    //     double deltaX = entityRenderState.getX() - entityRenderState.prevX;
    //     double deltaZ = entityRenderState.getZ() - entityRenderState.prevZ;
    //     double adjustY = RainlineEntity.getTargetHeight(entity.getWorld()) - entity.getY();
    //     poseStack.push();
    //     double t = partialTick - 1;
    //     double adjustX = t * deltaX;
    //     double adjustZ = t * deltaZ;
    //     poseStack.translate(adjustX, adjustY, adjustZ);
    //     super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    //     poseStack.pop();
    // }
}
