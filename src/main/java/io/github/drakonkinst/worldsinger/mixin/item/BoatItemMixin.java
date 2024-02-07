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

import com.llamalad7.mixinextras.sugar.Local;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.api.ModApi;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.entity.SilverLinedBoatData;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@SuppressWarnings("UnstableApiUsage")
@Mixin(BoatItem.class)
public abstract class BoatItemMixin extends Item {

    @Unique
    private static final int SILVER_METER_COLOR = 0xC0C0C0;

    @Unique
    private static final int SILVER_TEXT_COLOR = 0xC0C0C0;

    @Unique
    private static final Style SILVER_TEXT_STYLE = Style.EMPTY.withColor(
            TextColor.fromRgb(SILVER_TEXT_COLOR));

    @Unique
    private static final int MAX_METER_STEPS = 13;

    public BoatItemMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip,
            TooltipContext context) {
        SilverLined silverItemData = ModApi.SILVER_LINED_ITEM.find(stack, null);
        if (silverItemData == null) {
            return;
        }
        int silverDurability = silverItemData.getSilverDurability();
        if (silverDurability <= 0) {
            return;
        }

        if (context.isAdvanced()) {
            tooltip.add(Text.translatable("item.silver_durability", silverDurability,
                    silverItemData.getMaxSilverDurability()).setStyle(SILVER_TEXT_STYLE));
        } else {
            tooltip.add(Text.translatable("item.silver_lined").setStyle(SILVER_TEXT_STYLE));
        }
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        int silverDurability = this.getSilverDurability(stack);
        return super.isItemBarVisible(stack) || silverDurability > 0;
    }

    @Unique
    private int getSilverDurability(ItemStack stack) {
        SilverLined silverItemData = ModApi.SILVER_LINED_ITEM.find(stack, null);
        if (silverItemData == null) {
            Worldsinger.LOGGER.error(
                    "Expected to find silver data for boat item (testing " + stack.getItem()
                            .toString() + ")");
            return 0;
        }
        return silverItemData.getSilverDurability();
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        int silverDurability = this.getSilverDurability(stack);
        if (silverDurability <= 0) {
            return super.getItemBarColor(stack);
        }
        return SILVER_METER_COLOR;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int silverDurability = this.getSilverDurability(stack);
        if (silverDurability <= 0) {
            return super.getItemBarStep(stack);
        }
        int step = Math.min(Math.round(
                        (float) silverDurability * MAX_METER_STEPS / SilverLinedBoatData.MAX_DURABILITY),
                MAX_METER_STEPS);
        return step;
    }

    @ModifyVariable(method = "use", at = @At(value = "STORE"))
    private BoatEntity addDataToEntity(BoatEntity entity, @Local PlayerEntity user,
            @Local Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        SilverLined.transferDataFromItemStackToEntity(itemStack, entity);
        return entity;
    }
}
