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
package io.github.drakonkinst.worldsinger.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public final class BoxUtil {

    public static Box createBoxAroundBlock(BlockPos pos, double radius) {
        double minX = pos.getX() - radius;
        double minY = pos.getY() - radius;
        double minZ = pos.getZ() - radius;
        double maxX = pos.getX() + 1.0 + radius;
        double maxY = pos.getY() + 1.0 + radius;
        double maxZ = pos.getZ() + 1.0 + radius;
        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static Box createBoxAroundPos(Vec3d pos, double radius) {
        return BoxUtil.createBoxAroundPos(pos.getX(), pos.getY(), pos.getZ(), radius);
    }

    public static Box createBoxAroundPos(double x, double y, double z, double radius) {
        double minX = x - radius;
        double minY = y - radius;
        double minZ = z - radius;
        double maxX = x + radius;
        double maxY = y + radius;
        double maxZ = z + radius;
        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static Vec3d getRandomPointInBox(Box box, Random random) {
        double x = box.minX + (box.maxX - box.minX) * random.nextDouble();
        double y = box.minY + (box.maxY - box.minY) * random.nextDouble();
        double z = box.minZ + (box.maxZ - box.minZ) * random.nextDouble();
        return new Vec3d(x, y, z);
    }

    private BoxUtil() {}
}
