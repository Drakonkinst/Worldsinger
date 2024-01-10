package io.github.drakonkinst.worldsinger.world;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;

public interface CameraPosAccess {

    FluidState worldsinger$getSubmersedFluidState();

    BlockState worldsinger$getBlockState();
}
