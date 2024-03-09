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

package io.github.drakonkinst.worldsinger.world;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.lumar.ClientLunagreeData;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarLunagreeManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeLocation;
import io.github.drakonkinst.worldsinger.entity.ClientLunagreeDataAccess;
import io.github.drakonkinst.worldsinger.mixin.client.accessor.WorldRendererAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry.SkyRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public class LumarSkyRenderer implements SkyRenderer {

    private static final Identifier SUN = new Identifier("textures/environment/sun.png");
    private static final Identifier LUMAR_MOON = Worldsinger.id(
            "textures/environment/lumar_moon.png");
    private static final int MOON_TEXTURE_SECTIONS_Y = 1;
    private static final int MOON_TEXTURE_SECTIONS_X = 6;

    private static final float SUN_RADIUS = 30.0f;
    private static final float SUN_HEIGHT = 100.0f;
    private static final float MOON_RADIUS = 200.0f;
    private static final int[] SPORE_ID_TO_MOON_INDEX = { -1, 0, 1, 2, 3, 4, 5 };
    // 90 degrees above horizon (directly above)
    private static final float MOON_VERTICAL_ANGLE_START = 90.0f * MathHelper.RADIANS_PER_DEGREE;
    // 45 degrees below horizon
    private static final float MOON_VERTICAL_ANGLE_END = -45.0f * MathHelper.RADIANS_PER_DEGREE;
    private static final float MOON_VISUAL_HEIGHT_START = 100.0f;
    private static final float MOON_VISUAL_HEIGHT_END = 300.0f;

    private final VertexBuffer starsBuffer;
    private final VertexBuffer lightSkyBuffer;
    private final VertexBuffer darkSkyBuffer;

    public LumarSkyRenderer() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        // Stars
        this.starsBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder.BuiltBuffer starsBuffer = ((WorldRendererAccessor) MinecraftClient.getInstance().worldRenderer).worldsinger$renderStars(
                bufferBuilder);
        this.starsBuffer.bind();
        this.starsBuffer.upload(starsBuffer);
        VertexBuffer.unbind();

        // Light Sky
        this.lightSkyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder.BuiltBuffer lightSkyBuffer = WorldRendererAccessor.worldsinger$renderSky(
                bufferBuilder, 16.0F);
        this.lightSkyBuffer.bind();
        this.lightSkyBuffer.upload(lightSkyBuffer);
        VertexBuffer.unbind();

        // Dark Sky
        this.darkSkyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder.BuiltBuffer darkSkyBuffer = WorldRendererAccessor.worldsinger$renderSky(
                bufferBuilder, -16.0F);
        this.darkSkyBuffer.bind();
        this.darkSkyBuffer.upload(darkSkyBuffer);
        VertexBuffer.unbind();
    }

    @Override
    public void render(WorldRenderContext context) {
        final Matrix4f projectionMatrix = context.projectionMatrix();
        final float tickDelta = context.tickDelta();
        final Camera camera = context.camera();
        final GameRenderer gameRenderer = context.gameRenderer();
        final ClientWorld world = context.world();
        final ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert (player != null);

        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(context.positionMatrix());

        Vec3d skyColor = world.getSkyColor(gameRenderer.getCamera().getPos(), tickDelta);
        float red = (float) skyColor.x;
        float green = (float) skyColor.y;
        float blue = (float) skyColor.z;

        BackgroundRenderer.applyFogColor();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(red, green, blue, 1.0f);
        ShaderProgram shaderProgram = RenderSystem.getShader();

        // Draw light sky
        this.drawLightSky(bufferBuilder, matrices, projectionMatrix, shaderProgram, world,
                tickDelta);

        // Draw things in the sky
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE,
                GlStateManager.DstFactor.ZERO);
        matrices.push();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
        matrices.multiply(
                RotationAxis.POSITIVE_X.rotationDegrees(world.getSkyAngle(tickDelta) * 360.0f));
        this.drawSun(bufferBuilder, matrices);
        this.drawStars(matrices, projectionMatrix, world, gameRenderer, camera, tickDelta);
        matrices.pop();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        this.drawMoons(bufferBuilder, matrices, player, tickDelta);

        // Draw dark sky
        this.drawDarkSky(matrices, projectionMatrix, shaderProgram, world, tickDelta, player);

        RenderSystem.depthMask(true);
    }

    private void drawSun(BufferBuilder bufferBuilder, MatrixStack matrices) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, SUN);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(positionMatrix, -SUN_RADIUS, SUN_HEIGHT, -SUN_RADIUS)
                .texture(0.0f, 0.0f)
                .next();
        bufferBuilder.vertex(positionMatrix, SUN_RADIUS, SUN_HEIGHT, -SUN_RADIUS)
                .texture(1.0f, 0.0f)
                .next();
        bufferBuilder.vertex(positionMatrix, SUN_RADIUS, SUN_HEIGHT, SUN_RADIUS)
                .texture(1.0f, 1.0f)
                .next();
        bufferBuilder.vertex(positionMatrix, -SUN_RADIUS, SUN_HEIGHT, SUN_RADIUS)
                .texture(0.0f, 1.0f)
                .next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    private void drawMoons(BufferBuilder bufferBuilder, MatrixStack matrices,
            @NotNull ClientPlayerEntity player, float tickDelta) {
        RenderSystem.setShaderTexture(0, LUMAR_MOON);
        final ClientLunagreeData lunagreeData = ((ClientLunagreeDataAccess) player).worldsinger$getLunagreeData();
        final Vec3d playerPos = player.getCameraPosVec(tickDelta);
        for (LunagreeLocation location : lunagreeData.getLunagreeLocations()) {
            if (location == null) {
                break;
            }
            final double distSq = location.distSqTo(playerPos.getX(), playerPos.getZ());
            if (distSq
                    > LumarLunagreeManager.TRAVEL_DISTANCE * LumarLunagreeManager.TRAVEL_DISTANCE) {
                continue;
            }
            // Render moon
            drawMoonAtLocation(bufferBuilder, matrices, location, playerPos, distSq);
        }
    }

    private void drawMoonAtLocation(BufferBuilder bufferBuilder, MatrixStack matrices,
            LunagreeLocation lunagreeLocation, Vec3d playerPos, double distSq) {
        final int sporeId = lunagreeLocation.sporeId();
        if (sporeId < 0 || sporeId >= SPORE_ID_TO_MOON_INDEX.length) {
            Worldsinger.LOGGER.warn("Cannot render lunagree with unknown spore ID " + sporeId);
            return;
        }
        int moonIndex = SPORE_ID_TO_MOON_INDEX[sporeId];

        float distance = Math.sqrt((float) distSq);
        float progress = distance / LumarLunagreeManager.TRAVEL_DISTANCE;
        float multiplier = 1.0f - (float) Math.cos(progress * Math.PI * 0.5);

        // Calculate shrink factor
        float moonVisualDistance = Math.lerp(MOON_VISUAL_HEIGHT_START, MOON_VISUAL_HEIGHT_END,
                multiplier);
        // Calculate desired vertical angle
        float verticalAngle = Math.lerp(MOON_VERTICAL_ANGLE_START, MOON_VERTICAL_ANGLE_END,
                multiplier);

        // Solve for moon height given xz of moon, xyz of player, and desired vertical angle
        double deltaX = lunagreeLocation.blockX() - playerPos.getX();
        double deltaZ = lunagreeLocation.blockZ() - playerPos.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        // We don't actually need to know the height of the moon, but here it is
        // double moonY = playerPos.getY() + horizontalDistance * Math.tan(verticalAngle);

        // Get desired rotation in axis-angle notation
        double deltaY = horizontalDistance * Math.tan(verticalAngle);
        // Get the normalized direction from the player to the moon
        Vector3d targetDir = new Vector3d(deltaX, deltaY, deltaZ);
        targetDir.normalize();
        // Cross with the UP vector to get rotation axis
        Vector3d rotationAxis = new Vector3d(targetDir.z, 0.0, -targetDir.x);
        rotationAxis.normalize();
        // Get angle
        double rotationAngle = Math.acos(targetDir.y);

        // Convert axis-angle to quaternion (manually, to avoid creating another object)
        double sin = Math.sin(rotationAngle * 0.5f);
        double cos = Math.cosFromSin(sin, rotationAngle * 0.5f);
        float x = (float) (rotationAxis.x * sin);
        float y = (float) (rotationAxis.y * sin);
        float z = (float) (rotationAxis.z * sin);
        float w = (float) cos;
        Quaternionf rotation = new Quaternionf(x, y, z, w);

        drawMoon(bufferBuilder, matrices, moonIndex, MOON_RADIUS, moonVisualDistance, rotation);
    }

    private void drawMoon(BufferBuilder bufferBuilder, MatrixStack matrices, int moonIndex,
            float radius, float height, Quaternionf quaternion) {
        int xIndex = moonIndex % MOON_TEXTURE_SECTIONS_X;
        int yIndex = moonIndex / MOON_TEXTURE_SECTIONS_X % MOON_TEXTURE_SECTIONS_Y;
        float x1 = (float) xIndex / MOON_TEXTURE_SECTIONS_X;
        float y1 = (float) yIndex / MOON_TEXTURE_SECTIONS_Y;
        float x2 = (float) (xIndex + 1) / MOON_TEXTURE_SECTIONS_X;
        float y2 = (float) (yIndex + 1) / MOON_TEXTURE_SECTIONS_Y;

        matrices.push();

        // Position the moon
        matrices.multiply(quaternion);

        // Draw moon
        // Change coordinates to match the sun, since it should NOT be drawn on the
        // other side of the screen.
        Matrix4f moonPosition = matrices.peek().getPositionMatrix();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(moonPosition, -radius, height, -radius).texture(x1, y1).next();
        bufferBuilder.vertex(moonPosition, radius, height, -radius).texture(x2, y1).next();
        bufferBuilder.vertex(moonPosition, radius, height, radius).texture(x2, y2).next();
        bufferBuilder.vertex(moonPosition, -radius, height, radius).texture(x1, y2).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        matrices.pop();
    }

    private void drawStars(MatrixStack matrices, Matrix4f projectionMatrix, ClientWorld world,
            GameRenderer gameRenderer, Camera camera, float tickDelta) {
        float starBrightness = world.getStarBrightness(tickDelta);
        if (starBrightness > 0.0f) {
            RenderSystem.setShaderColor(starBrightness, starBrightness, starBrightness,
                    starBrightness);
            BackgroundRenderer.clearFog();
            this.starsBuffer.bind();
            this.starsBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix,
                    GameRenderer.getPositionProgram());
            VertexBuffer.unbind();

            // Render Fog
            this.renderFog(world, gameRenderer, camera, tickDelta);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private void renderFog(ClientWorld world, GameRenderer gameRenderer, Camera camera,
            float tickDelta) {
        float viewDistance = gameRenderer.getViewDistance();
        Vec3d cameraPos = camera.getPos();
        double cameraX = cameraPos.getX();
        double cameraY = cameraPos.getY();
        boolean useThickFog = world.getDimensionEffects()
                .useThickFog(MathHelper.floor(cameraX), MathHelper.floor(cameraY))
                || MinecraftClient.getInstance().inGameHud.getBossBarHud().shouldThickenFog();
        BackgroundRenderer.applyFog(camera, BackgroundRenderer.FogType.FOG_SKY, viewDistance,
                useThickFog, tickDelta);
    }

    private void drawLightSky(BufferBuilder bufferBuilder, MatrixStack matrices,
            Matrix4f projectionMatrix, ShaderProgram shaderProgram, ClientWorld world,
            float tickDelta) {
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
    }

    private void drawDarkSky(MatrixStack matrices, Matrix4f projectionMatrix,
            ShaderProgram shaderProgram, ClientWorld world, float tickDelta,
            @NotNull ClientPlayerEntity player) {
        RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
        double skyDarknessHeight = player.getCameraPosVec(tickDelta).y - world.getLevelProperties()
                .getSkyDarknessHeight(world);
        if (skyDarknessHeight < 0.0) {
            matrices.push();
            matrices.translate(0.0f, 12.0f, 0.0f);
            this.darkSkyBuffer.bind();
            this.darkSkyBuffer.draw(matrices.peek().getPositionMatrix(), projectionMatrix,
                    shaderProgram);
            VertexBuffer.unbind();
            matrices.pop();
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
