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

import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.World;

public final class BlockPosUtil {

    public static boolean isInvestitureBlocked(World world, BlockPos emitterPos,
            BlockPos listenerPos) {
        return BlockPosUtil.isOccluded(world, emitterPos, listenerPos,
                ModBlockTags.BLOCKS_INVESTITURE);
    }

    // Note: This only works for BlockPos, we may want something different for entities to account
    // for their hitboxes? Maybe raycast from all corners of their bounding box
    public static boolean isOccluded(World world, BlockPos emitterPos, BlockPos listenerPos,
            TagKey<Block> blockTag) {
        Vec3d fromPos = emitterPos.toCenterPos();
        Vec3d toPos = listenerPos.toCenterPos();
        return BlockPosUtil.isOccluded(world, fromPos, toPos, blockTag);
    }

    public static boolean isOccluded(World world, Vec3d fromPos, Vec3d toPos,
            TagKey<Block> blockTag) {
        for (Direction direction : ModConstants.CARDINAL_DIRECTIONS) {
            Vec3d vec3d3 = fromPos.offset(direction, 1.0E-5f);
            if (world.raycast(
                            new BlockStateRaycastContext(vec3d3, toPos, state -> state.isIn(blockTag)))
                    .getType() == HitResult.Type.BLOCK) {
                continue;
            }
            return false;
        }
        return true;
    }

    public static boolean isInvestitureBlocked(World world, Vec3d emitterPos, Vec3d listenerPos) {
        return BlockPosUtil.isOccluded(world, emitterPos, listenerPos,
                ModBlockTags.BLOCKS_INVESTITURE);
    }

    // Iterates over all blocks in an entity's bounding box. Number of blocks iterated is consistent
    // per entity type, regardless of position. Starts from the minimum noiseY (entity's position).
    public static Iterable<BlockPos> iterateBoundingBoxForEntity(Entity entity) {
        return BlockPosUtil.iterateBoundingBoxForEntity(entity,
                BlockPosUtil.toRoundedYBlockPos(entity.getPos()));
    }

    public static Iterable<BlockPos> iterateBoundingBoxForEntity(Entity entity, BlockPos blockPos) {
        return BlockPosUtil.iterateBoundingBoxForEntity(entity, blockPos, 0, 0, 0);
    }

    public static BlockPos toRoundedBlockPos(Vec3d pos) {
        int x = (int) Math.round(pos.getX());
        int y = (int) Math.round(pos.getY());
        int z = (int) Math.round(pos.getZ());
        return new BlockPos(x, y, z);
    }

    // Rounds only the y-coordinate
    public static BlockPos toRoundedYBlockPos(Vec3d pos) {
        int x = MathHelper.floor(pos.getX());
        int y = (int) Math.round(pos.getY());
        int z = MathHelper.floor(pos.getZ());
        return new BlockPos(x, y, z);
    }

    public static Iterable<BlockPos> iterateBoundingBoxForEntity(Entity entity, BlockPos blockPos,
            int offsetX, int offsetY, int offsetZ) {
        int width = MathHelper.ceil(entity.getWidth());
        int height = MathHelper.ceil(entity.getHeight());

        int minX = blockPos.getX() - (width / 2) + offsetX;
        int maxX = blockPos.getX() + ((width - 1) / 2) + offsetX;
        int minY = blockPos.getY() + offsetY;
        int maxY = blockPos.getY() + height - 1 + offsetY;
        int minZ = blockPos.getZ() - (width / 2) + offsetZ;
        int maxZ = blockPos.getZ() + ((width - 1) / 2) + offsetZ;
        return BlockPos.iterate(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static BlockPos toBlockPos(Vec3d pos) {
        return BlockPos.ofFloored(pos);
    }

    public static int getDistance(BlockPos pos1, BlockPos pos2) {
        return BlockPosUtil.getDistance(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(),
                pos2.getY(), pos2.getZ());
    }

    // Effective radius is a square, so use Chebyshev distance.
    // If we want a more realistic distance, consider Manhattan distance.
    public static int getDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        int deltaX = Math.abs(x1 - x2);
        int deltaY = Math.abs(y1 - y2);
        int deltaZ = Math.abs(z1 - z2);
        return Math.max(deltaX, Math.max(deltaY, deltaZ));
    }

    public static Vec3d getNormalizedVectorBetween(BlockPos from, BlockPos to, boolean negate) {
        return BlockPosUtil.getNormalizedVectorBetween(from.toCenterPos(), to.toCenterPos(),
                negate);
    }

    public static Vec3d getNormalizedVectorBetween(Vec3d from, Vec3d to, boolean negate) {
        Vec3d dir = from.subtract(to).normalize();
        if (negate) {
            return dir.negate();
        }
        return dir;
    }

    private BlockPosUtil() {}
}
