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

package io.github.drakonkinst.worldsinger.mixin.client.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.util.ModEnums;
import io.github.drakonkinst.worldsinger.world.LumarSkyRendering;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.FramePass;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererLumarMixin {

    @Shadow
    private @Nullable ClientWorld world;
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    protected abstract boolean isSkyDark(float tickDelta);

    @Shadow
    @Final
    private SkyRendering skyRendering;
    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;
    @Unique
    private LumarSkyRendering lumarSkyRendering;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeLumarSkyRendering(MinecraftClient client,
            EntityRenderDispatcher entityRenderDispatcher,
            BlockEntityRenderDispatcher blockEntityRenderDispatcher,
            BufferBuilderStorage bufferBuilders, CallbackInfo ci) {
        this.lumarSkyRendering = new LumarSkyRendering(skyRendering);
    }

    @WrapOperation(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/FramePass;setRenderer(Ljava/lang/Runnable;)V"))
    private void renderLumarCustomSky(FramePass instance, Runnable runnable,
            Operation<Void> original, FrameGraphBuilder frameGraphBuilder, Camera camera,
            float tickProgress, GpuBufferSlice fog) {
        if (this.world == null || !CosmerePlanet.isLumar(this.world)) {
            original.call(instance, runnable);
            return;
        }
        original.call(instance, (Runnable) (() -> {
            RenderSystem.setShaderFog(fog);

            MatrixStack matrices = new MatrixStack();
            DimensionEffects dimensionEffects = this.world.getDimensionEffects();
            float skyAngleRadians = this.world.getSkyAngleRadians(tickProgress);
            float skyAngle = this.world.getSkyAngle(tickProgress);
            float skyAlpha = 1.0F - this.world.getRainGradient(tickProgress);
            float starBrightness = this.world.getStarBrightness(tickProgress) * skyAlpha;
            int dimensionSkyColor = dimensionEffects.getSkyColor(skyAngle);
            int skyColor = this.world.getSkyColor(this.client.gameRenderer.getCamera().getPos(),
                    tickProgress);
            float r = ColorHelper.getRedFloat(skyColor);
            float g = ColorHelper.getGreenFloat(skyColor);
            float b = ColorHelper.getBlueFloat(skyColor);
            this.skyRendering.renderTopSky(r, g, b);
            Immediate vertexConsumers = this.bufferBuilders.getEntityVertexConsumers();
            if (dimensionEffects.isSunRisingOrSetting(skyAngle)) {
                this.skyRendering.renderGlowingSky(matrices, vertexConsumers, skyAngleRadians,
                        dimensionSkyColor);
            }

            // TODO: Restore
            // lumarSkyRendering.renderLumarCelestialBodies(matrices, vertexConsumers, tickDelta,
            // skyAngle, skyAlpha, starBrightness, fog);
            skyRendering.renderCelestialBodies(matrices, vertexConsumers, skyAngle, 0, skyAlpha,
                    starBrightness);
            vertexConsumers.draw();
            if (this.isSkyDark(tickProgress)) {
                this.skyRendering.renderSkyDark();
            }
        }));
    }

    @ModifyExpressionValue(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/block/enums/CameraSubmersionType;"))
    private CameraSubmersionType skipRenderingSkyInSporeFluid(CameraSubmersionType original) {
        // Treat spore sea camera like lava, so that it skips sky rendering when submerged
        if (original == ModEnums.CameraSubmersionType.SPORE_SEA) {
            return CameraSubmersionType.LAVA;
        }
        return original;
    }
}
