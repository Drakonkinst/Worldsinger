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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import io.github.drakonkinst.worldsinger.gui.ThirstStatusBar;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Inject(method = "renderStatusBars", at = @At("TAIL"))
    private void renderThirstStatusBar(DrawContext context, CallbackInfo ci) {
        PlayerEntity player = this.getCameraPlayer();
        if (player == null) {
            return;
        }
        if (ThirstStatusBar.shouldRenderThirstBar(player)) {
            Profiler profiler = Profilers.get();
            profiler.push("thirst");
            ThirstStatusBar.renderThirstStatusBar(client, context, player);
            profiler.pop();
        }
    }

    // Currently, this method is only used to get the number of health rows the player's mount has,
    // so it knows where to render the air meter.
    // Add an extra row to give space for the thirst meter, if it should render.
    @ModifyReturnValue(method = "getHeartRows", at = @At("RETURN"))
    private int adjustAirStatusMeter(int original) {
        if (ThirstStatusBar.shouldRenderThirstBar(this.getCameraPlayer())) {
            return original + 1;
        }
        return original;
    }

    // Occurs after the vignette based on graphics mode
    @Inject(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderTickCounter;getLastFrameDuration()F"), cancellable = true)
    private void renderPossessionOverlays(DrawContext context, RenderTickCounter tickCounter,
            CallbackInfo ci) {
        CameraPossessable possessionTarget = PossessionClientUtil.getPossessedEntity();
        if (possessionTarget == null) {
            return;
        }
        LivingEntity possessedEntity = possessionTarget.toEntity();

        if (possessedEntity instanceof MidnightCreatureEntity) {
            renderMidnightEssencePossessionVignette(context);
        }

        // Don't render anything except the frozen overlay, from the possessed entity's perspective
        if (possessedEntity.getFrozenTicks() > 0) {
            this.renderOverlay(context, POWDER_SNOW_OUTLINE, possessedEntity.getFreezingScale());
        }

        ci.cancel();
    }

    @Unique
    private void renderMidnightEssencePossessionVignette(DrawContext context) {
        // Prepare
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ZERO,
                GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO);

        // Use darkness = 1.0f, so no changes needed
        final int scaledWidth = context.getScaledWindowWidth();
        final int scaledHeight = context.getScaledWindowHeight();
        context.drawTexture(VIGNETTE_TEXTURE, 0, 0, -90, 0.0f, 0.0f, scaledWidth, scaledHeight,
                scaledWidth, scaledHeight);

        // Reset
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    @Shadow
    @Nullable
    protected abstract PlayerEntity getCameraPlayer();

    @Shadow
    @Final
    private MinecraftClient client;
}
