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

import io.github.drakonkinst.worldsinger.api.ModApi;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.cosmere.SilverLinedUtil;
import io.github.drakonkinst.worldsinger.registry.tag.ModConventionalItemTags;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class SilverLinedItemRecipe extends SpecialCraftingRecipe {

    public SilverLinedItemRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (input.getStackCount() < 2) {
            return false;
        }
        ItemStack silverLinedItem = ItemStack.EMPTY;
        int numSilverIngots = 0;
        for (int i = 0; i < input.size(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.isIn(ModConventionalItemTags.SILVER_INGOTS)) {
                numSilverIngots += 1;
            } else if (SilverLinedUtil.canBeSilverLined(stack)) {
                if (!silverLinedItem.isEmpty()) {
                    // Cannot have more than one silver lined slot
                    return false;
                }
                silverLinedItem = stack;
            } else {
                // Cannot have any other items
                return false;
            }
        }
        return !silverLinedItem.isEmpty() && numSilverIngots > 0;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        ItemStack silverLinedItem = ItemStack.EMPTY;
        int numSilverIngots = 0;
        for (int i = 0; i < input.size(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.isIn(ModConventionalItemTags.SILVER_INGOTS)) {
                numSilverIngots += 1;
            } else if (SilverLinedUtil.canBeSilverLined(stack)) {
                if (!silverLinedItem.isEmpty()) {
                    // Cannot have more than one silver lined slot
                    return ItemStack.EMPTY;
                }
                silverLinedItem = stack;
            } else {
                // Cannot have any other items
                return ItemStack.EMPTY;
            }
        }

        ItemStack result = silverLinedItem.copy();
        SilverLined silverData = ModApi.SILVER_LINED_ITEM.find(result, null);
        if (silverData == null) {
            return ItemStack.EMPTY;
        }
        silverData.setSilverDurability(
                silverData.getSilverDurability() + numSilverIngots * silverData.getRepairAmount());
        return result;
    }

    @Override
    public RecipeSerializer<SilverLinedItemRecipe> getSerializer() {
        return ModRecipeSerializer.SILVER_LINED_ITEM;
    }
}
