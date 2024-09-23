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

package io.github.drakonkinst.worldsinger.recipe;

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;

public final class ModRecipeSerializer {

    public static final RecipeSerializer<SaltedFoodRecipe> SALTED_FOOD = RecipeSerializer.register(
            Worldsinger.idStr("salted_food"), new SpecialRecipeSerializer<>(SaltedFoodRecipe::new));
    public static final RecipeSerializer<SilverLinedItemRecipe> SILVER_LINED_ITEM = RecipeSerializer.register(
            Worldsinger.idStr("silver_lined_item"),
            new SpecialRecipeSerializer<>(SilverLinedItemRecipe::new));
    public static final RecipeSerializer<WaterCannonballRecipe> WATER_CANNONBALL = RecipeSerializer.register(
            Worldsinger.idStr("water_cannonball"),
            new SpecialRecipeSerializer<>(WaterCannonballRecipe::new));
    public static final RecipeSerializer<SporeCannonballRecipe> SPORE_CANNONBALL = RecipeSerializer.register(
            Worldsinger.idStr("spore_cannonball"),
            new SpecialRecipeSerializer<>(SporeCannonballRecipe::new));

    public static void initialize() {}

    private ModRecipeSerializer() {}
}
