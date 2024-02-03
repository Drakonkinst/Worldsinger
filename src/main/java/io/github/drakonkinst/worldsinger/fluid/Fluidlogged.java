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
package io.github.drakonkinst.worldsinger.fluid;

import io.github.drakonkinst.worldsinger.WorldsingerConfig;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

// Manages making blocks fluidloggable by fluids other than water.
public final class Fluidlogged {

    public static final List<Identifier> WATERLOGGABLE_FLUIDS = WorldsingerConfig.instance()
            .getFluidloggableFluids();
    private static final Map<Fluid, FluidBlock> fluidToFluidBlocks = new HashMap<>();

    public static Fluid getFluid(BlockState state) {
        if (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED)) {
            return Fluids.WATER;
        }
        if (!state.contains(ModProperties.FLUIDLOGGED)) {
            return null;
        }
        int index = state.get(ModProperties.FLUIDLOGGED) - 1;
        if (index < 0) {
            return Fluids.EMPTY;
        }
        if (index >= Fluidlogged.WATERLOGGABLE_FLUIDS.size()) {
            return null;
        }
        Identifier id = Fluidlogged.WATERLOGGABLE_FLUIDS.get(index);
        if (id == null) {
            return null;
        }
        return Registries.FLUID.get(id);
    }

    public static void registerFluidBlockForFluid(Fluid fluid, FluidBlock fluidBlock) {
        fluidToFluidBlocks.put(fluid, fluidBlock);
    }

    public static FluidBlock getFluidBlockForFluid(Fluid fluid) {
        return fluidToFluidBlocks.get(fluid);
    }

    public static int getFluidIndex(Fluid fluid) {
        if (fluid.equals(Fluids.EMPTY)) {
            return 0;
        }
        return Fluidlogged.WATERLOGGABLE_FLUIDS.indexOf(Registries.FLUID.getId(fluid)) + 1;
    }

    public static void initialize() {}

    private Fluidlogged() {}
}
