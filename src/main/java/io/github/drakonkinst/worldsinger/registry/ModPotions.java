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

import io.github.drakonkinst.worldsinger.item.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.BrewingRecipeRegistry;

public final class ModPotions {

    public static void initialize() {
        BrewingRecipeRegistry.registerPotionType(ModItems.DEAD_SPORES_BOTTLE);
        BrewingRecipeRegistry.registerPotionType(ModItems.DEAD_SPORES_SPLASH_BOTTLE);
        BrewingRecipeRegistry.registerPotionType(ModItems.VERDANT_SPORES_BOTTLE);
        BrewingRecipeRegistry.registerPotionType(ModItems.VERDANT_SPORES_SPLASH_BOTTLE);
        BrewingRecipeRegistry.registerPotionType(ModItems.CRIMSON_SPORES_BOTTLE);
        BrewingRecipeRegistry.registerPotionType(ModItems.CRIMSON_SPORES_SPLASH_BOTTLE);
        BrewingRecipeRegistry.registerPotionType(ModItems.ZEPHYR_SPORES_BOTTLE);
        BrewingRecipeRegistry.registerPotionType(ModItems.ZEPHYR_SPORES_SPLASH_BOTTLE);
        BrewingRecipeRegistry.registerPotionType(ModItems.SUNLIGHT_SPORES_BOTTLE);
        BrewingRecipeRegistry.registerPotionType(ModItems.SUNLIGHT_SPORES_SPLASH_BOTTLE);
        BrewingRecipeRegistry.registerPotionType(ModItems.ROSEITE_SPORES_BOTTLE);
        BrewingRecipeRegistry.registerPotionType(ModItems.ROSEITE_SPORES_SPLASH_BOTTLE);
        BrewingRecipeRegistry.registerPotionType(ModItems.MIDNIGHT_SPORES_BOTTLE);
        BrewingRecipeRegistry.registerPotionType(ModItems.MIDNIGHT_SPORES_SPLASH_BOTTLE);

        BrewingRecipeRegistry.registerItemRecipe(Items.POTION, ModItems.ZEPHYR_SPORES_BOTTLE,
                Items.SPLASH_POTION);

        ModPotions.registerCustomSplashPotion(ModItems.DEAD_SPORES_BOTTLE,
                ModItems.DEAD_SPORES_SPLASH_BOTTLE);
        ModPotions.registerCustomSplashPotion(ModItems.VERDANT_SPORES_BOTTLE,
                ModItems.VERDANT_SPORES_SPLASH_BOTTLE);
        ModPotions.registerCustomSplashPotion(ModItems.CRIMSON_SPORES_BOTTLE,
                ModItems.CRIMSON_SPORES_SPLASH_BOTTLE);
        ModPotions.registerCustomSplashPotion(ModItems.ZEPHYR_SPORES_BOTTLE,
                ModItems.ZEPHYR_SPORES_SPLASH_BOTTLE);
        ModPotions.registerCustomSplashPotion(ModItems.SUNLIGHT_SPORES_BOTTLE,
                ModItems.SUNLIGHT_SPORES_SPLASH_BOTTLE);
        ModPotions.registerCustomSplashPotion(ModItems.ROSEITE_SPORES_BOTTLE,
                ModItems.ROSEITE_SPORES_SPLASH_BOTTLE);
        ModPotions.registerCustomSplashPotion(ModItems.MIDNIGHT_SPORES_BOTTLE,
                ModItems.MIDNIGHT_SPORES_SPLASH_BOTTLE);

    }

    private static void registerCustomSplashPotion(Item regularPotion, Item splashPotion) {
        BrewingRecipeRegistry.registerItemRecipe(regularPotion, Items.GUNPOWDER, splashPotion);
        BrewingRecipeRegistry.registerItemRecipe(regularPotion, ModItems.ZEPHYR_SPORES_BOTTLE,
                splashPotion);
    }

    private ModPotions() {}

}
