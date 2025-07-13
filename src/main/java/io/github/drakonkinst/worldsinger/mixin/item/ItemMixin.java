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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.drakonkinst.worldsinger.cosmere.SaltedFoodUtil;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.util.EntityUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Unique
    private static final String SALTED_FOOD_NAME_KEY = "item.worldsinger.salted_food";

    @ModifyReturnValue(method = "getName(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/text/Text;", at = @At(value = "RETURN"))
    private Text addSaltedDescriptor(Text original, ItemStack stack) {
        if (SaltedFoodUtil.canBeSalted(stack) && SaltedFoodUtil.isSalted(stack)) {
            return Text.translatable(SALTED_FOOD_NAME_KEY, original);
        }
        return original;
    }

    @Inject(method = "postMine", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;)V"))
    private void damageSilverDurability(ItemStack stack, World world, BlockState state,
            BlockPos pos, LivingEntity miner, CallbackInfoReturnable<Boolean> cir) {
        int silverDurability = SilverLined.getSilverDurability(stack);
        // TODO: There are some edge cases here where silver durability can fall out of sync
        if (silverDurability > 0 && EntityUtil.isNotCreativePlayer(miner)) {
            SilverLined.damageSilverDurability(stack);
        }
    }

    // Note: The item bar value will still be whatever the durability value is, by default
    // Make sure to override it for things like boats if we want to show a different value
    @WrapMethod(method = "isItemBarVisible")
    public boolean makeSilverLinedBarVisible(ItemStack stack, Operation<Boolean> original) {
        return original.call(stack) || SilverLined.isSilverLined(stack);
    }

    @WrapMethod(method = "getItemBarColor")
    private int addSilverLinedBar(ItemStack stack, Operation<Integer> original) {
        if (SilverLined.isSilverLined(stack)) {
            return SilverLined.SILVER_METER_COLOR;
        }
        return original.call(stack);
    }
}
