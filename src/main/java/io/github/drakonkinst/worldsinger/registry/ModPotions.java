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
import io.github.drakonkinst.worldsinger.item.ModItems;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public final class ModPotions {

    // Generic spores potion used in recipes
    public static final RegistryEntry<Potion> SPORES = register("spores", new Potion("spores"));
    public static final PotionContentsComponent SPORE_POTIONS_COMPONENT = new PotionContentsComponent(
            ModPotions.SPORES);

    public static void initialize() {
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.registerPotionType(ModItems.DEAD_SPORES_BOTTLE);
            builder.registerPotionType(ModItems.DEAD_SPORES_SPLASH_BOTTLE);
            builder.registerPotionType(ModItems.VERDANT_SPORES_BOTTLE);
            builder.registerPotionType(ModItems.VERDANT_SPORES_SPLASH_BOTTLE);
            builder.registerPotionType(ModItems.CRIMSON_SPORES_BOTTLE);
            builder.registerPotionType(ModItems.CRIMSON_SPORES_SPLASH_BOTTLE);
            builder.registerPotionType(ModItems.ZEPHYR_SPORES_BOTTLE);
            builder.registerPotionType(ModItems.ZEPHYR_SPORES_SPLASH_BOTTLE);
            builder.registerPotionType(ModItems.SUNLIGHT_SPORES_BOTTLE);
            builder.registerPotionType(ModItems.SUNLIGHT_SPORES_SPLASH_BOTTLE);
            builder.registerPotionType(ModItems.ROSEITE_SPORES_BOTTLE);
            builder.registerPotionType(ModItems.ROSEITE_SPORES_SPLASH_BOTTLE);
            builder.registerPotionType(ModItems.MIDNIGHT_SPORES_BOTTLE);
            builder.registerPotionType(ModItems.MIDNIGHT_SPORES_SPLASH_BOTTLE);

            builder.registerItemRecipe(Items.POTION, ModItems.ZEPHYR_SPORES_BOTTLE,
                    Items.SPLASH_POTION);
            builder.registerItemRecipe(Items.SPLASH_POTION, ModItems.CRIMSON_SPINE, Items.POTION);

            ModPotions.registerCustomSplashPotion(builder, ModItems.DEAD_SPORES_BOTTLE,
                    ModItems.DEAD_SPORES_SPLASH_BOTTLE);
            ModPotions.registerCustomSplashPotion(builder, ModItems.VERDANT_SPORES_BOTTLE,
                    ModItems.VERDANT_SPORES_SPLASH_BOTTLE);
            ModPotions.registerCustomSplashPotion(builder, ModItems.CRIMSON_SPORES_BOTTLE,
                    ModItems.CRIMSON_SPORES_SPLASH_BOTTLE);
            ModPotions.registerCustomSplashPotion(builder, ModItems.ZEPHYR_SPORES_BOTTLE,
                    ModItems.ZEPHYR_SPORES_SPLASH_BOTTLE);
            ModPotions.registerCustomSplashPotion(builder, ModItems.SUNLIGHT_SPORES_BOTTLE,
                    ModItems.SUNLIGHT_SPORES_SPLASH_BOTTLE);
            ModPotions.registerCustomSplashPotion(builder, ModItems.ROSEITE_SPORES_BOTTLE,
                    ModItems.ROSEITE_SPORES_SPLASH_BOTTLE);
            ModPotions.registerCustomSplashPotion(builder, ModItems.MIDNIGHT_SPORES_BOTTLE,
                    ModItems.MIDNIGHT_SPORES_SPLASH_BOTTLE);
        });

    }

    private static void registerCustomSplashPotion(BrewingRecipeRegistry.Builder builder,
            Item regularPotion, Item splashPotion) {
        builder.registerItemRecipe(regularPotion, Items.GUNPOWDER, splashPotion);
        builder.registerItemRecipe(splashPotion, ModItems.CRIMSON_SPINE, regularPotion);
        builder.registerItemRecipe(regularPotion, ModItems.ZEPHYR_SPORES_BOTTLE, splashPotion);
    }

    private static RegistryEntry<Potion> register(String name, Potion potion) {
        return Registry.registerReference(Registries.POTION, Worldsinger.id(name), potion);
    }

    private ModPotions() {}

}
