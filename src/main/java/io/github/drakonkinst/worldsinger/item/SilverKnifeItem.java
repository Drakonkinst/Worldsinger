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

import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightAetherBondManager;
import io.github.drakonkinst.worldsinger.entity.SilverVulnerable;
import io.github.drakonkinst.worldsinger.mixin.accessor.LivingEntityAccessor;
import io.github.drakonkinst.worldsinger.registry.ModToolMaterials;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class SilverKnifeItem extends KnifeItem {

    public static final float SILVER_BONUS_DAMAGE = 6.0f;

    public SilverKnifeItem(Settings settings) {
        super(ModToolMaterials.SILVER, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target instanceof SilverVulnerable) {
            // applyDamage() always applies the damage, versus damage() which only damages the mob
            // with the highest damage value that frame. So this is ideal for bonus damage
            ((LivingEntityAccessor) target).worldsinger$applyDamage(
                    attacker.getDamageSources().mobAttack(attacker), SILVER_BONUS_DAMAGE);
        }
        return super.postHit(stack, target, attacker);
    }

    // For now, we only support this using the silver knife, not any other spore-growth killing items.
    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity,
            Hand hand) {
        if (!user.getWorld().isClient() && entity instanceof PlayerEntity targetPlayer) {
            MidnightAetherBondManager midnightAetherBondData = targetPlayer.getAttachedOrCreate(
                    ModAttachmentTypes.MIDNIGHT_AETHER_BOND);
            if (midnightAetherBondData.hasAnyBonds()) {
                midnightAetherBondData.dispelAllBonds(targetPlayer, true);
                stack.damage(1, user,
                        hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                return ActionResult.success(true);
            }
        }
        return super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!user.getWorld().isClient()) {
            MidnightAetherBondManager midnightAetherBondData = user.getAttachedOrCreate(
                    ModAttachmentTypes.MIDNIGHT_AETHER_BOND);
            ItemStack stack = user.getStackInHand(hand);
            if (midnightAetherBondData.hasAnyBonds()) {
                midnightAetherBondData.dispelAllBonds(user, true);
                stack.damage(1, user,
                        hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                return TypedActionResult.success(stack);
            }
        }
        return super.use(world, user, hand);
    }
}
