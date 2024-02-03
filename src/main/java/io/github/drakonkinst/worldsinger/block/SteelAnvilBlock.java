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
package io.github.drakonkinst.worldsinger.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.Nullable;

public class SteelAnvilBlock extends AnvilBlock {

    // Unused Codec
    public static final MapCodec<SteelAnvilBlock> CODEC = AbstractBlock.createCodec(
            SteelAnvilBlock::new);

    @Nullable
    public static BlockState getLandingState(BlockState fallingState) {
        if (fallingState.isOf(ModBlocks.STEEL_ANVIL)) {
            return ModBlocks.CHIPPED_STEEL_ANVIL.getDefaultState()
                    .with(FACING, fallingState.get(FACING));
        }
        if (fallingState.isOf(ModBlocks.CHIPPED_STEEL_ANVIL)) {
            return ModBlocks.DAMAGED_STEEL_ANVIL.getDefaultState()
                    .with(FACING, fallingState.get(FACING));
        }
        return null;
    }

    public SteelAnvilBlock(Settings settings) {
        super(settings);
    }

    // @Override
    // public MapCodec<AnvilBlock> getCodec() {
    //     return CODEC;
    // }
}
