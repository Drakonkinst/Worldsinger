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
package io.github.drakonkinst.worldsinger.cosmere.lumar;

import io.github.drakonkinst.worldsinger.entity.cannonball.CannonballBehavior;
import io.github.drakonkinst.worldsinger.entity.spore_growth.SporeGrowthEntity;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import io.github.drakonkinst.worldsinger.util.BoxUtil;
import io.github.drakonkinst.worldsinger.util.math.Int3;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public abstract class GrowableAetherSpores<T extends SporeGrowthEntity> extends AetherSpores {

    private static final double COMBINE_GROWTH_MAX_RADIUS = 3.0;
    private final Class<T> sporeGrowthEntityTypeClass;

    public GrowableAetherSpores(Class<T> sporeGrowthEntityTypeClass) {
        this.sporeGrowthEntityTypeClass = sporeGrowthEntityTypeClass;
    }

    public abstract EntityType<T> getSporeGrowthEntityType();

    public abstract int getSmallStage();

    @Override
    public void doReaction(World world, Vec3d pos, int spores, int water, Random random) {
        this.spawnSporeGrowth(world, pos, spores, water, true, false, false, Int3.ZERO);
    }

    public void spawnSporeGrowth(World world, Vec3d pos, int spores, int water,
            boolean initialGrowth, boolean isSmall, boolean isSplit, Int3 lastDir) {
        // If one already exists nearby, just augment that one
        if (!isSplit && this.tryCombineWithNearbyGrowth(world, pos, spores, water, initialGrowth,
                isSmall)) {
            return;
        }

        T entity = this.getSporeGrowthEntityType().create(world);
        if (entity == null) {
            return;
        }
        entity.setPosition(pos);
        entity.setSpores(spores);
        entity.setWater(water);
        entity.setInitialGrowth(initialGrowth);
        entity.setLastDir(lastDir);
        if (isSmall) {
            entity.setInitialStage(this.getSmallStage());
        }

        world.spawnEntity(entity);
    }

    private boolean tryCombineWithNearbyGrowth(World world, Vec3d pos, int spores, int water,
            boolean isInitial, boolean isSmall) {
        Box box = BoxUtil.createBoxAroundPos(pos, COMBINE_GROWTH_MAX_RADIUS);
        List<T> nearbySporeGrowthEntities = world.getEntitiesByClass(sporeGrowthEntityTypeClass,
                box, sporeGrowthEntity -> sporeGrowthEntity.age == 0
                        && sporeGrowthEntity.isInitialGrowth() == isInitial
                        && (sporeGrowthEntity.getStage() == 1) == isSmall);
        if (nearbySporeGrowthEntities.isEmpty()) {
            return false;
        }
        T existingSporeGrowthEntity = nearbySporeGrowthEntities.get(0);
        existingSporeGrowthEntity.setSpores(existingSporeGrowthEntity.getSpores() + spores);
        existingSporeGrowthEntity.setWater(existingSporeGrowthEntity.getWater() + water);
        return true;
    }

    @Override
    public void doReactionFromFluidContainer(World world, BlockPos fluidContainerPos, int spores,
            int water, Random random) {
        super.doReactionFromFluidContainer(world, fluidContainerPos.up(), spores, water, random);
    }

    @Override
    public void doReactionFromParticle(World world, Vec3d pos, int spores, int water, Random random,
            boolean affectingFluidContainer) {
        if (affectingFluidContainer) {
            BlockPos posAbove = BlockPosUtil.toBlockPos(pos).up();
            BlockState stateAbove = world.getBlockState(posAbove);
            if (stateAbove.isIn(ModBlockTags.SPORES_CAN_GROW) || stateAbove.isIn(
                    ModBlockTags.SPORES_CAN_BREAK)) {
                this.spawnSporeGrowth(world, pos.add(0.0, 1.0, 0.0), spores, water, true, true,
                        false, Int3.ZERO);
            }
        }
        this.spawnSporeGrowth(world, pos, spores, water, true,
                spores < CannonballBehavior.SPORE_AMOUNT, false, Int3.ZERO);
    }
}
