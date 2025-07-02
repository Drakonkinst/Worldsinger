package io.github.drakonkinst.worldsinger.worldgen.feature;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.LakeFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig.Target;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public final class ModConfiguredFeatures {

    public static final RegistryKey<ConfiguredFeature<?, ?>> LAKE_WATER = ModConfiguredFeatures.of(
            "lake_water");
    public static final RegistryKey<ConfiguredFeature<?, ?>> ORE_CLAY_BURIED = ModConfiguredFeatures.of(
            "ore_clay_buried");
    public static final RegistryKey<ConfiguredFeature<?, ?>> ORE_COARSE_DIRT = ModConfiguredFeatures.of(
            "ore_coarse_dirt");
    public static final RegistryKey<ConfiguredFeature<?, ?>> ORE_SALT_BURIED = ModConfiguredFeatures.of(
            "ore_salt_buried");
    public static final RegistryKey<ConfiguredFeature<?, ?>> ORE_SAND_BURIED = ModConfiguredFeatures.of(
            "ore_sand_buried");
    public static final RegistryKey<ConfiguredFeature<?, ?>> ORE_SILVER_BURIED = ModConfiguredFeatures.of(
            "ore_silver_buried");

    private static RegistryKey<ConfiguredFeature<?, ?>> of(String id) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Worldsinger.id(id));
    }

    public static void initialize() {}

    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> featureRegisterable) {
        RuleTest testBaseStoneOverworld = new TagMatchRuleTest(BlockTags.BASE_STONE_OVERWORLD);
        RuleTest testStoneOreReplaceables = new TagMatchRuleTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest testDeepslateOreReplaceables = new TagMatchRuleTest(
                BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest testSaltstone = new BlockMatchRuleTest(ModBlocks.SALTSTONE);

        List<Target> silverTargets = List.of(OreFeatureConfig.createTarget(testStoneOreReplaceables,
                        ModBlocks.SILVER_ORE.getDefaultState()),
                OreFeatureConfig.createTarget(testDeepslateOreReplaceables,
                        ModBlocks.DEEPSLATE_SILVER_ORE.getDefaultState()));

        // noinspection deprecation
        register(featureRegisterable, LAKE_WATER, Feature.LAKE,
                new LakeFeature.Config(BlockStateProvider.of(Blocks.WATER.getDefaultState()),
                        BlockStateProvider.of(Blocks.STONE.getDefaultState())));
        register(featureRegisterable, ORE_CLAY_BURIED, Feature.ORE,
                new OreFeatureConfig(testBaseStoneOverworld, Blocks.CLAY.getDefaultState(), 20,
                        1.0f));
        register(featureRegisterable, ORE_COARSE_DIRT, Feature.ORE,
                new OreFeatureConfig(testBaseStoneOverworld, Blocks.COARSE_DIRT.getDefaultState(),
                        25, 0.0f));
        register(featureRegisterable, ORE_SALT_BURIED, Feature.ORE,
                new OreFeatureConfig(testSaltstone, ModBlocks.SALTSTONE_SALT_ORE.getDefaultState(),
                        17, 1.0f));
        register(featureRegisterable, ORE_SAND_BURIED, Feature.ORE,
                new OreFeatureConfig(testBaseStoneOverworld, Blocks.SAND.getDefaultState(), 20,
                        1.0f));
        register(featureRegisterable, ORE_SILVER_BURIED, Feature.ORE,
                new OreFeatureConfig(silverTargets, 9, 0.5f));
    }

    private static <FC extends FeatureConfig, F extends Feature<FC>> void register(
            Registerable<ConfiguredFeature<?, ?>> registerable,
            RegistryKey<ConfiguredFeature<?, ?>> key, F feature, FC config) {
        registerable.register(key, new ConfiguredFeature<>(feature, config));
    }

    private ModConfiguredFeatures() {}
}
