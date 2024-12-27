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

import com.llamalad7.mixinextras.sugar.Local;
import io.github.drakonkinst.worldsinger.entity.render.MidnightCreatureEntityRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* This mixin modifies OverlayTexture to fill unused rows with additional colors. More can be added
 * as needed. Vanilla uses row 3 for the red hurt animation, and row 10 for flashing TNT.
 *
 * If we REALLY need more colors, we can even use individual pixels. This has a higher chance to
 * break though, and we don't need more colors than current.
 *
 * Currently used colors should be documented here:
 * 0 = Midnight Overlay
 * 1 = Midnight Overlay Hurt
 * 3 = Red (default, used when LivingEntity entities are hurt)
 * 10 = White (default, used for TNTEntity flashing)
 */
@Mixin(OverlayTexture.class)
public abstract class OverlayTextureMixin {

    @SuppressWarnings("UnresolvedLocalCapture")
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;activeTexture(I)V", ordinal = 0))
    private void populateUnusedRows(CallbackInfo ci, @Local NativeImage nativeImage) {
        this.setRowColor(nativeImage, 0, MidnightCreatureEntityRenderer.MIDNIGHT_OVERLAY_COLOR);
        this.setRowColor(nativeImage, 1,
                MidnightCreatureEntityRenderer.MIDNIGHT_OVERLAY_HURT_COLOR);
    }

    @Unique
    private void setRowColor(NativeImage nativeImage, int row, int color) {
        for (int col = 0; col < 16; ++col) {
            nativeImage.setColorArgb(col, row, color);
        }
    }
}
