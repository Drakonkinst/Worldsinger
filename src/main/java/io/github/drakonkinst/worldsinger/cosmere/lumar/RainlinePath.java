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

import io.github.drakonkinst.worldsinger.registry.ModMapDecorationTypes;
import io.github.drakonkinst.worldsinger.util.math.Int2;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.component.type.MapDecorationsComponent.Decoration;
import net.minecraft.item.map.MapState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.noise.NoiseConfig;

public class RainlinePath {

    public static final int RAINLINE_NODE_COUNT = 8;
    public static final int MIN_RADIUS = 250;
    public static final int MAX_RADIUS = 2000;

    private static final float STEP_BLOCK_LENGTH = 16.0f;
    private static final float MAP_ICON_DISTANCE = 24.0f;

    private static final double ALPHA = 0.5;    // Centripetal Catmull-Rom Spline
    private static final float INV_TENSION = 1.0f;
    private static final float ANGLE_INCREMENT = MathHelper.TAU / 8;
    private static final float ANGLE_OFFSET = ANGLE_INCREMENT / 8;
    // Should be tuned alongside LumarLunagreeManager values
    private static final int LENGTH_APPROX_STEPS = 32;
    private static final int TICKS_PER_STEP = 20;
    private static final float STEPS_PER_TICK = 1.0f / TICKS_PER_STEP;

    private static int nextIconId = 0;

    private record Spline(float ax, float ay, float bx, float by, float cx, float cy, float dx,
                          float dy, float length) {

        private static float apply(float a, float b, float c, float d, float t) {
            final float t2 = t * t;
            return a * t2 * t + b * t2 + c * t + d;
        }

        private static float calculateApproxLength(float ax, float ay, float bx, float by, float cx,
                float cy, float dx, float dy) {
            Vec2f[] splinePoints = new Vec2f[LENGTH_APPROX_STEPS];
            for (int i = 0; i < LENGTH_APPROX_STEPS; ++i) {
                float t = (float) i / LENGTH_APPROX_STEPS;
                float x = Spline.apply(ax, bx, cx, dx, t);
                float y = Spline.apply(ay, by, cy, dy, t);
                splinePoints[i] = new Vec2f(x, y);
            }

            float distance = 0.0f;
            for (int i = 0; i < LENGTH_APPROX_STEPS; ++i) {
                Vec2f p1 = splinePoints[i];
                Vec2f p2 = splinePoints[(i + 1) % LENGTH_APPROX_STEPS];
                distance += MathHelper.sqrt(p1.distanceSquared(p2));
            }
            return distance;
        }

        public static Spline of(float ax, float ay, float bx, float by, float cx, float cy,
                float dx, float dy) {
            float length = Spline.calculateApproxLength(ax, ay, bx, by, cx, cy, dx, dy);
            return new Spline(ax, ay, bx, by, cx, cy, dx, dy, length);
        }

        public Vec2f apply(float t) {
            float x = applyX(t);
            float y = applyY(t);
            return new Vec2f(x, y);
        }

        public float applyX(float t) {
            return Spline.apply(ax, bx, cx, dx, t);
        }

        public float applyY(float t) {
            return Spline.apply(ay, by, cy, dy, t);
        }
    }

    // Based on Catmull-Rom Spline implementation in C++:
    // https://qroph.github.io/2018/07/30/smooth-paths-using-catmull-rom-splines.html
    private static Spline generateSpline(Int2 p0, Int2 p1, Int2 p2, Int2 p3) {
        final float t01 = (float) Math.pow(Int2.distance(p0, p1), ALPHA);
        final float t12 = (float) Math.pow(Int2.distance(p1, p2), ALPHA);
        final float t23 = (float) Math.pow(Int2.distance(p2, p3), ALPHA);
        float m1x = INV_TENSION * (p2.x() - p1.x() + t12 * ((p1.x() - p0.x()) / t01
                - (p2.x() - p0.x()) / (t01 + t12)));
        float m1y = INV_TENSION * (p2.y() - p1.y() + t12 * ((p1.y() - p0.y()) / t01
                - (p2.y() - p0.y()) / (t01 + t12)));
        float m2x = INV_TENSION * (p2.x() - p1.x() + t12 * ((p3.x() - p2.x()) / t23
                - (p3.x() - p1.x()) / (t12 + t23)));
        float m2y = INV_TENSION * (p2.y() - p1.y() + t12 * ((p3.y() - p2.y()) / t23
                - (p3.y() - p1.y()) / (t12 + t23)));
        float ax = 2.0f * (p1.x() - p2.x()) + m1x + m2x;
        float ay = 2.0f * (p1.y() - p2.y()) + m1y + m2y;
        float bx = -3.0f * (p1.x() - p2.x()) - m1x - m1x - m2x;
        float by = -3.0f * (p1.y() - p2.y()) - m1y - m1y - m2y;
        return Spline.of(ax, ay, bx, by, m1x, m1y, p1.x(), p1.y());
    }

    // https://stackoverflow.com/a/22157217
    public static List<Int2> generateRainlineNodes(int lunagreeX, int lunagreeZ, Random random) {
        List<Int2> rainlineNodes = new ArrayList<>(Collections.nCopies(RAINLINE_NODE_COUNT, null));
        for (int i = 0; i < RAINLINE_NODE_COUNT; ++i) {
            float angle =
                    i * ANGLE_INCREMENT + random.nextFloat() * 2.0f * ANGLE_OFFSET - ANGLE_OFFSET;
            int radius = random.nextBetween(MIN_RADIUS, MAX_RADIUS);
            int x = lunagreeX + Math.round(radius * MathHelper.cos(angle));
            int y = lunagreeZ + Math.round(radius * MathHelper.sin(angle));
            rainlineNodes.set(i, new Int2(x, y));
        }
        return rainlineNodes;
    }

    private final Spline[] splines;
    private final float totalLength;

    public RainlinePath(List<Int2> rainlineNodes) {
        this.splines = new Spline[RAINLINE_NODE_COUNT];
        this.totalLength = this.generateAllSplines(rainlineNodes);
    }

    public int applyMapDecorations(ServerWorld serverWorld, Map<String, Decoration> decorations,
            MapState mapState, int cellNeighborIndex) {
        nextIconId = 0;
        int numAdded = 0;
        NoiseConfig noiseConfig = serverWorld.getChunkManager().getNoiseConfig();
        for (int i = 0; i < RainlinePath.RAINLINE_NODE_COUNT; ++i) {
            numAdded += applyMapDecorationsForSpline(noiseConfig, decorations, mapState, splines[i],
                    cellNeighborIndex);
        }
        return numAdded;
    }

    // Gets the stepProgress offset for the rainline at provided index, or -1 if bad index
    public int getStepOffset(int index) {
        if (index < 0 || index >= RainlineSpawner.NUM_RAINLINES_PER_LUNAGREE) {
            return -1;
        }
        float percentageOffset = index * 1.0f / RainlineSpawner.NUM_RAINLINES_PER_LUNAGREE;
        return Math.round(percentageOffset * getMaxSteps());
    }

    public Vec2f getRainlinePosition(ServerWorld world, int stepOffset) {
        long gameTime = world.getTime();
        // Given game time and initial offset, where should we be?
        float stepProgress = (gameTime * STEPS_PER_TICK + stepOffset) % getMaxSteps();
        return getPositionForStepProgress(stepProgress);
    }

    private int applyMapDecorationsForSpline(NoiseConfig noiseConfig,
            Map<String, Decoration> decorations, MapState mapState, Spline spline,
            int cellNeighborIndex) {
        int numAdded = 0;
        float prevX = spline.applyX(0.0f);
        float prevZ = spline.applyY(0.0f);
        int splineSteps = MathHelper.floor(spline.length() / MAP_ICON_DISTANCE);
        for (int i = 1; i <= splineSteps; ++i) {
            float t = (float) i / splineSteps;
            float x = spline.applyX(t);
            float z = spline.applyY(t);
            // Only add if it is on the map AND it is not in the Crimson Sea
            if (isOnMap(mapState, x, z) && AetherSpores.hasRainlinePathsInSea(
                    LumarChunkGenerator.getSporeSeaEntryAtPos(noiseConfig, (int) x, (int) z)
                            .id())) {
                float rotation = (float) MathHelper.atan2(z - prevZ, x - prevX)
                        * MathHelper.DEGREES_PER_RADIAN;
                ++numAdded;
                decorations.put("rainline-" + cellNeighborIndex + "-" + (++nextIconId),
                        new Decoration(ModMapDecorationTypes.RAINLINE, x, z, rotation));
            }
            prevX = x;
            prevZ = z;
        }
        return numAdded;
    }

    private boolean isOnMap(MapState mapState, float x, float z) {
        float scaleModifier = 1 << mapState.scale;
        float mapX = (x - mapState.centerX) / scaleModifier;
        float mapY = (z - mapState.centerZ) / scaleModifier;
        return mapX >= -ModMapDecorationTypes.MAP_LIMITS
                && mapY >= -ModMapDecorationTypes.MAP_LIMITS
                && mapX <= ModMapDecorationTypes.MAP_LIMITS
                && mapY <= ModMapDecorationTypes.MAP_LIMITS;
    }

    private Spline calculateSpline(int i, List<Int2> rainlineNodes) {
        Int2 p0 = rainlineNodes.get((i + RAINLINE_NODE_COUNT - 1) % RAINLINE_NODE_COUNT);
        Int2 p1 = rainlineNodes.get(i);
        Int2 p2 = rainlineNodes.get((i + 1) % RAINLINE_NODE_COUNT);
        Int2 p3 = rainlineNodes.get((i + 2) % RAINLINE_NODE_COUNT);
        Spline spline = RainlinePath.generateSpline(p0, p1, p2, p3);
        // Worldsinger.LOGGER.info(
        //         "Between " + p1 + " and " + p2 + ": linear = " + Int2.distance(p1, p2)
        //                 + ", approximate = " + spline.length());
        return spline;
    }

    private float generateAllSplines(List<Int2> rainlineNodes) {
        float totalLength = 0.0f;
        for (int i = 0; i < RAINLINE_NODE_COUNT; ++i) {
            Spline spline = calculateSpline(i, rainlineNodes);
            totalLength += spline.length();
            splines[i] = spline;
        }
        return totalLength;
    }

    private Vec2f getPositionForStepProgress(float stepProgress) {
        float distanceAlongCycle = stepProgress * STEP_BLOCK_LENGTH;
        return getPositionForDistanceAlongCycle(distanceAlongCycle);
    }

    // Get position for distance in blocks, assuming distance is in [0, totalLength)
    private Vec2f getPositionForDistanceAlongCycle(float distanceAlongCycle) {
        if (distanceAlongCycle <= 0) {
            return getStartingPoint();
        }

        float current = 0.0f;
        float next;
        for (int i = 0; i < RAINLINE_NODE_COUNT; ++i) {
            Spline spline = splines[i];
            next = current + spline.length();
            if (next >= distanceAlongCycle) {
                float distanceAlongSpline = distanceAlongCycle - current;
                float t = MathHelper.clamp(distanceAlongSpline / spline.length(), 0.0f, 1.0f);
                return spline.apply(t);
            }
            current = next;
        }
        return getStartingPoint();
    }

    private Vec2f getStartingPoint() {
        return splines[0].apply(0.0f);
    }

    public int getMaxSteps() {
        return MathHelper.floor(totalLength / STEP_BLOCK_LENGTH);
    }
}
