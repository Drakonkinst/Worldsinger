package io.github.drakonkinst.worldsinger.worldgen.biome;

import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarBiomeCreator;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.PlacedFeature;

public final class ModBiomes {

    public static void bootstrap(Registerable<Biome> biomeRegisterable) {
        RegistryEntryLookup<PlacedFeature> featureLookup = biomeRegisterable.getRegistryLookup(
                RegistryKeys.PLACED_FEATURE);
        RegistryEntryLookup<ConfiguredCarver<?>> carverLookup = biomeRegisterable.getRegistryLookup(
                RegistryKeys.CONFIGURED_CARVER);

        biomeRegisterable.register(ModBiomeKeys.DEEP_SPORE_SEA,
                LumarBiomeCreator.createDeepSporeSea(featureLookup, carverLookup));
        biomeRegisterable.register(ModBiomeKeys.LUMAR_FOREST,
                LumarBiomeCreator.createLumarForest(featureLookup, carverLookup));
        biomeRegisterable.register(ModBiomeKeys.LUMAR_GRASSLANDS,
                LumarBiomeCreator.createLumarGrasslands(featureLookup, carverLookup));
        biomeRegisterable.register(ModBiomeKeys.LUMAR_PEAKS,
                LumarBiomeCreator.createLumarPeaks(featureLookup, carverLookup));
        biomeRegisterable.register(ModBiomeKeys.LUMAR_ROCKS,
                LumarBiomeCreator.createLumarRocks(featureLookup, carverLookup));
        biomeRegisterable.register(ModBiomeKeys.SALTSTONE_ISLAND,
                LumarBiomeCreator.createSaltstoneIsland(featureLookup, carverLookup));
        biomeRegisterable.register(ModBiomeKeys.SPORE_SEA,
                LumarBiomeCreator.createSporeSea(featureLookup, carverLookup));
    }
}
