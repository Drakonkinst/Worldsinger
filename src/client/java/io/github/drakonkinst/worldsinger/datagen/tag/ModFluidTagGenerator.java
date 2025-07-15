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

import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.FluidTagProvider;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class ModFluidTagGenerator extends FluidTagProvider {

    public ModFluidTagGenerator(FabricDataOutput output,
            CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(WrapperLookup wrapperLookup) {
        valueLookupBuilder(ModFluidTags.SUNLIGHT).add(ModFluids.SUNLIGHT);
        valueLookupBuilder(ModFluidTags.DEAD_SPORES).add(ModFluids.DEAD_SPORES)
                .add(ModFluids.FLOWING_DEAD_SPORES);
        valueLookupBuilder(ModFluidTags.VERDANT_SPORES).add(ModFluids.VERDANT_SPORES)
                .add(ModFluids.FLOWING_VERDANT_SPORES);
        valueLookupBuilder(ModFluidTags.CRIMSON_SPORES).add(ModFluids.CRIMSON_SPORES)
                .add(ModFluids.FLOWING_CRIMSON_SPORES);
        valueLookupBuilder(ModFluidTags.ZEPHYR_SPORES).add(ModFluids.ZEPHYR_SPORES)
                .add(ModFluids.FLOWING_ZEPHYR_SPORES);
        valueLookupBuilder(ModFluidTags.SUNLIGHT_SPORES).add(ModFluids.SUNLIGHT_SPORES)
                .add(ModFluids.FLOWING_SUNLIGHT_SPORES);
        valueLookupBuilder(ModFluidTags.ROSEITE_SPORES).add(ModFluids.ROSEITE_SPORES)
                .add(ModFluids.FLOWING_ROSEITE_SPORES);
        valueLookupBuilder(ModFluidTags.MIDNIGHT_SPORES).add(ModFluids.MIDNIGHT_SPORES)
                .add(ModFluids.FLOWING_MIDNIGHT_SPORES);
        valueLookupBuilder(ModFluidTags.STILL_AETHER_SPORES).add(ModFluids.DEAD_SPORES)
                .add(ModFluids.VERDANT_SPORES)
                .add(ModFluids.CRIMSON_SPORES)
                .add(ModFluids.ZEPHYR_SPORES)
                .add(ModFluids.SUNLIGHT_SPORES)
                .add(ModFluids.ROSEITE_SPORES)
                .add(ModFluids.MIDNIGHT_SPORES);
        valueLookupBuilder(ModFluidTags.AETHER_SPORES).addOptionalTag(ModFluidTags.DEAD_SPORES)
                .addOptionalTag(ModFluidTags.VERDANT_SPORES)
                .addOptionalTag(ModFluidTags.CRIMSON_SPORES)
                .addOptionalTag(ModFluidTags.ZEPHYR_SPORES)
                .addOptionalTag(ModFluidTags.SUNLIGHT_SPORES)
                .addOptionalTag(ModFluidTags.ROSEITE_SPORES)
                .addOptionalTag(ModFluidTags.MIDNIGHT_SPORES);
        valueLookupBuilder(ModFluidTags.AETHER_SPORES_OR_SUNLIGHT).addOptionalTag(
                ModFluidTags.AETHER_SPORES).addOptionalTag(ModFluidTags.SUNLIGHT);
    }
}
