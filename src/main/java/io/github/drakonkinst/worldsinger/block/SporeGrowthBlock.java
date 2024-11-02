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

import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public interface SporeGrowthBlock {

    // Spore growth blocks decay if not persistent and it is not raining.
    static boolean canDecay(ServerWorld world, BlockPos pos, BlockState state, Random random) {
        BlockPos abovePos = pos.add(0, 1, 0);
        // Decays much slower unless on Lumar and the Seethe is on
        int chanceDecay;
        if (CosmerePlanet.isLumar(world) && SeetheManager.areSporesFluidized(world)) {
            chanceDecay = 1;
        } else {
            chanceDecay = 10;
        }
        // Decay slower if not open to sky or not above a fluid
        if (!world.isSkyVisible(abovePos) && world.getFluidState(pos.down()).isOf(Fluids.EMPTY)) {
            chanceDecay += 5;
        }
        return (random.nextInt() == chanceDecay) && !state.get(Properties.PERSISTENT)
                && !world.hasRain(abovePos);
    }
}
