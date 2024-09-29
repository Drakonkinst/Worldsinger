/*
 * MIT License
 *
 * Copyright (c) 2024 Drakonkinst
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

package io.github.drakonkinst.worldsinger.entity.cannonball;

import io.github.drakonkinst.worldsinger.cosmere.WaterReactionManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleSpawner;
import io.github.drakonkinst.worldsinger.entity.CannonballEntity;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballContents;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HollowSporeCannonballBehavior implements CannonballBehavior {

    private static final int SPORE_AMOUNT = 83;
    private static final int WATER_AMOUNT_PER_LEVEL = 80;

    private final Object2IntMap<CannonballContents> contentMap;

    public HollowSporeCannonballBehavior(Object2IntMap<CannonballContents> contentMap) {
        this.contentMap = contentMap;
    }

    @Override
    public void onCollisionClient(CannonballEntity entity, Vec3d hitPos) {

    }

    @Override
    public void onCollisionServer(CannonballEntity entity, Vec3d hitPos) {
        if (!(entity.getWorld() instanceof ServerWorld world)) {
            return;
        }
        BlockPos blockPos = BlockPosUtil.toBlockPos(hitPos);

        for (Object2IntMap.Entry<CannonballContents> entry : contentMap.object2IntEntrySet()) {
            AetherSpores sporeType = entry.getKey().getSporeType();
            int strength = entry.getIntValue();
            if (sporeType == null) {
                continue;
            }
            SporeParticleSpawner.spawnSporeCannonballParticle(world, sporeType, hitPos, strength);
            if (!sporeType.isDead()) {
                handleLivingSporeBehavior(world, sporeType, blockPos, hitPos, strength);
            }
        }
    }

    private void handleLivingSporeBehavior(World world, AetherSpores sporeType, BlockPos blockPos,
            Vec3d pos, int strength) {
        BlockState blockState = world.getBlockState(blockPos);
        // TODO: Make this a helper method to trigger a catalyzation
        if (world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
            int waterAmount = WaterReactionManager.absorbWaterAndCollectReactives(world, blockPos,
                    null);
            sporeType.doReactionFromProjectile(world, pos, SPORE_AMOUNT * strength, waterAmount,
                    world.getRandom(), false);
        } else if (blockState.isOf(Blocks.WATER_CAULDRON)) {
            int waterAmount = WATER_AMOUNT_PER_LEVEL * blockState.get(LeveledCauldronBlock.LEVEL);
            world.setBlockState(blockPos, Blocks.CAULDRON.getStateWithProperties(blockState));
            sporeType.doReactionFromProjectile(world, pos, SPORE_AMOUNT, waterAmount,
                    world.getRandom(), true);
        }
    }
}
