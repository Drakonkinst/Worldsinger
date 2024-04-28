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

package io.github.drakonkinst.worldsinger.registry;

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;

public final class ModBiomeTags {

    public static final TagKey<Biome> IS_LUMAR_BEACH = ModBiomeTags.of("is_lumar_beach");
    public static final TagKey<Biome> IS_LUMAR_OCEAN = ModBiomeTags.of("is_lumar_ocean");
    public static final TagKey<Biome> LUMAR_MINESHAFT_HAS_STRUCTURE = ModBiomeTags.of(
            "has_structure/lumar_mineshaft");
    public static final TagKey<Biome> LUMAR_SALTSTONE_WELL_HAS_STRUCTURE = ModBiomeTags.of(
            "has_structure/lumar_saltstone_well");
    public static final TagKey<Biome> LUMAR_SHIPWRECK_HAS_STRUCTURE = ModBiomeTags.of(
            "has_structure/lumar_shipwreck");

    private static TagKey<Biome> of(String id) {
        return TagKey.of(RegistryKeys.BIOME, Worldsinger.id(id));
    }

    private ModBiomeTags() {}
}
