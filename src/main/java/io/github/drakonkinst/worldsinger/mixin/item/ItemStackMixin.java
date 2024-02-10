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

package io.github.drakonkinst.worldsinger.mixin.item;

import io.github.drakonkinst.worldsinger.cosmere.SaltedFoodUtil;
import io.github.drakonkinst.worldsinger.item.SaltedFoodComponent;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements FabricItemStack {

    @Override
    public @Nullable FoodComponent getFoodComponent() {
        FoodComponent component = FabricItemStack.super.getFoodComponent();
        if (component == null) {
            return null;
        }
        ItemStack stack = (ItemStack) (Object) this;
        if (SaltedFoodUtil.canBeSalted(stack) && SaltedFoodUtil.isSalted(stack)) {
            return SaltedFoodComponent.of(component);
        }
        return component;
    }
}
