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
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.SaltedFoodUtil;
import io.github.drakonkinst.worldsinger.cosmere.ThirstManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class ItemMixin {

    @Unique
    private static final String SALTED_FOOD_NAME_KEY = Util.createTranslationKey("items",
            Worldsinger.id("salted_food"));

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;canConsume(Z)Z"))
    private boolean allowEatingIfAffectsThirst(PlayerEntity instance, boolean ignoreHunger,
            Operation<Boolean> original, World world, PlayerEntity user, Hand hand) {
        if (original.call(instance, ignoreHunger)) {
            return true;
        }
        Item item = (Item) (Object) this;
        ItemStack stack = user.getStackInHand(hand);
        int thirstModifier = ThirstManager.getThirst(item, stack);
        ThirstManager thirstManager = instance.getAttachedOrCreate(ModAttachmentTypes.THIRST);
        return thirstModifier < 0 || (thirstModifier > 0 && !thirstManager.isFull());
    }

    @ModifyReturnValue(method = "getName(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/text/Text;", at = @At(value = "RETURN"))
    private Text addSaltedDescriptor(Text original, ItemStack stack) {
        if (SaltedFoodUtil.canBeSalted(stack) && SaltedFoodUtil.isSalted(stack)) {
            return Text.translatable(SALTED_FOOD_NAME_KEY, original);
        }
        return original;
    }
}
