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

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.drakonkinst.worldsinger.entity.rainline.RainlineEntity;
import io.github.drakonkinst.worldsinger.mixin.client.accessor.WorldRendererAccessor;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry.WeatherRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.World;

@SuppressWarnings("resource")
public class LumarWeatherRenderer implements WeatherRenderer {

    private static final Identifier RAIN = Identifier.of("textures/environment/rain.png");

    private static final int SEARCH_BONUS_RADIUS = 64;
    private static final float SCROLL_WIDTH = 32.0f;
    private static final int NORMAL_DIM = 16;

    private final float[] NORMAL_LINE_DX = new float[1024];
    private final float[] NORMAL_LINE_DZ = new float[1024];

    public LumarWeatherRenderer() {
        for (int i = 0; i < NORMAL_DIM * 2; ++i) {
            for (int j = 0; j < NORMAL_DIM * 2; ++j) {
                float f = (float) (j - NORMAL_DIM);
                float g = (float) (i - NORMAL_DIM);
                float h = MathHelper.sqrt(f * f + g * g);
                this.NORMAL_LINE_DX[i << 5 | j] = -g / h;
                this.NORMAL_LINE_DZ[i << 5 | j] = f / h;
            }
        }
    }

    @Override
    public void render(WorldRenderContext context) {
        Vec3d cameraPos = context.camera().getPos();
        // TODO: If we want to optimize, this can potentially be called every tick rather than every frame
        List<RainlineEntity> nearbyRainlines = RainlineEntity.getNearbyRainlineEntities(
                context.world(), cameraPos, SEARCH_BONUS_RADIUS);

        LightmapTextureManager manager = context.lightmapTextureManager();
        manager.enable();
        World world = context.world();
        int cameraX = MathHelper.floor(cameraPos.getX());
        int cameraY = MathHelper.floor(cameraPos.getY());
        int cameraZ = MathHelper.floor(cameraPos.getZ());
        int ticks = ((WorldRendererAccessor) context.worldRenderer()).worldsinger$getTicks();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = null;

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        int renderDistance = 5;
        if (MinecraftClient.isFancyGraphicsOrBetter()) {
            renderDistance = 10;
        }

        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        boolean drawingAnyRain = false;
        int tracker = -1;

        RenderSystem.setShader(GameRenderer::getParticleProgram);

        BlockPos.Mutable mutable = new Mutable();

        for (int z = cameraZ - renderDistance; z <= cameraZ + renderDistance; ++z) {
            for (int x = cameraX - renderDistance; x <= cameraX + renderDistance; ++x) {
                int normalLineIndex =
                        (z - cameraZ + NORMAL_DIM) * NORMAL_DIM * 2 + x - cameraX + NORMAL_DIM;
                float normalDx = this.NORMAL_LINE_DX[normalLineIndex] * 0.5f;
                float normalDz = this.NORMAL_LINE_DZ[normalLineIndex] * 0.5f;
                mutable.set(x, cameraY, z);
                // TODO: Move this logic to a mixin below this line
                if (isWithinAnyRainline(nearbyRainlines, mutable)) {
                    int surfaceHeight = world.getTopY(Type.MOTION_BLOCKING, x, z);
                    int minFrustumHeight = cameraY - renderDistance;
                    int maxFrustumHeight = cameraY + renderDistance;
                    if (minFrustumHeight < surfaceHeight) {
                        minFrustumHeight = surfaceHeight;
                    }

                    if (maxFrustumHeight < surfaceHeight) {
                        maxFrustumHeight = surfaceHeight;
                    }

                    int t = Math.max(surfaceHeight, cameraY);
                    if (minFrustumHeight != maxFrustumHeight) {
                        Random seededRandom = Random.create(
                                x * x * 3121L + x * 45238971L ^ z * z * 418711L + z * 13761L);
                        mutable.set(x, minFrustumHeight, z);
                        if (!drawingAnyRain || bufferBuilder == null) {
                            drawingAnyRain = true;
                            RenderSystem.setShaderTexture(0, RAIN);
                            bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS,
                                    VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                        }

                        // Variable of death
                        float scrollDistance = -(((ticks & 131071) + (
                                x * x * 3121 + x * 45238971 + z * z * 418711 + z * 13761 & 0xFF))
                                + context.tickCounter().getTickDelta(false)) / SCROLL_WIDTH * (3.0f
                                + seededRandom.nextFloat());
                        float scrollOffset = scrollDistance % SCROLL_WIDTH;
                        float deltaX = x - cameraX + 0.5f;
                        float deltaZ = z - cameraZ + 0.5f;
                        float scaledDist = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ)
                                / (float) renderDistance;
                        float alpha = ((1.0F - scaledDist * scaledDist) * 0.5F + 0.5F);
                        mutable.set(x, t, z);
                        int uv = WorldRenderer.getLightmapCoordinates(world, mutable);
                        bufferBuilder.vertex(x - cameraX + 0.5f - normalDx,
                                        maxFrustumHeight - cameraY, deltaZ - normalDz)
                                .texture(0.0F, minFrustumHeight * 0.25F + scrollOffset)
                                .color(1.0F, 1.0F, 1.0F, alpha)
                                .light(uv);
                        bufferBuilder.vertex(x - cameraX + 0.5f + normalDx,
                                        maxFrustumHeight - cameraY, deltaZ + normalDz)
                                .texture(1.0F, minFrustumHeight * 0.25F + scrollOffset)
                                .color(1.0F, 1.0F, 1.0F, alpha)
                                .light(uv);
                        bufferBuilder.vertex(x - cameraX + 0.5f + normalDx,
                                        minFrustumHeight - cameraY, deltaZ + normalDz)
                                .texture(1.0F, maxFrustumHeight * 0.25F + scrollOffset)
                                .color(1.0F, 1.0F, 1.0F, alpha)
                                .light(uv);
                        bufferBuilder.vertex(x - cameraX + 0.5f - normalDx,
                                        minFrustumHeight - cameraY, deltaZ - normalDz)
                                .texture(0.0F, maxFrustumHeight * 0.25F + scrollOffset)
                                .color(1.0F, 1.0F, 1.0F, alpha)
                                .light(uv);
                    }
                }
                // TODO: Move this logic to a mixin above this line
            }
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        manager.disable();
    }

    private boolean isWithinAnyRainline(List<RainlineEntity> rainlineEntities,
            BlockPos.Mutable mutable) {
        int posX = mutable.getX();
        int posZ = mutable.getZ();
        for (RainlineEntity rainlineEntity : rainlineEntities) {
            Vec3d rainlinePos = rainlineEntity.getPos();
            double deltaX = rainlinePos.getX() - posX;
            double deltaZ = rainlinePos.getZ() - posZ;
            double distSq = deltaX * deltaX + deltaZ * deltaZ;
            if (distSq < RainlineEntity.RAINLINE_RADIUS * RainlineEntity.RAINLINE_RADIUS) {
                return true;
            }
        }
        return false;
    }
}
