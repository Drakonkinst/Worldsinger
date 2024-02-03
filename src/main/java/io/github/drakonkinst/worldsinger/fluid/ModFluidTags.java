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
package io.github.drakonkinst.worldsinger.fluid;

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class ModFluidTags {

    public static final TagKey<Fluid> AETHER_SPORES = ModFluidTags.of("aether_spores");
    public static final TagKey<Fluid> STILL_AETHER_SPORES = ModFluidTags.of("still_aether_spores");
    public static final TagKey<Fluid> VERDANT_SPORES = ModFluidTags.of("verdant_spores");
    public static final TagKey<Fluid> CRIMSON_SPORES = ModFluidTags.of("crimson_spores");
    public static final TagKey<Fluid> ZEPHYR_SPORES = ModFluidTags.of("zephyr_spores");
    public static final TagKey<Fluid> SUNLIGHT_SPORES = ModFluidTags.of("sunlight_spores");
    public static final TagKey<Fluid> ROSEITE_SPORES = ModFluidTags.of("roseite_spores");
    public static final TagKey<Fluid> MIDNIGHT_SPORES = ModFluidTags.of("midnight_spores");
    public static final TagKey<Fluid> DEAD_SPORES = ModFluidTags.of("dead_spores");
    public static final TagKey<Fluid> SUNLIGHT = ModFluidTags.of("sunlight");
    public static final TagKey<Fluid> AETHER_SPORES_OR_SUNLIGHT = ModFluidTags.of(
            "aether_spores_or_sunlight");

    private static TagKey<Fluid> of(String id) {
        return TagKey.of(RegistryKeys.FLUID, Worldsinger.id(id));
    }

    private ModFluidTags() {}
}
