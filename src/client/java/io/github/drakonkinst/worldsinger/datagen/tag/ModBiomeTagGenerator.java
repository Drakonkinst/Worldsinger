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

package io.github.drakonkinst.worldsinger.datagen.tag;

import io.github.drakonkinst.worldsinger.registry.ModBiomeTags;
import io.github.drakonkinst.worldsinger.worldgen.ModBiomes;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.biome.Biome;

public class ModBiomeTagGenerator extends FabricTagProvider<Biome> {

    public ModBiomeTagGenerator(FabricDataOutput output,
            CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.BIOME, registriesFuture);
    }

    @Override
    protected void configure(WrapperLookup wrapperLookup) {
        // Vanilla tags
        this.builder(BiomeTags.VILLAGE_PLAINS_HAS_STRUCTURE)
                .addOptional(ModBiomes.LUMAR_FOREST)
                .addOptional(ModBiomes.LUMAR_GRASSLANDS);
        this.builder(BiomeTags.VILLAGE_TAIGA_HAS_STRUCTURE)
                .addOptional(ModBiomes.LUMAR_ROCKS)
                .addOptional(ModBiomes.LUMAR_PEAKS);
        this.builder(BiomeTags.PILLAGER_OUTPOST_HAS_STRUCTURE)
                .addOptional(ModBiomes.LUMAR_PEAKS)
                .addOptional(ModBiomes.LUMAR_FOREST)
                .addOptional(ModBiomes.LUMAR_GRASSLANDS)
                .addOptional(ModBiomes.LUMAR_ROCKS);

        // Mod tags
        this.builder(ModBiomeTags.IS_LUMAR_BEACH)
                .addOptional(ModBiomes.SALTSTONE_ISLAND)
                .addOptional(ModBiomes.LUMAR_ROCKS);
        this.builder(ModBiomeTags.IS_LUMAR_OCEAN)
                .addOptional(ModBiomes.SPORE_SEA)
                .addOptional(ModBiomes.DEEP_SPORE_SEA);
        this.builder(ModBiomeTags.SEAGULLS_CAN_SPAWN)
                .addOptional(ModBiomes.SALTSTONE_ISLAND)
                .addOptional(ModBiomes.LUMAR_ROCKS)
                .addOptional(ModBiomes.LUMAR_PEAKS)
                .addOptional(ModBiomes.DEEP_SPORE_SEA)
                .addOptional(ModBiomes.SPORE_SEA);

        // Mod structure tags
        this.builder(ModBiomeTags.LUMAR_MINESHAFT_HAS_STRUCTURE)
                .addOptional(ModBiomes.SALTSTONE_ISLAND);
        this.builder(ModBiomeTags.LUMAR_SALTSTONE_WELL_HAS_STRUCTURE)
                .addOptional(ModBiomes.SALTSTONE_ISLAND);
        this.builder(ModBiomeTags.LUMAR_SHIPWRECK_HAS_STRUCTURE)
                .addOptionalTag(ModBiomeTags.IS_LUMAR_OCEAN)
                .addOptionalTag(ModBiomeTags.IS_LUMAR_BEACH);
    }
}
