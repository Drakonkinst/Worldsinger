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

import io.github.drakonkinst.worldsinger.api.ModApi;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.cosmere.SilverLinedUtil;
import io.github.drakonkinst.worldsinger.entity.SilverVulnerable;
import io.github.drakonkinst.worldsinger.item.ModItemTags;
import io.github.drakonkinst.worldsinger.item.SilverKnifeItem;
import io.github.drakonkinst.worldsinger.mixin.accessor.LivingEntityAccessor;
import io.github.drakonkinst.worldsinger.util.EntityUtil;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AxeItem.class)
public abstract class AxeItemMixin extends MiningToolItem {

    public AxeItemMixin(float attackDamage, float attackSpeed, ToolMaterial material,
            TagKey<Block> effectiveBlocks, Settings settings) {
        super(attackDamage, attackSpeed, material, effectiveBlocks, settings);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return super.isItemBarVisible(stack) || SilverLinedUtil.isSilverLined(stack);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        if (SilverLinedUtil.isSilverLined(stack)) {
            return SilverLinedUtil.SILVER_METER_COLOR;
        }
        return super.getItemBarColor(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip,
            TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (!stack.isIn(ModItemTags.EXCLUDE_SILVER_LINED)) {
            SilverLinedUtil.appendSilverDurabilityTooltip(stack, tooltip, context, 1.0f);
        }
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        SilverLined silverData = ModApi.SILVER_LINED_ITEM.find(stack, null);
        if (silverData != null && silverData.getSilverDurability() > 0) {
            boolean isNotCreativePlayer = EntityUtil.isNotCreativePlayer(attacker);
            if (target instanceof SilverVulnerable) {
                // applyDamage() always applies the damage, versus damage() which only damages the mob
                // with the highest damage value that frame. So this is ideal for bonus damage
                ((LivingEntityAccessor) target).worldsinger$applyDamage(
                        attacker.getDamageSources().mobAttack(attacker),
                        SilverKnifeItem.SILVER_BONUS_DAMAGE);
                if (isNotCreativePlayer) {
                    silverData.decrementDurability();
                }
            }
            if (isNotCreativePlayer) {
                if (!silverData.decrementDurability()) {
                    SilverLinedUtil.onSilverLinedItemBreak(attacker.getWorld(), attacker);
                }
            }
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos,
            LivingEntity miner) {
        SilverLined silverData = ModApi.SILVER_LINED_ITEM.find(stack, null);
        if (silverData != null && silverData.getSilverDurability() > 0
                && EntityUtil.isNotCreativePlayer(miner)) {
            if (!silverData.decrementDurability()) {
                SilverLinedUtil.onSilverLinedItemBreak(world, miner);
            }
        }
        return super.postMine(stack, world, state, pos, miner);
    }
}
