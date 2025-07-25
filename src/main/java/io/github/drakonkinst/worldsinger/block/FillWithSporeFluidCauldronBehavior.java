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

import com.google.common.base.Suppliers;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeKillingUtil;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FillWithSporeFluidCauldronBehavior extends FillWithFluidCauldronBehavior {

    private final Supplier<Block> deadSporeCauldronBlock;

    public FillWithSporeFluidCauldronBehavior(Supplier<Block> cauldronBlock,
            Supplier<Block> deadSporeCauldronBlock) {
        super(cauldronBlock, ModSoundEvents.ITEM_BUCKET_EMPTY_AETHER_SPORE);
        this.deadSporeCauldronBlock = Suppliers.memoize(deadSporeCauldronBlock::get);
    }

    @Override
    public ActionResult interact(BlockState state, World world, BlockPos pos, PlayerEntity player,
            Hand hand, ItemStack stack) {
        if (SporeKillingUtil.isSporeKillingBlockNearby(world, pos)) {
            return CauldronBehavior.fillCauldron(world, pos, player, hand, stack,
                    deadSporeCauldronBlock.get()
                            .getDefaultState()
                            .with(LeveledCauldronBlock.LEVEL, 3), fillCauldronSound);
        }
        return super.interact(state, world, pos, player, hand, stack);
    }
}
