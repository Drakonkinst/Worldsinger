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
package io.github.drakonkinst.worldsinger.mixin.client.entity;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    // TODO: RESTORE
    // @WrapOperation(method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;getShadowRadius(Lnet/minecraft/client/render/entity/state/EntityRenderState;)F"))
    // private void adjustShadowSizeForShapeshifters(MatrixStack matrices,
    //         VertexConsumerProvider vertexConsumers, EntityRenderState renderState, float opacity,
    //         float tickDelta, WorldView world, float radius, Operation<Void> original) {
    //     if (entity instanceof Shapeshifter shapeshifter) {
    //         LivingEntity morph = shapeshifter.getMorph();
    //         if (morph != null) {
    //             EntityRenderer<? super LivingEntity> morphRenderer = MinecraftClient.getInstance()
    //                     .getEntityRenderDispatcher()
    //                     .getRenderer(morph);
    //             float morphShadowRadius = ((EntityRendererAccessor) morphRenderer).worldsinger$getShadowRadius();
    //             original.call(matrices, vertexConsumers, entity, opacity, tickDelta, world,
    //                     morphShadowRadius);
    //             return;
    //         }
    //     }
    //     original.call(matrices, vertexConsumers, entity, opacity, tickDelta, world, radius);
    // }
}
