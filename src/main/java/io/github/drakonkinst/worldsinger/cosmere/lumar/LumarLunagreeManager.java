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

import com.mojang.datafixers.util.Pair;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.command.LocateSporeSeaCommand;
import io.github.drakonkinst.worldsinger.item.map.CustomMapDecorationsComponent.Decoration;
import io.github.drakonkinst.worldsinger.network.packet.LunagreeSyncPayload;
import io.github.drakonkinst.worldsinger.util.math.Int2;
import io.github.drakonkinst.worldsinger.worldgen.ModBiomes;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator.SporeSeaEntry;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.NotNull;

public class LumarLunagreeManager extends LunagreeManager {

    public static final float TRAVEL_DISTANCE = 2000.0f;
    public static final float CELL_SIZE = 1800.0f; // Should always be less than TRAVEL_DISTANCE
    public static final int SEARCH_RADIUS = 1000;  // Should always be less than CELL_SIZE
    public static final int SPORE_FALL_RADIUS = 200;
    public static final int NULL_LUNAGREE_SPORE_ID = 0;
    public static final String NAME = "lunagrees";

    private static final int CENTER_X = 0;
    private static final int CENTER_Z = 0;
    private static final String KEY_LUNAGREES = "lunagrees";
    private static final String KEY_CELL = "cell";
    private static final String KEY_DATA = "data";

    public static final int SEARCH_CHECK_INTERVAL = 64;

    private static final IntSet VALID_SPORE_IDS = IntSet.of(DeadSpores.ID, VerdantSpores.ID,
            CrimsonSpores.ID, ZephyrSpores.ID, SunlightSpores.ID, RoseiteSpores.ID,
            MidnightSpores.ID);
    // Associative array of direction vector offsets for axial hex coordinates
    private static final int[] DIRECTION_Q = { +1, +1, +0, -1, -1, +0 };
    private static final int[] DIRECTION_R = { +0, -1, -1, +0, +1, +1 };
    private static final float RAD_3 = MathHelper.sqrt(3);
    private static final float RAD_3_OVER_3 = RAD_3 / 3.0f;

    public static PersistentState.Type<LumarLunagreeManager> getPersistentStateType(
            ServerWorld world) {
        return new PersistentState.Type<>(() -> new LumarLunagreeManager(world),
                (nbt, registryLookup) -> LumarLunagreeManager.fromNbt(world, nbt),
                DataFixTypes.LEVEL);
    }

    // https://stackoverflow.com/questions/12772939/java-storing-two-ints-in-a-long
    private static long toKey(int q, int r) {
        return (((long) q) << 32) | (r & 0xffffffffL);
    }

    private static int getQ(long key) {
        return (int) (key >> 32);
    }

    private static int getR(long key) {
        return (int) key;
    }

    private static String keyToString(long key) {
        int q = LumarLunagreeManager.getQ(key);
        int r = LumarLunagreeManager.getR(key);
        return LumarLunagreeManager.cellToString(q, r);
    }

    private static String cellToString(int q, int r) {
        return "(" + q + ", " + r + ")";
    }

    private static long roundAxial(float fracQ, float fracR) {
        float fracS = -fracQ - fracR;
        int q = Math.round(fracQ);
        int r = Math.round(fracR);
        int s = Math.round(fracS);
        float deltaQ = Math.abs(q - fracQ);
        float deltaR = Math.abs(r - fracR);
        float deltaS = Math.abs(s - fracS);

        if (deltaQ > deltaR && deltaQ > deltaS) {
            q = -r - s;
        } else if (deltaR > deltaS) {
            r = -q - s;
        }
        return LumarLunagreeManager.toKey(q, r);
    }

    private final ServerWorld world;
    private final Long2ObjectMap<LunagreeLocation> lunagreeMap = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<RainlinePath> rainlinePaths = new Long2ObjectOpenHashMap<>();

    public LumarLunagreeManager(ServerWorld world) {
        this.world = world;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        NbtList lunagreeDataList = new NbtList();
        for (Long2ObjectMap.Entry<LunagreeLocation> entry : lunagreeMap.long2ObjectEntrySet()) {
            NbtCompound valueData = new NbtCompound();
            LunagreeLocation value = entry.getValue();
            value.writeNbt(valueData);
            NbtCompound entryData = new NbtCompound();
            entryData.putLong(KEY_CELL, entry.getLongKey());
            entryData.put(KEY_DATA, valueData);
            lunagreeDataList.add(entryData);
        }
        nbt.put(KEY_LUNAGREES, lunagreeDataList);
        return nbt;
    }

    public static LumarLunagreeManager fromNbt(ServerWorld world, NbtCompound nbt) {
        LumarLunagreeManager lunagreeManager = new LumarLunagreeManager(world);
        NbtList lunagreeDataList = nbt.getList(KEY_LUNAGREES, NbtElement.COMPOUND_TYPE);
        boolean anyInvalid = false;
        for (NbtElement entryData : lunagreeDataList) {
            NbtCompound entryCompound = (NbtCompound) entryData;
            long key = entryCompound.getLong(KEY_CELL);
            NbtCompound valueData = entryCompound.getCompound(KEY_DATA);
            LunagreeLocation location = LunagreeLocation.fromNbt(valueData);
            if (location.rainlineNodes()[0] == null) {
                Worldsinger.LOGGER.warn(
                        "Failed to parse rainline nodes for " + LumarLunagreeManager.keyToString(
                                key) + ". Re-generating nodes");
                Int2[] rainlineNodes = RainlinePath.generateRainlineNodes(location.blockX(),
                        location.blockZ(), world.getRandom());
                location.setRainlineNodes(rainlineNodes);
                anyInvalid = true;
            }
            lunagreeManager.lunagreeMap.put(key, location);
        }
        if (anyInvalid) {
            lunagreeManager.markDirty();
        }
        return lunagreeManager;
    }

    private LunagreeLocation getLunagreeFor(int q, int r, boolean shouldCreate) {
        long key = LumarLunagreeManager.toKey(q, r);
        LunagreeLocation entry = lunagreeMap.get(key);
        if (entry == null && shouldCreate) {
            entry = generateLunagreeFor(q, r);
            lunagreeMap.put(key, entry);
            this.markDirty();
        }
        return entry;
    }

    private IntSet generatePossibleSporeIds(int q, int r) {
        IntSet possibleSporeIds = new IntArraySet(VALID_SPORE_IDS);
        // Remove spore IDs that are already nearby. Removing this in order to make generation
        // more deterministic
        // for (int i = 0; i < DIRECTION_Q.length; ++i) {
        //     int neighborQ = q + DIRECTION_Q[i];
        //     int neighborR = r + DIRECTION_R[i];
        //     long neighborKey = LumarLunagreeManager.toKey(neighborQ, neighborR);
        //     if (lunagreeMap.containsKey(neighborKey)) {
        //         possibleSporeIds.remove(lunagreeMap.get(neighborKey).sporeId());
        //     }
        // }
        // No valid options, so allow repeats
        if (possibleSporeIds.isEmpty()) {
            possibleSporeIds.addAll(VALID_SPORE_IDS);
        }
        return possibleSporeIds;
    }

    @NotNull
    private LunagreeLocation generateLunagreeFor(int q, int r) {
        // Find a good position
        IntSet possibleSporeIds = generatePossibleSporeIds(q, r);
        IntIntPair center = this.getCenterBlockPosForHexCell(q, r);
        Pair<BlockPos, SporeSeaEntry> result = LocateSporeSeaCommand.locateSporeSea(world,
                center.firstInt(), center.secondInt(), SEARCH_RADIUS, SEARCH_CHECK_INTERVAL, false,
                possibleSporeIds,
                biome -> ModBiomes.DEEP_SPORE_SEA.equals(biome.getKey().orElse(null)));

        int lunagreeX;
        int lunagreeZ;
        int sporeId;
        if (result == null) {
            Worldsinger.LOGGER.info(
                    "Failed to generate lunagree for " + LumarLunagreeManager.cellToString(q, r));
            lunagreeX = center.firstInt();
            lunagreeZ = center.secondInt();
            sporeId = NULL_LUNAGREE_SPORE_ID;
        } else {
            BlockPos lunagreePos = result.getFirst();
            lunagreeX = lunagreePos.getX();
            lunagreeZ = lunagreePos.getZ();
            sporeId = result.getSecond().id();
        }

        // Generate the result
        // TODO: Is it possible to make rainlines generate the same way every time the world is generated?
        Int2[] rainlineNodes = RainlinePath.generateRainlineNodes(lunagreeX, lunagreeZ,
                world.getRandom());
        LunagreeLocation entry = new LunagreeLocation(lunagreeX, lunagreeZ, sporeId, rainlineNodes);
        Worldsinger.LOGGER.info("Generated lunagree of spore ID " + sporeId + " for "
                + LumarLunagreeManager.cellToString(q, r) + " with rainline nodes: "
                + Arrays.toString(entry.rainlineNodes()));
        return entry;
    }

    // Triggered when the player loads new chunks. This can also generate new lunagrees.
    @Override
    public void updateLunagreeDataForPlayer(ServerPlayerEntity player) {
        List<LunagreeLocation> locations = getLunagreesNear(player.getBlockX(), player.getBlockZ(),
                true);
        ServerPlayNetworking.send(player, new LunagreeSyncPayload(locations));
    }

    @Override
    public Optional<LunagreeLocation> getNearestLunagree(int blockX, int blockZ, int maxDistance) {
        List<LunagreeLocation> candidates = getLunagreesNear(blockX, blockZ);
        LunagreeLocation nearestLocation = null;
        int minDistSq = Integer.MAX_VALUE;
        for (LunagreeLocation location : candidates) {
            int deltaX = blockX - location.blockX();
            int deltaZ = blockZ - location.blockZ();
            int distSq = deltaX * deltaX + deltaZ * deltaZ;
            if (distSq < minDistSq && distSq < maxDistance * maxDistance) {
                nearestLocation = location;
                minDistSq = distSq;
            }
        }
        return Optional.ofNullable(nearestLocation);
    }

    @Override
    public List<LunagreeLocation> getLunagreesNear(int blockX, int blockZ) {
        return getLunagreesNear(blockX, blockZ, false);
    }

    @Override
    public int applyMapDecorations(Map<String, Decoration> decorations, MapState mapState) {
        long key = getKeyForPos(mapState.centerX, mapState.centerZ);
        int q = LumarLunagreeManager.getQ(key);
        int r = LumarLunagreeManager.getR(key);

        int numAdded = 0;
        RainlinePath centerPath = getOrCreateRainlineData(q, r);
        numAdded += centerPath.applyMapDecorations(world, decorations, mapState);
        for (int i = 0; i < DIRECTION_Q.length; ++i) {
            int neighborQ = q + DIRECTION_Q[i];
            int neighborR = r + DIRECTION_R[i];
            RainlinePath neighborPath = getOrCreateRainlineData(neighborQ, neighborR);
            numAdded += neighborPath.applyMapDecorations(world, decorations, mapState);
        }
        // Worldsinger.LOGGER.info("Applying " + numAdded + " rainline icons to map");
        return numAdded;
    }

    private RainlinePath getOrCreateRainlineData(int q, int r) {
        long key = LumarLunagreeManager.toKey(q, r);
        RainlinePath entry = rainlinePaths.get(key);
        LunagreeLocation lunagreeLocation = getLunagreeFor(q, r, true);
        if (entry == null) {
            entry = new RainlinePath(lunagreeLocation.rainlineNodes());
            rainlinePaths.put(key, entry);
        }
        return entry;
    }

    private List<LunagreeLocation> getLunagreesNear(int blockX, int blockZ, boolean shouldCreate) {
        long key = getKeyForPos(blockX, blockZ);
        int q = LumarLunagreeManager.getQ(key);
        int r = LumarLunagreeManager.getR(key);

        List<LunagreeLocation> locations = new ArrayList<>(DIRECTION_Q.length + 1);
        LunagreeLocation currentLocation = getLunagreeFor(q, r, shouldCreate);
        if (currentLocation != null) {
            locations.add(currentLocation);
        }
        for (int i = 0; i < DIRECTION_Q.length; ++i) {
            int neighborQ = q + DIRECTION_Q[i];
            int neighborR = r + DIRECTION_R[i];
            LunagreeLocation neighborLocation = getLunagreeFor(neighborQ, neighborR, shouldCreate);
            if (neighborLocation != null) {
                locations.add(neighborLocation);
            }
        }
        return locations;
    }

    // Convert hex cell to the center block pos
    private IntIntPair getCenterBlockPosForHexCell(int q, int r) {
        int x = Math.round(1.5f * q * CELL_SIZE) + CENTER_X;
        int z = Math.round((RAD_3 * 0.5f * q + RAD_3 * r) * CELL_SIZE) + CENTER_Z;
        return new IntIntImmutablePair(x, z);
    }

    // Convert block pos to flat-top pixel coordinates, rounded
    private long getHexCellForBlockPos(int blockX, int blockZ) {
        float fracQ = (2.0f / 3.0f * (blockX - CENTER_X)) / CELL_SIZE;
        float fracR = ((-1.0f / 3.0f) * (blockX - CENTER_X) + RAD_3_OVER_3 * (blockZ - CENTER_Z))
                / CELL_SIZE;
        return LumarLunagreeManager.roundAxial(fracQ, fracR);
    }

    @Override
    public long getKeyForPos(int blockX, int blockZ) {
        return getHexCellForBlockPos(blockX, blockZ);
    }
}
