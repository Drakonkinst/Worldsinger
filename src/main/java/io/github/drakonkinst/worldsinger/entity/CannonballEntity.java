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

import io.github.drakonkinst.worldsinger.entity.cannonball.CannonballBehavior;
import io.github.drakonkinst.worldsinger.entity.cannonball.EmptyCannonballBehavior;
import io.github.drakonkinst.worldsinger.entity.cannonball.WaterCannonballBehavior;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.item.cannonball.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.cannonball.CannonballComponent.CannonballCore;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CannonballEntity extends ThrownItemEntity implements FlyingItemEntity {

    // TODO: Fuse data
    private static final float ENTITY_COLLISION_DAMAGE = 2.0f;
    private static final float PARTICLE_SPEED = 0.25f;
    private static final CannonballBehavior EMPTY_CANNONBALL_BEHAVIOR = new EmptyCannonballBehavior();
    private static final CannonballBehavior WATER_CANNONBALL_BEHAVIOR = new WaterCannonballBehavior();

    private static CannonballBehavior getCannonballBehavior(CannonballComponent component) {
        if (component != null && component.core() == CannonballCore.WATER) {
            return WATER_CANNONBALL_BEHAVIOR;
        }
        return EMPTY_CANNONBALL_BEHAVIOR;
    }

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

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        CannonballComponent cannonballComponent = this.getStack()
                .get(ModDataComponentTypes.CANNONBALL);
        CannonballBehavior behavior = getCannonballBehavior(cannonballComponent);
        World world = this.getWorld();
        Vec3d hitPos = hitResult.getPos();

        if (world.isClient) {
            behavior.onCollisionClient(this, hitPos);
        } else {
            behavior.onCollisionServer(this, hitPos);
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
}
