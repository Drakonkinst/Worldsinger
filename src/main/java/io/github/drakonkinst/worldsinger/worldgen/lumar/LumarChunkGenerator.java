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
package io.github.drakonkinst.worldsinger.worldgen.lumar;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.worldgen.dimension.CustomNoiseChunkGenerator;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.AquiferSampler.FluidLevelSampler;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;

public class LumarChunkGenerator extends CustomNoiseChunkGenerator {

    public static final int SEA_LEVEL = 80;
    public static final Block PLACEHOLDER_BLOCK = ModBlocks.DEAD_SPORE_SEA;
    private static final Supplier<FluidLevelSampler> SPORE_SEA_PLACEHOLDER = Suppliers.memoize(
            LumarChunkGenerator::createFluidLevelSampler);
    public static final Codec<LumarChunkGenerator> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(BiomeSource.CODEC.fieldOf("biome_source")
                                    .forGetter(LumarChunkGenerator::getBiomeSource),
                            ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings")
                                    .forGetter(LumarChunkGenerator::getSettings))
                    .apply(instance, instance.stable(LumarChunkGenerator::new)));
    private static final BlockState WATER = Blocks.WATER.getDefaultState();
    private static final BlockState LAVA = Blocks.LAVA.getDefaultState();
    private static final BlockState EMERALD_SEA = ModBlocks.VERDANT_SPORE_SEA.getDefaultState();
    private static final BlockState CRIMSON_SEA = ModBlocks.CRIMSON_SPORE_SEA.getDefaultState();
    private static final BlockState ZEPHYR_SEA = ModBlocks.ZEPHYR_SPORE_SEA.getDefaultState();
    private static final BlockState SUNLIGHT_SEA = ModBlocks.SUNLIGHT_SPORE_SEA.getDefaultState();
    private static final BlockState ROSEITE_SEA = ModBlocks.ROSEITE_SPORE_SEA.getDefaultState();
    private static final BlockState MIDNIGHT_SEA = ModBlocks.MIDNIGHT_SPORE_SEA.getDefaultState();

    private static AquiferSampler.FluidLevelSampler createFluidLevelSampler() {
        AquiferSampler.FluidLevel fluidLevel = new AquiferSampler.FluidLevel(SEA_LEVEL,
                PLACEHOLDER_BLOCK.getDefaultState());
        return (x, y, z) -> fluidLevel;
    }

    public static BlockState getSporeSeaBlockAtPos(NoiseConfig noiseConfig, int x, int y, int z) {
        DensityFunction.UnblendedNoisePos noisePos = new DensityFunction.UnblendedNoisePos(x, y, z);
        NoiseRouter noiseRouter = noiseConfig.getNoiseRouter();
        double temperature = noiseRouter.temperature().sample(noisePos);
        if (temperature <= -0.66f) {
            return ROSEITE_SEA;
        }
        if (temperature <= -0.33f) {
            return EMERALD_SEA;
        }
        if (temperature <= 0.0f) {
            return ZEPHYR_SEA;
        }
        if (temperature <= 0.33) {
            return SUNLIGHT_SEA;
        }
        if (temperature <= 0.66) {
            return CRIMSON_SEA;
        }
        return MIDNIGHT_SEA;
    }

    public LumarChunkGenerator(BiomeSource biomeSource,
            RegistryEntry<ChunkGeneratorSettings> settings) {
        super(biomeSource, settings, SPORE_SEA_PLACEHOLDER);
    }

    @Override
    public BlockState modifyBlockState(BlockState state, NoiseConfig noiseConfig, int x, int y,
            int z) {
        if (!state.isOf(PLACEHOLDER_BLOCK)) {
            return state;
        }

        return LumarChunkGenerator.getSporeSeaBlockAtPos(noiseConfig, x, y, z);
    }

    @Override
    protected boolean shouldSkipPostProcessing(AquiferSampler aquiferSampler, FluidState fluidState,
            int y) {
        return super.shouldSkipPostProcessing(aquiferSampler, fluidState, y) && !(
                !fluidState.isEmpty() && y == this.getSeaLevel() - 1 && fluidState.isIn(
                        ModFluidTags.AETHER_SPORES));
    }

    @Override
    public int getSeaLevel() {
        return SEA_LEVEL;
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }
}
