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
import io.github.drakonkinst.worldsinger.network.packet.LunagreeSyncPayload;
import io.github.drakonkinst.worldsinger.worldgen.ModBiomes;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator.SporeSeaEntry;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.FileUtils;

public class LumarLunagreeManager extends LunagreeManager {

    public static final float TRAVEL_DISTANCE = 2000.0f;
    public static final float CELL_SIZE = 1800.0f; // Should always be less than TRAVEL_DISTANCE
    public static final int SEARCH_RADIUS = 1700;  // Should always be less than CELL_SIZE
    private static final int CENTER_X = 0;
    private static final int CENTER_Z = 0;

    public static final int SEARCH_CHECK_INTERVAL = 64;

    private static final IntSet VALID_SPORE_IDS = IntSet.of(DeadSpores.ID, VerdantSpores.ID,
            CrimsonSpores.ID, ZephyrSpores.ID, SunlightSpores.ID, RoseiteSpores.ID,
            MidnightSpores.ID);
    // Associative array of direction vector offsets for axial hex coordinates
    private static final int[] DIRECTION_Q = { +1, +1, +0, -1, -1, +0 };
    private static final int[] DIRECTION_R = { +0, -1, -1, +0, +1, +1 };
    private static final float RAD_3 = MathHelper.sqrt(3);
    private static final float RAD_3_OVER_3 = RAD_3 / 3.0f;

    public static ByteDataType<LumarLunagreeManager> getPersistentByteDataType(ServerWorld world) {
        return new ByteDataType<>(() -> new LumarLunagreeManager(world));
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

    public LumarLunagreeManager(ServerWorld world) {
        this.world = world;
    }

    @Override
    public void saveBytesToFile(File file) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bos);
            for (Long2ObjectMap.Entry<LunagreeLocation> entry : lunagreeMap.long2ObjectEntrySet()) {
                LunagreeLocation value = entry.getValue();
                out.writeLong(entry.getLongKey());
                out.writeInt(value.blockX());
                out.writeInt(value.blockZ());
                out.write(value.sporeId());
            }
            out.close();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bos.writeTo(fileOutputStream);
            bos.close();
            fileOutputStream.close();
        } catch (IOException e) {
            Worldsinger.LOGGER.error("Could not save data {}", this, e);
        }
    }

    @Override
    public void loadBytesFromFile(File file) {
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(
                    FileUtils.readFileToByteArray(file));
            DataInputStream in = new DataInputStream(byteStream);
            while (in.available() > 0) {
                long key = in.readLong();
                int blockX = in.readInt();
                int blockZ = in.readInt();
                int sporeId = in.read();
                lunagreeMap.put(key, new LunagreeLocation(blockX, blockZ, sporeId));
            }
            in.close();
            byteStream.close();
        } catch (IOException e) {
            Worldsinger.LOGGER.error("Error loading saved data: {}", this, e);
        }
    }

    private Optional<LunagreeLocation> getOrCreateLunagreeFor(int q, int r) {
        long key = LumarLunagreeManager.toKey(q, r);
        Optional<LunagreeLocation> entry;
        if (lunagreeMap.containsKey(key)) {
            entry = Optional.of(lunagreeMap.get(key));
        } else {
            entry = generateLunagreeFor(q, r);
        }
        return entry;
    }

    private boolean hasEntryFor(int q, int r) {
        long key = LumarLunagreeManager.toKey(q, r);
        return lunagreeMap.containsKey(key);
    }

    private IntSet generatePossibleSporeIds(int q, int r) {
        IntSet possibleSporeIds = new IntArraySet(VALID_SPORE_IDS);
        for (int i = 0; i < DIRECTION_Q.length; ++i) {
            int neighborQ = q + DIRECTION_Q[i];
            int neighborR = r + DIRECTION_R[i];
            long neighborKey = LumarLunagreeManager.toKey(neighborQ, neighborR);
            if (lunagreeMap.containsKey(neighborKey)) {
                possibleSporeIds.remove(lunagreeMap.get(neighborKey).sporeId());
            }
        }
        // No valid options, so allow repeats
        if (possibleSporeIds.isEmpty()) {
            possibleSporeIds.addAll(VALID_SPORE_IDS);
        }
        return possibleSporeIds;
    }

    private Optional<LunagreeLocation> generateLunagreeFor(int q, int r) {
        // Find a good position
        IntSet possibleSporeIds = generatePossibleSporeIds(q, r);
        IntIntPair center = this.getCenterBlockPosForHexCell(q, r);
        Pair<BlockPos, SporeSeaEntry> result = LocateSporeSeaCommand.locateSporeSea(world,
                center.firstInt(), center.secondInt(), SEARCH_RADIUS, SEARCH_CHECK_INTERVAL, false,
                possibleSporeIds,
                biome -> ModBiomes.DEEP_SPORE_SEA.equals(biome.getKey().orElse(null)));
        if (result == null) {
            return Optional.empty();
        }

        // Generate the result
        BlockPos lunagreePos = result.getFirst();
        int sporeId = result.getSecond().id();
        LunagreeLocation entry = new LunagreeLocation(lunagreePos.getX(), lunagreePos.getZ(),
                sporeId);

        // Store it in the map
        long key = LumarLunagreeManager.toKey(q, r);
        lunagreeMap.put(key, entry);
        this.markDirty();
        return Optional.of(entry);
    }

    // Triggered when the player loads new chunks. This can also generate new lunagrees.
    @Override
    public void updateLunagreeDataForPlayer(ServerPlayerEntity player) {
        long key = getKeyForPos(player.getBlockX(), player.getBlockZ());
        int q = LumarLunagreeManager.getQ(key);
        int r = LumarLunagreeManager.getR(key);

        List<LunagreeLocation> locations = new ArrayList<>(DIRECTION_Q.length + 1);
        getOrCreateLunagreeFor(q, r).ifPresent(locations::add);
        for (int i = 0; i < DIRECTION_Q.length; ++i) {
            int neighborQ = q + DIRECTION_Q[i];
            int neighborR = r + DIRECTION_R[i];
            getOrCreateLunagreeFor(neighborQ, neighborR).ifPresent(locations::add);
        }

        // Send packet
        ServerPlayNetworking.send(player, new LunagreeSyncPayload(locations));
        Worldsinger.LOGGER.info(
                "Sending lunagree data in cell (" + q + ", " + r + "): " + locations);
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
