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
import io.github.drakonkinst.worldsinger.cosmere.lumar.RainlinePath.ClosestStepResult;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RainlinePath.RainlinePathInfo;
import io.github.drakonkinst.worldsinger.entity.RainlineEntity;
import io.github.drakonkinst.worldsinger.item.map.CustomMapDecorationsComponent.Decoration;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.item.map.MapState;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;

// Rainlines on Lumar are placed around lunagrees, so it is tightly coupled to lunagree generation
public class LumarRainlineManager implements RainlineManager {

    // TODO: Can increase to something like 30 later
    private static final int RAINLINE_UPDATE_INTERVAL = 5 * ModConstants.SECONDS_TO_TICKS;

    private final Long2ObjectMap<RainlinePath> rainlinePaths = new Long2ObjectOpenHashMap<>();
    private final LunagreeGenerator generator;

    public LumarRainlineManager(LunagreeGenerator generator) {
        this.generator = generator;
    }

    public void serverTick(ServerWorld world) {
        if (world.getTime() % RAINLINE_UPDATE_INTERVAL == 0) {
            doRainlineTick(world);
        }
    }

    @Override
    public int applyMapDecorations(ServerWorld world, Map<String, Decoration> decorations,
            MapState mapState) {
        long key = generator.getKeyForPos(mapState.centerX, mapState.centerZ);
        long[] neighborKeys = generator.getNeighborKeys(key);

        int numAdded = 0;
        RainlinePath centerPath = getOrCreateRainlineData(key);
        numAdded += centerPath.applyMapDecorations(world, decorations, mapState);
        for (int i = 0; i < neighborKeys.length; ++i) {
            long neighborKey = neighborKeys[i];
            RainlinePath neighborPath = getOrCreateRainlineData(neighborKey);
            numAdded += neighborPath.applyMapDecorations(world, decorations, mapState);
        }
        // Worldsinger.LOGGER.info("Applying " + numAdded + " rainline icons to map");
        return numAdded;
    }

    private void doRainlineTick(ServerWorld world) {
        List<RainlineEntity> rainlineEntities = new ArrayList<>();
        world.collectEntitiesByType(TypeFilter.instanceOf(RainlineEntity.class),
                EntityPredicates.VALID_ENTITY, rainlineEntities);
        Worldsinger.LOGGER.info("Found " + rainlineEntities.size() + " active rainline entities: "
                + rainlineEntities);

        LongSet cellsWithValidRainline = new LongOpenHashSet();
        for (RainlineEntity entity : rainlineEntities) {

        }

        // For each player, check the cells around them for rainlines and determine if they should spawn
        for (ServerPlayerEntity player : world.getPlayers()) {
            updateRainlinesAroundPlayer(player);
        }
    }

    private void updateRainlinesAroundPlayer(ServerPlayerEntity player) {
        List<LunagreeLocation> lunagreeLocations = generator.getLunagreesNearPos(player.getBlockX(),
                player.getBlockZ());
        long key = generator.getKeyForPos(player.getBlockX(), player.getBlockZ());
        long[] neighborKeys = generator.getNeighborKeys(key);

        List<LunagreeLocation> locations = new ArrayList<>(neighborKeys.length + 1);
        RainlinePath currentPath = getOrCreateRainlineData(key);
        for (int i = 0; i < neighborKeys.length; ++i) {
            long neighborKey = neighborKeys[i];
            RainlinePath neighborPath = getOrCreateRainlineData(neighborKey);
        }
    }

    private RainlinePath getOrCreateRainlineData(long key) {
        RainlinePath entry = rainlinePaths.get(key);
        if (entry == null) {
            LunagreeLocation lunagreeLocation = generator.getLunagreeForKey(key, true);
            entry = new RainlinePath(lunagreeLocation.rainlineNodes());
            rainlinePaths.put(key, entry);
        }
        return entry;
    }

    @Override
    public RainlinePathInfo getClosestRainlinePathInfo(float x, float z) {
        // Get distance to every neighboring rainline
        long key = generator.getKeyForPos((int) x, (int) z);
        long[] neighborKeys = generator.getNeighborKeys(key);

        RainlinePath nearestPath = getOrCreateRainlineData(key);
        ClosestStepResult nearestResult = nearestPath.getClosestStep(x, z);
        for (long neighborKey : neighborKeys) {
            RainlinePath neighborPath = getOrCreateRainlineData(neighborKey);
            ClosestStepResult result = neighborPath.getClosestStep(x, z);
            if (result.distanceSqFromStep() < nearestResult.distanceSqFromStep()) {
                nearestPath = neighborPath;
                nearestResult = result;
            }
        }
        return new RainlinePathInfo(nearestPath, nearestResult.nearestStep());
    }
}
