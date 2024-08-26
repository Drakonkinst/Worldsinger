/*
 * MIT License
 *
 * Copyright (c) 2024 Drakonkinst
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
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
import io.github.drakonkinst.worldsinger.network.packet.LunagreeSyncPayload;
import io.github.drakonkinst.worldsinger.util.math.Int2;
import io.github.drakonkinst.worldsinger.worldgen.ModBiomes;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator.SporeSeaEntry;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.datafixer.DataFixTypes;
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

// Manages lunagree placement and rainlines that orbit lunagrees.
// On Lumar, lunagrees are placed on an approximate hex grid.
public class LumarLunagreeGenerator extends PersistentState implements LunagreeGenerator {

    public static final float TRAVEL_DISTANCE = 2000.0f;
    public static final float CELL_SIZE = 1800.0f; // Should always be less than TRAVEL_DISTANCE
    public static final int SEARCH_RADIUS = 1000;  // Should always be less than CELL_SIZE
    public static final int SPORE_FALL_RADIUS = 200;
    public static final int NULL_LUNAGREE_SPORE_ID = 0;
    public static final String NAME = "lunagrees";

    // Offset it by a bit to increase the chance players won't spawn directly in a lunagree
    private static final int CENTER_X = 1600;
    private static final int CENTER_Z = 1600;
    private static final String KEY_LUNAGREES = "lunagrees";
    private static final String KEY_CELL = "cell";
    private static final String KEY_DATA = "data";

    public static final int SEARCH_CHECK_INTERVAL = 64;

    private static final IntSet VALID_SPORE_IDS = IntSet.of(VerdantSpores.ID, CrimsonSpores.ID,
            ZephyrSpores.ID, SunlightSpores.ID, RoseiteSpores.ID, MidnightSpores.ID);
    // Associative array of direction vector offsets for axial hex coordinates
    private static final int[] DIRECTION_Q = { +1, +1, +0, -1, -1, +0 };
    private static final int[] DIRECTION_R = { +0, -1, -1, +0, +1, +1 };
    private static final float RAD_3 = MathHelper.sqrt(3);
    private static final float RAD_3_OVER_3 = RAD_3 / 3.0f;

    public static PersistentState.Type<LumarLunagreeGenerator> getPersistentStateType(
            ServerWorld world) {
        return new PersistentState.Type<>(() -> new LumarLunagreeGenerator(world),
                (nbt, registryLookup) -> LumarLunagreeGenerator.fromNbt(world, nbt),
                DataFixTypes.LEVEL);
    }

    private static LumarLunagreeGenerator fromNbt(ServerWorld world, NbtCompound nbt) {
        LumarLunagreeGenerator lunagreeManager = new LumarLunagreeGenerator(world);
        NbtList lunagreeDataList = nbt.getList(KEY_LUNAGREES, NbtElement.COMPOUND_TYPE);
        boolean anyInvalid = false;
        for (NbtElement entryData : lunagreeDataList) {
            NbtCompound entryCompound = (NbtCompound) entryData;
            long key = entryCompound.getLong(KEY_CELL);
            NbtCompound valueData = entryCompound.getCompound(KEY_DATA);
            LunagreeLocation location = LunagreeLocation.fromNbt(valueData);
            if (location.rainlineNodes()[0] == null) {
                Worldsinger.LOGGER.warn(
                        "Failed to parse rainline nodes for " + LumarLunagreeGenerator.keyToString(
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

    // Key is hex coordinates packed into a long
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
        int q = LumarLunagreeGenerator.getQ(key);
        int r = LumarLunagreeGenerator.getR(key);
        return LumarLunagreeGenerator.cellToString(q, r);
    }

    private static String cellToString(int q, int r) {
        return "(" + q + ", " + r + ")";
    }

    private static int getCenterXForHexCell(int q, int r) {
        return Math.round(1.5f * q * CELL_SIZE) + CENTER_X;
    }

    private static int getCenterZForHexCell(int q, int r) {
        return Math.round((RAD_3 * 0.5f * q + RAD_3 * r) * CELL_SIZE) + CENTER_Z;
    }

    // Convert block pos to flat-top pixel coordinates, rounded
    private static long getHexCellForBlockPos(int blockX, int blockZ) {
        float fracQ = (2.0f / 3.0f * (blockX - CENTER_X)) / CELL_SIZE;
        float fracR = ((-1.0f / 3.0f) * (blockX - CENTER_X) + RAD_3_OVER_3 * (blockZ - CENTER_Z))
                / CELL_SIZE;
        return LumarLunagreeGenerator.roundAxial(fracQ, fracR);
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
        return LumarLunagreeGenerator.toKey(q, r);
    }

    private final ServerWorld world;
    private final Long2ObjectMap<LunagreeLocation> lunagreeMap = new Long2ObjectOpenHashMap<>();

    public LumarLunagreeGenerator(ServerWorld world) {
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

    @Override
    public long getKeyForPos(int blockX, int blockZ) {
        return LumarLunagreeGenerator.getHexCellForBlockPos(blockX, blockZ);
    }

    @Override
    public long[] getNeighborKeys(long centerKey) {
        long[] neighborKeys = new long[DIRECTION_Q.length];
        int q = LumarLunagreeGenerator.getQ(centerKey);
        int r = LumarLunagreeGenerator.getR(centerKey);

        for (int i = 0; i < DIRECTION_Q.length; ++i) {
            int neighborQ = q + DIRECTION_Q[i];
            int neighborR = r + DIRECTION_R[i];
            neighborKeys[i] = LumarLunagreeGenerator.toKey(neighborQ, neighborR);
        }
        return neighborKeys;
    }

    @Override
    public LunagreeLocation getLunagreeForKey(long key, boolean shouldCreate) {
        LunagreeLocation entry = lunagreeMap.get(key);
        if (entry == null && shouldCreate) {
            entry = generateLunagreeFor(key);
            lunagreeMap.put(key, entry);
            this.markDirty();
        }
        return entry;
    }

    // Triggered when the player loads new chunks. This can also generate new lunagrees.
    @Override
    public void updateLunagreeDataForPlayer(ServerPlayerEntity player) {
        long key = getKeyForPos(player.getBlockX(), player.getBlockZ());
        List<LunagreeLocation> locations = getNeighboringLunagrees(key, true, true);
        ServerPlayNetworking.send(player, new LunagreeSyncPayload(locations));
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
    private LunagreeLocation generateLunagreeFor(long key) {
        int q = LumarLunagreeGenerator.getQ(key);
        int r = LumarLunagreeGenerator.getR(key);

        // Find a good position
        IntSet possibleSporeIds = generatePossibleSporeIds(q, r);
        int centerX = LumarLunagreeGenerator.getCenterXForHexCell(q, r);
        int centerZ = LumarLunagreeGenerator.getCenterZForHexCell(q, r);
        Pair<BlockPos, SporeSeaEntry> result = LocateSporeSeaCommand.locateSporeSea(world, centerX,
                centerZ, SEARCH_RADIUS, SEARCH_CHECK_INTERVAL, false, possibleSporeIds,
                biome -> ModBiomes.DEEP_SPORE_SEA.equals(biome.getKey().orElse(null)));

        int lunagreeX;
        int lunagreeZ;
        int sporeId;
        if (result == null) {
            Worldsinger.LOGGER.info(
                    "Failed to generate lunagree for " + LumarLunagreeGenerator.cellToString(q, r));
            lunagreeX = centerX;
            lunagreeZ = centerZ;
            sporeId = NULL_LUNAGREE_SPORE_ID;
        } else {
            BlockPos lunagreePos = result.getFirst();
            lunagreeX = lunagreePos.getX();
            lunagreeZ = lunagreePos.getZ();
            sporeId = result.getSecond().id();
        }

        // Generate the result
        Int2[] rainlineNodes = RainlinePath.generateRainlineNodes(lunagreeX, lunagreeZ,
                world.getRandom());
        LunagreeLocation entry = new LunagreeLocation(lunagreeX, lunagreeZ, sporeId, rainlineNodes);
        Worldsinger.LOGGER.info("Generated lunagree of spore ID " + sporeId + " for "
                + LumarLunagreeGenerator.cellToString(q, r) + " with rainline nodes: "
                + Arrays.toString(entry.rainlineNodes()));
        return entry;
    }
}
