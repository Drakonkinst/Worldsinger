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

import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.entity.SilverVulnerable;
import io.github.drakonkinst.worldsinger.item.SilverKnifeItem;
import io.github.drakonkinst.worldsinger.mixin.accessor.LivingEntityAccessor;
import io.github.drakonkinst.worldsinger.util.EntityUtil;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.BundleTooltipData;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AxeItem.class)
public abstract class AxeItemMixin extends Item {

    public AxeItemMixin(Settings settings) {
        super(settings);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return super.isItemBarVisible(stack) || SilverLined.isSilverLined(stack);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        if (SilverLined.isSilverLined(stack)) {
            return SilverLined.SILVER_METER_COLOR;
        }
        return super.getItemBarColor(stack);
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        TooltipDisplayComponent tooltipDisplayComponent = stack.getOrDefault(
                DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT);
        return !tooltipDisplayComponent.shouldDisplay(DataComponentTypes.BUNDLE_CONTENTS)
                ? Optional.empty()
                : Optional.ofNullable(stack.get(DataComponentTypes.BUNDLE_CONTENTS))
                        .map(BundleTooltipData::new);
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (SilverLined.isSilverLined(stack)) {
            int silverDamage = 0;
            boolean isNotCreativePlayer = EntityUtil.isNotCreativePlayer(attacker);
            if (target instanceof SilverVulnerable) {
                // applyDamage() always applies the damage, versus damage() which only damages the mob
                // with the highest damage value that frame. So this is ideal for bonus damage
                if (target.getWorld() instanceof ServerWorld serverWorld) {
                    ((LivingEntityAccessor) target).worldsinger$applyDamage(serverWorld,
                            attacker.getDamageSources().mobAttack(attacker),
                            SilverKnifeItem.SILVER_BONUS_DAMAGE);
                }
                if (isNotCreativePlayer) {
                    silverDamage += 1;
                }
            }
            if (isNotCreativePlayer) {
                silverDamage += 1;

            }
            if (silverDamage > 0) {
                if (!SilverLined.damageSilverDurability(stack, silverDamage)) {
                    SilverLined.onSilverLinedItemBreak(attacker.getWorld(), attacker);
                }
            }
        }
        super.postHit(stack, target, attacker);
    }
}
