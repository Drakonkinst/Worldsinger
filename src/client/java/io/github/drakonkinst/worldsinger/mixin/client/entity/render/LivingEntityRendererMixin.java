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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.worldsinger.entity.render.MidnightCreatureEntityRenderer;
import io.github.drakonkinst.worldsinger.entity.render.state.ExtendedLivingEntityRenderState;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends
        EntityRenderer<T, S> implements FeatureRendererContext<S, M> {

    protected LivingEntityRendererMixin(Context ctx) {
        super(ctx);
    }

    @ModifyReturnValue(method = "getOverlay", at = @At("RETURN"))
    private static int renderModelWithMidnightOverlay(int original, LivingEntityRenderState state,
            float whiteOverlayProgress) {
        boolean hasMidnightOverlay = ((ExtendedLivingEntityRenderState) state).worldsinger$hasMidnightOverlay();
        if (hasMidnightOverlay) {
            boolean shouldFlashRed = state.hurt;
            if (shouldFlashRed) {
                return MidnightCreatureEntityRenderer.MIDNIGHT_OVERLAY_HURT_UV;
            }
            return MidnightCreatureEntityRenderer.MIDNIGHT_OVERLAY_UV;
        }
        return original;
    }
}
