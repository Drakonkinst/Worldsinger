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

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypeTags;
import io.github.drakonkinst.worldsinger.particle.AbstractSporeDustParticleEffect;
import io.github.drakonkinst.worldsinger.particle.FallingSporeDustParticleEffect;
import io.github.drakonkinst.worldsinger.particle.SporeDustParticleEffect;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

// Creates configurable spore particles that interact with entities.
public final class SporeParticleManager {

    public static final int SPORE_EFFECT_DURATION_TICKS_DEFAULT = 40;

    private static final int FLOATING_ID_OFFSET = 100;
    private static final float CACHED_SIZE_PRECISION = 20.0f;
    private static final Int2ObjectMap<AbstractSporeDustParticleEffect> cachedDustParticleEffects = new Int2ObjectOpenHashMap<>();
    private static final int SPORE_EFFECT_DURATION_TICKS_MIN = 20;
    private static final float MIN_PARTICLE_SIZE = 0.3f;
    private static final float MAX_PARTICLE_SIZE = 10.0f;
    private static final double MIN_RADIUS = 0.1;
    private static final double MAX_RADIUS = 5.0;
    private static final double MIN_HEIGHT = 0.1;
    private static final double MAX_HEIGHT = 5.0;
    private static final double PARTICLE_VISUAL_HEIGHT_PENALTY = 0.0;
    private static final double PARTICLE_VISUAL_RADIUS_PENALTY = 0.5;
    private static final double DISTANCE_MULTIPLIER = 0.5;
    private static final double PARTICLE_SPEED = 0.75;
    private static final double DISPLAY_PARTICLE_SPEED = 0.75;
    private static final Random random = Random.create();

    // Creates a configurable spore particle cloud based on entity size (using width as the metric)
    public static void createRandomSporeParticlesForEntity(ServerWorld world,
            AetherSpores sporeType, Entity entity, double radiusWidthMultiplier, double radiusDev,
            double heightMean, double heightDev, float particleSizeWidthMultiplier,
            int particleCountPerBlock) {
        float particleSize = entity.getWidth() * particleSizeWidthMultiplier;
        double radius = entity.getWidth() * radiusWidthMultiplier;
        SporeParticleManager.createRandomSporeParticles(world, sporeType, entity.getPos(), radius,
                radiusDev, heightMean, heightDev, particleSize, particleCountPerBlock, false);
    }

    // Creates a configurable spore particle cloud with parameters within given ranges
    public static void createRandomSporeParticles(ServerWorld world, AetherSpores sporeType,
            Vec3d pos, double baseRadius, double radiusDev, double baseHeight, double heightDev,
            float particleSize, int particleCountPerBlock, boolean useDistance) {
        double radius = baseRadius;
        double height = baseHeight;

        if (radiusDev > 0.0) {
            radius = baseRadius - radiusDev + random.nextDouble() * (2.0 * radiusDev);
        }
        if (heightDev > 0.0) {
            height = baseHeight - heightDev + random.nextDouble() * (2.0 * heightDev);
        }

        SporeParticleManager.createSporeParticles(world, sporeType, pos.getX(), pos.getY(),
                pos.getZ(), radius, height, particleSize, particleCountPerBlock, useDistance);
    }

    // Create a cloud of spore particles at the given location.
    // Note: minY is the bottom of the particle cloud, not the center, for ease of use.
    public static void createSporeParticles(ServerWorld world, AetherSpores sporeType, double x,
            double minY, double z, double radius, double height, float particleSize,
            int particleCountPerBlock, boolean useDistance) {

        // Clamp values
        particleSize = MathHelper.clamp(particleSize, MIN_PARTICLE_SIZE, MAX_PARTICLE_SIZE);
        height = MathHelper.clamp(height, MIN_HEIGHT, MAX_HEIGHT);
        radius = MathHelper.clamp(radius, MIN_RADIUS, MAX_RADIUS);

        // Keep particle density consistent, but at least particleCountPerBlock
        double volume = 4.0 * radius * radius * height;
        int particleCount = Math.max(particleCountPerBlock,
                MathHelper.ceil(particleCountPerBlock * volume));

        double minX = x - radius;
        double minZ = z - radius;
        double maxX = x + radius;
        double maxY = minY + height;
        double maxZ = z + radius;

        if (SporeParticleManager.shouldConvertToDead(world, minX, minY, minZ, maxX, maxY, maxZ,
                sporeType)) {
            sporeType = DeadSpores.getInstance();
        }

        SporeParticleManager.spawnVisualSporeParticles(world, sporeType, x, minY, z, radius, height,
                particleSize, particleCount);
        SporeParticleManager.damageEntities(world, sporeType, minX, minY, minZ, maxX, maxY, maxZ,
                useDistance);
    }

    // Returns if a spore cloud should be spore-killed or not.
    private static boolean shouldConvertToDead(ServerWorld world, double minX, double minY,
            double minZ, double maxX, double maxY, double maxZ, AetherSpores sporeType) {
        if (sporeType.isDead()) {
            // Already dead, skip checks
            return false;
        }

        // Should be dead if a spore-killing block is nearby
        if (SporeKillingUtil.isSporeKillingBlockNearbyForRange(world, minX, minY, minZ, maxX, maxY,
                maxZ)) {
            return true;
        }

        // Should be dead if a spore-killing entity is nearby
        return SporeKillingUtil.checkNearbyEntitiesForRange(world, minX, minY, minZ, maxX, maxY,
                maxZ);
    }

    // Spawn visual particles on server-side
    private static void spawnVisualSporeParticles(ServerWorld world, AetherSpores sporeType,
            double x, double minY, double z, double radius, double height, float particleSize,
            int count) {
        double deltaY;
        double y;

        // Visual height should be reduced compared to the logical hitbox, so it seems less of a fluke
        // when you are caught by one.
        // Adjust both the center and delta as necessary.
        if (height > PARTICLE_VISUAL_HEIGHT_PENALTY) {
            deltaY = (height - PARTICLE_VISUAL_HEIGHT_PENALTY) * 0.5;
            y = (minY + minY + deltaY) * 0.5;
        } else {
            deltaY = height * 0.5;
            y = minY + deltaY;
        }

        // Radius should also be penalized.
        radius = Math.max(radius - PARTICLE_VISUAL_RADIUS_PENALTY, PARTICLE_VISUAL_RADIUS_PENALTY);

        // Spawn particle
        ParticleEffect particleEffect = SporeParticleManager.getCachedSporeParticleEffect(sporeType,
                particleSize, false);
        world.spawnParticles(particleEffect, x, y, z, count, radius, deltaY, radius,
                SporeParticleManager.PARTICLE_SPEED);
    }

    // Apply spore effect to entities within a zone
    private static void damageEntities(ServerWorld world, AetherSpores sporeType, double minX,
            double minY, double minZ, double maxX, double maxY, double maxZ, boolean useDistance) {
        Box box = new Box(minX, minY, minZ, maxX, maxY, maxZ);
        SporeParticleManager.damageEntitiesInBox(world, sporeType, box, useDistance);
    }

    private static AbstractSporeDustParticleEffect getCachedSporeParticleEffect(
            AetherSpores sporeType, float size, boolean floating) {
        // Only cache particle effect if from the AetherSporeType enum
        int id = sporeType.getId();
        if (floating) {
            id += FLOATING_ID_OFFSET;
        }
        int key = SporeParticleManager.hashTwoInts(id,
                (int) Math.floor(size * CACHED_SIZE_PRECISION));
        return cachedDustParticleEffects.computeIfAbsent(key,
                k -> SporeParticleManager.createDustParticleEffect(sporeType, size, floating));
    }

    // Apply spore effect to entities within a given box
    public static void damageEntitiesInBox(World world, AetherSpores sporeType, Box box,
            boolean useDistance) {
        if (sporeType.isDead()) {
            // Dead spores cannot damage anything
            return;
        }

        // Retrieve status effect
        RegistryEntry<StatusEffect> statusEffect = sporeType.getStatusEffect();
        if (statusEffect == null) {
            Worldsinger.LOGGER.error(
                    "SporeType does not have associated status effect: " + sporeType.getId());
            return;
        }

        // Gather all candidate entities
        Vec3d centerPos = box.getCenter();
        List<LivingEntity> entitiesInRange = world.getEntitiesByClass(LivingEntity.class, box,
                entity -> AetherSpores.sporesCanAffect(entity) && (
                        entity.getType().isIn(ModEntityTypeTags.SPORES_ALWAYS_AFFECT)
                                || box.contains(entity.getEyePos())));

        // Deal damage
        for (LivingEntity entity : entitiesInRange) {
            // Duration is constant or linearly decreasing based on distance
            int duration = SPORE_EFFECT_DURATION_TICKS_DEFAULT;
            if (useDistance) {
                double distance = centerPos.distanceTo(entity.getPos());
                duration = (int) Math.max(SPORE_EFFECT_DURATION_TICKS_MIN,
                        duration - distance * DISTANCE_MULTIPLIER);
            }
            // Apply the effect which deals damage
            SporeParticleManager.applySporeEffect(entity, statusEffect, duration);
        }
    }

    // https://stackoverflow.com/a/13871379
    private static int hashTwoInts(int a, int b) {
        return (a + b) * (a + b + 1) / 2 + a;
    }

    // Generate particle effect with given color and size
    private static AbstractSporeDustParticleEffect createDustParticleEffect(AetherSpores sporeType,
            float size, boolean floating) {
        // Only cache particle effect if from the AetherSporeType enum
        // Lock size to nearest cached size precision to prevent unintentional imprecision
        size = ((int) (size * CACHED_SIZE_PRECISION)) / CACHED_SIZE_PRECISION;
        Worldsinger.LOGGER.info(
                "Caching new dust particle effect (" + sporeType.getId() + ", " + size + "), " + (
                        cachedDustParticleEffects.size() + 1) + " particles cached");

        // Create particle effect
        if (floating) {
            return new SporeDustParticleEffect(sporeType, size);
        }
        return new FallingSporeDustParticleEffect(sporeType, size);
    }

    // Apply spore status effect to the entity for the duration
    public static void applySporeEffect(LivingEntity entity,
            RegistryEntry<StatusEffect> statusEffect, int duration) {
        entity.addStatusEffect(new StatusEffectInstance(statusEffect, duration, 0, true, true));
    }

    // Spawns client-side only particles in a single block that have no effect.
    public static void addClientDisplayParticle(World world, AetherSpores sporeType, double x,
            double y, double z, float particleSize, boolean floating, Random random) {
        if (sporeType == null) {
            return;
        }

        if (!sporeType.isDead() && (
                SporeKillingUtil.isSporeKillingBlockNearby(world, BlockPos.ofFloored(x, y, z))
                        || SporeKillingUtil.checkNearbyEntities(world, new Vec3d(x, y, z)))) {
            sporeType = DeadSpores.getInstance();
        }

        ParticleEffect particleEffect = SporeParticleManager.getCachedSporeParticleEffect(sporeType,
                particleSize, floating);
        double velocityX = random.nextDouble() * DISPLAY_PARTICLE_SPEED;
        double velocityY = random.nextDouble() * DISPLAY_PARTICLE_SPEED;
        double velocityZ = random.nextDouble() * DISPLAY_PARTICLE_SPEED;
        world.addParticle(particleEffect, x, y, z, velocityX, velocityY, velocityZ);
    }

    // Apply spore effect to entities within a specific block
    public static void damageEntitiesInBlock(World world, AetherSpores sporeType, BlockPos pos) {
        Box box = new Box(pos);
        SporeParticleManager.damageEntitiesInBox(world, sporeType, box, false);
    }

    private SporeParticleManager() {}
}
