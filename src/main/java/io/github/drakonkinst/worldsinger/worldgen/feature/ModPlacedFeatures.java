package io.github.drakonkinst.worldsinger.worldgen.feature;

import io.github.drakonkinst.worldsinger.Worldsinger;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.UndergroundConfiguredFeatures;
import net.minecraft.world.gen.feature.VegetationConfiguredFeatures;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.CountPlacementModifier;
import net.minecraft.world.gen.placementmodifier.EnvironmentScanPlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightmapPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.placementmodifier.SurfaceThresholdFilterPlacementModifier;

public final class ModPlacedFeatures {

    public static final RegistryKey<PlacedFeature> LAKE_WATER_UNDERGROUND = ModPlacedFeatures.of(
            "lake_water_underground");
    public static final RegistryKey<PlacedFeature> AMETHYST_GEODE_LOWER = ModPlacedFeatures.of(
            "amethyst_geode_lower");
    public static final RegistryKey<PlacedFeature> ORE_CLAY_LUMAR = ModPlacedFeatures.of(
            "ore_clay_lumar");
    public static final RegistryKey<PlacedFeature> ORE_SALT_LUMAR = ModPlacedFeatures.of(
            "ore_salt_lumar");
    public static final RegistryKey<PlacedFeature> ORE_COARSE_DIRT = ModPlacedFeatures.of(
            "ore_coarse_dirt");
    public static final RegistryKey<PlacedFeature> ORE_SAND_LUMAR = ModPlacedFeatures.of(
            "ore_sand_lumar");
    public static final RegistryKey<PlacedFeature> PATCH_DEAD_BUSH_HIGHER = ModPlacedFeatures.of(
            "patch_dead_bush_higher");
    public static final RegistryKey<PlacedFeature> ORE_SILVER_LUMAR = ModPlacedFeatures.of(
            "ore_silver_lumar");

    public static void initialize() {}

    private static RegistryKey<PlacedFeature> of(String id) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Worldsinger.id(id));
    }

    private static void register(Registerable<PlacedFeature> featureRegisterable,
            RegistryKey<PlacedFeature> key, RegistryEntry<ConfiguredFeature<?, ?>> feature,
            PlacementModifier... modifiers) {
        featureRegisterable.register(key, new PlacedFeature(feature, List.of(modifiers)));
    }

    public static void bootstrap(Registerable<PlacedFeature> featureRegisterable) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> configuredFeatureLookup = featureRegisterable.getRegistryLookup(
                RegistryKeys.CONFIGURED_FEATURE);

        register(featureRegisterable, ModPlacedFeatures.AMETHYST_GEODE_LOWER,
                configuredFeatureLookup.getOrThrow(UndergroundConfiguredFeatures.AMETHYST_GEODE),
                RarityFilterPlacementModifier.of(24), SquarePlacementModifier.of(),
                HeightRangePlacementModifier.uniform(YOffset.aboveBottom(6), YOffset.fixed(16)),
                BiomePlacementModifier.of());
        register(featureRegisterable, ModPlacedFeatures.LAKE_WATER_UNDERGROUND,
                configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.LAKE_WATER),
                RarityFilterPlacementModifier.of(9), SquarePlacementModifier.of(),
                // TODO: Testing this adjusted height, used to be 0
                HeightRangePlacementModifier.uniform(YOffset.belowTop(0), YOffset.fixed(24)),
                EnvironmentScanPlacementModifier.of(Direction.DOWN, BlockPredicate.allOf(
                        BlockPredicate.not(BlockPredicate.matchingBlocks(Blocks.AIR)),
                        BlockPredicate.insideWorldBounds(new Vec3i(0, -5, 0))), 32),
                SurfaceThresholdFilterPlacementModifier.of(Type.OCEAN_FLOOR_WG, Integer.MIN_VALUE,
                        -15), BiomePlacementModifier.of());
        register(featureRegisterable, ModPlacedFeatures.ORE_CLAY_LUMAR,
                configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.ORE_CLAY_BURIED),
                CountPlacementModifier.of(8), SquarePlacementModifier.of(),
                HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.fixed(160)),
                BiomePlacementModifier.of());
        register(featureRegisterable, ModPlacedFeatures.ORE_COARSE_DIRT,
                configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.ORE_COARSE_DIRT),
                CountPlacementModifier.of(4), SquarePlacementModifier.of(),
                HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.fixed(160)),
                BiomePlacementModifier.of());
        register(featureRegisterable, ModPlacedFeatures.ORE_SALT_LUMAR,
                configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.ORE_SALT_BURIED),
                CountPlacementModifier.of(40), SquarePlacementModifier.of(),
                HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.belowTop(0)),
                BiomePlacementModifier.of());
        register(featureRegisterable, ModPlacedFeatures.ORE_SAND_LUMAR,
                configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.ORE_SAND_BURIED),
                CountPlacementModifier.of(4), SquarePlacementModifier.of(),
                HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.fixed(160)),
                BiomePlacementModifier.of());
        register(featureRegisterable, ModPlacedFeatures.ORE_SILVER_LUMAR,
                configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.ORE_SILVER_BURIED),
                CountPlacementModifier.of(4), SquarePlacementModifier.of(),
                HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.fixed(50)),
                BiomePlacementModifier.of());
        register(featureRegisterable, ModPlacedFeatures.PATCH_DEAD_BUSH_HIGHER,
                configuredFeatureLookup.getOrThrow(VegetationConfiguredFeatures.PATCH_DEAD_BUSH),
                CountPlacementModifier.of(2), SquarePlacementModifier.of(),
                HeightmapPlacementModifier.of(Type.WORLD_SURFACE_WG),
                HeightRangePlacementModifier.uniform(YOffset.fixed(110), YOffset.belowTop(0)),
                BiomePlacementModifier.of());

    }

    private ModPlacedFeatures() {}
}
