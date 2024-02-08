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
package io.github.drakonkinst.worldsinger.mixin.entity;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class ItemEntityCustomFluidMovementMixin extends Entity {

    @Unique
    private static final float HEIGHT_OFFSET = 1.0f / 9.0f;
    @Unique
    private static final double HORIZONTAL_BUOYANCY_DRAG = 0.95;
    @Unique
    private static final double HORIZONTAL_LAND_DRAG = 0.7;
    @Unique
    private static final double VERTICAL_BUOYANCY_FORCE_VANILLA = 5.0E-4;
    @Unique
    private static final double LAND_BUOYANCY = VERTICAL_BUOYANCY_FORCE_VANILLA;
    @Unique
    private static final double VERTICAL_BUOYANCY_FORCE = VERTICAL_BUOYANCY_FORCE_VANILLA * 4;
    @Unique
    private static final double MAX_VERTICAL_VELOCITY_VANILLA = 0.06;
    @Unique
    private static final double MAX_VERTICAL_VELOCITY = MAX_VERTICAL_VELOCITY_VANILLA * 4;

    public ItemEntityCustomFluidMovementMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    protected abstract void applyLavaBuoyancy();

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;applyGravity()V"))
    private boolean injectCustomFluidCheck(ItemEntity instance) {
        double height = this.getStandingEyeHeight() - HEIGHT_OFFSET;
        if (EntityUtil.isSubmergedInSporeSea(this)
                && this.getFluidHeight(ModFluidTags.AETHER_SPORES) > height) {
            this.applySporeSeaBuoyancy();
            // Skip original gravity
            return false;
        }

        if (EntityUtil.isSubmergedInFluid(this, ModFluidTags.SUNLIGHT)
                && this.getFluidHeight(ModFluidTags.SUNLIGHT) > height) {
            // Identical to lava buoyancy
            this.applyLavaBuoyancy();
            return false;
        }
        return true;
    }

    @Unique
    private void applySporeSeaBuoyancy() {
        World world = this.getWorld();
        if (!SeetheManager.areSporesFluidized(world)) {
            // Items should not move in solid spores
            this.setVelocity(this.getVelocity().getX() * HORIZONTAL_LAND_DRAG, LAND_BUOYANCY,
                    this.getVelocity().getZ() * HORIZONTAL_LAND_DRAG);
            this.setOnGround(true);
            return;
        }

        Vec3d vec3d = this.getVelocity();
        double yVelocityOffset = vec3d.y < MAX_VERTICAL_VELOCITY ? VERTICAL_BUOYANCY_FORCE : 0.0;
        this.setVelocity(vec3d.x * HORIZONTAL_BUOYANCY_DRAG, vec3d.y + yVelocityOffset,
                vec3d.z * HORIZONTAL_BUOYANCY_DRAG);
    }

}
