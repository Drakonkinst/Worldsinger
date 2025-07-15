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

package io.github.drakonkinst.worldsinger.entity.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.entity.SilverLinedEntityData;

// Contrary to the name, the component is not limited to being used just for boats
// However, we don't want to store type information (max durability) in the component, so
// it gets its own special class instead. This leaves room for other entities to be silver-lined
// with their own durability values.
public class SilverLinedBoatData extends SilverLinedEntityData {

    public static Codec<SilverLinedBoatData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(Codec.INT.fieldOf(KEY_SILVER_LINED)
                            .forGetter(SilverLinedEntityData::getSilverDurability))
                    .apply(instance, SilverLinedBoatData::new));

    public SilverLinedBoatData(int durability) {
        setSilverDurability(durability);
    }

    @Override
    public int getRepairAmount() {
        return SilverLined.BOAT_MAX_DURABILITY / 4;
    }

    @Override
    public int getMaxSilverDurability() {
        return SilverLined.BOAT_MAX_DURABILITY;
    }
}
