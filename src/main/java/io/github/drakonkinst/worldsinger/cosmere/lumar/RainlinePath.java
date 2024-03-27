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

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.item.map.CustomMapDecoration;
import io.github.drakonkinst.worldsinger.item.map.CustomMapDecorationsComponent.Decoration;
import io.github.drakonkinst.worldsinger.util.math.Int2;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator;
import java.util.Map;
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

    private static final double ALPHA = 0.5;    // Centripetal Catmull-Rom Spline
    private static final float INV_TENSION = 1.0f;
    private static final float ANGLE_INCREMENT = MathHelper.TAU / 8;
    private static final float ANGLE_OFFSET = ANGLE_INCREMENT / 8;
    // Should be tuned alongside LumarLunagreeManager values
    private static final int MAP_SPLINE_STEPS = 100;
    private static final int LENGTH_APPROX_STEPS = 32;

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

    public record ClosestStepResult(int nearestStep, float distanceSqFromStep) {}

    public record RainlinePathInfo(RainlinePath path, int nearestStep) {}

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

    private final Int2[] rainlineNodes;
    private final Spline[] splines;
    private final float totalLength;

    public RainlinePath(Int2[] rainlineNodes) {
        this.rainlineNodes = rainlineNodes;
        this.splines = new Spline[RAINLINE_NODE_COUNT];
        this.totalLength = this.generateAllSplines(rainlineNodes);
    }

    public int applyMapDecorations(ServerWorld serverWorld, Map<String, Decoration> decorations,
            MapState mapState) {
        int numAdded = 0;
        NoiseConfig noiseConfig = serverWorld.getChunkManager().getNoiseConfig();
        for (int i = 0; i < RainlinePath.RAINLINE_NODE_COUNT; ++i) {
            numAdded += applyMapDecorationsForSpline(noiseConfig, decorations, mapState,
                    splines[i]);
        }
        return numAdded;
    }

    private int applyMapDecorationsForSpline(NoiseConfig noiseConfig,
            Map<String, Decoration> decorations, MapState mapState, Spline spline) {
        int numAdded = 0;
        for (int i = 0; i < MAP_SPLINE_STEPS; ++i) {
            float t = (float) i / MAP_SPLINE_STEPS;
            float x = spline.applyX(t);
            float z = spline.applyY(t);
            // Only add if it is on the map AND it is not in the Crimson Sea
            if (isOnMap(mapState, x, z)
                    && LumarChunkGenerator.getSporeSeaEntryAtPos(noiseConfig, (int) x, (int) z).id()
                    != CrimsonSpores.ID) {
                ++numAdded;
                decorations.put("rainline-" + (++nextIconId),
                        new Decoration(CustomMapDecoration.Type.RAINLINE, x, z, 0.0f));
            }
        }
        return numAdded;
    }

    private boolean isOnMap(MapState mapState, float x, float z) {
        float scaleModifier = 1 << mapState.scale;
        float mapX = (x - mapState.centerX) / scaleModifier;
        float mapY = (z - mapState.centerZ) / scaleModifier;
        return mapX >= -CustomMapDecoration.MAP_LIMITS && mapY >= -CustomMapDecoration.MAP_LIMITS
                && mapX <= CustomMapDecoration.MAP_LIMITS && mapY <= CustomMapDecoration.MAP_LIMITS;
    }

    private Spline calculateSpline(int i, Int2[] rainlineNodes) {
        Int2 p0 = rainlineNodes[(i + RAINLINE_NODE_COUNT - 1) % RAINLINE_NODE_COUNT];
        Int2 p1 = rainlineNodes[i];
        Int2 p2 = rainlineNodes[(i + 1) % RAINLINE_NODE_COUNT];
        Int2 p3 = rainlineNodes[(i + 2) % RAINLINE_NODE_COUNT];
        Spline spline = RainlinePath.generateSpline(p0, p1, p2, p3);
        // Worldsinger.LOGGER.info(
        //         "Between " + p1 + " and " + p2 + ": linear = " + Int2.distance(p1, p2)
        //                 + ", approximate = " + spline.length());
        return spline;
    }

    private float generateAllSplines(Int2[] rainlineNodes) {
        float totalLength = 0.0f;
        for (int i = 0; i < RAINLINE_NODE_COUNT; ++i) {
            Spline spline = calculateSpline(i, rainlineNodes);
            totalLength += spline.length();
            splines[i] = spline;
        }
        return totalLength;
    }

    // Get position of the rainline following this rainline path depending on the game time
    public Vec2f getPositionAtTime(long gameTime, float blocksPerTick) {
        float ticksPerCycle = totalLength / blocksPerTick;
        float cycleProgress = (float) gameTime % ticksPerCycle;
        return getPositionForDistanceAlongCycle(cycleProgress);
    }

    public Vec2f getPositionAtStep(int step) {
        float distanceAlongCycle = step * STEP_BLOCK_LENGTH;
        return getPositionForDistanceAlongCycle(distanceAlongCycle);
    }

    public ClosestStepResult getClosestStep(float x, float z) {
        // Find the nearest spline on this path
        int nearestSplineIndex = getNearestSplineIndex(x, z);
        Spline spline = splines[nearestSplineIndex];

        // Check every position on this spline
        int numSteps = MathHelper.floor(spline.length() / STEP_BLOCK_LENGTH);
        Worldsinger.LOGGER.info("Checking " + numSteps + " steps");
        int nearestStep = 0;
        float minDistSq = Float.MAX_VALUE;
        for (int step = 0; step < numSteps; ++step) {
            Vec2f stepPos = getPositionAtStep(step);
            float deltaX = stepPos.x - x;
            float deltaZ = stepPos.y - z;
            float distSq = deltaX * deltaX + deltaZ * deltaZ;
            if (distSq < minDistSq) {
                nearestStep = step;
                minDistSq = distSq;
            }
        }
        return new ClosestStepResult(nearestStep, minDistSq);
    }

    private int getNearestSplineIndex(float x, float z) {
        // Find the index of the closest node
        int nearestNodeIndex = 0;
        float minDistSq = Float.MAX_VALUE;
        float[] cachedDistances = new float[RAINLINE_NODE_COUNT];
        for (int i = 0; i < RAINLINE_NODE_COUNT; ++i) {
            Int2 node = rainlineNodes[i];
            float deltaX = node.x() - x;
            float deltaZ = node.y() - z;
            float distSq = deltaX * deltaX + deltaZ * deltaZ;
            if (distSq < minDistSq) {
                nearestNodeIndex = i;
                minDistSq = distSq;
            }
            cachedDistances[i] = distSq;
        }

        // Find the second-closest node and return the spline connecting those two nodes
        // A spline's index is its first node index
        int nextNodeIndex = (nearestNodeIndex + 1) % RAINLINE_NODE_COUNT;
        int prevNodeIndex = (nearestNodeIndex + RAINLINE_NODE_COUNT - 1) % RAINLINE_NODE_COUNT;
        if (cachedDistances[nextNodeIndex] < cachedDistances[prevNodeIndex]) {
            // Next node is closer, so use this node's index as the spline
            return nearestNodeIndex;
        }
        // Previous node is closer
        return prevNodeIndex;
    }

    private Vec2f getPositionForDistanceAlongCycle(float distanceAlongCycle) {
        if (distanceAlongCycle <= 0) {
            return getStartingPoint();
        }
        if (distanceAlongCycle > totalLength) {
            distanceAlongCycle = distanceAlongCycle % totalLength;
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
