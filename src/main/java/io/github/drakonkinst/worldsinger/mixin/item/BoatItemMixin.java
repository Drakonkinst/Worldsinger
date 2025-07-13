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
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BoatItem.class)
public abstract class BoatItemMixin extends Item {

    public BoatItemMixin(Settings settings) {
        super(settings);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int silverDurability = SilverLined.getSilverDurability(stack);
        if (silverDurability <= 0) {
            return super.getItemBarStep(stack);
        }
        int maxSilverDurability = stack.getOrDefault(ModDataComponentTypes.MAX_SILVER_DURABILITY,
                1);
        int step = Math.round(
                (float) silverDurability * ModConstants.ITEM_DURABILITY_METER_MAX_STEPS
                        / maxSilverDurability);
        return Math.min(step, ModConstants.ITEM_DURABILITY_METER_MAX_STEPS);
    }

    @ModifyReturnValue(method = "createEntity", at = @At("RETURN"))
    private AbstractBoatEntity addDataToEntity(AbstractBoatEntity original, World world,
            HitResult hitResult, ItemStack stack, PlayerEntity player) {
        if (original == null) {
            return null;
        }
        SilverLined.transferDataFromItemStackToEntity(stack, original);
        return original;
    }
}
