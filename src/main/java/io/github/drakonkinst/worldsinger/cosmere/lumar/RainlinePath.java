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

package io.github.drakonkinst.worldsinger.cosmere.lumar;

import io.github.drakonkinst.worldsinger.util.math.Int2;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

public class RainlinePath {

    public static final int RAINLINE_NODE_COUNT = 8;

    private static final double ALPHA = 0.5;    // Centripetal
    private static final float INV_TENSION = 1.0f;
    private static final float ANGLE_INCREMENT = MathHelper.TAU / 8;
    private static final float ANGLE_OFFSET = ANGLE_INCREMENT / 8;
    // Should be tuned alongside LumarLunagreeManager values
    private static final int MIN_RADIUS = 250;
    private static final int MAX_RADIUS = 2000;

    public record Spline(float ax, float ay, float bx, float by, float cx, float cy, float dx,
                         float dy) {

        public float applyX(float t) {
            final float t2 = t * t;
            return ax * t2 * t + bx * t2 + cx * t + dx;
        }

        public float applyY(float t) {
            final float t2 = t * t;
            return ay * t2 * t + by * t2 + cy * t + dy;
        }
    }

    // Based on Catmull-Rom Spline implementation in C++:
    // https://qroph.github.io/2018/07/30/smooth-paths-using-catmull-rom-splines.html
    private static Spline generateSpline(Int2 p0, Int2 p1, Int2 p2, Int2 p3) {
        final float t01 = (float) Math.pow(Int2.distance(p0, p1), ALPHA);
        final float t12 = (float) Math.pow(Int2.distance(p1, p2), ALPHA);
        final float t23 = (float) Math.pow(Int2.distance(p2, p3), ALPHA);
        float m1x = INV_TENSION * (p2.x() - p1.x() + t12 * ((p1.x() - p0.x() / t01
                - (p2.x() - p0.x()) / (t01 + t12))));
        float m1y = INV_TENSION * (p2.y() - p1.y() + t12 * ((p1.y() - p0.y() / t01
                - (p2.y() - p0.y()) / (t01 + t12))));
        float m2x = INV_TENSION * (p2.x() - p1.x() + t12 * ((p3.x() - p2.x() / t23
                - (p3.x() - p1.x()) / (t12 + t23))));
        float m2y = INV_TENSION * (p2.y() - p1.y() + t12 * ((p3.y() - p2.y() / t23
                - (p3.y() - p1.y()) / (t12 + t23))));
        float ax = 2.0f * (p1.x() - p2.x()) + m1x + m2x;
        float ay = 2.0f * (p1.y() - p2.y()) + m1y + m2y;
        float bx = -3.0f * (p1.x() - p2.x()) - m1x - m1x - m2x;
        float by = -3.0f * (p1.y() - p2.y()) - m1y - m1y - m2y;
        return new Spline(ax, ay, bx, by, m1x, m1y, p1.x(), p1.y());
    }

    // https://stackoverflow.com/a/22157217
    public static Int2[] generateRainlineNodes(int lunagreeX, int lunagreeZ, Random random) {
        Int2[] rainlineNodes = new Int2[RAINLINE_NODE_COUNT];
        for (int i = 0; i < RAINLINE_NODE_COUNT; ++i) {
            float angle =
                    i * ANGLE_INCREMENT + random.nextFloat() * 2.0f * ANGLE_OFFSET - ANGLE_OFFSET;
            int radius = random.nextBetween(MIN_RADIUS, MAX_RADIUS);
            int x = lunagreeX + Math.round(radius * MathHelper.cos(angle));
            int y = lunagreeZ + Math.round(radius * MathHelper.sin(angle));
            rainlineNodes[i] = new Int2(x, y);
        }
        return rainlineNodes;
    }

    private final Spline[] splines;

    public RainlinePath(Int2[] rainlineNodes) {
        this.splines = new Spline[RAINLINE_NODE_COUNT];
        this.generateAllSplines(rainlineNodes);
    }

    // In case we want to do dynamic generation later
    private Spline getOrCalculateSpline(int i, Int2[] rainlineNodes) {
        Spline spline = splines[i];
        if (spline != null) {
            return spline;
        }
        spline = calculateSpline(i, rainlineNodes);
        splines[i] = spline;
        return spline;
    }

    private Spline calculateSpline(int i, Int2[] rainlineNodes) {
        Int2 p0 = rainlineNodes[(i + RAINLINE_NODE_COUNT - 1) % RAINLINE_NODE_COUNT];
        Int2 p1 = rainlineNodes[i];
        Int2 p2 = rainlineNodes[(i + 1) % RAINLINE_NODE_COUNT];
        Int2 p3 = rainlineNodes[(i + 2) % RAINLINE_NODE_COUNT];
        return RainlinePath.generateSpline(p0, p1, p2, p3);
    }

    public void generateAllSplines(Int2[] rainlineNodes) {
        for (int i = 0; i < RAINLINE_NODE_COUNT; ++i) {
            if (splines[i] == null) {
                splines[i] = calculateSpline(i, rainlineNodes);
            }
        }
    }
}
