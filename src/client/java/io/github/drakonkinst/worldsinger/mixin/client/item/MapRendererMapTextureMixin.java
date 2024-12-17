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

package io.github.drakonkinst.worldsinger.mixin.client.item;

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "net.minecraft.client.render.MapRenderer$MapTexture")
public abstract class MapRendererMapTextureMixin {

    @Unique
    private static final Identifier RAINLINE_ICON_TEXTURE = Worldsinger.id(
            "textures/map/rainline.png");
    @Unique
    private static final RenderLayer RAINLINE_RENDER_LAYER = RenderLayer.getText(
            RAINLINE_ICON_TEXTURE);

    @Shadow
    private MapState state;

    // TODO: RESTORE
    // @Inject(method = "draw", at = @At("TAIL"))
    // private void drawCustomMapIcons(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
    //         boolean hidePlayerIcons, int light, CallbackInfo ci) {
    //     CustomMapStateAccess customMapState = (CustomMapStateAccess) state;
    //     int layer = 0;
    //     for (CustomMapDecoration mapIcon : customMapState.worldsinger$getCustomMapIcons()
    //             .values()) {
    //         if (mapIcon.type() != CustomMapDecoration.Type.RAINLINE) {
    //             // Idk how to do those yet
    //             continue;
    //         }
    //         matrices.push();
    //         matrices.translate(mapIcon.x() / 2.0f + 64.0f, mapIcon.z() / 2.0f + 64.0f, -0.02f);
    //         matrices.multiply(
    //                 RotationAxis.POSITIVE_Z.rotationDegrees((mapIcon.rotation() * 360) / 16.0f));
    //         matrices.scale(4.0f, 4.0f, 3.0f);
    //         matrices.translate(-0.125f, 0.125f, 0.0f);
    //
    //         float minU = 0.0f;
    //         float minV = 0.0f;
    //         float maxU = 1.0f;
    //         float maxV = 1.0f;
    //         Matrix4f matrix = matrices.peek().getPositionMatrix();
    //         VertexConsumer vertexConsumer2 = vertexConsumers.getBuffer(RAINLINE_RENDER_LAYER);
    //         vertexConsumer2.vertex(matrix, -1.0F, 1.0F, layer * -0.001F)
    //                 .color(255, 255, 255, 255)
    //                 .texture(minU, minV)
    //                 .light(light);
    //         vertexConsumer2.vertex(matrix, 1.0F, 1.0F, layer * -0.001F)
    //                 .color(255, 255, 255, 255)
    //                 .texture(maxU, minV)
    //                 .light(light);
    //         vertexConsumer2.vertex(matrix, 1.0F, -1.0F, layer * -0.001F)
    //                 .color(255, 255, 255, 255)
    //                 .texture(maxU, maxV)
    //                 .light(light);
    //         vertexConsumer2.vertex(matrix, -1.0F, -1.0F, layer * -0.001F)
    //                 .color(255, 255, 255, 255)
    //                 .texture(minU, maxV)
    //                 .light(light);
    //         matrices.pop();
    //         ++layer;
    //     }
    // }
}
