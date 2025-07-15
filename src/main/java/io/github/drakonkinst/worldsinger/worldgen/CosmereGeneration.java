package io.github.drakonkinst.worldsinger.worldgen;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.MiscConfiguredFeatures;

public final class CosmereGeneration {

    public static void generateBonusChest(MinecraftServer server, RegistryKey<World> registryKey,
            BlockPos spawnPos) {
        ServerWorld world = server.getWorld(registryKey);
        if (world == null) {
            return;
        }
        ServerChunkManager chunkManager = world.getChunkManager();
        world.getRegistryManager()
                .getOptional(RegistryKeys.CONFIGURED_FEATURE)
                .flatMap(featureRegistry -> featureRegistry.getOptional(
                        MiscConfiguredFeatures.BONUS_CHEST))
                .ifPresent(feature -> feature.value()
                        .generate(world, chunkManager.getChunkGenerator(), world.random, spawnPos));
    }

    private CosmereGeneration() {}
}
