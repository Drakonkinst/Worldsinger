package io.github.drakonkinst.worldsinger.mixin.block;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin extends Block {

    @Shadow
    @Final
    public static IntProperty MOISTURE;

    @Shadow
    private static boolean hasCrop(BlockView world, BlockPos pos) {
        return false;
    }

    @Shadow
    public static void setToDirt(@Nullable Entity entity, BlockState state, World world,
            BlockPos pos) {
    }

    public FarmlandBlockMixin(Settings settings) {
        super(settings);
    }

    @Unique
    private static boolean isSaltNearby(WorldView world, BlockPos pos) {
        for (BlockPos blockPos : BlockPos.iterate(pos.add(-4, 0, -4), pos.add(4, 1, 4))) {
            if (world.getBlockState(blockPos).isIn(ModBlockTags.HAS_SALT)) {
                return true;
            }
        }

        return false;
    }

    @WrapMethod(method = "randomTick")
    private void makeSaltKillFarmland(BlockState state, ServerWorld world, BlockPos pos,
            Random random, Operation<Void> original) {
        if (isSaltNearby(world, pos)) {
            int moisture = state.get(MOISTURE);
            if (moisture > 0) {
                world.setBlockState(pos, state.with(MOISTURE, moisture - 1),
                        Block.NOTIFY_LISTENERS);
            } else if (!hasCrop(world, pos)) {
                setToDirt(null, state, world, pos);
            }
        } else {
            original.call(state, world, pos, random);
        }
    }

}
