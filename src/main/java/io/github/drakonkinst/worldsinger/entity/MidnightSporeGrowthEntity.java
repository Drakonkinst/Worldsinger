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
package io.github.drakonkinst.worldsinger.entity;

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleManager;
import io.github.drakonkinst.worldsinger.particle.ModParticleTypes;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.util.math.Int3;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MidnightSporeGrowthEntity extends SporeGrowthEntity {

    private static final int COST_MIDNIGHT_ESSENCE = 50;
    private static final int GROWTH_DELAY = 4;

    public MidnightSporeGrowthEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected int getMaxStage() {
        return 1;
    }

    @Override
    protected BlockState getNextBlock() {
        return ModBlocks.MIDNIGHT_ESSENCE.getDefaultState();
    }

    @Override
    protected boolean canBreakHere(BlockState state) {
        return false;
    }

    @Override
    protected boolean canGrowHere(BlockState state) {
        return state.isIn(ModBlockTags.SPORES_CAN_GROW);
    }

    @Override
    protected int getWeight(World world, BlockPos pos, Int3 direction, boolean allowPassthrough) {
        BlockState state = world.getBlockState(pos);
        int weight = -1;

        if (this.canGrowHere(state)) {
            weight = 100;
        } else if (allowPassthrough && this.isGrowthBlock(state)) {
            // If allowPassthrough is true, we assume that no actual block will be placed
            weight = 50;
        }

        if (weight < 0) {
            return 0;
        }

        // Strongly prefer to grow downwards
        weight -= 1000 * direction.y();

        // Bonus for moving towards origin, keeping it in a clump
        int bonusDistanceFromOrigin =
                this.getDistanceFromOrigin(this.getBlockPos()) - this.getDistanceFromOrigin(pos);
        weight += 50 * bonusDistanceFromOrigin;

        // Massive bonus for going along with external force
        double forceModifier = this.getExternalForceModifier(direction);
        weight += MathHelper.floor(FORCE_MODIFIER_MULTIPLIER * forceModifier);

        // Always have some weight, so it is an options if no other options are good
        weight = Math.max(1, weight);

        return weight;
    }

    @Override
    protected boolean isGrowthBlock(BlockState state) {
        return state.isOf(ModBlocks.MIDNIGHT_ESSENCE);
    }

    @Override
    protected void onGrowBlock(BlockPos pos, BlockState state, BlockState originalState) {
        this.doGrowEffects(pos, state, COST_MIDNIGHT_ESSENCE, true, true, true);

        if (this.getWorld() instanceof ServerWorld world) {
            Vec3d centerPos = pos.toCenterPos();
            world.spawnParticles(ModParticleTypes.MIDNIGHT_ESSENCE, centerPos.getX(),
                    centerPos.getY(), centerPos.getZ(), 10, 0.5, 0.5, 0.5, 0.0);
        }

        SporeParticleManager.damageEntitiesInBlock(this.getWorld(), MidnightSpores.getInstance(),
                pos);
    }

    @Override
    protected void placeDecorator(BlockPos pos, Direction direction) {
        // Do nothing
    }

    @Override
    protected int getGrowthDelay() {
        return GROWTH_DELAY;
    }
}
