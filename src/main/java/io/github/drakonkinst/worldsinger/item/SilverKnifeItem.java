package io.github.drakonkinst.worldsinger.item;

import io.github.drakonkinst.worldsinger.component.MidnightAetherBondComponent;
import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.entity.SilverVulnerable;
import io.github.drakonkinst.worldsinger.material.ModToolMaterials;
import io.github.drakonkinst.worldsinger.mixin.accessor.LivingEntityAccessor;
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

    public SilverKnifeItem(float attackDamage, float attackSpeed, Settings settings) {
        super(attackDamage, attackSpeed, ModToolMaterials.SILVER, settings);
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
            MidnightAetherBondComponent midnightAetherBondData = ModComponents.MIDNIGHT_AETHER_BOND.get(
                    targetPlayer);
            if (midnightAetherBondData.hasAnyBonds()) {
                midnightAetherBondData.dispelAllBonds(true);
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
            MidnightAetherBondComponent midnightAetherBondData = ModComponents.MIDNIGHT_AETHER_BOND.get(
                    user);
            ItemStack stack = user.getStackInHand(hand);
            if (midnightAetherBondData.hasAnyBonds()) {
                midnightAetherBondData.dispelAllBonds(true);
                stack.damage(1, user,
                        hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                return TypedActionResult.success(stack);
            }
        }
        return super.use(world, user, hand);
    }
}
