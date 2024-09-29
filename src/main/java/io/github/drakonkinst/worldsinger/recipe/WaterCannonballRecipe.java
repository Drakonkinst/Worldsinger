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

import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballCore;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import java.util.Collections;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;

public class WaterCannonballRecipe extends SpecialCraftingRecipe {

    public WaterCannonballRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    private static boolean isEmptyCannonball(ItemStack stack) {
        CannonballComponent component = stack.get(ModDataComponentTypes.CANNONBALL);
        return component != null && component.core().isReplaceable() && component.fuse() <= 0
                && component.contents().isEmpty();
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        int numCannonballs = 0;
        ItemStack cannonballStack = ItemStack.EMPTY;
        boolean hasWaterBucket = false;
        for (int i = 0; i < input.getSize(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.isIn(ConventionalItemTags.WATER_BUCKETS)) {
                if (hasWaterBucket) {
                    return false;
                }
                hasWaterBucket = true;
            } else if (isEmptyCannonball(stack)) {
                // Must all be same item type
                if (cannonballStack.isEmpty()) {
                    cannonballStack = stack;
                } else if (!ItemStack.areItemsAndComponentsEqual(cannonballStack, stack)) {
                    return false;
                }
                numCannonballs++;
            } else {
                return false;
            }
        }
        return !cannonballStack.isEmpty() && numCannonballs > 0 && hasWaterBucket;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, WrapperLookup lookup) {
        int numCannonballs = 0;
        ItemStack cannonballStack = ItemStack.EMPTY;
        boolean hasWaterBucket = false;
        for (int i = 0; i < input.getSize(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.isIn(ConventionalItemTags.WATER_BUCKETS)) {
                if (hasWaterBucket) {
                    return ItemStack.EMPTY;
                }
                hasWaterBucket = true;
            } else if (isEmptyCannonball(stack)) {
                if (cannonballStack.isEmpty()) {
                    cannonballStack = stack;
                } else if (!ItemStack.areItemsAndComponentsEqual(cannonballStack, stack)) {
                    return ItemStack.EMPTY;
                }
                numCannonballs++;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (!(!cannonballStack.isEmpty() && numCannonballs > 0 && hasWaterBucket)) {
            return ItemStack.EMPTY;
        }
        ItemStack result = cannonballStack.copyWithCount(numCannonballs);
        CannonballComponent oldComponent = cannonballStack.get(ModDataComponentTypes.CANNONBALL);
        if (oldComponent == null) {
            return ItemStack.EMPTY;
        }
        result.set(ModDataComponentTypes.CANNONBALL,
                new CannonballComponent(oldComponent.shell(), CannonballCore.WATER, 0,
                        Collections.emptyList()));
        return result;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializer.WATER_CANNONBALL;
    }
}
