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
package io.github.drakonkinst.worldsinger.util.math;

import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;

public class ExtendedRaycastContext extends RaycastContext {

    private final ExtendedFluidHandling extendedFluidHandling;

    public ExtendedRaycastContext(Vec3d start, Vec3d end, ShapeType shapeType,
            ExtendedFluidHandling fluidHandling, Entity entity) {
        super(start, end, shapeType, FluidHandling.NONE, entity);
        this.extendedFluidHandling = fluidHandling;
    }

    @Override
    public VoxelShape getFluidShape(FluidState state, BlockView world, BlockPos pos) {
        return extendedFluidHandling.handled(state) ? state.getShape(world, pos)
                : VoxelShapes.empty();
    }

    public enum ExtendedFluidHandling {
        // Also includes Sunlight blocks for gameplay cohesion
        // Also includes flowing blocks, since this is only currently used for splash potions
        SPORE_SEA(state -> state.isIn(ModFluidTags.AETHER_SPORES_OR_SUNLIGHT));

        private final Predicate<FluidState> predicate;

        ExtendedFluidHandling(Predicate<FluidState> predicate) {
            this.predicate = predicate;
        }

        public boolean handled(FluidState state) {
            return this.predicate.test(state);
        }
    }
}
