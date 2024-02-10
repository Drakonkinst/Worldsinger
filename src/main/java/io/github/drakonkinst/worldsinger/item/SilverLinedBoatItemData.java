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
package io.github.drakonkinst.worldsinger.item;

import net.minecraft.item.ItemStack;

public class SilverLinedBoatItemData extends SilverLinedItemData {

    public static final int MAX_DURABILITY = 45000;
    public static final int SILVER_REPAIR_AMOUNT = MAX_DURABILITY / 4;
    public static final float VISUAL_SCALE_FACTOR = 1.0f / 100.0f;

    public SilverLinedBoatItemData(ItemStack stack) {
        super(stack);
    }

    @Override
    public int getRepairAmount() {
        return SILVER_REPAIR_AMOUNT;
    }

    @Override
    public int getMaxSilverDurability() {
        return MAX_DURABILITY;
    }
}
