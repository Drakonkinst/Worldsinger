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
import io.github.drakonkinst.worldsinger.cosmere.CosmereWorldAccess;
import io.github.drakonkinst.worldsinger.cosmere.CosmereWorldData;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator.SporeSeaEntry;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.time.Duration;
import java.time.Instant;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.MiscConfiguredFeatures;

public class LumarManager {

    public static final LumarManager NULL = new LumarManager(SeetheManager.NULL,
            LunagreeGenerator.NULL, new NullRainlineManager());
    private static final int SPAWN_SEARCH_RADIUS = 6400;
    private static final int SPAWN_SEARCH_INTERVAL = 16;

    private static Pair<BlockPos, SporeSeaEntry> searchForSpawnPos(ServerWorld lumar,
            IntSet validSporeSeaIds) {
        return LocateSporeSeaCommand.locateSporeSea(lumar, 0, 0, SPAWN_SEARCH_RADIUS,
                SPAWN_SEARCH_INTERVAL, true, validSporeSeaIds, biome -> true);
    }

    public static BlockPos generateOrFetchStartingPos(ServerWorld world) {
        CosmereWorldData cosmereWorldData = ((CosmereWorldAccess) world).worldsinger$getCosmereWorldData();
        if (cosmereWorldData.getSpawnPos() == null) {
            Worldsinger.LOGGER.info("Attempting to locate a safe spawn position on Lumar...");
            Instant start = Instant.now();
            Pair<BlockPos, SporeSeaEntry> result = searchForSpawnPos(world,
                    getIdealSpawnSporeSeaIds());
            if (result == null) {
                Worldsinger.LOGGER.info("Failed to find ideal biome, searching again");
                result = searchForSpawnPos(world, getAllSpawnSporeSeaIds());
            }
            if (result == null) {
                Worldsinger.LOGGER.warn(
                        "Failed to find a safe spawn position, enjoy eating spores");
            } else {
                BlockPos pos = result.getFirst();
                int x = pos.getX();
                int z = pos.getZ();
                int y = world.getChunkManager()
                        .getChunkGenerator()
                        .getHeight(x, z, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, world,
                                world.getChunkManager().getNoiseConfig());
                BlockPos newSpawnPos = new BlockPos(x, y, z);
                cosmereWorldData.setSpawnPos(newSpawnPos);
                cosmereWorldData.markDirty();

                Worldsinger.LOGGER.info("Found a safe position at ({}, {}, {})", x, y, z);

                // Bonus chest!
                if (world.getServer().getSaveProperties().getGeneratorOptions().hasBonusChest()) {
                    Worldsinger.LOGGER.info("Spawning bonus chest!");
                    world.getRegistryManager()
                            .getOptional(RegistryKeys.CONFIGURED_FEATURE)
                            .flatMap(featureRegistry -> featureRegistry.getEntry(
                                    MiscConfiguredFeatures.BONUS_CHEST))
                            .ifPresent(feature -> feature.value()
                                    .generate(world, world.getChunkManager().getChunkGenerator(),
                                            world.random, world.getSpawnPos()));
                }
            }
            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            Worldsinger.LOGGER.info("Safe spawn operation took {} ms", timeElapsed.toMillis());
        }
        return world.getSpawnPos();
    }

    private static IntSet getIdealSpawnSporeSeaIds() {
        return IntSet.of(VerdantSpores.ID, ZephyrSpores.ID, RoseiteSpores.ID);
    }

    private static IntSet getAllSpawnSporeSeaIds() {
        return IntSet.of(VerdantSpores.ID, ZephyrSpores.ID, RoseiteSpores.ID, CrimsonSpores.ID,
                MidnightSpores.ID, SunlightSpores.ID);
    }

    private final SeetheManager seetheManager;
    private final LunagreeGenerator lunagreeGenerator;
    private final RainlineManager rainlineManager;

    public LumarManager(SeetheManager seetheManager, LunagreeGenerator lunagreeGenerator) {
        this(seetheManager, lunagreeGenerator, new LumarRainlineManager(lunagreeGenerator));
    }

    protected LumarManager(SeetheManager seetheManager, LunagreeGenerator lunagreeGenerator,
            RainlineManager rainlineManager) {
        this.seetheManager = seetheManager;
        this.lunagreeGenerator = lunagreeGenerator;
        this.rainlineManager = rainlineManager;
    }

    public void serverTick(ServerWorld world) {
        // Seethe ticks are handled by weather
        rainlineManager.serverTick(world);
    }

    public SeetheManager getSeetheManager() {
        return seetheManager;
    }

    public LunagreeGenerator getLunagreeGenerator() {
        return lunagreeGenerator;
    }

    public RainlineManager getRainlineManager() {
        return rainlineManager;
    }
}
