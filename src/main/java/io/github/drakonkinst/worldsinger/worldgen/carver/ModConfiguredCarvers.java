package io.github.drakonkinst.worldsinger.worldgen.carver;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import net.minecraft.util.math.floatprovider.TrapezoidFloatProvider;
import net.minecraft.util.math.floatprovider.UniformFloatProvider;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.CarverDebugConfig;
import net.minecraft.world.gen.carver.CaveCarverConfig;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.RavineCarverConfig;
import net.minecraft.world.gen.heightprovider.UniformHeightProvider;

public final class ModConfiguredCarvers {

    public static final RegistryKey<ConfiguredCarver<?>> LUMAR_CANYON = ModConfiguredCarvers.of(
            "lumar_canyon");
    public static final RegistryKey<ConfiguredCarver<?>> LUMAR_CAVE = ModConfiguredCarvers.of(
            "lumar_cave");
    public static final RegistryKey<ConfiguredCarver<?>> LUMAR_CAVE_EXTRA_UNDERGROUND = ModConfiguredCarvers.of(
            "lumar_cave_extra_underground");

    public static void initialize() {}

    private static RegistryKey<ConfiguredCarver<?>> of(String id) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_CARVER, Worldsinger.id(id));
    }

    public static void bootstrap(Registerable<ConfiguredCarver<?>> carverRegisterable) {
        RegistryEntryLookup<Block> blockLookup = carverRegisterable.getRegistryLookup(
                RegistryKeys.BLOCK);

        // Based on ConfiguredCarvers.CAVE
        carverRegisterable.register(LUMAR_CAVE, Carver.CAVE.configure(new CaveCarverConfig(0.15F,
                UniformHeightProvider.create(YOffset.aboveBottom(8), YOffset.fixed(180)),
                UniformFloatProvider.create(0.1F, 0.9F), YOffset.aboveBottom(8),
                CarverDebugConfig.create(false, Blocks.CRIMSON_BUTTON.getDefaultState()),
                blockLookup.getOrThrow(ModBlockTags.LUMAR_CARVER_REPLACEABLES),
                UniformFloatProvider.create(0.7F, 1.4F), UniformFloatProvider.create(0.8F, 1.3F),
                UniformFloatProvider.create(-1.0F, -0.4F))));
        // Based on ConfiguredCarvers.CAVE_EXTRA_UNDERGROUND
        carverRegisterable.register(LUMAR_CAVE_EXTRA_UNDERGROUND, Carver.CAVE.configure(
                new CaveCarverConfig(0.07F,
                        UniformHeightProvider.create(YOffset.aboveBottom(8), YOffset.fixed(47)),
                        UniformFloatProvider.create(0.1F, 0.9F), YOffset.aboveBottom(8),
                        CarverDebugConfig.create(false, Blocks.OAK_BUTTON.getDefaultState()),
                        blockLookup.getOrThrow(ModBlockTags.LUMAR_CARVER_REPLACEABLES),
                        UniformFloatProvider.create(0.7F, 1.4F),
                        UniformFloatProvider.create(0.8F, 1.3F),
                        UniformFloatProvider.create(-1.0F, -0.4F))));
        // Based on ConfiguredCarvers.CANYON
        carverRegisterable.register(LUMAR_CANYON, Carver.RAVINE.configure(
                new RavineCarverConfig(0.01F,
                        UniformHeightProvider.create(YOffset.fixed(10), YOffset.fixed(67)),
                        ConstantFloatProvider.create(3.0F), YOffset.aboveBottom(8),
                        CarverDebugConfig.create(false, Blocks.WARPED_BUTTON.getDefaultState()),
                        blockLookup.getOrThrow(ModBlockTags.LUMAR_CARVER_REPLACEABLES),
                        UniformFloatProvider.create(-0.125F, 0.125F),
                        new RavineCarverConfig.Shape(UniformFloatProvider.create(0.75F, 1.0F),
                                TrapezoidFloatProvider.create(0.0F, 6.0F, 2.0F), 3,
                                UniformFloatProvider.create(0.75F, 1.0F), 1.0F, 0.0F))));
    }

    private ModConfiguredCarvers() {}
}
