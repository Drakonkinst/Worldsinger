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

package io.github.drakonkinst.worldsinger.entity;

import io.github.drakonkinst.worldsinger.cosmere.WaterReactionManager;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.item.cannonball.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.cannonball.CannonballComponent.CannonballCore;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class CannonballEntity extends ThrownItemEntity implements FlyingItemEntity {

    // TODO: Fuse data

    private static final float ENTITY_COLLISION_DAMAGE = 2.0f;
    private static final float PARTICLE_SPEED = 0.5f;
    private static final int WATER_SPLASH_RADIUS_HORIZONTAL = 4;
    private static final int WATER_SPLASH_RADIUS_VERTICAL = 2;

    public CannonballEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public CannonballEntity(World world, LivingEntity owner) {
        super(ModEntityTypes.CANNONBALL, owner, world);
    }

    public CannonballEntity(World world, double x, double y, double z) {
        super(ModEntityTypes.CANNONBALL, x, y, z, world);
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES) {
            // TODO: Make a custom particle effect for this?
            ItemStack stack = this.getStack();
            if (stack.isOf(ModItems.CERAMIC_CANNONBALL)) {
                stack = Items.BRICK.getDefaultStack();
            }
            ParticleEffect particleEffect = new ItemStackParticleEffect(ParticleTypes.ITEM, stack);

            for (int i = 0; i < 8; i++) {
                float velocityX = this.random.nextFloat() * PARTICLE_SPEED * 2.0f - PARTICLE_SPEED;
                float velocityY = this.random.nextFloat() * PARTICLE_SPEED * 2.0f - PARTICLE_SPEED;
                float velocityZ = this.random.nextFloat() * PARTICLE_SPEED * 2.0f - PARTICLE_SPEED;
                this.getWorld()
                        .addParticle(particleEffect, this.getX(), this.getY(), this.getZ(),
                                velocityX, velocityY, velocityZ);
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        entity.damage(this.getDamageSources().thrown(this, this.getOwner()),
                ENTITY_COLLISION_DAMAGE);
    }

    private boolean isWaterCannonball(CannonballComponent cannonballComponent) {
        return cannonballComponent != null && cannonballComponent.core() == CannonballCore.WATER;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        CannonballComponent cannonballComponent = this.getStack()
                .get(ModDataComponentTypes.CANNONBALL);
        World world = this.getWorld();
        Vec3d hitPos = hitResult.getPos();

        if (world.isClient) {
            if (isWaterCannonball(cannonballComponent)) {
                // Play splash particles
                double width = this.getWidth();
                double radius = width * 0.5;
                // TODO: This happens inconsistently for some reason
                for (int i = 0; i < 20; i++) {
                    double offsetX = this.random.nextDouble() * width - radius;
                    double offsetY = this.random.nextDouble() * width - radius;
                    double offsetZ = this.random.nextDouble() * width - radius;
                    world.addParticle(ParticleTypes.SPLASH, this.getX() + offsetX,
                            this.getY() + offsetY, this.getZ() + offsetZ, 0.0f, 0.0f, 0.0f);
                }
            }
        } else {
            if (isWaterCannonball(cannonballComponent)) {
                this.extinguishNearbyBlocks(hitResult.getPos());
                this.splashWaterOnNearbyEntities();
                WaterReactionManager.catalyzeAroundWaterEffect(world, this.getBlockPos(),
                        WATER_SPLASH_RADIUS_HORIZONTAL, WATER_SPLASH_RADIUS_VERTICAL,
                        WaterReactionManager.WATER_AMOUNT_STILL);
            }
            // TODO: This should probably be client-side
            world.playSound(null, hitPos.getX(), hitPos.getY(), hitPos.getZ(),
                    ModSoundEvents.ENTITY_CANNONBALL_BREAK, SoundCategory.PLAYERS, 1.0f,
                    random.nextFloat() * 0.1f + 1.25f, world.getRandom().nextLong());
            this.getWorld()
                    .sendEntityStatus(this,
                            EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
            this.discard();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.CERAMIC_CANNONBALL;
    }

    private void splashWaterOnNearbyEntities() {
        Box box = this.getBoundingBox()
                .expand(WATER_SPLASH_RADIUS_HORIZONTAL, WATER_SPLASH_RADIUS_VERTICAL,
                        WATER_SPLASH_RADIUS_HORIZONTAL);

        for (LivingEntity entity : this.getWorld()
                .getEntitiesByClass(LivingEntity.class, box, PotionEntity.AFFECTED_BY_WATER)) {
            double distSq = this.squaredDistanceTo(entity);
            if (distSq < WATER_SPLASH_RADIUS_HORIZONTAL * WATER_SPLASH_RADIUS_HORIZONTAL) {
                if (entity.hurtByWater()) {
                    entity.damage(this.getDamageSources().indirectMagic(this, this.getOwner()),
                            1.0F);
                }

                if (entity.isOnFire() && entity.isAlive()) {
                    entity.extinguishWithSound();
                }
            }
        }

        for (AxolotlEntity axolotlEntity : this.getWorld()
                .getNonSpectatingEntities(AxolotlEntity.class, box)) {
            axolotlEntity.hydrateFromPotion();
        }
    }

    private void extinguishNearbyBlocks(Vec3d hitPos) {
        BlockPos centerPos = BlockPosUtil.toBlockPos(hitPos);
        this.extinguishFire(centerPos);
        for (Direction offset : ModConstants.CARDINAL_DIRECTIONS) {
            this.extinguishFire(centerPos.offset(offset));
        }
    }

    private void extinguishFire(BlockPos pos) {
        BlockState blockState = this.getWorld().getBlockState(pos);
        if (blockState.isIn(BlockTags.FIRE)) {
            this.getWorld().breakBlock(pos, false, this);
        } else if (AbstractCandleBlock.isLitCandle(blockState)) {
            AbstractCandleBlock.extinguish(null, blockState, this.getWorld(), pos);
        } else if (CampfireBlock.isLitCampfire(blockState)) {
            this.getWorld().syncWorldEvent(null, WorldEvents.FIRE_EXTINGUISHED, pos, 0);
            CampfireBlock.extinguish(this.getOwner(), this.getWorld(), pos, blockState);
            this.getWorld().setBlockState(pos, blockState.with(CampfireBlock.LIT, Boolean.FALSE));
        }
    }
}
