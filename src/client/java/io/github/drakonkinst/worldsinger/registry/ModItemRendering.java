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
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.util.LayeredBakedModel;
import io.github.drakonkinst.worldsinger.util.LayeredBakedModelCache;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.util.ModelIdentifier;

public final class ModItemRendering {

    public static final ModelIdentifier SALT_OVERLAY = new ModelIdentifier(
            Worldsinger.id("salted_overlay"), "inventory");
    public static final LayeredBakedModelCache SALT_OVERLAY_CACHE = LayeredBakedModel.registerCache(
            new LayeredBakedModelCache());

    public static void register() {
        ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> tintIndex > 0 ? -1 : AetherSpores.getBottleColor(stack),
                ModItems.DEAD_SPORES_BOTTLE, ModItems.VERDANT_SPORES_BOTTLE,
                ModItems.CRIMSON_SPORES_BOTTLE, ModItems.ZEPHYR_SPORES_BOTTLE,
                ModItems.SUNLIGHT_SPORES_BOTTLE, ModItems.ROSEITE_SPORES_BOTTLE,
                ModItems.MIDNIGHT_SPORES_BOTTLE, ModItems.DEAD_SPORES_SPLASH_BOTTLE,
                ModItems.VERDANT_SPORES_SPLASH_BOTTLE, ModItems.CRIMSON_SPORES_SPLASH_BOTTLE,
                ModItems.ZEPHYR_SPORES_SPLASH_BOTTLE, ModItems.SUNLIGHT_SPORES_SPLASH_BOTTLE,
                ModItems.ROSEITE_SPORES_SPLASH_BOTTLE, ModItems.MIDNIGHT_SPORES_SPLASH_BOTTLE);
    }

    private ModItemRendering() {}
}
