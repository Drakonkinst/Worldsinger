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
import io.github.drakonkinst.worldsinger.world.PersistentByteData;
import io.github.drakonkinst.worldsinger.worldgen.ModBiomes;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator.SporeSeaEntry;
import it.unimi.dsi.fastutil.ints.IntArraySet;
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
import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class LunagreeManager extends PersistentByteData {

    public static final float TRAVEL_DISTANCE = 1000.0f;
    public static final String NAME = "lunagrees";

    private static final IntSet VALID_SPORE_IDS = IntSet.of(DeadSpores.ID, VerdantSpores.ID,
            CrimsonSpores.ID, ZephyrSpores.ID, SunlightSpores.ID, RoseiteSpores.ID,
            MidnightSpores.ID);
    // Associative array of direction vector offsets for axial hex coordinates
    private static final int[] DIRECTION_Q = { +1, +1, +0, -1, -1, +0 };
    private static final int[] DIRECTION_R = { +0, -1, -1, +0, +1, +1 };

    public static ByteDataType<LunagreeManager> getPersistentByteDataType(ServerWorld world) {
        return new ByteDataType<>(() -> new LunagreeManager(world));
    }

    // Does not use NBT
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveBytesToFile(File file) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);
        try {
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
            fileOutputStream.close();
        } catch (IOException e) {
            Worldsinger.LOGGER.error("Could not save data {}", this, e);
        }
    }

    @Override
    public void loadBytes(ByteArrayInputStream bytes) {
        DataInputStream in = new DataInputStream(bytes);
        try {
            while (in.available() > 0) {
                long key = in.readLong();
                int blockX = in.readInt();
                int blockZ = in.readInt();
                int sporeId = in.read();
                lunagreeMap.put(key, new LunagreeLocation(blockX, blockZ, sporeId));
            }
            in.close();
        } catch (IOException e) {
            Worldsinger.LOGGER.error("Error loading saved data: {}", this, e);
        }

    }

    public record LunagreeLocation(int blockX, int blockZ, int sporeId) {

        public double distSqTo(double x, double z) {
            final double deltaX = blockX - x;
            final double deltaZ = blockZ - z;
            return deltaX * deltaX + deltaZ * deltaZ;
        }
    }

    private static IntIntPair getHexCellForBlockPos(BlockPos pos) {
        return null;
    }

    private static BlockPos getCenterBlockPosForHexCell(int q, int r) {
        // TODO
        return null;
    }

    // https://stackoverflow.com/questions/12772939/java-storing-two-ints-in-a-long
    private static long toKey(int q, int r) {
        return (((long) q) << 32) | (r & 0xffffffffL);
    }

    private final ServerWorld world;
    private final Long2ObjectMap<LunagreeLocation> lunagreeMap = new Long2ObjectOpenHashMap<>();

    public LunagreeManager(ServerWorld world) {
        this.world = world;
    }

    private Optional<LunagreeLocation> getOrCreateLunagreeFor(int q, int r, int s) {
        long key = LunagreeManager.toKey(q, r);
        Optional<LunagreeLocation> entry;
        if (lunagreeMap.containsKey(key)) {
            entry = Optional.of(lunagreeMap.get(key));
        } else {
            entry = generateLunagreeFor(q, r);
        }
        return entry;
    }

    private boolean hasEntryFor(int q, int r) {
        long key = LunagreeManager.toKey(q, r);
        return lunagreeMap.containsKey(key);
    }

    private IntSet generatePossibleSporeIds(int q, int r) {
        IntSet possibleSporeIds = new IntArraySet(VALID_SPORE_IDS);
        for (int i = 0; i < DIRECTION_Q.length; ++i) {
            int neighborQ = q + DIRECTION_Q[i];
            int neighborR = r + DIRECTION_R[i];
            long neighborKey = LunagreeManager.toKey(neighborQ, neighborR);
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
        // TODO Set constants
        final int RADIUS = 6400;
        final int BLOCK_CHECK_INTERVAL = 64;
        // Find a good position

        IntSet possibleSporeIds = generatePossibleSporeIds(q, r);
        BlockPos centerPos = LunagreeManager.getCenterBlockPosForHexCell(q, r);
        Pair<BlockPos, SporeSeaEntry> result = LocateSporeSeaCommand.locateSporeSea(world,
                centerPos, RADIUS, BLOCK_CHECK_INTERVAL, false, possibleSporeIds,
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
        long key = LunagreeManager.toKey(q, r);
        lunagreeMap.put(key, entry);
        this.markDirty();
        return Optional.of(entry);
    }
}
