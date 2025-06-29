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
package io.github.drakonkinst.worldsinger.cosmere;

import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.item.component.SilverLinedComponent;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import io.github.drakonkinst.worldsinger.registry.tag.ModItemTags;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@SuppressWarnings("UnstableApiUsage")
public interface SilverLined {

    int SILVER_METER_COLOR = 0xC1D5D5;
    int SILVER_TEXT_COLOR = 0xC1D5D5;
    int BOAT_MAX_DURABILITY = 45000;
    float BOAT_VISUAL_SCALE_FACTOR = 1.0f / 100.0f;

    static void transferDataFromEntityToItemStack(Entity entity, ItemStack itemStack) {
        SilverLined silverEntityData = entity.getAttached(ModAttachmentTypes.SILVER_LINED_BOAT);
        if (silverEntityData != null) {
            SilverLined.setSilverDurability(itemStack, silverEntityData.getSilverDurability());
        }
    }

    static void transferDataFromItemStackToEntity(ItemStack itemStack, AbstractBoatEntity entity) {
        int silverDurability = SilverLined.getSilverDurability(itemStack);
        if (silverDurability <= 0) {
            return;
        }
        SilverLined silverEntityData = entity.getAttachedOrCreate(
                ModAttachmentTypes.SILVER_LINED_BOAT);
        silverEntityData.setSilverDurability(silverDurability);
    }

    static boolean canBeSilverLined(ItemStack stack) {
        return stack.contains(ModDataComponentTypes.MAX_SILVER_DURABILITY) && !stack.isIn(
                ModItemTags.EXCLUDE_SILVER_LINED);
    }

    static boolean isSilverLined(ItemStack stack) {
        return SilverLined.canBeSilverLined(stack) && SilverLined.getSilverDurability(stack) > 0;
    }

    static int getMaxSilverDurability(ComponentsAccess stack, int defaultValue) {
        return stack.getOrDefault(ModDataComponentTypes.MAX_SILVER_DURABILITY, defaultValue);
    }

    static int getSilverDurability(ComponentsAccess stack) {
        SilverLinedComponent silverLinedComponent = stack.get(
                ModDataComponentTypes.SILVER_DURABILITY);
        if (silverLinedComponent != null) {
            return silverLinedComponent.value();
        }
        return 0;
    }

    static int setSilverDurability(ItemStack stack, int value) {
        int maxDurability = stack.getOrDefault(ModDataComponentTypes.MAX_SILVER_DURABILITY, -1);
        if (maxDurability <= 0) {
            return 0;
        }
        int newValue = Math.clamp(value, 0, maxDurability);
        stack.set(ModDataComponentTypes.SILVER_DURABILITY, new SilverLinedComponent(newValue));
        return newValue;
    }

    static ItemStack repairSilverDurability(ItemStack stack, int times) {
        int silverDurability = SilverLined.getSilverDurability(stack);
        int maxSilverDurability = SilverLined.getMaxSilverDurability(stack, 0);
        if (maxSilverDurability <= 0) {
            return ItemStack.EMPTY;
        }
        int repairAmount = MathHelper.ceil(maxSilverDurability / 4.0f);
        SilverLined.setSilverDurability(stack, silverDurability + repairAmount * times);
        return stack;
    }

    static boolean damageSilverDurability(ItemStack stack) {
        return damageSilverDurability(stack, 1);
    }

    // Return true if it's broken
    static boolean damageSilverDurability(ItemStack stack, int amount) {
        SilverLinedComponent silverLinedComponent = stack.get(
                ModDataComponentTypes.SILVER_DURABILITY);
        if (silverLinedComponent == null) {
            return true;
        }
        int silverDurability = silverLinedComponent.value();
        return SilverLined.setSilverDurability(stack, silverDurability - amount) == 0;
    }

    static void onSilverLinedItemBreak(World world, Entity entity) {
        if (!world.isClient()) {
            // TODO: Play sound and particles
        }
    }

    void setSilverDurability(int durability);

    int getSilverDurability();

    int getRepairAmount();

    int getMaxSilverDurability();

    default boolean decrementDurability() {
        setSilverDurability(getSilverDurability() - 1);
        return getSilverDurability() > 0;
    }

    default void repair() {
        setSilverDurability(getSilverDurability() + getRepairAmount());
    }
}
