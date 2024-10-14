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
package io.github.drakonkinst.worldsinger.registry;

import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;

public final class ModSoundGroups {

    public static final BlockSoundGroup SPORES = new BlockSoundGroup(1.0F, 1.0F,
            SoundEvents.BLOCK_SAND_BREAK, SoundEvents.BLOCK_SAND_STEP,
            ModSoundEvents.BLOCK_SPORE_BLOCK_PLACE, SoundEvents.BLOCK_SAND_HIT,
            SoundEvents.BLOCK_SAND_FALL);
    public static final BlockSoundGroup SALT = BlockSoundGroup.CALCITE;
    public static final BlockSoundGroup SALTSTONE = BlockSoundGroup.NETHERRACK;
    public static final BlockSoundGroup ROSEITE = BlockSoundGroup.GLASS;
    public static final BlockSoundGroup CRIMSON_GROWTH = BlockSoundGroup.DRIPSTONE_BLOCK;
    public static final BlockSoundGroup CRIMSON_SPINE = BlockSoundGroup.POINTED_DRIPSTONE;
    public static final BlockSoundGroup VERDANT_VINE_BRANCH = BlockSoundGroup.GRASS;
    public static final BlockSoundGroup VERDANT_VINE_SNARE = BlockSoundGroup.GRASS;
    public static final BlockSoundGroup TWISTING_VERDANT_VINES = BlockSoundGroup.WEEPING_VINES;
    public static final BlockSoundGroup MIDNIGHT_ESSENCE = BlockSoundGroup.SOUL_SAND;

    private ModSoundGroups() {}
}
