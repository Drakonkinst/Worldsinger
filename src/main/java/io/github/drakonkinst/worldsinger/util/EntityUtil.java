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
package io.github.drakonkinst.worldsinger.util;

import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.mixin.accessor.EntityAccessor;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public final class EntityUtil {

    private static final float MIN_ROTATION = -180.0f;
    private static final float MAX_ROTATION = 180.0f;
    private static final float FULL_DEGREE = 360.0f;

    public static boolean isTouchingSporeSea(Entity entity) {
        return EntityUtil.isTouchingFluid(entity, ModFluidTags.AETHER_SPORES);
    }

    public static boolean isTouchingFluid(Entity entity, TagKey<Fluid> fluidTag) {
        return EntityUtil.notFirstUpdate(entity) && entity.getFluidHeight(fluidTag) > 0.0;
    }

    private static boolean notFirstUpdate(Entity entity) {
        return !((EntityAccessor) entity).worldsinger$isFirstUpdate();
    }

    public static boolean isSubmergedInSporeSea(Entity entity) {
        return EntityUtil.isSubmergedInFluid(entity, ModFluidTags.AETHER_SPORES);
    }

    // Used as a metric of how big an entity is, used for a variety of size-based
    // calculations. Volume is not a great value here, as it is cubic.
    public static float getSize(Entity entity) {
        return entity.getWidth() * entity.getHeight();
    }

    public static boolean isSubmergedInFluid(Entity entity, TagKey<Fluid> fluidTag) {
        return EntityUtil.notFirstUpdate(entity) && entity.isSubmergedIn(fluidTag);
    }

    @NotNull
    public static EntityAttributeInstance getRequiredAttributeInstance(LivingEntity entity,
            RegistryEntry<EntityAttribute> attribute) {
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        Objects.requireNonNull(instance);
        return instance;
    }

    public static Vec3d getRotationVector(Entity entity) {
        return EntityUtil.getRotationVector(entity.getPitch(), entity.getYaw());
    }

    public static Vec3d getLookRotationVector(Entity entity) {
        return EntityUtil.getRotationVector(entity.getPitch(), entity.getHeadYaw());
    }

    public static Vec3d getRotationVector(float pitch, float yaw) {
        float pitchRadians = pitch * MathHelper.RADIANS_PER_DEGREE;
        float yawRadians = -yaw * MathHelper.RADIANS_PER_DEGREE;
        float cosPitch = MathHelper.cos(pitchRadians);
        float sinPitch = MathHelper.sin(pitchRadians);
        float cosYaw = MathHelper.cos(yawRadians);
        float sinYaw = MathHelper.sin(yawRadians);
        return new Vec3d(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
    }

    public static Vec3d getCenterPos(Entity entity) {
        return new Vec3d(entity.getX(), entity.getY() + entity.getHeight() * 0.5, entity.getZ());
    }

    // Gets the number of fully open blocks needed to fit this entity, rounded up
    public static int getBlocksInBoundingBox(Entity entity) {
        int width = MathHelper.ceil(entity.getWidth());
        int height = MathHelper.ceil(entity.getHeight());
        return width * width * height;
    }

    // On server-side, entity yaw and pitch are typically wrapped to [-180, 180) which reflects
    // in their NBT. On client-side, they are NOT wrapped for better interpolation. This code
    // runs in a LivingEntity's tick() method to make the "previous" values get as close as possible
    // to the unwrapped yaw and pitch so no weird snapping and jitter occurs. Sometimes, we need to
    // update it faster than a tick on the rendering side, so we use this method here.
    public static void fixYawAndPitch(LivingEntity entity) {
        while (entity.getYaw() - entity.prevYaw < MIN_ROTATION) {
            entity.prevYaw -= FULL_DEGREE;
        }

        while (entity.getYaw() - entity.prevYaw >= MAX_ROTATION) {
            entity.prevYaw += FULL_DEGREE;
        }

        while (entity.bodyYaw - entity.prevBodyYaw < MIN_ROTATION) {
            entity.prevBodyYaw -= FULL_DEGREE;
        }

        while (entity.bodyYaw - entity.prevBodyYaw >= MAX_ROTATION) {
            entity.prevBodyYaw += FULL_DEGREE;
        }

        while (entity.getPitch() - entity.prevPitch < MIN_ROTATION) {
            entity.prevPitch -= FULL_DEGREE;
        }

        while (entity.getPitch() - entity.prevPitch >= MAX_ROTATION) {
            entity.prevPitch += FULL_DEGREE;
        }

        while (entity.headYaw - entity.prevHeadYaw < MIN_ROTATION) {
            entity.prevHeadYaw -= FULL_DEGREE;
        }

        while (entity.headYaw - entity.prevHeadYaw >= MAX_ROTATION) {
            entity.prevHeadYaw += FULL_DEGREE;
        }
    }

    private EntityUtil() {}
}
