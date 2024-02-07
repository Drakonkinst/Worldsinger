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
        return SeetheManager.areSporesFluidized(world) && !state.get(Properties.PERSISTENT)
                && !world.isRaining() && (world.isSkyVisible(pos.up()) || !world.getFluidState(
                pos.down()).isOf(Fluids.EMPTY) || random.nextInt(5) == 0);
    }
}
