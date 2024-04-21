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
package io.github.drakonkinst.worldsinger.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.datatables.DataTable;
import io.github.drakonkinst.datatables.DataTableRegistry;
import io.github.drakonkinst.worldsinger.block.SporeKillable;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeKillingUtil;
import io.github.drakonkinst.worldsinger.fluid.FluidShapes;
import io.github.drakonkinst.worldsinger.fluid.Fluidlogged;
import io.github.drakonkinst.worldsinger.mixin.accessor.AbstractBlockAccessor;
import io.github.drakonkinst.worldsinger.mixin.accessor.AbstractBlockSettingsAccessor;
import io.github.drakonkinst.worldsinger.registry.ModDataTables;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import java.util.function.ToIntFunction;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {

    @Shadow
    public abstract Block getBlock();

    @Shadow
    protected abstract BlockState asBlockState();

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/function/ToIntFunction;applyAsInt(Ljava/lang/Object;)I"))
    private <T> int injectLuminance(ToIntFunction<T> instance, T t) {
        if (t instanceof BlockState state && state.getEntries() != null && state.contains(
                ModProperties.FLUIDLOGGED)) {
            Fluid fluid = Fluidlogged.getFluid(state);
            FluidBlock fluidBlock = Fluidlogged.getFluidBlockForFluid(fluid);
            if (fluidBlock != null) {
                // Apply luminance from the fluid block to the block itself
                AbstractBlock.Settings settings = ((AbstractBlockAccessor) fluidBlock).worldsinger$getSettings();
                return ((AbstractBlockSettingsAccessor) settings).worldsinger$getLuminance()
                        .applyAsInt(state);
            }
        }
        return instance.applyAsInt(t);
    }

    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"))
    private void makeCustomFluidTickable(Direction direction, BlockState neighborState,
            WorldAccess world, BlockPos pos, BlockPos neighborPos,
            CallbackInfoReturnable<BlockState> cir) {
        Fluid fluid = Fluidlogged.getFluid(this.asBlockState());
        boolean noFluid = (fluid == null) || Fluids.EMPTY.equals(fluid);
        if (!noFluid) {
            world.scheduleFluidTick(pos, fluid, fluid.getTickRate(world));
        }
    }

    @ModifyReturnValue(method =
            "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;"
                    + "Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", at = @At("RETURN"))
    private VoxelShape fixFluidloggedCollisionShape(VoxelShape originalShape, BlockView world,
            BlockPos pos, ShapeContext context) {
        FluidState fluidState = world.getFluidState(pos);
        int level = fluidState.getLevel();
        if (level == 0) {
            return originalShape;
        }

        VoxelShape shape = FluidShapes.VOXEL_SHAPES[level];
        VoxelShape shapeBelow = FluidShapes.VOXEL_SHAPES[level - 1];
        if (context.isAbove(shapeBelow, pos, true) && context.canWalkOnFluid(
                world.getFluidState(pos.up()), fluidState)) {
            return VoxelShapes.union(originalShape, shape);
        }
        return originalShape;
    }

    @Inject(method = "getFluidState", at = @At("RETURN"), cancellable = true)
    private void worldsinger$supportMultipleFluidsInState(CallbackInfoReturnable<FluidState> cir) {
        BlockState state = this.asBlockState();
        Fluid fluid = Fluidlogged.getFluid(state);
        if (fluid != null) {
            cir.setReturnValue(fluid.getDefaultState());
        }
    }

    @ModifyArg(method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getCollisionShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"), index = 0)
    private BlockState injectCustomFluidCollisionShape(BlockState state) {
        if (state.contains(ModProperties.FLUIDLOGGED)) {
            return state.with(ModProperties.FLUIDLOGGED, 0);
        }
        return state;
    }

    @ModifyArg(method = "getOutlineShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getOutlineShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"), index = 0)
    private BlockState injectCustomFluidOutlineShape(BlockState state) {
        if (state.contains(ModProperties.FLUIDLOGGED)) {
            return state.with(ModProperties.FLUIDLOGGED, 0);
        }
        return state;
    }

    @ModifyArg(method = "getSidesShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getSidesShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;"), index = 0)
    private BlockState injectCustomFluidSidesShape(BlockState state) {
        if (state.contains(ModProperties.FLUIDLOGGED)) {
            return state.with(ModProperties.FLUIDLOGGED, 0);
        }
        return state;
    }

    @Inject(method = "onStateReplaced", at = @At("TAIL"))
    private void addBlockPlaceBehaviors(World world, BlockPos pos, BlockState state, boolean moved,
            CallbackInfo ci) {
        checkSporeKillingBehavior(world, pos, state);
        checkSporeKilledOnPlace(world, pos, state);
    }

    @Unique
    private void checkSporeKillingBehavior(World world, BlockPos pos, BlockState state) {
        if (!state.isIn(ModBlockTags.KILLS_SPORES)) {
            return;
        }
        DataTable dataTable = DataTableRegistry.INSTANCE.get(ModDataTables.SPORE_KILLING_RADIUS);
        int radius = dataTable.getIntForBlock(state);
        if (radius <= 0) {
            return;
        }
        SporeKillingUtil.killNearbySpores(world, pos, radius);
    }

    @Unique
    private void checkSporeKilledOnPlace(World world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof SporeKillable sporeKillable
                && sporeKillable.isSporeKillable(world, pos, state)) {
            sporeKillable.checkKillSporeBlock(world, pos, state);
        }
    }
}
