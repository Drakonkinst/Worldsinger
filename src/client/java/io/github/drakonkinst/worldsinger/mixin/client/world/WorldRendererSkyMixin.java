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
package io.github.drakonkinst.worldsinger.mixin.client.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.registry.ModClientEnums;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererSkyMixin {

    @Unique
    private static final Identifier LUMAR_MOON = Worldsinger.id(
            "textures/environment/lumar_moon.png");
    @Unique
    private static final int MOON_TEXTURE_SECTIONS_Y = 2;
    @Unique
    private static final int MOON_TEXTURE_SECTIONS_X = 4;
    @Shadow
    @Final
    private static Identifier SUN;
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private @Nullable ClientWorld world;
    @Shadow
    private @Nullable VertexBuffer lightSkyBuffer;
    @Shadow
    private @Nullable VertexBuffer starsBuffer;
    @Shadow
    private @Nullable VertexBuffer darkSkyBuffer;

    @ModifyExpressionValue(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/client/render/CameraSubmersionType;"))
    private CameraSubmersionType skipRenderingInSporeFluid(CameraSubmersionType original) {
        // Treat spore sea camera like lava, so that it skips sky rendering when submerged
        if (original == ModClientEnums.CameraSubmersionType.SPORE_SEA) {
            return CameraSubmersionType.LAVA;
        }
        return original;
    }

    @Inject(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/DimensionEffects;getSkyType()Lnet/minecraft/client/render/DimensionEffects$SkyType;"), cancellable = true)
    private void addLumarSky(MatrixStack matrices, Matrix4f projectionMatrix, float tickDelta,
            Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
        ClientWorld world = this.world;
        if (world == null) {
            return;
        }

        if (world.getDimensionEffects().getSkyType() == ModClientEnums.SkyType.LUMAR) {
            renderLumarSky(world, matrices, projectionMatrix, tickDelta, fogCallback);
            ci.cancel();
        }
    }

    @Unique
    private void renderLumarSky(@NotNull ClientWorld world, MatrixStack matrices,
            Matrix4f projectionMatrix, float tickDelta, Runnable fogCallback) {
        Vec3d vec3d = world.getSkyColor(this.client.gameRenderer.getCamera().getPos(), tickDelta);
        float x = (float) vec3d.x;
        float y = (float) vec3d.y;
        float z = (float) vec3d.z;
        BackgroundRenderer.applyFogColor();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(x, y, z, 1.0f);
        ShaderProgram shaderProgram = RenderSystem.getShader();

        // Grabbing a copy of the original matrix for later
        matrices.push();
        Matrix4f fixedPositionMatrix = matrices.peek().getPositionMatrix();
        matrices.pop();

        this.lightSkyBuffer.bind();
        this.lightSkyBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix,
                shaderProgram);
        VertexBuffer.unbind();
        RenderSystem.enableBlend();
        float[] fogRgba = world.getDimensionEffects()
                .getFogColorOverride(world.getSkyAngle(tickDelta), tickDelta);
        if (fogRgba != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            float i = MathHelper.sin(world.getSkyAngleRadians(tickDelta)) < 0.0f ? 180.0f : 0.0f;
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(i));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix4f, 0.0f, 100.0f, 0.0f)
                    .color(fogRgba[0], fogRgba[1], fogRgba[2], fogRgba[3])
                    .next();
            for (int n = 0; n <= 16; ++n) {
                float o = (float) n * ((float) Math.PI * 2) / 16.0f;
                float p = MathHelper.sin(o);
                float q = MathHelper.cos(o);
                bufferBuilder.vertex(matrix4f, p * 120.0f, q * 120.0f, -q * 40.0f * fogRgba[3])
                        .color(fogRgba[0], fogRgba[1], fogRgba[2], 0.0f)
                        .next();
            }
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            matrices.pop();
        }
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO);
        matrices.push();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
        matrices.multiply(
                RotationAxis.POSITIVE_X.rotationDegrees(world.getSkyAngle(tickDelta) * 360.0f));

        // Draw sun
        float radius = 30.0f;
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, SUN);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(positionMatrix, -radius, 100.0f, -radius).texture(0.0f, 0.0f).next();
        bufferBuilder.vertex(positionMatrix, radius, 100.0f, -radius).texture(1.0f, 0.0f).next();
        bufferBuilder.vertex(positionMatrix, radius, 100.0f, radius).texture(1.0f, 1.0f).next();
        bufferBuilder.vertex(positionMatrix, -radius, 100.0f, radius).texture(0.0f, 1.0f).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        // Draw moon
        radius = 250.0f;
        RenderSystem.setShaderTexture(0, LUMAR_MOON);

        int moonPhase = this.getMoonIndex(world);
        int xIndex = moonPhase % MOON_TEXTURE_SECTIONS_X;
        int yIndex = moonPhase / MOON_TEXTURE_SECTIONS_X % MOON_TEXTURE_SECTIONS_Y;
        float t = (float) xIndex / MOON_TEXTURE_SECTIONS_X;
        float o = (float) yIndex / MOON_TEXTURE_SECTIONS_Y;
        float p = (float) (xIndex + 1) / MOON_TEXTURE_SECTIONS_X;
        float q = (float) (yIndex + 1) / MOON_TEXTURE_SECTIONS_Y;
        Matrix4f moonPosition = fixedPositionMatrix.rotate(
                        RotationAxis.POSITIVE_X.rotationDegrees(180.0f))
                .rotate(RotationAxis.POSITIVE_Y.rotationDegrees(45.0f))
                .rotate(RotationAxis.POSITIVE_Z.rotationDegrees(67.5f));
        float moonHeight = -200.0f;

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(moonPosition, -radius, moonHeight, radius).texture(p, q).next();
        bufferBuilder.vertex(moonPosition, radius, moonHeight, radius).texture(t, q).next();
        bufferBuilder.vertex(moonPosition, radius, moonHeight, -radius).texture(t, o).next();
        bufferBuilder.vertex(moonPosition, -radius, moonHeight, -radius).texture(p, o).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        // Draw stars
        float u = world.method_23787(tickDelta);
        if (u > 0.0f) {
            RenderSystem.setShaderColor(u, u, u, u);
            BackgroundRenderer.clearFog();
            this.starsBuffer.bind();
            this.starsBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix,
                    GameRenderer.getPositionProgram());
            VertexBuffer.unbind();
            fogCallback.run();
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        matrices.pop();
        RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
        double d = this.client.player.getCameraPosVec(tickDelta).y - world.getLevelProperties()
                .getSkyDarknessHeight(world);
        if (d < 0.0) {
            matrices.push();
            matrices.translate(0.0f, 12.0f, 0.0f);
            this.darkSkyBuffer.bind();
            this.darkSkyBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix,
                    shaderProgram);
            VertexBuffer.unbind();
            matrices.pop();
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.depthMask(true);
    }

    @Unique
    private int getMoonIndex(ClientWorld world) {
        // int moonPhase = world.getMoonPhase();
        // TODO: Switch moon depending on temperature?

        return 0;
    }
}
