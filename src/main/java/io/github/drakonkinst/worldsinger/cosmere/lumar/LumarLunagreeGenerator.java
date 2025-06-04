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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.command.LocateSporeSeaCommand;
import io.github.drakonkinst.worldsinger.network.packet.LunagreeSyncPayload;
import io.github.drakonkinst.worldsinger.util.HexCoordUtil;
import io.github.drakonkinst.worldsinger.util.math.Int2;
import io.github.drakonkinst.worldsinger.worldgen.ModBiomes;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator.SporeSeaEntry;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import org.jetbrains.annotations.NotNull;

// Manages lunagree placement and rainlines that orbit lunagrees.
// On Lumar, lunagrees are placed on an approximate hex grid.
public class LumarLunagreeGenerator extends PersistentState implements LunagreeGenerator {

    private static final String NAME = "lunagrees";
    private static final Codec<Pair<Long, LunagreeLocation>> LUNAGREE_ENTRY_CODEC = Codec.mapPair(
            Codec.LONG.fieldOf("cell"), LunagreeLocation.CODEC.fieldOf("data")).codec();
    public static final Codec<LumarLunagreeGenerator> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(LUNAGREE_ENTRY_CODEC.listOf()
                            .optionalFieldOf("lunagrees", Collections.emptyList())
                            .forGetter(LumarLunagreeGenerator::saveLunagreeMap))
                    .apply(instance, LumarLunagreeGenerator::create));
    public static final PersistentStateType<LumarLunagreeGenerator> STATE_TYPE = new PersistentStateType<>(
            NAME, LumarLunagreeGenerator::new, CODEC, DataFixTypes.LEVEL);

    private static LumarLunagreeGenerator create(List<Pair<Long, LunagreeLocation>> lunagreeList) {
        Long2ObjectOpenHashMap<LunagreeLocation> map = new Long2ObjectOpenHashMap<>();
        for (Pair<Long, LunagreeLocation> pair : lunagreeList) {
            map.put(pair.getFirst().longValue(), pair.getSecond());
        }
        return new LumarLunagreeGenerator(map);
    }

    public static final float TRAVEL_DISTANCE = 2000.0f;
    public static final float CELL_SIZE = 1800.0f; // Should always be less than TRAVEL_DISTANCE
    public static final int SEARCH_RADIUS = 1000;  // Should always be less than CELL_SIZE
    public static final int SPORE_FALL_RADIUS = 100;
    public static final int NULL_LUNAGREE_SPORE_ID = 0;

    // Offset it by a bit to increase the chance players won't spawn directly in a lunagree
    private static final int CENTER_X = 1600;
    private static final int CENTER_Z = 1600;

    public static final int SEARCH_CHECK_INTERVAL = 64;

    private static final IntSet VALID_SPORE_IDS = IntSet.of(VerdantSpores.ID, CrimsonSpores.ID,
            ZephyrSpores.ID, SunlightSpores.ID, RoseiteSpores.ID, MidnightSpores.ID);

    private final Long2ObjectMap<LunagreeLocation> lunagreeMap;

    public LumarLunagreeGenerator() {
        this.lunagreeMap = new Long2ObjectOpenHashMap<>();
        this.markDirty();
    }

    private LumarLunagreeGenerator(Long2ObjectOpenHashMap<LunagreeLocation> lunagreeMap) {
        this.lunagreeMap = lunagreeMap;
    }

    @Override
    public long getKeyForPos(int blockX, int blockZ) {
        return HexCoordUtil.getHexCellForBlockPos(blockX, blockZ, CELL_SIZE, CENTER_X, CENTER_Z);
    }

    @Override
    public long[] getNeighborKeys(long centerKey) {
        return HexCoordUtil.getNeighborKeys(centerKey);
    }

    @Override
    public LunagreeLocation getLunagreeForKey(ServerWorld world, long key, boolean shouldCreate) {
        LunagreeLocation entry = lunagreeMap.get(key);
        if (entry == null && shouldCreate) {
            entry = generateLunagreeFor(world, key);
            lunagreeMap.put(key, entry);
            this.markDirty();
        }
        return entry;
    }

    // Triggered when the player loads new chunks. This can also generate new lunagrees.
    @Override
    public void updateLunagreeDataForPlayer(ServerPlayerEntity player) {
        long key = getKeyForPos(player.getBlockX(), player.getBlockZ());
        List<LunagreeLocation> locations = getNeighboringLunagrees(player.getWorld(), key, true,
                true);
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
    private LunagreeLocation generateLunagreeFor(ServerWorld world, long key) {
        int q = HexCoordUtil.getQ(key);
        int r = HexCoordUtil.getR(key);

        // Find a good position
        IntSet possibleSporeIds = generatePossibleSporeIds(q, r);
        int centerX = HexCoordUtil.getCenterXForHexCell(q, r, CELL_SIZE) + CENTER_X;
        int centerZ = HexCoordUtil.getCenterZForHexCell(q, r, CELL_SIZE) + CENTER_Z;
        Pair<BlockPos, SporeSeaEntry> result = LocateSporeSeaCommand.locateSporeSea(world, centerX,
                centerZ, SEARCH_RADIUS, SEARCH_CHECK_INTERVAL, false, possibleSporeIds,
                biome -> ModBiomes.DEEP_SPORE_SEA.equals(biome.getKey().orElse(null)));

        int lunagreeX;
        int lunagreeZ;
        int sporeId;
        if (result == null) {
            Worldsinger.LOGGER.info(
                    "Failed to generate lunagree for " + HexCoordUtil.cellToString(q, r));
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
        List<Int2> rainlineNodes = RainlinePath.generateRainlineNodes(lunagreeX, lunagreeZ,
                world.getRandom());
        LunagreeLocation entry = new LunagreeLocation(lunagreeX, lunagreeZ, sporeId, rainlineNodes);
        Worldsinger.LOGGER.info("Generated lunagree of spore ID {} for {} with rainline nodes: {}",
                sporeId, HexCoordUtil.cellToString(q, r), entry.rainlineNodes().toString());
        return entry;
    }

    private List<Pair<Long, LunagreeLocation>> saveLunagreeMap() {
        List<Pair<Long, LunagreeLocation>> data = new ArrayList<>(lunagreeMap.size());
        for (Long2ObjectMap.Entry<LunagreeLocation> entry : lunagreeMap.long2ObjectEntrySet()) {
            data.add(Pair.of(entry.getLongKey(), entry.getValue()));
        }
        return data;
    }
}
