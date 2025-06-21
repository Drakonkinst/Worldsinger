/*
 * MIT License
 *
 * Copyright (c) 2024 Drakonkinst
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
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class ModMapDecorationTypes {

    public static final RegistryEntry<MapDecorationType> RAINLINE = register("rainline", "rainline",
            true, MapDecorationType.NO_MAP_COLOR, false, false);
    public static final float MAP_LIMITS = 63.0f;

    public static void initialize() {}

    private static RegistryEntry<MapDecorationType> register(String id, String assetId,
            boolean showOnItemFrame, int mapColor, boolean trackCount,
            boolean explorationMapElement) {
        RegistryKey<MapDecorationType> registryKey = RegistryKey.of(
                RegistryKeys.MAP_DECORATION_TYPE, Identifier.ofVanilla(id));
        MapDecorationType mapDecorationType = new MapDecorationType(Worldsinger.id(assetId),
                showOnItemFrame, mapColor, explorationMapElement, trackCount);
        return Registry.registerReference(Registries.MAP_DECORATION_TYPE, registryKey,
                mapDecorationType);
    }
}
