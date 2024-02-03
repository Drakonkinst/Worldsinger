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
package io.github.drakonkinst.worldsinger.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.cosmere.ShapeshiftingManager;
import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypeTags;
import io.github.drakonkinst.worldsinger.particle.ModParticleTypes;
import io.github.drakonkinst.worldsinger.util.EntityUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

public class MidnightCreatureImitation<E extends MidnightCreatureEntity> extends
        ExtendedBehaviour<E> {

    private static final List<Pair<MemoryModuleType<?>, MemoryModuleState>> MEMORY_REQUIREMENTS = ObjectArrayList.of(
            Pair.of(MemoryModuleType.MOBS, MemoryModuleState.VALUE_PRESENT),
            Pair.of(SBLMemoryTypes.NEARBY_BLOCKS.get(), MemoryModuleState.REGISTERED));
    private static final int IMITATION_ATTEMPT_INTERVAL = 20;
    private static final double LOOK_NEAR_THRESHOLD = 1.0 - 0.025;
    private static final int ABSORB_PARTICLE_COUNT = 10;
    private static final double ABSORB_PARTICLE_VELOCITY = 0.1f;

    private int imitationAttemptTicks = 0;
    private int imitationAttemptsRemaining = 6;
    private boolean shouldRecalculateAbsorbables = true;
    private final List<MidnightCreatureEntity> nearbyAbsorbableEntities = new ArrayList<>();
    private final List<BlockPos> nearbyAbsorbableBlocks = new ArrayList<>();
    private int absorbableAmount = 0;

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryModuleState>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean shouldRun(ServerWorld level, E entity) {
        return entity.getMorph() == null;
    }

    @Override
    protected void tick(E entity) {
        ++imitationAttemptTicks;
        if (imitationAttemptTicks >= IMITATION_ATTEMPT_INTERVAL) {
            imitationAttemptTicks = 0;
            BrainUtils.withMemory(entity, MemoryModuleType.MOBS, nearbyEntities -> {
                List<Pair<BlockPos, BlockState>> nearbyBlocks = BrainUtils.memoryOrDefault(entity,
                        SBLMemoryTypes.NEARBY_BLOCKS.get(), Collections::emptyList);
                attemptImitation(entity, nearbyEntities, nearbyBlocks);
            });
        }
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return entity.getMorph() == null;
    }

    // Midnight Essence required directly ties to their bounding box, rounded to the nearest block
    private int getMidnightEssenceRequired(LivingEntity entity) {
        return EntityUtil.getBlocksInBoundingBox(entity);
    }

    private void calculateNearbyAbsorbables(@Nullable PlayerEntity controller,
            List<LivingEntity> nearbyEntities, List<Pair<BlockPos, BlockState>> nearbyBlocks) {
        if (!shouldRecalculateAbsorbables) {
            return;
        }

        nearbyAbsorbableEntities.clear();
        nearbyAbsorbableBlocks.clear();
        absorbableAmount = nearbyBlocks.size();
        for (LivingEntity entity : nearbyEntities) {
            if (entity instanceof MidnightCreatureEntity midnightCreatureEntity && (
                    controller == null
                            || midnightCreatureEntity.getControllerUuid() == controller.getUuid())
                    && midnightCreatureEntity.getMorph() == null && !entity.isRemoved()) {
                nearbyAbsorbableEntities.add(midnightCreatureEntity);
                absorbableAmount += midnightCreatureEntity.getMidnightEssenceAmount();
            }
        }
        for (Pair<BlockPos, BlockState> pair : nearbyBlocks) {
            nearbyAbsorbableBlocks.add(pair.getFirst());
        }
        shouldRecalculateAbsorbables = false;
    }

    // Get midnightCreature nearest to the look direction of the player.
    @Nullable
    private LivingEntity getPlayerMorphTarget(PlayerEntity controller,
            List<LivingEntity> nearbyEntities) {
        // See if the player is looking at a mob for the midnightCreature to imitate
        // Uses logic similar to an Enderman
        Vec3d lookDir = EntityUtil.getLookRotationVector(controller).normalize();
        Vec3d controllerPos = controller.getEyePos();
        LivingEntity nearestEntity = null;
        double nearestEntityDistanceSq = Double.MAX_VALUE;

        for (LivingEntity entity : nearbyEntities) {
            if (entity.getType().isIn(ModEntityTypeTags.MIDNIGHT_CREATURES_CANNOT_IMITATE)) {
                continue;
            }
            Vec3d entityPos = EntityUtil.getCenterPos(entity);
            Vec3d delta = entityPos.subtract(controllerPos);
            double distanceToEntity = delta.length();
            double dotProduct = lookDir.dotProduct(delta.normalize());
            if (dotProduct > LOOK_NEAR_THRESHOLD / distanceToEntity) {
                // Can consider as a candidate
                double distSqFromLine = controllerPos.crossProduct(delta).lengthSquared();
                if (distSqFromLine < nearestEntityDistanceSq) {
                    nearestEntity = entity;
                    nearestEntityDistanceSq = distSqFromLine;
                }
            }
        }
        return nearestEntity;
    }

    @Nullable
    public LivingEntity getNearestMorphTarget(E midnightCreature, List<LivingEntity> nearbyEntities,
            int midnightEssenceAmount) {
        LivingEntity nearestEntity = null;
        double nearestEntityDistanceSq = Double.MAX_VALUE;
        Vec3d pos = midnightCreature.getPos();
        for (LivingEntity entity : nearbyEntities) {
            if (entity.getType().isIn(ModEntityTypeTags.MIDNIGHT_CREATURES_CANNOT_IMITATE)) {
                continue;
            }
            double distSq = entity.getPos().squaredDistanceTo(pos);
            if (distSq < nearestEntityDistanceSq) {
                int essenceRequired = getMidnightEssenceRequired(entity);
                if (essenceRequired <= midnightEssenceAmount) {
                    nearestEntity = entity;
                    nearestEntityDistanceSq = distSq;
                }
            }
        }
        return nearestEntity;
    }

    private void attemptImitation(E midnightCreature, List<LivingEntity> nearbyEntities,
            List<Pair<BlockPos, BlockState>> nearbyBlocks) {
        PlayerEntity controller = midnightCreature.getController();
        shouldRecalculateAbsorbables = true;

        if (controller != null) {
            attemptTransformIntoPlayerTarget(midnightCreature, controller, nearbyEntities,
                    nearbyBlocks);
            if (midnightCreature.getMorph() != null) {
                return;
            }
        }

        if (imitationAttemptsRemaining > 0) {
            --imitationAttemptsRemaining;
        }

        if (imitationAttemptsRemaining <= 0) {
            // Just imitate nearest that it can imitate
            attemptTransformIntoNearest(midnightCreature, nearbyEntities, nearbyBlocks);
        }
    }

    private void attemptTransformIntoPlayerTarget(E midnightCreature, PlayerEntity controller,
            List<LivingEntity> nearbyEntities, List<Pair<BlockPos, BlockState>> nearbyBlocks) {
        LivingEntity nearestEntity = getPlayerMorphTarget(controller, nearbyEntities);
        if (nearestEntity != null) {
            int essenceRequired = getMidnightEssenceRequired(nearestEntity);
            int currentAmount = midnightCreature.getMidnightEssenceAmount();

            // If not enough essence, attempt to absorb more from the environment
            if (essenceRequired > currentAmount) {
                calculateNearbyAbsorbables(controller, nearbyEntities, nearbyBlocks);
                int needed = essenceRequired - currentAmount;
                if (absorbableAmount >= needed && absorbUpTo(midnightCreature, needed)) {
                    currentAmount = midnightCreature.getMidnightEssenceAmount();
                }
            }

            if (currentAmount >= essenceRequired) {
                // Transform!
                ShapeshiftingManager.createMorphFromEntity(midnightCreature, nearestEntity, true);
            }
        }
    }

    private void attemptTransformIntoNearest(E midnightCreature, List<LivingEntity> nearbyEntities,
            List<Pair<BlockPos, BlockState>> nearbyBlocks) {
        calculateNearbyAbsorbables(null, nearbyEntities, nearbyBlocks);
        int currentAmount = midnightCreature.getMidnightEssenceAmount();
        LivingEntity nearestEntity = getNearestMorphTarget(midnightCreature, nearbyEntities,
                currentAmount + absorbableAmount);
        if (nearestEntity == null) {
            return;
        }
        int essenceRequired = getMidnightEssenceRequired(nearestEntity);
        if (absorbUpTo(midnightCreature, essenceRequired - currentAmount)) {
            currentAmount = midnightCreature.getMidnightEssenceAmount();
        }
        if (currentAmount >= essenceRequired) {
            ShapeshiftingManager.createMorphFromEntity(midnightCreature, nearestEntity, true);
        }
    }

    // Returns true if anything was absorbed, and the midnight essence maxAmount was changed
    private boolean absorbUpTo(E midnightCreature, int maxAmount) {
        if (maxAmount <= 0 || !(midnightCreature.getWorld() instanceof ServerWorld world)) {
            return false;
        }

        int currentAmount = midnightCreature.getMidnightEssenceAmount();
        int absorbed = 0;
        // Absorb blocks first, then entities.
        for (BlockPos pos : nearbyAbsorbableBlocks) {
            if (world.getBlockState(pos).isOf(ModBlocks.MIDNIGHT_ESSENCE)) {
                world.removeBlock(pos, false);
                absorbed += 1;
                Vec3d centerPos = pos.toCenterPos();
                world.spawnParticles(ModParticleTypes.MIDNIGHT_ESSENCE, centerPos.getX(),
                        centerPos.getY(), centerPos.getZ(), ABSORB_PARTICLE_COUNT, 0.5, 0.5, 0.5,
                        ABSORB_PARTICLE_VELOCITY);
            }
            if (absorbed >= maxAmount) {
                midnightCreature.setMidnightEssenceAmount(currentAmount + absorbed);
                return true;
            }
        }

        for (MidnightCreatureEntity absorbable : nearbyAbsorbableEntities) {
            absorbed += absorbable.getMidnightEssenceAmount();
            Vec3d centerPos = EntityUtil.getCenterPos(absorbable);
            world.spawnParticles(ModParticleTypes.MIDNIGHT_ESSENCE, centerPos.getX(),
                    centerPos.getY(), centerPos.getZ(), ABSORB_PARTICLE_COUNT, 0.5, 0.5, 0.5,
                    ABSORB_PARTICLE_VELOCITY);
            absorbable.discard();
            if (absorbed >= maxAmount) {
                midnightCreature.setMidnightEssenceAmount(currentAmount + absorbed);
                return true;
            }
        }
        midnightCreature.setMidnightEssenceAmount(currentAmount + absorbed);
        return false;
    }
}
