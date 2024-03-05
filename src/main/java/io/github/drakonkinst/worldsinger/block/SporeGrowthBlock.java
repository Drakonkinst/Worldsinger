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

import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public interface SporeGrowthBlock {

    // Spore growth blocks decay if not persistent, is not raining, and seethe is on.
    // They decay faster if open to sky or above a fluid.
    static boolean canDecay(ServerWorld world, BlockPos pos, BlockState state, Random random) {
        BlockPos abovePos = pos.add(0, 1, 0);
        return SeetheManager.areSporesFluidized(world) && !state.get(Properties.PERSISTENT)
                && !world.hasRain(abovePos) && (world.isSkyVisible(abovePos)
                || !world.getFluidState(pos.down()).isOf(Fluids.EMPTY) || random.nextInt(5) == 0);
    }
}
