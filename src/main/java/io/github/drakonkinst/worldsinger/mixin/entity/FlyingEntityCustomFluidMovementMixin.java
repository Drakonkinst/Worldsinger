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

import io.github.drakonkinst.worldsinger.fluid.AetherSporeFluid;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.fluid.SunlightFluid;
import io.github.drakonkinst.worldsinger.util.EntityUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ FlyingEntity.class, AllayEntity.class })
public abstract class FlyingEntityCustomFluidMovementMixin extends MobEntity {

    protected FlyingEntityCustomFluidMovementMixin(EntityType<? extends MobEntity> entityType,
            World world) {
        super(entityType, world);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void injectCustomFluidPhysics(Vec3d movementInput, CallbackInfo ci) {
        if (!this.isLogicalSideForUpdatingMovement()) {
            return;
        }

        // Won't get too fancy here since most of these things will be dead anyways.
        if (EntityUtil.isTouchingSporeSea(this)) {
            this.updateVelocity(0.02f, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity()
                    .multiply(AetherSporeFluid.HORIZONTAL_DRAG_MULTIPLIER,
                            AetherSporeFluid.VERTICAL_DRAG_MULTIPLIER,
                            AetherSporeFluid.HORIZONTAL_DRAG_MULTIPLIER));
            this.updateLimbs(false);
            ci.cancel();
        } else if (EntityUtil.isTouchingFluid(this, ModFluidTags.SUNLIGHT)) {
            this.updateVelocity(0.02f, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(SunlightFluid.HORIZONTAL_DRAG_MULTIPLIER));
            this.updateLimbs(false);
            ci.cancel();
        }
    }
}
