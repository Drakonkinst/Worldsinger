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

import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballContent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballCore;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;

public class SporeCannonballRecipe extends SpecialCraftingRecipe {

    private static final int BOTTLE_AMOUNT = 1;
    private static final int BUCKET_AMOUNT = 3;

    private static final Map<Item, IntObjectPair<CannonballContent>> ITEM_TO_CONTENTS = createItemToContentsMap();

    private static Map<Item, IntObjectPair<CannonballContent>> createItemToContentsMap() {
        Map<Item, IntObjectPair<CannonballContent>> itemToContents = new HashMap<>();
        addSporesToMap(itemToContents, ModItems.DEAD_SPORES_BOTTLE, ModItems.DEAD_SPORES_BUCKET,
                CannonballContent.DEAD_SPORES);
        addSporesToMap(itemToContents, ModItems.VERDANT_SPORES_BOTTLE,
                ModItems.VERDANT_SPORES_BUCKET, CannonballContent.VERDANT_SPORES);
        addSporesToMap(itemToContents, ModItems.CRIMSON_SPORES_BOTTLE,
                ModItems.CRIMSON_SPORES_BUCKET, CannonballContent.CRIMSON_SPORES);
        addSporesToMap(itemToContents, ModItems.ZEPHYR_SPORES_BOTTLE, ModItems.ZEPHYR_SPORES_BUCKET,
                CannonballContent.ZEPHYR_SPORES);
        addSporesToMap(itemToContents, ModItems.SUNLIGHT_SPORES_BOTTLE,
                ModItems.SUNLIGHT_SPORES_BUCKET, CannonballContent.SUNLIGHT_SPORES);
        addSporesToMap(itemToContents, ModItems.ROSEITE_SPORES_BOTTLE,
                ModItems.ROSEITE_SPORES_BUCKET, CannonballContent.ROSEITE_SPORES);
        addSporesToMap(itemToContents, ModItems.MIDNIGHT_SPORES_BOTTLE,
                ModItems.MIDNIGHT_SPORES_BUCKET, CannonballContent.MIDNIGHT_SPORES);
        return itemToContents;
    }

    private static void addSporesToMap(Map<Item, IntObjectPair<CannonballContent>> itemToContents,
            Item bottleItem, Item bucketItem, CannonballContent contents) {
        itemToContents.put(bottleItem, IntObjectPair.of(BOTTLE_AMOUNT, contents));
        itemToContents.put(bucketItem, IntObjectPair.of(BUCKET_AMOUNT, contents));
    }

    private static boolean isValidCannonball(ItemStack stack) {
        CannonballComponent component = stack.get(ModDataComponentTypes.CANNONBALL);
        return component != null && component.core().isFillable();
    }

    public SporeCannonballRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (input.getStackCount() < 2) {
            return false;
        }
        ItemStack cannonballStack = ItemStack.EMPTY;
        boolean hasRoseiteCore = false;
        int numFuse = 0;
        int numContents = 0;
        for (int i = 0; i < input.size(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.isOf(ModItems.ROSEITE_CORE)) {
                if (hasRoseiteCore) {
                    return false;
                }
                hasRoseiteCore = true;
            } else if (stack.isOf(ModItems.VERDANT_VINE)) {
                numFuse++;
            } else if (isValidCannonball(stack)) {
                if (cannonballStack.isEmpty()) {
                    cannonballStack = stack;
                } else {
                    return false;
                }
            } else {
                IntObjectPair<CannonballContent> stackContents = ITEM_TO_CONTENTS.get(
                        stack.getItem());
                if (stackContents == null) {
                    // Unknown item
                    return false;
                }
                numContents += stackContents.firstInt();
            }
        }
        // Invalid if no cannonball
        if (cannonballStack.isEmpty()) {
            return false;
        }
        // Invalid if nothing changed
        if (numFuse <= 0 && numContents <= 0 && !hasRoseiteCore) {
            return false;
        }
        CannonballComponent oldComponent = cannonballStack.get(ModDataComponentTypes.CANNONBALL);
        // Invalid if no cannonball component
        if (oldComponent == null) {
            return false;
        }
        CannonballCore newCore = oldComponent.core();
        // Invalid if core cannot be inserted (one already exists)
        if (hasRoseiteCore) {
            if (!oldComponent.core().isReplaceable()) {
                return false;
            }
            newCore = CannonballCore.ROSEITE;
        }
        // Invalid if attempting to add fuse but not supported
        if (!newCore.canHaveFuse() && numFuse > 0) {
            return false;
        }
        // Invalid if it can have fuse but has too much
        if (newCore.canHaveFuse() && oldComponent.fuse() + numFuse > CannonballComponent.MAX_FUSE) {
            return false;
        }
        // Invalid if we want to add contents, but it is full
        if (oldComponent.contents().size() >= CannonballComponent.MAX_CONTENTS_LENGTH
                && numContents > 0) {
            return false;
        }
        return true;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, WrapperLookup lookup) {
        ItemStack cannonballStack = ItemStack.EMPTY;
        boolean hasRoseiteCore = false;
        int numFuse = 0;
        List<CannonballContent> contents = new ArrayList<>(3);
        for (int i = 0; i < input.size(); ++i) {
            ItemStack stack = input.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.isOf(ModItems.ROSEITE_CORE)) {
                if (hasRoseiteCore) {
                    return ItemStack.EMPTY;
                }
                hasRoseiteCore = true;
            } else if (stack.isOf(ModItems.VERDANT_VINE)) {
                numFuse++;
            } else if (isValidCannonball(stack)) {
                if (cannonballStack.isEmpty()) {
                    cannonballStack = stack;
                } else {
                    return ItemStack.EMPTY;
                }
            } else {
                IntObjectPair<CannonballContent> stackContents = ITEM_TO_CONTENTS.get(
                        stack.getItem());
                if (stackContents == null) {
                    // Unknown item
                    return ItemStack.EMPTY;
                }
                for (int j = 0; j < stackContents.firstInt(); ++j) {
                    contents.add(stackContents.second());
                }
            }
        }
        CannonballComponent oldComponent = cannonballStack.get(ModDataComponentTypes.CANNONBALL);
        // Invalid if no cannonball component
        if (oldComponent == null) {
            return ItemStack.EMPTY;
        }
        numFuse += oldComponent.fuse();
        contents.addAll(0, oldComponent.contents());
        CannonballCore newCore = oldComponent.core();
        // Invalid if core cannot be inserted (one already exists)
        if (hasRoseiteCore) {
            if (oldComponent.core().isReplaceable()) {
                newCore = CannonballCore.ROSEITE;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (newCore.canHaveFuse()) {
            // Invalid if it can have fuse but has too much
            if (numFuse > CannonballComponent.MAX_FUSE) {
                return ItemStack.EMPTY;
            }
        } else {
            // Invalid if attempting to add fuse but not supported
            if (numFuse > 0) {
                return ItemStack.EMPTY;
            }
        }
        if (contents.size() >= CannonballComponent.MAX_CONTENTS_LENGTH) {
            // Trim contents to size
            contents = contents.subList(0, CannonballComponent.MAX_CONTENTS_LENGTH);
        }
        contents.sort(Comparator.comparingInt(CannonballContent::getId));
        ItemStack result = cannonballStack.copyWithCount(1);
        result.set(ModDataComponentTypes.CANNONBALL,
                new CannonballComponent(oldComponent.shell(), newCore, numFuse, contents));
        return result;
    }

    @Override
    public RecipeSerializer<SporeCannonballRecipe> getSerializer() {
        return ModRecipeSerializer.SPORE_CANNONBALL;
    }
}
