package io.github.drakonkinst.worldsinger.block;

import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeKillingManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface SporeKillable {

    Block getDeadSporeBlock();

    default void checkKillSporeBlock(World world, BlockPos pos, BlockState state) {
        if (SporeKillingManager.isSporeKillingBlockNearby(world, pos)) {
            world.setBlockState(pos, SporeKillingManager.convertToDeadVariant(this, state));
        }
    }
}
