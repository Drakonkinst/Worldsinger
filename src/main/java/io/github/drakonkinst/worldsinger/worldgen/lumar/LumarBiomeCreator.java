package io.github.drakonkinst.worldsinger.worldgen.lumar;

import io.github.drakonkinst.worldsinger.worldgen.carver.ModConfiguredCarvers;
import io.github.drakonkinst.worldsinger.worldgen.feature.ModPlacedFeatures;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.MusicType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.BiomeEffects.Builder;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.SpawnSettings.SpawnEntry;
import net.minecraft.world.gen.GenerationStep.Feature;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.MiscPlacedFeatures;
import net.minecraft.world.gen.feature.OrePlacedFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.UndergroundPlacedFeatures;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;
import org.jetbrains.annotations.Nullable;

public final class LumarBiomeCreator {

    private static final int DEFAULT_WATER_COLOR = 4159204;
    private static final int DEFAULT_FOG_COLOR = 12638463;
    private static final int DEFAULT_WATER_FOG_COLOR = 329011;
    private static final float DEFAULT_TEMPERATURE = 2.0f;
    private static final int SPORE_SEA_SKY_COLOR = 8103167;

    private LumarBiomeCreator() {}

    private static Biome createBiome(int skyColor, SpawnSettings.Builder spawnSettings,
            GenerationSettings.LookupBackedBuilder generationSettings, @Nullable MusicSound music) {
        BiomeEffects.Builder builder = new Builder().waterColor(DEFAULT_WATER_COLOR)
                .fogColor(DEFAULT_FOG_COLOR)
                .waterFogColor(DEFAULT_WATER_FOG_COLOR)
                .skyColor(skyColor)
                .moodSound(BiomeMoodSound.CAVE)
                .music(music);
        return new Biome.Builder().precipitation(false)
                .temperature(DEFAULT_TEMPERATURE)
                .downfall(0.0f)
                .effects(builder.build())
                .spawnSettings(spawnSettings.build())
                .generationSettings(generationSettings.build())
                .build();
    }

    private static void addExtraCaves(GenerationSettings.LookupBackedBuilder builder) {
        builder.carver(ModConfiguredCarvers.LUMAR_CAVE);
        builder.carver(ModConfiguredCarvers.LUMAR_CANYON);
        builder.feature(Feature.VEGETAL_DECORATION, UndergroundPlacedFeatures.GLOW_LICHEN);
    }

    private static void addDirt(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIRT);
    }

    private static void addCoarseDirt(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(Feature.UNDERGROUND_ORES, ModPlacedFeatures.ORE_COARSE_DIRT);
    }

    private static void addEmeralds(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_EMERALD);
    }

    private static void addSaltOre(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(Feature.UNDERGROUND_ORES, ModPlacedFeatures.ORE_SALT_LUMAR);
    }

    private static void addAmethystGeodes(GenerationSettings.LookupBackedBuilder builder,
            boolean higher) {
        if (higher) {
            builder.feature(Feature.LOCAL_MODIFICATIONS, UndergroundPlacedFeatures.AMETHYST_GEODE);
        } else {
            builder.feature(Feature.LOCAL_MODIFICATIONS, ModPlacedFeatures.AMETHYST_GEODE_LOWER);
        }
    }

    private static void addMushrooms(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.BROWN_MUSHROOM_NORMAL);
        builder.feature(Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.RED_MUSHROOM_NORMAL);
    }

    private static void addCommonFeatures(GenerationSettings.LookupBackedBuilder builder) {
        // Carvers
        builder.carver(ModConfiguredCarvers.LUMAR_CAVE_EXTRA_UNDERGROUND);
        // Other Land Carvers
        builder.feature(Feature.LAKES, MiscPlacedFeatures.LAKE_LAVA_UNDERGROUND);
        builder.feature(Feature.LAKES, ModPlacedFeatures.LAKE_WATER_UNDERGROUND);
        // Underground Ores
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_GRAVEL);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_GRANITE_UPPER);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_GRANITE_LOWER);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIORITE_UPPER);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIORITE_LOWER);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_ANDESITE_UPPER);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_ANDESITE_LOWER);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_TUFF);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_COAL_UPPER);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_COAL_LOWER);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_IRON_UPPER);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_IRON_MIDDLE);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_IRON_SMALL);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_GOLD);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_REDSTONE);
        builder.feature(Feature.UNDERGROUND_ORES, ModPlacedFeatures.ORE_SILVER_LUMAR);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIAMOND);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIAMOND_LARGE);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIAMOND_BURIED);
        builder.feature(Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_COPPER);
        builder.feature(Feature.UNDERGROUND_ORES, ModPlacedFeatures.ORE_SAND_LUMAR);
        builder.feature(Feature.UNDERGROUND_ORES, ModPlacedFeatures.ORE_CLAY_LUMAR);
        // Springs
        builder.feature(Feature.FLUID_SPRINGS, MiscPlacedFeatures.SPRING_WATER);
    }

    private static void addBatsAndMonsters(SpawnSettings.Builder builder) {
        builder.spawn(SpawnGroup.AMBIENT, 10, new SpawnEntry(EntityType.BAT, 8, 8));
        builder.spawn(SpawnGroup.MONSTER, 100, new SpawnEntry(EntityType.SPIDER, 4, 4));
        builder.spawn(SpawnGroup.MONSTER, 95, new SpawnEntry(EntityType.ZOMBIE, 4, 4));
        builder.spawn(SpawnGroup.MONSTER, 5, new SpawnEntry(EntityType.ZOMBIE_VILLAGER, 1, 1));
        builder.spawn(SpawnGroup.MONSTER, 100, new SpawnEntry(EntityType.SKELETON, 4, 4));
        builder.spawn(SpawnGroup.MONSTER, 100, new SpawnEntry(EntityType.SLIME, 4, 4));
        builder.spawn(SpawnGroup.MONSTER, 100, new SpawnEntry(EntityType.ENDERMAN, 1, 4));
        builder.spawn(SpawnGroup.MONSTER, 100, new SpawnEntry(EntityType.WITCH, 1, 1));
    }

    private static void addGlowSquids(SpawnSettings.Builder builder) {
        builder.spawn(SpawnGroup.UNDERGROUND_WATER_CREATURE, 10,
                new SpawnEntry(EntityType.GLOW_SQUID, 4, 6));
    }

    private static void addWaterCreatures(SpawnSettings.Builder builder) {
        builder.spawn(SpawnGroup.WATER_AMBIENT, 15, new SpawnEntry(EntityType.SALMON, 1, 5));
        builder.spawn(SpawnGroup.WATER_CREATURE, 10, new SpawnEntry(EntityType.SALMON, 1, 2));
    }

    private static void addSeagulls(SpawnSettings.Builder builder) {
        builder.spawn(SpawnGroup.CREATURE, 10,
                new SpawnSettings.SpawnEntry(EntityType.CHICKEN, 4, 4));
    }

    public static Biome createDeepSporeSea(RegistryEntryLookup<PlacedFeature> featureLookup,
            RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder generationSettings = new GenerationSettings.LookupBackedBuilder(
                featureLookup, carverLookup);
        SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();

        addCommonFeatures(generationSettings);
        addCoarseDirt(generationSettings);
        addAmethystGeodes(generationSettings, false);

        addBatsAndMonsters(spawnSettings);
        addSeagulls(spawnSettings);

        return createBiome(SPORE_SEA_SKY_COLOR, spawnSettings, generationSettings, null);
    }

    public static Biome createLumarForest(RegistryEntryLookup<PlacedFeature> featureLookup,
            RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder generationSettings = new GenerationSettings.LookupBackedBuilder(
                featureLookup, carverLookup);
        SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();

        addCommonFeatures(generationSettings);
        addExtraCaves(generationSettings);
        addDirt(generationSettings);
        addAmethystGeodes(generationSettings, true);
        addMushrooms(generationSettings);
        generationSettings.feature(Feature.VEGETAL_DECORATION,
                VegetationPlacedFeatures.FOREST_FLOWERS);
        generationSettings.feature(Feature.VEGETAL_DECORATION,
                VegetationPlacedFeatures.TREES_BIRCH_AND_OAK);
        generationSettings.feature(Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_BUSH);
        generationSettings.feature(Feature.VEGETAL_DECORATION,
                VegetationPlacedFeatures.FLOWER_DEFAULT);
        generationSettings.feature(Feature.VEGETAL_DECORATION,
                VegetationPlacedFeatures.PATCH_GRASS_FOREST);
        generationSettings.feature(Feature.VEGETAL_DECORATION,
                VegetationPlacedFeatures.PATCH_SUGAR_CANE);
        generationSettings.feature(Feature.VEGETAL_DECORATION,
                VegetationPlacedFeatures.PATCH_PUMPKIN);
        generationSettings.feature(Feature.VEGETAL_DECORATION,
                VegetationPlacedFeatures.PATCH_FIREFLY_BUSH_NEAR_WATER);

        addBatsAndMonsters(spawnSettings);
        DefaultBiomeFeatures.addFarmAnimals(spawnSettings);
        addWaterCreatures(spawnSettings);
        addGlowSquids(spawnSettings);

        MusicSound musicSound = MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_FOREST);
        return createBiome(8037887, spawnSettings, generationSettings, musicSound);
    }

    public static Biome createLumarGrasslands(RegistryEntryLookup<PlacedFeature> featureLookup,
            RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder generationSettings = new GenerationSettings.LookupBackedBuilder(
                featureLookup, carverLookup);
        SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();

        addCommonFeatures(generationSettings);
        addExtraCaves(generationSettings);
        addDirt(generationSettings);
        addAmethystGeodes(generationSettings, true);
        addMushrooms(generationSettings);
        generationSettings.feature(Feature.VEGETAL_DECORATION,
                VegetationPlacedFeatures.PATCH_TALL_GRASS_2);
        generationSettings.feature(Feature.VEGETAL_DECORATION,
                VegetationPlacedFeatures.TREES_PLAINS);
        generationSettings.feature(Feature.VEGETAL_DECORATION,
                VegetationPlacedFeatures.FLOWER_PLAIN);
        generationSettings.feature(Feature.VEGETAL_DECORATION,
                VegetationPlacedFeatures.PATCH_GRASS_PLAIN);
        generationSettings.feature(Feature.VEGETAL_DECORATION,
                VegetationPlacedFeatures.PATCH_SUGAR_CANE);
        generationSettings.feature(Feature.VEGETAL_DECORATION,
                VegetationPlacedFeatures.PATCH_PUMPKIN);

        addBatsAndMonsters(spawnSettings);
        DefaultBiomeFeatures.addFarmAnimals(spawnSettings);
        addWaterCreatures(spawnSettings);
        addGlowSquids(spawnSettings);
        spawnSettings.spawn(SpawnGroup.CREATURE, 5,
                new SpawnSettings.SpawnEntry(EntityType.HORSE, 2, 6));
        spawnSettings.spawn(SpawnGroup.CREATURE, 1,
                new SpawnSettings.SpawnEntry(EntityType.DONKEY, 1, 3));

        return createBiome(7907327, spawnSettings, generationSettings, null);
    }

    public static Biome createLumarPeaks(RegistryEntryLookup<PlacedFeature> featureLookup,
            RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder generationSettings = new GenerationSettings.LookupBackedBuilder(
                featureLookup, carverLookup);
        SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();

        addCommonFeatures(generationSettings);
        addExtraCaves(generationSettings);
        addEmeralds(generationSettings);
        addAmethystGeodes(generationSettings, true);

        addBatsAndMonsters(spawnSettings);
        addSeagulls(spawnSettings);
        addGlowSquids(spawnSettings);
        spawnSettings.spawn(SpawnGroup.CREATURE, 5,
                new SpawnSettings.SpawnEntry(EntityType.GOAT, 1, 3));

        MusicSound musicSound = MusicType.createIngameMusic(
                SoundEvents.MUSIC_OVERWORLD_JAGGED_PEAKS);
        return createBiome(8756735, spawnSettings, generationSettings, musicSound);
    }

    public static Biome createLumarRocks(RegistryEntryLookup<PlacedFeature> featureLookup,
            RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder generationSettings = new GenerationSettings.LookupBackedBuilder(
                featureLookup, carverLookup);
        SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();

        addCommonFeatures(generationSettings);
        addExtraCaves(generationSettings);
        addCoarseDirt(generationSettings);
        addAmethystGeodes(generationSettings, true);

        addBatsAndMonsters(spawnSettings);
        addSeagulls(spawnSettings);
        addGlowSquids(spawnSettings);

        return createBiome(8233727, spawnSettings, generationSettings, null);
    }

    public static Biome createSaltstoneIsland(RegistryEntryLookup<PlacedFeature> featureLookup,
            RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder generationSettings = new GenerationSettings.LookupBackedBuilder(
                featureLookup, carverLookup);
        SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();

        addCommonFeatures(generationSettings);
        addExtraCaves(generationSettings);
        addSaltOre(generationSettings);
        addAmethystGeodes(generationSettings, true);
        addMushrooms(generationSettings);
        generationSettings.feature(Feature.VEGETAL_DECORATION,
                ModPlacedFeatures.PATCH_DEAD_BUSH_HIGHER);

        addBatsAndMonsters(spawnSettings);
        addSeagulls(spawnSettings);
        addGlowSquids(spawnSettings);

        return createBiome(7907327, spawnSettings, generationSettings, null);
    }

    public static Biome createSporeSea(RegistryEntryLookup<PlacedFeature> featureLookup,
            RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder generationSettings = new GenerationSettings.LookupBackedBuilder(
                featureLookup, carverLookup);
        SpawnSettings.Builder spawnSettings = new SpawnSettings.Builder();

        addCommonFeatures(generationSettings);
        addCoarseDirt(generationSettings);
        addAmethystGeodes(generationSettings, false);

        addBatsAndMonsters(spawnSettings);
        addSeagulls(spawnSettings);

        return createBiome(SPORE_SEA_SKY_COLOR, spawnSettings, generationSettings, null);
    }

}
