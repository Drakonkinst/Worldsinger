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

package io.github.drakonkinst.worldsinger.world;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.lumar.ClientLunagreeData;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarLunagreeGenerator;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeLocation;
import io.github.drakonkinst.worldsinger.entity.ClientLunagreeDataAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public class LumarSkyRendering {

    private static final Identifier LUMAR_MOON = Worldsinger.id(
            "textures/environment/lumar_moon.png");
    private static final int MOON_TEXTURE_SECTIONS_Y = 1;
    private static final int MOON_TEXTURE_SECTIONS_X = 6;

    private static final float MOON_RADIUS = 200.0f;
    private static final int[] SPORE_ID_TO_MOON_INDEX = { -1, 0, 1, 2, 3, 4, 5 };
    // 90 degrees above horizon (directly above)
    private static final float MOON_VERTICAL_ANGLE_START = 90.0f * MathHelper.RADIANS_PER_DEGREE;
    // 45 degrees below horizon
    private static final float MOON_VERTICAL_ANGLE_END = -45.0f * MathHelper.RADIANS_PER_DEGREE;
    private static final float MOON_VISUAL_HEIGHT_START = 100.0f;
    private static final float MOON_VISUAL_HEIGHT_END = 300.0f;

    // TODO: Temp
    public final SkyRendering skyRendering;

    public LumarSkyRendering(SkyRendering skyRendering) {
        this.skyRendering = skyRendering;
    }

    @NotNull
    private static Quaternionf solveForRotation(double deltaX, double deltaZ, float verticalAngle) {
        Vector3d targetDir = getTargetDirection(deltaX, deltaZ, verticalAngle);
        // Cross with the UP vector to get rotation axis
        Vector3d rotationAxis = new Vector3d(targetDir.z, 0.0, -targetDir.x);
        rotationAxis.normalize();
        // Get angle
        double rotationAngle = Math.acos(targetDir.y);

        // Convert axis-angle to quaternion (manually, to avoid creating another object)
        double sin = Math.sin(rotationAngle * 0.5f);
        double cos = Math.cos(rotationAngle * 0.5f);
        float x = (float) (rotationAxis.x * sin);
        float y = (float) (rotationAxis.y * sin);
        float z = (float) (rotationAxis.z * sin);
        float w = (float) cos;
        return new Quaternionf(x, y, z, w);
    }

    private static @NotNull Vector3d getTargetDirection(double deltaX, double deltaZ,
            float verticalAngle) {
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        // We don't actually need to know the height of the moon, but here it is
        // double moonY = playerPos.getY() + horizontalDistance * Math.tan(verticalAngle);

        // Get desired rotation in axis-angle notation
        double deltaY = horizontalDistance * Math.tan(verticalAngle);
        // Get the normalized direction from the player to the moon
        Vector3d targetDir = new Vector3d(deltaX, deltaY, deltaZ);
        targetDir.normalize();
        return targetDir;
    }

    public void renderLumarCelestialBodies(MatrixStack matrices, Immediate vertexConsumers,
            float skyAngle, float tickDelta, float skyAlpha, float starBrightness, Fog fog) {
        skyRendering.renderCelestialBodies(matrices, vertexConsumers, skyAngle, 0, skyAlpha,
                starBrightness, fog);

        // TODO: Restore
        // Render normal sky without moon
        // matrices.push();
        // matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
        // matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(skyAngle * 360.0F));
        // ((SkyRenderingInvoker) skyRendering).worldsinger$renderSun(skyAlpha, vertexConsumers,
        //         matrices);
        // vertexConsumers.draw();
        // if (starBrightness > 0.0F) {
        //     ((SkyRenderingInvoker) skyRendering).worldsinger$renderStars(fog, starBrightness,
        //             matrices);
        // }
        // matrices.pop();

        // Render moons
        // renderMoons(vertexConsumers, matrices, tickDelta);
        // vertexConsumers.draw();
    }

    public void renderMoons(Immediate vertexConsumers, MatrixStack matrices, float tickDelta) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert (player != null);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LUMAR_MOON);
        final ClientLunagreeData lunagreeData = ((ClientLunagreeDataAccess) player).worldsinger$getLunagreeData();
        final Vec3d playerPos = player.getCameraPosVec(tickDelta);
        for (LunagreeLocation location : lunagreeData.getLunagreeLocations()) {
            if (location == null) {
                break;
            }
            final double distSq = location.distSqTo(playerPos.getX(), playerPos.getZ());
            if (distSq > LumarLunagreeGenerator.TRAVEL_DISTANCE
                    * LumarLunagreeGenerator.TRAVEL_DISTANCE) {
                continue;
            }
            // Render moon
            renderMoonAtLocation(vertexConsumers, matrices, location, playerPos, distSq);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
    }

    private void renderMoonAtLocation(Immediate vertexConsumers, MatrixStack matrices,
            LunagreeLocation lunagreeLocation, Vec3d playerPos, double distSq) {
        final int sporeId = lunagreeLocation.sporeId();
        if (sporeId < 0 || sporeId >= SPORE_ID_TO_MOON_INDEX.length) {
            Worldsinger.LOGGER.warn("Cannot render lunagree with unknown spore ID {}", sporeId);
            return;
        }
        int moonIndex = SPORE_ID_TO_MOON_INDEX[sporeId];

        float distance = MathHelper.sqrt((float) distSq);
        float progress = distance / LumarLunagreeGenerator.TRAVEL_DISTANCE;
        float multiplier = 1.0f - MathHelper.cos(progress * MathHelper.PI * 0.5f);

        // Calculate shrink factor
        float moonVisualDistance = MathHelper.lerp(multiplier, MOON_VISUAL_HEIGHT_START,
                MOON_VISUAL_HEIGHT_END);
        // Calculate desired vertical angle
        float verticalAngle = MathHelper.lerp(multiplier, MOON_VERTICAL_ANGLE_START,
                MOON_VERTICAL_ANGLE_END);

        // Solve for moon height given xz of moon, xyz of player, and desired vertical angle
        double deltaX = lunagreeLocation.blockX() - playerPos.getX();
        double deltaZ = lunagreeLocation.blockZ() - playerPos.getZ();
        Quaternionf rotation = solveForRotation(deltaX, deltaZ, verticalAngle);

        renderMoon(vertexConsumers, matrices, moonIndex, moonVisualDistance, rotation);
    }

    @SuppressWarnings("ConstantValue")
    private void renderMoon(VertexConsumerProvider vertexConsumers, MatrixStack matrices,
            int moonIndex, float height, Quaternionf quaternion) {
        int xIndex = moonIndex % MOON_TEXTURE_SECTIONS_X;
        int yIndex = moonIndex / MOON_TEXTURE_SECTIONS_X % MOON_TEXTURE_SECTIONS_Y;
        float x1 = (float) xIndex / MOON_TEXTURE_SECTIONS_X;
        float y1 = (float) yIndex / MOON_TEXTURE_SECTIONS_Y;
        float x2 = (float) (xIndex + 1) / MOON_TEXTURE_SECTIONS_X;
        float y2 = (float) (yIndex + 1) / MOON_TEXTURE_SECTIONS_Y;
        float radius = MOON_RADIUS;

        matrices.push();

        // Position the moon
        matrices.multiply(quaternion);

        // Draw moon
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                RenderLayer.getCelestial(LUMAR_MOON));
        int color = ColorHelper.getWhite(1.0f);
        Matrix4f moonPosition = matrices.peek().getPositionMatrix();
        vertexConsumer.vertex(moonPosition, -radius, height, -radius).texture(x1, y1).color(color);
        vertexConsumer.vertex(moonPosition, radius, height, -radius).texture(x2, y1).color(color);
        vertexConsumer.vertex(moonPosition, radius, height, radius).texture(x2, y2).color(color);
        vertexConsumer.vertex(moonPosition, -radius, height, radius).texture(x1, y2).color(color);
        matrices.pop();
    }
}
