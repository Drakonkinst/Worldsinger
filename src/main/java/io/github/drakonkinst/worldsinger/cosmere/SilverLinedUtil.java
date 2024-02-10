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

package io.github.drakonkinst.worldsinger.cosmere;

import io.github.drakonkinst.worldsinger.api.ModApi;
import io.github.drakonkinst.worldsinger.item.ModItemTags;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public final class SilverLinedUtil {

    public static final int SILVER_METER_COLOR = 0xC0C0C0;
    private static final int SILVER_TEXT_COLOR = 0xC0C0C0;
    private static final Style SILVER_TEXT_STYLE = Style.EMPTY.withColor(
            TextColor.fromRgb(SILVER_TEXT_COLOR));

    public static void appendSilverDurabilityTooltip(ItemStack stack, List<Text> tooltip,
            TooltipContext context, float scaleFactor) {
        SilverLined silverItemData = ModApi.SILVER_LINED_ITEM.find(stack, null);
        if (silverItemData == null) {
            return;
        }
        int silverDurability = silverItemData.getSilverDurability();
        if (silverDurability <= 0) {
            return;
        }

        if (context.isAdvanced()) {
            int maxDurability = MathHelper.floor(
                    silverItemData.getMaxSilverDurability() * scaleFactor);
            int durability = MathHelper.floor(silverDurability * scaleFactor);
            tooltip.add(Text.translatable("item.silver_durability", durability, maxDurability)
                    .setStyle(SilverLinedUtil.SILVER_TEXT_STYLE));
        } else {
            tooltip.add(Text.translatable("item.silver_lined")
                    .setStyle(SilverLinedUtil.SILVER_TEXT_STYLE));
        }
    }

    public static boolean isSilverLined(ItemStack stack) {
        if (stack.isIn(ModItemTags.EXCLUDE_SILVER_LINED)) {
            return false;
        }
        SilverLined silverItemData = ModApi.SILVER_LINED_ITEM.find(stack, null);
        if (silverItemData == null) {
            return false;
        }
        int silverDurability = silverItemData.getSilverDurability();
        return silverDurability > 0;
    }

    public static boolean canBeSilverLined(ItemStack stack) {
        return !stack.isIn(ModItemTags.EXCLUDE_SILVER_LINED)
                && ModApi.SILVER_LINED_ITEM.find(stack, null) != null;
    }

    public static void onSilverLinedItemBreak(World world, Entity entity) {
        if (!world.isClient()) {
            // TODO: Play sound and particles
        }
    }
}
