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
package io.github.drakonkinst.worldsinger.entity.data;

import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.component.SilverLinedComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;

public abstract class SilverLinedEntityData implements SilverLinedComponent {

    private static final String NBT_KEY = "SilverDurability";

    private final Entity entity;
    private int silverDurability;

    public SilverLinedEntityData(BoatEntity boatEntity) {
        this.entity = boatEntity;
    }

    @Override
    public void setSilverDurability(int durability) {
        this.silverDurability = Math.max(0, Math.min(durability, this.getMaxSilverDurability()));
        ModComponents.SILVER_LINED.sync(entity);
    }

    @Override
    public int getSilverDurability() {
        return silverDurability;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.silverDurability = tag.getInt(NBT_KEY);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt(NBT_KEY, this.silverDurability);
    }

    public Entity getEntity() {
        return entity;
    }
}
