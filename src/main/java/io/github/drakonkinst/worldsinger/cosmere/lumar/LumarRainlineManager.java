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
import io.github.drakonkinst.worldsinger.entity.ModEntityTypes;
import io.github.drakonkinst.worldsinger.entity.rainline.RainlineBehavior;
import io.github.drakonkinst.worldsinger.entity.rainline.RainlineEntity;
import io.github.drakonkinst.worldsinger.entity.rainline.RainlineFollowPathBehavior;
import io.github.drakonkinst.worldsinger.item.map.CustomMapDecorationsComponent.Decoration;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator.SporeSeaEntry;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongByteMutablePair;
import it.unimi.dsi.fastutil.longs.LongBytePair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.item.map.MapState;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

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

        int totalNumAdded = 0;
        RainlinePath centerPath = getOrCreateRainlineData(key);
        totalNumAdded += centerPath.applyMapDecorations(world, decorations, mapState, 1);
        for (int i = 0; i < neighborKeys.length; ++i) {
            long neighborKey = neighborKeys[i];
            RainlinePath neighborPath = getOrCreateRainlineData(neighborKey);
            int numAdded = neighborPath.applyMapDecorations(world, decorations, mapState, i + 2);
            totalNumAdded += numAdded;
        }
        // Worldsinger.LOGGER.info("Applying " + totalNumAdded + " rainline icons to map");
        return totalNumAdded;
    }

    @Override
    public @Nullable RainlinePath getRainlinePathById(long id) {
        return rainlinePaths.get(id);
    }

    private void doRainlineTick(ServerWorld world) {
        List<RainlineEntity> rainlineEntities = new ArrayList<>();
        world.collectEntitiesByType(TypeFilter.instanceOf(RainlineEntity.class),
                EntityPredicates.VALID_ENTITY, rainlineEntities);
        Worldsinger.LOGGER.info("Found " + rainlineEntities.size() + " active rainline entities: "
                + rainlineEntities);
        doRainlinePathsTick(world, rainlineEntities);
        doRainlineWanderingTick(world);
    }

    private void doRainlinePathsTick(ServerWorld world, List<RainlineEntity> rainlineEntities) {
        Set<LongBytePair> existingPathIds = removeDuplicateRainlinePaths(rainlineEntities);
        Set<LunagreeLocation> lunagreeLocations = getLunagreeLocationsToUpdate(world);
        for (LunagreeLocation location : lunagreeLocations) {
            spawnRainlinesForLocation(world, location, existingPathIds);
        }
    }

    private void spawnRainlinesForLocation(ServerWorld world, LunagreeLocation location,
            Set<LongBytePair> existingPathIds) {
        long locationId = generator.getKeyForPos(location.blockX(), location.blockZ());
        RainlinePath path = rainlinePaths.get(locationId);
        if (path == null) {
            return;
        }
        LongBytePair pathId = new LongByteMutablePair(locationId, (byte) -1);
        for (int pathIndex = 0; pathIndex < NUM_RAINLINES_PER_LUNAGREE; ++pathIndex) {
            pathId.second((byte) pathIndex);
            ensureRainlineIsSpawned(world, pathId, path, existingPathIds);
        }
    }

    private void ensureRainlineIsSpawned(ServerWorld world, LongBytePair pathId, RainlinePath path,
            Set<LongBytePair> existingPathIds) {
        // Check if a rainline following that path already exists
        if (existingPathIds.contains(pathId)) {
            return;
        }
        // Check if position is loaded in the world
        int stepOffset = path.getStepOffset(pathId.secondByte());
        if (stepOffset < 0) {
            return;
        }
        Vec2f rainlinePos = path.getRainlinePosition(world, stepOffset);
        if (!isPosLoaded(world, rainlinePos)) {
            return;
        }
        // Ensure that no rainlines are spawned in the Crimson
        int blockX = (int) rainlinePos.x;
        int blockZ = (int) rainlinePos.y;
        SporeSeaEntry sporeSeaEntry = LumarChunkGenerator.getSporeSeaEntryAtPos(
                world.getChunkManager().getNoiseConfig(), blockX, blockZ);
        if (!AetherSpores.hasRainlinePathsInSea(sporeSeaEntry.id())) {
            return;
        }

        boolean success = spawnRainlineFollowingPath(world, rainlinePos, path, pathId.firstLong(),
                pathId.secondByte());
        if (success) {
            Worldsinger.LOGGER.info("Spawning rainline at ({}, {})", blockX, blockZ);
        } else {
            Worldsinger.LOGGER.warn("Failed to spawn rainline at ({}, {})", blockX, blockZ);
        }
    }

    private boolean isPosLoaded(ServerWorld world, Vec2f pos) {
        // noinspection deprecation
        return world.isPosLoaded((int) pos.x, (int) pos.y);
    }

    private boolean spawnRainlineFollowingPath(ServerWorld world, Vec2f pos, RainlinePath path,
            long locationId, byte index) {
        RainlineBehavior behavior = new RainlineFollowPathBehavior(path, locationId, index);
        RainlineEntity rainlineEntity = ModEntityTypes.RAINLINE.create(world);
        if (rainlineEntity == null) {
            return false;
        }
        rainlineEntity.setPosition(pos.x, RainlineEntity.getTargetHeight(world), pos.y);
        rainlineEntity.setRainlineBehavior(behavior);
        return world.spawnEntity(rainlineEntity);
    }

    private void doRainlineWanderingTick(ServerWorld world) {
        // TODO
    }

    private Set<LongBytePair> removeDuplicateRainlinePaths(List<RainlineEntity> rainlineEntities) {
        Set<LongBytePair> existingPathIds = new HashSet<>();
        List<RainlineEntity> rainlineEntitiesToRemove = new ArrayList<>();
        for (RainlineEntity entity : rainlineEntities) {
            if (entity.getRainlineBehavior() instanceof RainlineFollowPathBehavior followPathBehavior) {
                if (!existingPathIds.add(followPathBehavior.getPathId())) {
                    rainlineEntitiesToRemove.add(entity);
                }
            }
        }
        rainlineEntities.removeAll(rainlineEntitiesToRemove);
        return existingPathIds;
    }

    private Set<LunagreeLocation> getLunagreeLocationsToUpdate(ServerWorld world) {
        Set<LunagreeLocation> lunagreeLocations = new HashSet<>();
        for (ServerPlayerEntity player : world.getPlayers()) {
            List<LunagreeLocation> locationsNearPlayer = generator.getLunagreesNearPos(
                    player.getBlockX(), player.getBlockZ());
            lunagreeLocations.addAll(locationsNearPlayer);
        }
        return lunagreeLocations;
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
}
