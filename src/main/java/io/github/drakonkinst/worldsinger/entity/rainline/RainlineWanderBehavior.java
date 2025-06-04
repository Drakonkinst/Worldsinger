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

package io.github.drakonkinst.worldsinger.entity.rainline;

import com.mojang.serialization.Codec;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarLunagreeGenerator;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManagerAccess;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeLocation;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator.SporeSeaEntry;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.joml.Vector2d;

public class RainlineWanderBehavior implements RainlineBehavior {

    // Steering behaviors
    private static final float SPEED_BLOCKS_PER_TICK = 0.5f;
    private static final float WANDER_CIRCLE_RADIUS = 20.0f;
    private static final float WANDER_CIRCLE_DISTANCE = 20.0f;
    private static final float WANDER_ANGLE_CHANGE = 0.2f;
    private static final float STEERING_SCALE = 1.0f;
    private static final float AVOIDANCE_FORCE = 50.0f;
    private static final float SEEK_FORCE = 50.0f;
    private static final int SEEK_DISTANCE = 16;
    private static final float DESPAWN_DISTANCE_THRESHOLD = 128.0f;
    private static final int MAX_DESPAWN_TIMER = 6000;

    private static final String KEY_WANDER_ANGLE = "wander_angle";
    private static final String KEY_DESPAWN_TIMER = "despawn_timer";

    public static RainlineWanderBehavior readCustomData(ReadView view, Random random) {
        Optional<Float> wanderAngle = view.read(KEY_WANDER_ANGLE, Codec.FLOAT);
        if (wanderAngle.isPresent()) {
            int despawnTimer = view.getInt(KEY_DESPAWN_TIMER, 0);
            return new RainlineWanderBehavior(wanderAngle.get(), despawnTimer);
        }
        return new RainlineWanderBehavior(random);
    }

    private final Vector2d steeringForce = new Vector2d();
    private float wanderAngle;
    private int despawnTimer;

    public RainlineWanderBehavior(Random random) {
        this.wanderAngle = random.nextFloat() * MathHelper.TAU;
        this.despawnTimer = 0;
    }

    private RainlineWanderBehavior(float startingWanderAngle, int despawnTimer) {
        this.wanderAngle = startingWanderAngle;
        this.despawnTimer = despawnTimer;
    }

    @Override
    public void serverTick(ServerWorld world, RainlineEntity entity) {
        BlockPos pos = entity.getBlockPos();
        Vec3d velocity = entity.getVelocity();
        LumarManager lumarManager = ((LumarManagerAccess) world).worldsinger$getLumarManager();

        // Tick despawn timer
        if (!isPlayerNearby(world, entity)) {
            if (++despawnTimer >= MAX_DESPAWN_TIMER) {
                Worldsinger.LOGGER.info("Despawned wandering rainline");
                entity.discard();
            }
        }

        // Wander randomly
        if (velocity.equals(Vec3d.ZERO)) {
            // If no velocity, go in a random direction
            float randomAngle = entity.getRandom().nextFloat() * MathHelper.TAU;
            float velocityX = MathHelper.cos(randomAngle) * SPEED_BLOCKS_PER_TICK;
            float velocityZ = MathHelper.sin(randomAngle) * SPEED_BLOCKS_PER_TICK;
            wanderAngle = (float) MathHelper.atan2(velocityZ, velocityX);
            steeringForce.set(velocityX, velocityZ);
        } else {
            // If current velocity, use wander steering behavior
            steeringForce.set(velocity.getX(), velocity.getZ())
                    .normalize()
                    .mul(WANDER_CIRCLE_DISTANCE);
            float displacementX = MathHelper.cos(wanderAngle) * WANDER_CIRCLE_RADIUS;
            float displacementZ = MathHelper.sin(wanderAngle) * WANDER_CIRCLE_RADIUS;
            steeringForce.add(displacementX, displacementZ).mul(STEERING_SCALE);
            wanderAngle += entity.getRandom().nextFloat() * WANDER_ANGLE_CHANGE * 2.0f
                    - WANDER_ANGLE_CHANGE;
            if (wanderAngle < 0) {
                wanderAngle += MathHelper.TAU;
            }
            if (wanderAngle > MathHelper.TAU) {
                wanderAngle -= MathHelper.TAU;
            }
        }

        // Avoid lunagrees
        LunagreeLocation nearbyLunagree = lumarManager.getLunagreeGenerator()
                .getNearestLunagree(world, pos.getX(), pos.getZ(),
                        LumarLunagreeGenerator.SPORE_FALL_RADIUS * 2);
        if (nearbyLunagree != null) {
            Vector2d avoidForce = new Vector2d(pos.getX() - nearbyLunagree.blockX(),
                    pos.getZ() - nearbyLunagree.blockZ());
            avoidForce.normalize(AVOIDANCE_FORCE);
            steeringForce.add(avoidForce);
        }

        // Avoid other spore seas
        // Pick 8 points in a grid. Create a pulling force to all points that are in the Crimson Sea
        // If there are no points in the Crimson Sea, there is no effect; same if all points are in the Crimson Sea
        NoiseConfig noiseConfig = world.getChunkManager().getNoiseConfig();
        for (int xOffset = -1; xOffset <= 1; ++xOffset) {
            for (int zOffset = -1; zOffset <= 1; ++zOffset) {
                if (xOffset == 0 && zOffset == 0) {
                    continue;
                }
                int x = pos.getX() + xOffset * SEEK_DISTANCE;
                int z = pos.getZ() + zOffset * SEEK_DISTANCE;
                SporeSeaEntry entry = LumarChunkGenerator.getSporeSeaEntryAtPos(noiseConfig, x, z);
                if (!AetherSpores.hasRainlinePathsInSea(entry.id())) {
                    steeringForce.add(
                            new Vector2d(x - pos.getX(), z - pos.getZ()).normalize(SEEK_FORCE));
                }
            }
        }

        Vec3d newVelocity = entity.getVelocity()
                .add(steeringForce.x(), 0.0, steeringForce.y()) // Apply steering force
                .multiply(1.0, 0.0, 1.0) // Zero out the y-velocity
                .normalize()
                .multiply(SPEED_BLOCKS_PER_TICK);
        Vec3d newPos = entity.getPos().add(newVelocity);
        entity.setVelocity(newVelocity);
        entity.setPos(newPos.getX(), RainlineEntity.getTargetHeight(world), newPos.getZ());
    }

    private boolean isPlayerNearby(ServerWorld world, RainlineEntity entity) {
        for (PlayerEntity playerEntity : world.getPlayers()) {
            double deltaX = playerEntity.getX() - entity.getX();
            double deltaZ = playerEntity.getZ() - entity.getZ();
            double distSq = deltaX * deltaX + deltaZ * deltaZ;
            if (distSq <= DESPAWN_DISTANCE_THRESHOLD * DESPAWN_DISTANCE_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFollowingPath() {
        return false;
    }

    @Override
    public void writeCustomData(WriteView view) {
        view.putFloat(KEY_WANDER_ANGLE, wanderAngle);
        view.putInt(KEY_DESPAWN_TIMER, despawnTimer);
    }
}
