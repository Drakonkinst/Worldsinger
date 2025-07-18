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

import io.github.drakonkinst.worldsinger.registry.ModClientEnums;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class LumarDimensionEffects extends DimensionEffects {

    public LumarDimensionEffects() {
        super(ModClientEnums.SkyType.LUMAR, false, false);
    }

    @Override
    public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
        return color.multiply(sunHeight * 0.94f + 0.06f, sunHeight * 0.94f + 0.06f,
                sunHeight * 0.91f + 0.09f);
    }

    @Override
    public int getSkyColor(float skyAngle) {
        float f = MathHelper.cos(skyAngle * (float) (Math.PI * 2));
        float g = f / 0.4F * 0.5F + 0.5F;
        float h = MathHelper.square(1.0F - (1.0F - MathHelper.sin(g * (float) Math.PI)) * 0.99F);
        return ColorHelper.fromFloats(h, g * 0.3F + 0.7F, g * g * 0.7F + 0.2F, 0.2F);
    }

    @Override
    public boolean isSunRisingOrSetting(float skyAngle) {
        float f = MathHelper.cos(skyAngle * (float) (Math.PI * 2));
        return f >= -0.4F && f <= 0.4F;
    }

    @Override
    public boolean useThickFog(int camX, int camY) {
        return false;
    }
}
