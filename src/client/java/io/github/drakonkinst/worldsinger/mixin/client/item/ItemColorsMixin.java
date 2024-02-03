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
package io.github.drakonkinst.worldsinger.mixin.client.item;

import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.item.ModItems;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemColors.class)
public abstract class ItemColorsMixin {

    @Inject(method = "create", at = @At("RETURN"), cancellable = true)
    private static void addSporePotionItemColors(BlockColors blockColors,
            CallbackInfoReturnable<ItemColors> cir) {
        ItemColors itemColors = cir.getReturnValue();
        itemColors.register(
                (stack, tintIndex) -> tintIndex > 0 ? -1 : AetherSpores.getBottleColor(stack),
                ModItems.DEAD_SPORES_BOTTLE, ModItems.VERDANT_SPORES_BOTTLE,
                ModItems.CRIMSON_SPORES_BOTTLE, ModItems.ZEPHYR_SPORES_BOTTLE,
                ModItems.SUNLIGHT_SPORES_BOTTLE, ModItems.ROSEITE_SPORES_BOTTLE,
                ModItems.MIDNIGHT_SPORES_BOTTLE, ModItems.DEAD_SPORES_SPLASH_BOTTLE,
                ModItems.VERDANT_SPORES_SPLASH_BOTTLE, ModItems.CRIMSON_SPORES_SPLASH_BOTTLE,
                ModItems.ZEPHYR_SPORES_SPLASH_BOTTLE, ModItems.SUNLIGHT_SPORES_SPLASH_BOTTLE,
                ModItems.ROSEITE_SPORES_SPLASH_BOTTLE, ModItems.MIDNIGHT_SPORES_SPLASH_BOTTLE);
        cir.setReturnValue(itemColors);
    }
}
