package io.github.drakonkinst.worldsinger.world.lumar;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

// Spawns various kinds of spore particles
public final class SporeParticleSpawner {

    // TODO: Should tune these values later
    // Splashing when landing on block or in a fluid
    private static final double SPLASH_RADIUS_MULTIPLIER = 0.75;
    private static final double SPLASH_HEIGHT_WIDTH_MULTIPLIER = 5.0 / 3.0;
    private static final double SPLASH_HEIGHT_PER_BLOCK_WALKING = 0.05;
    private static final double SPLASH_HEIGHT_PER_BLOCK_SNEAKING = 0.025;
    private static final double SPLASH_HEIGHT_PER_BLOCK_SPRINTING = 0.1;
    private static final double SPLASH_HEIGHT_DEV = 0.75;
    private static final float SPLASH_PARTICLE_SIZE_MULTIPLIER = 5.0f / 3.0f;
    private static final int SPLASH_PARTICLE_COUNT_FLUID = 5;
    private static final int SPLASH_PARTICLE_COUNT_BLOCK = 10;

    // Footsteps when walking or sprinting on block
    // Walking on spores should never kill the player, but act as a warning
    // Sprinting on spores is dangerous
    private static final double FOOTSTEP_RADIUS_MULTIPLIER = 0.5;
    private static final double FOOTSTEP_HEIGHT_SPRINTING = 1.0;
    private static final double FOOTSTEP_HEIGHT_WALKING = 0.5;
    private static final double FOOTSTEP_HEIGHT_DEV = 1.0;
    private static final float FOOTSTEP_PARTICLE_SIZE_MULTIPLIER = 5.0f / 3.0f;
    private static final int FOOTSTEP_PARTICLE_COUNT = 5;
    private static final int FOOTSTEP_PARTICLE_COUNT_SPRINTING_MULTIPLIER = 2;

    // Projectiles hitting a block
    // Radius and height are consistent across all projectiles
    private static final double PROJECTILE_RADIUS = 0.25;
    private static final double PROJECTILE_HEIGHT = 0.5;
    private static final float PROJECTILE_PARTICLE_SIZE = 0.75f;
    private static final int PROJECTILE_PARTICLE_COUNT = 3;

    // Oars rowing
    private static final double ROWING_RADIUS = 1.0;
    private static final double ROWING_RADIUS_DEV = 0.25;
    private static final double ROWING_HEIGHT = 1.25;
    private static final double ROWING_HEIGHT_DEV = 0.35;
    private static final float ROWING_PARTICLE_SIZE = 1.0f;
    private static final int ROWING_PARTICLE_COUNT = 3;

    private static final double BLOCK_HALF_WIDTH = 0.5;
    private static final float BLOCK_PARTICLE_SIZE = 1.0f;
    private static final int BLOCK_PARTICLE_COUNT = 5;

    public static void spawnSplashParticles(ServerWorld world,
            AetherSporeType aetherSporeType, Entity entity, float fallDistance, boolean fluid) {
        double multiplier = entity.isSneaking() ? SPLASH_HEIGHT_PER_BLOCK_SNEAKING
                : entity.isSprinting() ? SPLASH_HEIGHT_PER_BLOCK_SPRINTING
                        : SPLASH_HEIGHT_PER_BLOCK_WALKING;
        double height =
                SPLASH_HEIGHT_WIDTH_MULTIPLIER * entity.getWidth() + fallDistance * multiplier;
        int particleCount = fluid ? SPLASH_PARTICLE_COUNT_FLUID : SPLASH_PARTICLE_COUNT_BLOCK;

        SporeParticleManager.createRandomSporeParticlesForEntity(world, aetherSporeType, entity,
                SPLASH_RADIUS_MULTIPLIER, 0.0, height, SPLASH_HEIGHT_DEV,
                SPLASH_PARTICLE_SIZE_MULTIPLIER, particleCount);
    }

    public static void spawnFootstepParticles(ServerWorld world, AetherSporeType sporeType,
            Entity entity) {
        double height;
        int particleCount = FOOTSTEP_PARTICLE_COUNT;
        if (entity.isSprinting()) {
            particleCount *= FOOTSTEP_PARTICLE_COUNT_SPRINTING_MULTIPLIER;
            height = FOOTSTEP_HEIGHT_SPRINTING;
        } else {
            height = FOOTSTEP_HEIGHT_WALKING;
        }
        SporeParticleManager.createRandomSporeParticlesForEntity(world, sporeType, entity,
                FOOTSTEP_RADIUS_MULTIPLIER, 0.0, height, FOOTSTEP_HEIGHT_DEV,
                FOOTSTEP_PARTICLE_SIZE_MULTIPLIER, particleCount);
    }

    public static void spawnProjectileParticles(ServerWorld world, AetherSporeType sporeType,
            Vec3d pos) {
        SporeParticleManager.createRandomSporeParticles(world, sporeType, pos,
                PROJECTILE_RADIUS, 0.0, PROJECTILE_HEIGHT,
                0.0, PROJECTILE_PARTICLE_SIZE, PROJECTILE_PARTICLE_COUNT);
    }

    public static void spawnRowingParticles(ServerWorld world, AetherSporeType sporeType,
            Vec3d pos) {
        SporeParticleManager.createRandomSporeParticles(world, sporeType, pos, ROWING_RADIUS,
                ROWING_RADIUS_DEV, ROWING_HEIGHT, ROWING_HEIGHT_DEV, ROWING_PARTICLE_SIZE,
                ROWING_PARTICLE_COUNT);
    }

    public static void spawnBlockParticles(ServerWorld world, AetherSporeType sporeType,
            BlockPos blockPos, double blockRadius, double heightMultiplier) {
        double x = blockPos.getX() + BLOCK_HALF_WIDTH;
        double y = blockPos.getY();
        double z = blockPos.getZ() + BLOCK_HALF_WIDTH;
        SporeParticleManager.createRandomSporeParticles(world, sporeType, new Vec3d(x, y, z),
                blockRadius, 0.0, blockRadius * 2.0 * heightMultiplier, 0.0, BLOCK_PARTICLE_SIZE,
                BLOCK_PARTICLE_COUNT);
    }

    private SporeParticleSpawner() {}
}