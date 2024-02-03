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

import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;

public final class VoxelShapeUtil {

    public static final double MAX_VOXEL = 16.0;
    public static final Axis[] AXIS_VALUES = Axis.values();

    public static VoxelShape createOffsetCuboid(double widthOffset, double heightOffset) {
        return VoxelShapeUtil.createUpwardsCuboid(widthOffset, heightOffset,
                MAX_VOXEL - heightOffset);
    }

    public static VoxelShape createUpwardsCuboid(double widthOffset, double minHeight,
            double maxHeight) {
        return VoxelShapeUtil.createDirectionAlignedShape(Direction.UP, widthOffset, minHeight,
                maxHeight);
    }

    public static VoxelShape createDirectionAlignedShape(Direction direction, double widthOffset,
            double minHeight, double maxHeight) {
        VoxelShape shape;
        switch (direction) {
            case UP -> shape = Block.createCuboidShape(widthOffset, minHeight, widthOffset,
                    MAX_VOXEL - widthOffset, maxHeight, MAX_VOXEL - widthOffset);
            case DOWN ->
                    shape = Block.createCuboidShape(widthOffset, MAX_VOXEL - maxHeight, widthOffset,
                            MAX_VOXEL - widthOffset, MAX_VOXEL - minHeight,
                            MAX_VOXEL - widthOffset);
            case NORTH ->
                    shape = Block.createCuboidShape(widthOffset, widthOffset, MAX_VOXEL - maxHeight,
                            MAX_VOXEL - widthOffset, MAX_VOXEL - widthOffset,
                            MAX_VOXEL - minHeight);
            case SOUTH -> shape = Block.createCuboidShape(widthOffset, widthOffset, minHeight,
                    MAX_VOXEL - widthOffset, MAX_VOXEL - widthOffset, maxHeight);
            case EAST ->
                    shape = Block.createCuboidShape(minHeight, widthOffset, widthOffset, maxHeight,
                            MAX_VOXEL - widthOffset, MAX_VOXEL - widthOffset);
            default ->
                    shape = Block.createCuboidShape(MAX_VOXEL - maxHeight, widthOffset, widthOffset,
                            MAX_VOXEL - minHeight, MAX_VOXEL - widthOffset,
                            MAX_VOXEL - widthOffset);
        }
        return shape;
    }

    public static VoxelShape[] createAxisAlignedShapes(double widthOffset, double heightOffset) {
        VoxelShape[] shapes = new VoxelShape[AXIS_VALUES.length];
        for (Axis axis : AXIS_VALUES) {
            int index = axis.ordinal();
            shapes[index] = VoxelShapeUtil.createAxisAlignedShape(axis, widthOffset, heightOffset);
        }
        return shapes;
    }

    // Creates a symmetric axis aligned shape with equal widths, and height along the primary axis.
    public static VoxelShape createAxisAlignedShape(Axis axis, double widthOffset,
            double heightOffset) {
        VoxelShape shape;
        switch (axis) {
            case Y -> shape = Block.createCuboidShape(widthOffset, heightOffset, widthOffset,
                    MAX_VOXEL - widthOffset, MAX_VOXEL - heightOffset, MAX_VOXEL - widthOffset);
            case X -> shape = Block.createCuboidShape(heightOffset, widthOffset, widthOffset,
                    MAX_VOXEL - heightOffset, MAX_VOXEL - widthOffset, MAX_VOXEL - widthOffset);
            // Z
            default -> shape = Block.createCuboidShape(widthOffset, widthOffset, heightOffset,
                    MAX_VOXEL - widthOffset, MAX_VOXEL - widthOffset, MAX_VOXEL - heightOffset);
        }
        return shape;
    }

    public static VoxelShape[] createDirectionAlignedShapes(double widthOffset, double minHeight,
            double maxHeight) {
        VoxelShape[] shapes = new VoxelShape[ModConstants.CARDINAL_DIRECTIONS.length];
        for (Direction direction : ModConstants.CARDINAL_DIRECTIONS) {
            int index = direction.ordinal();
            shapes[index] = VoxelShapeUtil.createDirectionAlignedShape(direction, widthOffset,
                    minHeight, maxHeight);
        }
        return shapes;
    }

    private VoxelShapeUtil() {}
}
