package io.github.drakonkinst.worldsinger.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.entity.SilverVulnerable;
import io.github.drakonkinst.worldsinger.item.SilverKnifeItem;
import io.github.drakonkinst.worldsinger.mixin.accessor.LivingEntityAccessor;
import io.github.drakonkinst.worldsinger.util.EntityUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @WrapOperation(method = "postHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)V"))
    private void dealSilverLinedBonusDamage(Item instance, ItemStack stack, LivingEntity target,
            LivingEntity attacker, Operation<Void> original) {
        original.call(instance, stack, target, attacker);
        if (SilverLined.isSilverLined(stack) && stack.contains(DataComponentTypes.WEAPON)) {
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
    }
}
