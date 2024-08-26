/*
 * MIT License
 *
 * Copyright (c) 2011-2017 mortuusars
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

package io.github.drakonkinst.worldsinger.cosmere;

import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import io.github.drakonkinst.worldsinger.registry.tag.ModItemTags;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;

public final class SaltedFoodUtil {

    public static final int HUNGER_MODIFIER = 2;
    public static final int THIRST_MODIFIER = -1;
    public static final float SATURATION_MODIFIER = 0.0f;
    private static final Map<FoodComponent, FoodComponent> CACHE = new HashMap<>();

    public static boolean isSalted(ItemStack stack) {
        return stack.contains(ModDataComponentTypes.SALTED) && Boolean.TRUE.equals(
                stack.get(ModDataComponentTypes.SALTED));
    }

    // Returns if the base item can be salted, not if the stack itself can
    public static boolean canBeSalted(ItemStack stack) {
        return stack.isIn(ModItemTags.CAN_BE_SALTED);
    }

    private static FoodComponent getOrCreateSaltedVariant(FoodComponent component) {
        return CACHE.computeIfAbsent(component,
                foodComponent -> new FoodComponent(foodComponent.nutrition() + HUNGER_MODIFIER,
                        foodComponent.saturation() + SATURATION_MODIFIER,
                        foodComponent.canAlwaysEat(), foodComponent.eatSeconds(), Optional.empty(),
                        foodComponent.effects()));
    }

    public static ItemStack makeSalted(ItemStack stack) {
        stack.set(ModDataComponentTypes.SALTED, true);
        FoodComponent foodComponent = stack.get(DataComponentTypes.FOOD);
        if (foodComponent != null) {
            stack.set(DataComponentTypes.FOOD,
                    SaltedFoodUtil.getOrCreateSaltedVariant(foodComponent));
        }
        return stack;
    }

    private SaltedFoodUtil() {}
}
