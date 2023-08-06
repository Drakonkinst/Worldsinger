package io.github.drakonkinst.examplemod.mixin.entity;

import io.github.drakonkinst.examplemod.entity.SporeFluidEntityStateAccess;
import io.github.drakonkinst.examplemod.fluid.AetherSporeFluid;
import io.github.drakonkinst.examplemod.fluid.ModFluidTags;
import io.github.drakonkinst.examplemod.weather.LumarSeetheManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    protected abstract boolean shouldSwimInFluids();

    @Shadow
    protected boolean jumping;

    @Shadow
    protected abstract void swimUpward(TagKey<Fluid> fluid);

    @Shadow
    public abstract boolean canWalkOnFluid(FluidState state);

    @Shadow
    public abstract Vec3d applyFluidMovingSpeed(double gravity, boolean falling,
            Vec3d motion);

    @Shadow
    public abstract boolean hasStatusEffect(StatusEffect effect);

    @Shadow
    public abstract void updateLimbs(boolean flutter);

    @Shadow
    public abstract boolean isClimbing();

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
    private void allowWalkingOnSporesDuringRain(FluidState state,
            CallbackInfoReturnable<Boolean> cir) {
        // if (state.isIn(ModFluidTags.STILL_AETHER_SPORES) && !LumarSeetheManager.areSporesFluidized(this.getWorld())) {
        if (state.isIn(ModFluidTags.AETHER_SPORES) && !LumarSeetheManager.areSporesFluidized(
                this.getWorld())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        checkSporeSeaDamage();
    }

    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void allowCustomFluidSwimming(CallbackInfo ci) {
        if (this.jumping && this.shouldSwimInFluids()) {
            if (this.isOnGround()) {
                return;
            }
            // Vanilla fluids are handled in the method already, so just worry about custom ones
            if (((SporeFluidEntityStateAccess) this).examplemod$isTouchingSporeSea()) {
                FluidState fluidState = this.getWorld().getFluidState(this.getBlockPos());
                double maxFluidHeight = this.getFluidHeight(ModFluidTags.AETHER_SPORES);
                double swimHeight = this.getSwimHeight();

                // During stillings, this block does not act like a liquid and can climb out at any height
                boolean canSwim =
                        maxFluidHeight > swimHeight || (maxFluidHeight > 0.0 && this.canWalkOnFluid(
                                fluidState));
                if (canSwim) {
                    // Swimming in liquid is the same velocity regardless of liquid, unless we decide to override the swimUpward function
                    this.swimUpward(ModFluidTags.AETHER_SPORES);
                }
            }
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void injectCustomFluidPhysics(Vec3d movementInput, CallbackInfo ci) {
        if (!this.isLogicalSideForUpdatingMovement()) {
            return;
        }

        if (!this.shouldSwimInFluids()) {
            return;
        }

        boolean isFalling = this.getVelocity().y <= 0.0;
        double gravity = 0.08;
        if (isFalling && this.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            gravity = 0.01;
        }

        FluidState fluidState = this.getWorld().getFluidState(this.getBlockPos());
        if (((SporeFluidEntityStateAccess) this).examplemod$isTouchingSporeSea()) {
            float horizontalMovementMultiplier = AetherSporeFluid.HORIZONTAL_DRAG_MULTIPLIER;
            float verticalMovementMultiplier = AetherSporeFluid.VERTICAL_DRAG_MULTIPLIER;
            if (this.canWalkOnFluid(fluidState)) {
                // Stuck in the spores!
                gravity = 0.0;
                isFalling = false;
                horizontalMovementMultiplier = 0.0f;
                movementInput = new Vec3d(0.0, movementInput.getY(), 0.0);
            }

            double currYPos = this.getY();
            this.updateVelocity(0.02f, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            Vec3d vec3d = this.getVelocity();

            if (this.horizontalCollision && this.isClimbing()) {
                vec3d = new Vec3d(vec3d.x, 0.2, vec3d.z);
            }

            this.setVelocity(
                    vec3d.multiply(horizontalMovementMultiplier, verticalMovementMultiplier,
                            horizontalMovementMultiplier));
            vec3d = this.applyFluidMovingSpeed(gravity, isFalling, this.getVelocity());
            this.setVelocity(vec3d);

            // Apply gravity
            if (!this.hasNoGravity()) {
                this.setVelocity(this.getVelocity().add(0.0, -gravity / 4.0, 0.0));
            }
            if (this.horizontalCollision && this.doesNotCollide(vec3d.x,
                    vec3d.y + (double) 0.6f - this.getY() + currYPos, vec3d.z)) {
                this.setVelocity(vec3d.x, 0.3f, vec3d.z);
            }

            // End of method
            updateLimbs(this instanceof Flutterer);
            ci.cancel();
        }
    }

    @Unique
    private void checkSporeSeaDamage() {
        if (((SporeFluidEntityStateAccess) this).examplemod$isInSporeSea()) {
            damageFromSporeSea();
        }
    }


    @Unique
    private void damageFromSporeSea() {
        if (this.damage(this.getDamageSources().drown(), AetherSporeFluid.DAMAGE)) {
            // this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4f, 2.0f + this.random.nextFloat() * 0.4f);
        }
    }
}