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

package io.github.drakonkinst.worldsinger.api;

import io.github.drakonkinst.worldsinger.cosmere.lumar.RainlineManager;
import io.github.drakonkinst.worldsinger.entity.rainline.RainlineEntity;
import io.github.drakonkinst.worldsinger.util.VectorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

public class ClientRainlineData {

    private static final float RAINLINE_GRADIENT_RADIUS = 32;
    private static final float RAINLINE_SKY_GRADIENT_RADIUS = 128;

    private RainlineEntity nearestRainlineEntity = null;

    public void update(ClientWorld world, ClientPlayerEntity player) {
        nearestRainlineEntity = RainlineManager.getNearestRainlineEntity(world, getCameraPos(),
                RAINLINE_SKY_GRADIENT_RADIUS);
    }

    public RainlineEntity getNearestRainlineEntity() {
        return nearestRainlineEntity;
    }

    public boolean isRainlineNearby() {
        return nearestRainlineEntity != null;
    }

    public float getRainlineGradient(boolean isSkyDarken) {
        if (nearestRainlineEntity == null) {
            return 0.0f;
        }
        float radius = isSkyDarken ? RAINLINE_SKY_GRADIENT_RADIUS : RAINLINE_GRADIENT_RADIUS;
        double distSq = VectorUtil.getHorizontalDistSq(getCameraPos(),
                nearestRainlineEntity.getPos());
        if (distSq > radius * radius) {
            // Too far away
            return 0.0f;
        }
        return 1.0f - (float) Math.sqrt(distSq) / radius;
    }

    private Vec3d getCameraPos() {
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        return camera.getPos();
    }
}
