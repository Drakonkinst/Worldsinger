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

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.api.ModApi;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.item.ItemStack;

@SuppressWarnings("UnstableApiUsage")
public interface SilverLined {

    static void transferDataFromEntityToItemStack(Entity entity, ItemStack itemStack) {
        SilverLined silverEntityData = entity.getAttached(ModAttachmentTypes.SILVER_LINED_BOAT);
        if (silverEntityData != null && silverEntityData.getSilverDurability() > 0) {
            SilverLined silverItemData = ModApi.SILVER_LINED_ITEM.find(itemStack, null);
            if (silverItemData != null) {
                silverItemData.setSilverDurability(silverEntityData.getSilverDurability());
            } else {
                Worldsinger.LOGGER.error("Expected to find silver data for new boat item");
            }
        }
    }

    static void transferDataFromItemStackToEntity(ItemStack itemStack, AbstractBoatEntity entity) {
        SilverLined silverItemData = ModApi.SILVER_LINED_ITEM.find(itemStack, null);
        if (silverItemData == null || silverItemData.getSilverDurability() <= 0) {
            return;
        }
        SilverLined silverEntityData = entity.getAttachedOrCreate(
                ModAttachmentTypes.SILVER_LINED_BOAT);
        silverEntityData.setSilverDurability(silverItemData.getSilverDurability());
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
