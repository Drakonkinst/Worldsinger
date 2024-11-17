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

package io.github.drakonkinst.worldsinger.recipe;

import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;

public class SilverLinedChestBoatRecipe extends SpecialCraftingRecipe {

    private static final Map<Item, Item> ITEM_TO_RESULT = createItemToContentsMap();

    private static Map<Item, Item> createItemToContentsMap() {
        // Hardcoding this, sorry :(
        Map<Item, Item> itemToResultMap = new HashMap<>();
        itemToResultMap.put(Items.OAK_BOAT, Items.OAK_CHEST_BOAT);
        itemToResultMap.put(Items.SPRUCE_BOAT, Items.SPRUCE_CHEST_BOAT);
        itemToResultMap.put(Items.BIRCH_BOAT, Items.BIRCH_CHEST_BOAT);
        itemToResultMap.put(Items.JUNGLE_BOAT, Items.JUNGLE_CHEST_BOAT);
        itemToResultMap.put(Items.ACACIA_BOAT, Items.ACACIA_CHEST_BOAT);
        itemToResultMap.put(Items.DARK_OAK_BOAT, Items.DARK_OAK_CHEST_BOAT);
        itemToResultMap.put(Items.MANGROVE_BOAT, Items.MANGROVE_CHEST_BOAT);
        itemToResultMap.put(Items.CHERRY_BOAT, Items.CHERRY_CHEST_BOAT);
        itemToResultMap.put(Items.BAMBOO_RAFT, Items.BAMBOO_CHEST_RAFT);
        // TODO: More boat types
        // TODO: Populate this dynamically based on BoatItem creations in the future
        return itemToResultMap;
    }

    public SilverLinedChestBoatRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        Item boatItem = null;
        boolean hasChest = false;
        for (int i = 0; i < input.getSize(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (ITEM_TO_RESULT.containsKey(stack.getItem()) && stack.contains(
                    ModDataComponentTypes.SILVER_DURABILITY)) {
                if (boatItem != null) {
                    return false;
                }
                boatItem = stack.getItem();
            } else if (stack.isOf(Items.CHEST)) {
                if (hasChest) {
                    return false;
                }
                hasChest = true;
            } else {
                return false;
            }
        }
        return boatItem != null && hasChest;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, WrapperLookup lookup) {
        Item boatItem = null;
        int silverDurability = -1;
        for (int i = 0; i < input.getSize(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (ITEM_TO_RESULT.containsKey(stack.getItem()) && stack.contains(
                    ModDataComponentTypes.SILVER_DURABILITY)) {
                boatItem = stack.getItem();
                silverDurability = stack.getOrDefault(ModDataComponentTypes.SILVER_DURABILITY, -1);
            }
        }
        if (boatItem == null || silverDurability < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = ITEM_TO_RESULT.get(boatItem).getDefaultStack();
        result.set(ModDataComponentTypes.SILVER_DURABILITY, silverDurability);
        return result;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializer.SILVER_LINED_CHEST_BOAT;
    }
}
