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

package io.github.drakonkinst.worldsinger.entity;

import io.github.drakonkinst.worldsinger.api.sync.SyncableAttachment;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

public abstract class SilverLinedEntityData implements SilverLined, SyncableAttachment {

    protected static final String KEY_SILVER_LINED = "silver_lined";

    private int silverDurability = 0;

    @Override
    public void setSilverDurability(int durability) {
        silverDurability = MathHelper.clamp(durability, 0, getMaxSilverDurability());
    }

    @Override
    public int getSilverDurability() {
        return silverDurability;
    }

    @Override
    public void syncToNbt(NbtCompound nbt) {
        nbt.putInt(KEY_SILVER_LINED, silverDurability);
    }

    @Override
    public void syncFromNbt(NbtCompound nbt) {
        silverDurability = nbt.getInt(KEY_SILVER_LINED, 0);
    }
}
