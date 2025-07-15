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

import io.github.drakonkinst.worldsinger.entity.rainline.RainlineEntity;
import io.github.drakonkinst.worldsinger.util.VectorUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.component.type.MapDecorationsComponent.Decoration;
import net.minecraft.item.map.MapState;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface RainlineSpawner {

    RainlineSpawner NULL = new NullRainlineSpawner();
    int RAINLINE_RADIUS = 8;
    int RAINLINE_EFFECT_RADIUS = 4;
    int NUM_RAINLINES_PER_LUNAGREE = 8;

    // Mainly used for client-side rendering since there are server bugs
    @Nullable
    static RainlineEntity getNearestRainlineEntity(World world, Vec3d pos, double bonusRadius) {
        List<RainlineEntity> nearbyRainlines = getNearbyRainlineEntities(world, pos, bonusRadius);
        double minDistSq = Double.MAX_VALUE;
        RainlineEntity nearestEntity = null;

        for (RainlineEntity entity : nearbyRainlines) {
            double distSq = VectorUtil.getHorizontalDistSq(entity.getPos(), pos);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearestEntity = entity;
            }
        }
        return nearestEntity;
    }

    static List<RainlineEntity> getNearbyRainlineEntities(World world, Vec3d pos, double radius) {
        final double x = pos.getX();
        final double z = pos.getZ();
        final double searchRadius = RAINLINE_RADIUS + radius;
        final Box box = new Box(x - searchRadius, world.getBottomY(), z - searchRadius,
                x + searchRadius, world.getTopYInclusive(), z + searchRadius);
        return world.getEntitiesByClass(RainlineEntity.class, box, EntityPredicates.VALID_ENTITY);
    }

    static boolean shouldRainlineAffectBlocks(ServerWorld world, Vec3d pos) {
        // final double x = pos.getX();
        // final double z = pos.getZ();
        // final Box box = new Box(x - RAINLINE_SEARCH_RADIUS, world.getBottomY(),
        //         z - RAINLINE_SEARCH_RADIUS, x + RAINLINE_SEARCH_RADIUS, world.getTopYInclusive(),
        //         z + RAINLINE_SEARCH_RADIUS);
        // List<RainlineEntity> nearbyRainlines = world.getEntitiesByClass(RainlineEntity.class, box,
        //         EntityPredicates.VALID_ENTITY);

        // For some reason there is a bug where the rainline, even though nearby, doesn't get caught in the box after a few seconds. So doing it manually
        List<RainlineEntity> nearbyRainlines = new ArrayList<>();
        world.collectEntitiesByType(TypeFilter.instanceOf(RainlineEntity.class),
                EntityPredicates.VALID_ENTITY, nearbyRainlines);
        for (RainlineEntity entity : nearbyRainlines) {
            double distSq = VectorUtil.getHorizontalDistSq(entity.getPos(), pos);
            if (distSq <= RAINLINE_EFFECT_RADIUS * RAINLINE_EFFECT_RADIUS) {
                return true;
            }
        }
        return false;
    }

    void serverTick(ServerWorld world);

    int applyMapDecorations(ServerWorld world, Map<String, Decoration> decorations,
            MapState mapState);

    @Nullable
    RainlinePath getRainlinePathById(long id);
}
