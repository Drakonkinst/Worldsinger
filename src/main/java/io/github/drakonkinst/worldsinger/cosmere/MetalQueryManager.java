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

import io.github.drakonkinst.datatables.DataTable;
import io.github.drakonkinst.worldsinger.item.ModItemTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class MetalQueryManager {

    private static final int HELD_ITEM_METAL_VALUE = 1;
    private static final int USING_SHIELD_BONUS = 2;

    public static int getIronContentForEntity(Entity entity, DataTable entityDataTable,
            DataTable armorDataTable) {
        return MetalQueryManager.getMetalContentForEntity(entity, Metals.IRON, entityDataTable,
                armorDataTable);
    }

    public static int getMetalContentForEntity(Entity entity, Metal metal,
            DataTable entityDataTable, DataTable armorDataTable) {
        int ironContent = 0;

        if (entity.getType().isIn(metal.getEntityTypeTag())) {
            ironContent += entityDataTable.getIntForEntity(entity);
        }

        for (ItemStack itemStack : entity.getHandItems()) {
            if (!itemStack.isEmpty() && itemStack.isIn(metal.getItemTag())) {
                ironContent += HELD_ITEM_METAL_VALUE;
            }
        }

        for (ItemStack itemStack : entity.getArmorItems()) {
            if (!itemStack.isEmpty() && itemStack.isIn(metal.getItemTag())) {
                ironContent += armorDataTable.getIntForItem(itemStack.getItem());
            }
        }

        if (entity instanceof PlayerEntity playerEntity) {
            ItemStack activeItem = playerEntity.getActiveItem();
            if (!activeItem.isEmpty() && activeItem.isIn(metal.getItemTag())) {
                if (activeItem.isIn(ModItemTags.SHIELDS)) {
                    ironContent += USING_SHIELD_BONUS;
                }
            }
        }

        return ironContent;
    }

    public static int getSteelContentForEntity(Entity entity, DataTable entityDataTable,
            DataTable armorDataTable) {
        return MetalQueryManager.getMetalContentForEntity(entity, Metals.STEEL, entityDataTable,
                armorDataTable);
    }

    private MetalQueryManager() {}
}
