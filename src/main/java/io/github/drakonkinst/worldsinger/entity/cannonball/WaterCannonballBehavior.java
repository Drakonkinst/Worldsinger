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
import io.github.drakonkinst.worldsinger.entity.CannonballEntity;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class WaterCannonballBehavior implements CannonballBehavior {

    private static final int WATER_SPLASH_RADIUS_HORIZONTAL = 4;
    private static final int WATER_SPLASH_RADIUS_VERTICAL = 2;

    public static void spawnWaterParticlesClient(CannonballEntity entity) {
        double width = entity.getWidth();
        double radius = width * 0.5;
        Random random = entity.getRandom();
        World world = entity.getWorld();
        for (int i = 0; i < 20; i++) {
            double offsetX = random.nextDouble() * width - radius;
            double offsetY = random.nextDouble() * width - radius;
            double offsetZ = random.nextDouble() * width - radius;
            world.addParticleClient(ParticleTypes.SPLASH, entity.getX() + offsetX,
                    entity.getY() + offsetY, entity.getZ() + offsetZ, 0.0f, 0.0f, 0.0f);
        }
    }

    @Override
    public void onCollisionClient(CannonballEntity entity, Vec3d hitPos) {
        // Since this is called inconsistently, moving it somewhere else
        // spawnWaterParticlesClient(entity);
    }

    @Override
    public void onCollisionServer(CannonballEntity entity, Vec3d hitPos) {
        this.extinguishNearbyBlocks(entity, hitPos);
        this.splashWaterOnNearbyEntities(entity);
        WaterReactionManager.catalyzeAroundWaterEffect(entity.getWorld(), entity.getBlockPos(),
                WATER_SPLASH_RADIUS_HORIZONTAL, WATER_SPLASH_RADIUS_VERTICAL,
                WaterReactionManager.WATER_AMOUNT_STILL);
    }

    private void splashWaterOnNearbyEntities(CannonballEntity entity) {
        World world = entity.getWorld();
        Box box = entity.getBoundingBox()
                .expand(WATER_SPLASH_RADIUS_HORIZONTAL, WATER_SPLASH_RADIUS_VERTICAL,
                        WATER_SPLASH_RADIUS_HORIZONTAL);

        for (LivingEntity otherEntity : world.getEntitiesByClass(LivingEntity.class, box,
                PotionEntity.AFFECTED_BY_WATER)) {
            double distSq = entity.squaredDistanceTo(otherEntity);
            if (distSq < WATER_SPLASH_RADIUS_HORIZONTAL * WATER_SPLASH_RADIUS_HORIZONTAL) {
                if (otherEntity.hurtByWater()
                        && entity.getWorld() instanceof ServerWorld serverWorld) {
                    otherEntity.damage(serverWorld,
                            entity.getDamageSources().indirectMagic(entity, entity.getOwner()),
                            1.0F);
                }

                if (otherEntity.isOnFire() && otherEntity.isAlive()) {
                    otherEntity.extinguishWithSound();
                }
            }
        }

        for (AxolotlEntity axolotlEntity : world.getNonSpectatingEntities(AxolotlEntity.class,
                box)) {
            axolotlEntity.hydrateFromPotion();
        }
    }

    private void extinguishNearbyBlocks(CannonballEntity entity, Vec3d hitPos) {
        BlockPos centerPos = BlockPosUtil.toBlockPos(hitPos);
        this.extinguishFire(entity, centerPos);
        for (Direction offset : ModConstants.CARDINAL_DIRECTIONS) {
            this.extinguishFire(entity, centerPos.offset(offset));
        }
    }

    private void extinguishFire(CannonballEntity entity, BlockPos pos) {
        World world = entity.getWorld();
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isIn(BlockTags.FIRE)) {
            world.breakBlock(pos, false, entity);
        } else if (AbstractCandleBlock.isLitCandle(blockState)) {
            AbstractCandleBlock.extinguish(null, blockState, world, pos);
        } else if (CampfireBlock.isLitCampfire(blockState)) {
            world.syncWorldEvent(null, WorldEvents.FIRE_EXTINGUISHED, pos, 0);
            CampfireBlock.extinguish(entity.getOwner(), world, pos, blockState);
            world.setBlockState(pos, blockState.with(CampfireBlock.LIT, Boolean.FALSE));
        }
    }
}
