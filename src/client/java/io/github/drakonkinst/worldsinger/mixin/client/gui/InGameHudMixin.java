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
package io.github.drakonkinst.worldsinger.mixin.client.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import io.github.drakonkinst.worldsinger.entity.PossessionClientUtil;
import io.github.drakonkinst.worldsinger.gui.ThirstStatusBar;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    @Final
    private static Identifier VIGNETTE_TEXTURE;

    @Shadow
    protected abstract void renderOverlay(DrawContext context, Identifier texture, float opacity);

    @Shadow
    @Final
    private static Identifier POWDER_SNOW_OUTLINE;

    // Add an extra row to give space for the thirst meter, if it should render.
    @ModifyExpressionValue(method = "getAirBubbleY", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;getHeartRows(I)I"))
    private int adjustAirStatusMeter(int original) {
        if (ThirstStatusBar.shouldRenderThirstBar(this.getCameraPlayer())) {
            return original + 1;
        }
        return original;
    }

    // Occurs after the vignette based on graphics mode
    @Inject(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderTickCounter;getDynamicDeltaTicks()F"), cancellable = true)
    private void renderPossessionOverlays(DrawContext context, RenderTickCounter tickCounter,
            CallbackInfo ci) {
        CameraPossessable possessionTarget = PossessionClientUtil.getPossessedEntity();
        if (possessionTarget == null) {
            return;
        }
        LivingEntity possessedEntity = possessionTarget.toEntity();

        if (possessedEntity instanceof MidnightCreatureEntity) {
            context.drawTexture(RenderPipelines.VIGNETTE, VIGNETTE_TEXTURE, 0, 0, 0.0F, 0.0F,
                    context.getScaledWindowWidth(), context.getScaledWindowHeight(),
                    context.getScaledWindowWidth(), context.getScaledWindowHeight(),
                    ColorHelper.fromFloats(1.0f, 1.0f, 1.0f, 1.0f));
        }

        // Don't render anything except the frozen overlay, from the possessed entity's perspective
        // TODO: Probably a better way to do this
        if (possessedEntity.getFrozenTicks() > 0) {
            this.renderOverlay(context, POWDER_SNOW_OUTLINE, possessedEntity.getFreezingScale());
        }

        ci.cancel();
    }

    @Shadow
    @Nullable
    protected abstract PlayerEntity getCameraPlayer();
}
