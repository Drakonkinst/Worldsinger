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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.cosmere.lumar.CrimsonSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RoseiteSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SunlightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.VerdantSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.ZephyrSpores;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.worldgen.dimension.CustomNoiseChunkGenerator;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.AquiferSampler.FluidLevelSampler;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseConfig;

public class LumarChunkGenerator extends CustomNoiseChunkGenerator {

    public record SporeSeaEntry(int id, BlockState blockState, double noiseX, double noiseY) {}

    public static final int SEA_LEVEL = 80;
    public static final Block PLACEHOLDER_BLOCK = ModBlocks.DEAD_SPORE_SEA;
    public static final MapCodec<LumarChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(BiomeSource.CODEC.fieldOf("biome_source")
                                    .forGetter(LumarChunkGenerator::getBiomeSource),
                            ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings")
                                    .forGetter(LumarChunkGenerator::getSettings))
                    .apply(instance, instance.stable(LumarChunkGenerator::new)));

    private static final Supplier<FluidLevelSampler> SPORE_SEA_PLACEHOLDER = Suppliers.memoize(
            LumarChunkGenerator::createFluidLevelSampler);
    private static final SporeSeaEntry[] SPORE_SEA_ENTRIES = new SporeSeaEntry[] {
            new SporeSeaEntry(VerdantSpores.ID, ModBlocks.VERDANT_SPORE_SEA.getDefaultState(), -0.5,
                    -0.1),
            new SporeSeaEntry(CrimsonSpores.ID, ModBlocks.CRIMSON_SPORE_SEA.getDefaultState(), 0.0,
                    -0.1),
            new SporeSeaEntry(ZephyrSpores.ID, ModBlocks.ZEPHYR_SPORE_SEA.getDefaultState(), -0.5,
                    0.1),
            new SporeSeaEntry(SunlightSpores.ID, ModBlocks.SUNLIGHT_SPORE_SEA.getDefaultState(),
                    0.5, 0.1),
            new SporeSeaEntry(RoseiteSpores.ID, ModBlocks.ROSEITE_SPORE_SEA.getDefaultState(), 0.0,
                    0.1),
            new SporeSeaEntry(MidnightSpores.ID, ModBlocks.MIDNIGHT_SPORE_SEA.getDefaultState(),
                    0.5, -0.1)
    };
    private static final int SHIFT_X = 10000;
    private static final int SHIFT_Z = 10000;

    private static AquiferSampler.FluidLevelSampler createFluidLevelSampler() {
        AquiferSampler.FluidLevel fluidLevel = new AquiferSampler.FluidLevel(SEA_LEVEL,
                PLACEHOLDER_BLOCK.getDefaultState());
        return (x, y, z) -> fluidLevel;
    }

    public static BlockState getSporeSeaBlockAtPos(NoiseConfig noiseConfig, int x, int z) {
        return LumarChunkGenerator.getSporeSeaEntryAtPos(noiseConfig, x, z).blockState();
    }

    public static SporeSeaEntry getSporeSeaEntryAtPos(NoiseConfig noiseConfig, int x, int z) {
        DensityFunction temperature = noiseConfig.getNoiseRouter().temperature();
        double first = temperature.sample(new DensityFunction.UnblendedNoisePos(x, 0, z));
        double second = temperature.sample(
                new DensityFunction.UnblendedNoisePos(z + SHIFT_X, 0, x + SHIFT_Z));
        SporeSeaEntry entry = LumarChunkGenerator.getNearestSporeSeaEntry(first, second);
        return entry;
    }

    private static SporeSeaEntry getNearestSporeSeaEntry(double x, double y) {
        double minDistSq = Double.MAX_VALUE;
        SporeSeaEntry result = SPORE_SEA_ENTRIES[0];

        for (SporeSeaEntry entry : SPORE_SEA_ENTRIES) {
            double deltaX = entry.noiseX() - x;
            double deltaY = entry.noiseY() - y;
            double distSq = deltaX * deltaX + deltaY * deltaY;
            if (distSq < minDistSq) {
                result = entry;
                minDistSq = distSq;
            }
        }

        return result;
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

        return LumarChunkGenerator.getSporeSeaBlockAtPos(noiseConfig, x, z);
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
    protected MapCodec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }
}
