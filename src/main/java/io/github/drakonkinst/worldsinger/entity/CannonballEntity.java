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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.entity.cannonball.CannonballBehavior;
import io.github.drakonkinst.worldsinger.entity.cannonball.EmptyCannonballBehavior;
import io.github.drakonkinst.worldsinger.entity.cannonball.HollowSporeCannonballBehavior;
import io.github.drakonkinst.worldsinger.entity.cannonball.RoseiteSporeCannonballBehavior;
import io.github.drakonkinst.worldsinger.entity.cannonball.WaterCannonballBehavior;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballContent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballCore;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.concurrent.TimeUnit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class CannonballEntity extends ThrownItemEntity implements FlyingItemEntity {

    private static final TrackedData<Integer> FUSE = DataTracker.registerData(
            CannonballEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final String FUSE_NBT_KEY = "fuse";
    public static final int FUSE_DELAY = 20;

    private static final float ENTITY_COLLISION_DAMAGE = 2.0f;
    private static final float PARTICLE_SPEED = 0.25f;
    private static final CannonballBehavior EMPTY_CANNONBALL_BEHAVIOR = new EmptyCannonballBehavior();
    private static final CannonballBehavior WATER_CANNONBALL_BEHAVIOR = new WaterCannonballBehavior();
    private static final LoadingCache<CannonballComponent, CannonballBehavior> CACHED_CANNONBALL_BEHAVIORS = CacheBuilder.newBuilder()
            // .recordStats() // TODO: Turn off for release build
            .maximumSize(12) // 12 Aethers :P no other reason
            .expireAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<>() {
                @Override
                public @NotNull CannonballBehavior load(@NotNull CannonballComponent component) {
                    Object2IntMap<CannonballContent> contentMap = CannonballComponent.getContentMap(
                            component);
                    if (component.core() == CannonballCore.ROSEITE) {
                        return new RoseiteSporeCannonballBehavior(contentMap);
                    } else if (component.core() == CannonballCore.HOLLOW) {
                        return new HollowSporeCannonballBehavior(contentMap);
                    } else {
                        Worldsinger.LOGGER.warn("Invalid component detected in cache {}",
                                component);
                        return EMPTY_CANNONBALL_BEHAVIOR;
                    }
                }
            });

    private static CannonballBehavior getCannonballBehavior(CannonballComponent component) {
        if (component != null) {
            if (component.core() == CannonballCore.WATER) {
                return WATER_CANNONBALL_BEHAVIOR;
            } else if (component.core() == CannonballCore.HOLLOW
                    || component.core() == CannonballCore.ROSEITE) {
                // TODO: Turn off for release build
                // Worldsinger.LOGGER.info(CACHED_CANNONBALL_BEHAVIORS.stats().toString());
                return CACHED_CANNONBALL_BEHAVIORS.getUnchecked(component);
            }
        }
        return EMPTY_CANNONBALL_BEHAVIOR;
    }

    public CannonballEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public CannonballEntity(World world, LivingEntity owner, ItemStack stack) {
        super(ModEntityTypes.CANNONBALL, owner, world, stack);
    }

    public CannonballEntity(World world, double x, double y, double z, ItemStack stack) {
        super(ModEntityTypes.CANNONBALL, x, y, z, world, stack);
    }

    @Override
    protected void initDataTracker(Builder builder) {
        super.initDataTracker(builder);
        builder.add(FUSE, 0);
    }

    public void setFuse(int fuse) {
        this.dataTracker.set(FUSE, fuse);
    }

    public int getFuse() {
        return this.dataTracker.get(FUSE);
    }

    private boolean hasFuseExpired(int fuseTime) {
        CannonballComponent cannonballComponent = this.getStack()
                .get(ModDataComponentTypes.CANNONBALL);
        return cannonballComponent != null && cannonballComponent.core().canHaveFuse()
                && cannonballComponent.fuse() > 0
                && fuseTime >= cannonballComponent.fuse() * FUSE_DELAY;
    }

    @Override
    public void tick() {
        super.tick();
        int nextFuse = this.getFuse() + 1;
        this.setFuse(nextFuse);
        if (hasFuseExpired(nextFuse)) {
            explode(this.getPos());
        }
    }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putShort(FUSE_NBT_KEY, (short) this.getFuse());
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setFuse(view.getShort(FUSE_NBT_KEY, (short) 0));
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
                        .addParticleClient(particleEffect, this.getX(), this.getY(), this.getZ(),
                                velocityX, velocityY, velocityZ);
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        if (entity.getWorld() instanceof ServerWorld serverWorld) {
            entity.damage(serverWorld, this.getDamageSources().thrown(this, this.getOwner()),
                    ENTITY_COLLISION_DAMAGE);
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        Vec3d hitPos = hitResult.getPos();
        explode(hitPos);
    }

    private void explode(Vec3d hitPos) {
        World world = this.getWorld();
        CannonballComponent cannonballComponent = this.getStack()
                .get(ModDataComponentTypes.CANNONBALL);
        CannonballBehavior behavior = getCannonballBehavior(cannonballComponent);

        if (world.isClient) {
            behavior.onCollisionClient(this, hitPos);
            world.playSoundClient(hitPos.getX(), hitPos.getY(), hitPos.getZ(),
                    ModSoundEvents.ENTITY_CANNONBALL_BREAK, SoundCategory.PLAYERS, 1.0f,
                    random.nextFloat() * 0.1f + 1.25f, true);
        } else {
            behavior.onCollisionServer(this, hitPos);
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
