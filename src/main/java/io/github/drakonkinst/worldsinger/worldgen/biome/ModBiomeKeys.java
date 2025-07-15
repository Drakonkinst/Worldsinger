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
package io.github.drakonkinst.worldsinger.worldgen.biome;

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;

public final class ModBiomeKeys {

    public static final RegistryKey<Biome> LUMAR_FOREST = ModBiomeKeys.of("lumar_forest");
    public static final RegistryKey<Biome> LUMAR_GRASSLANDS = ModBiomeKeys.of("lumar_grasslands");
    public static final RegistryKey<Biome> LUMAR_PEAKS = ModBiomeKeys.of("lumar_peaks");
    public static final RegistryKey<Biome> LUMAR_ROCKS = ModBiomeKeys.of("lumar_rocks");
    public static final RegistryKey<Biome> SALTSTONE_ISLAND = ModBiomeKeys.of("saltstone_island");
    public static final RegistryKey<Biome> SPORE_SEA = ModBiomeKeys.of("spore_sea");
    public static final RegistryKey<Biome> DEEP_SPORE_SEA = ModBiomeKeys.of("deep_spore_sea");

    private static RegistryKey<Biome> of(String id) {
        return RegistryKey.of(RegistryKeys.BIOME, Worldsinger.id(id));
    }

    private ModBiomeKeys() {}
}
