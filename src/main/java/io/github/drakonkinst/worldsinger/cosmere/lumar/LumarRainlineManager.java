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
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypes;
import io.github.drakonkinst.worldsinger.entity.rainline.RainlineBehavior;
import io.github.drakonkinst.worldsinger.entity.rainline.RainlineEntity;
import io.github.drakonkinst.worldsinger.entity.rainline.RainlineFollowPathBehavior;
import io.github.drakonkinst.worldsinger.entity.rainline.RainlineWanderBehavior;
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
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

// Rainlines on Lumar are placed around lunagrees, so it is tightly coupled to lunagree generation
public class LumarRainlineManager extends PersistentState implements RainlineManager {

    private static final int RAINLINE_UPDATE_INTERVAL = 5 * ModConstants.SECONDS_TO_TICKS;

    // Wandering rainline spawn parameters
    private static final int WANDERING_RAINLINE_UPDATE_INTERVAL = ModConstants.MINUTES_TO_TICKS;
    private static final int WANDERING_RAINLINE_SPAWN_INTERVAL = 5 * ModConstants.MINUTES_TO_TICKS;
    private static final int WANDERING_RAINLINE_SPAWN_FAIL_BONUS =
            2 * ModConstants.MINUTES_TO_TICKS;
    private static final int WANDERING_RAINLINE_MIN_SPAWN_DISTANCE = 64;
    private static final int WANDERING_RAINLINE_MAX_SPAWN_DISTANCE = 80;
    private static final float MIN_RAINLINE_SPAWN_SEPARATION = 64.0f;

    public static final String NAME = "rainlines";
    private static final String KEY_SPAWN_DELAY = "spawn_delay";

    public static PersistentState.Type<LumarRainlineManager> getPersistentStateType(
            LunagreeGenerator generator) {
        return new PersistentState.Type<>(() -> new LumarRainlineManager(generator, 0),
                (nbt, registryLookup) -> LumarRainlineManager.fromNbt(generator, nbt),
                DataFixTypes.LEVEL);
    }

    private static LumarRainlineManager fromNbt(LunagreeGenerator generator, NbtCompound nbt) {
        int spawnDelay = nbt.getInt(KEY_SPAWN_DELAY);
        return new LumarRainlineManager(generator, spawnDelay);
    }

    private final Long2ObjectMap<RainlinePath> rainlinePaths = new Long2ObjectOpenHashMap<>();
    private final LunagreeGenerator generator;
    private int spawnDelay;

    public LumarRainlineManager(LunagreeGenerator generator, int spawnDelay) {
        this.generator = generator;
        this.spawnDelay = spawnDelay;
    }

    public void serverTick(ServerWorld world) {
        if (world.getTime() % RAINLINE_UPDATE_INTERVAL == 0 && CosmerePlanet.isLumar(world)) {
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
        // Worldsinger.LOGGER.info("Found " + rainlineEntities.size() + " active rainline entities: "
        //         + rainlineEntities);
        doRainlinePathsTick(world, rainlineEntities);
        doRainlineWanderingTick(world, rainlineEntities);
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
        RainlineEntity rainlineEntity = ModEntityTypes.RAINLINE.create(world, SpawnReason.NATURAL);
        if (rainlineEntity == null) {
            return false;
        }
        rainlineEntity.setPosition(pos.x, RainlineEntity.getTargetHeight(world), pos.y);
        rainlineEntity.setRainlineBehavior(behavior);
        return world.spawnEntity(rainlineEntity);
    }

    private void doRainlineWanderingTick(ServerWorld world, List<RainlineEntity> rainlineEntities) {
        if (world.getTime() % WANDERING_RAINLINE_UPDATE_INTERVAL != 0) {
            return;
        }
        spawnDelay += WANDERING_RAINLINE_UPDATE_INTERVAL;
        this.markDirty();
        if (spawnDelay < WANDERING_RAINLINE_SPAWN_INTERVAL) {
            return;
        }
        spawnDelay -= WANDERING_RAINLINE_SPAWN_INTERVAL;
        // Worldsinger.LOGGER.info("Attempting to spawn wandering rainline");
        if (!attemptSpawnWanderingRainline(world, rainlineEntities)) {
            spawnDelay += WANDERING_RAINLINE_SPAWN_FAIL_BONUS;
        }
    }

    private boolean attemptSpawnWanderingRainline(ServerWorld world,
            List<RainlineEntity> rainlineEntities) {
        int numPlayers = world.getPlayers().size();
        if (numPlayers == 0) {
            return false;
        }
        Random random = world.getRandom();
        PlayerEntity playerEntity = world.getPlayers().get(random.nextInt(numPlayers));
        BlockPos playerPos = playerEntity.getBlockPos();
        int spawnX = playerPos.getX() + getRandomSpawnOffset(random);
        int spawnZ = playerPos.getZ() + getRandomSpawnOffset(random);
        // Ensure it is not too close to any existing rainline
        for (RainlineEntity rainlineEntity : rainlineEntities) {
            double deltaX = rainlineEntity.getX() - spawnX;
            double deltaZ = rainlineEntity.getZ() - spawnZ;
            double distSq = deltaX * deltaX + deltaZ * deltaZ;
            if (distSq > MIN_RAINLINE_SPAWN_SEPARATION * MIN_RAINLINE_SPAWN_SEPARATION) {
                return false;
            }
        }
        // noinspection deprecation
        if (!world.isPosLoaded(spawnX, spawnZ)) {
            return false;
        }
        SporeSeaEntry sporeSeaEntry = LumarChunkGenerator.getSporeSeaEntryAtPos(
                world.getChunkManager().getNoiseConfig(), spawnX, spawnZ);
        if (AetherSpores.hasRainlinePathsInSea(sporeSeaEntry.id())) {
            return false;
        }
        RainlineEntity rainlineEntity = ModEntityTypes.RAINLINE.create(world, SpawnReason.NATURAL);
        if (rainlineEntity == null) {
            return false;
        }
        rainlineEntity.setPosition(spawnX, RainlineEntity.getTargetHeight(world), spawnZ);
        rainlineEntity.setRainlineBehavior(new RainlineWanderBehavior(rainlineEntity.getRandom()));
        world.spawnEntity(rainlineEntity);
        Worldsinger.LOGGER.info("Spawning wandering rainline at ({}, {})", spawnX, spawnZ);
        return true;
    }

    private int getRandomSpawnOffset(Random random) {
        return (WANDERING_RAINLINE_MIN_SPAWN_DISTANCE + random.nextInt(
                WANDERING_RAINLINE_MAX_SPAWN_DISTANCE - WANDERING_RAINLINE_MIN_SPAWN_DISTANCE)) * (
                random.nextBoolean() ? -1 : 1);
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
    public NbtCompound writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        nbt.putInt(KEY_SPAWN_DELAY, spawnDelay);
        return nbt;
    }
}
