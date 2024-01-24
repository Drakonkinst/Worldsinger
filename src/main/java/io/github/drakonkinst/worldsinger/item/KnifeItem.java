package io.github.drakonkinst.worldsinger.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;

// By design, knives do not have Sweeping Edge. However, they can receive both mining and
// weapon enchantments.
public class KnifeItem extends MiningToolItem {

    public KnifeItem(float attackDamage, float attackSpeed, ToolMaterial material,
            Settings settings) {
        super(attackDamage, attackSpeed, material, BlockTags.SWORD_EFFICIENT, settings);
    }

    // To maintain parity with the sword, only applies a 1.5x mining multiplier instead of
    // relying on its tool material
    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        boolean isEfficient = super.getMiningSpeedMultiplier(stack, state) > 1.0f;
        return isEfficient ? 1.5f : 1.0f;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.damage(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }
}
