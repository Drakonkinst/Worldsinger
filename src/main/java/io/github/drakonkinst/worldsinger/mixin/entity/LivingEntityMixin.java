package io.github.drakonkinst.worldsinger.mixin.entity;

import com.google.common.collect.ImmutableMap;
import io.github.drakonkinst.worldsinger.effect.ModStatusEffects;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypeTags;
import io.github.drakonkinst.worldsinger.fluid.AetherSporeFluid;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.fluid.SunlightFluid;
import io.github.drakonkinst.worldsinger.registry.ModDamageTypes;
import io.github.drakonkinst.worldsinger.util.EntityUtil;
import io.github.drakonkinst.worldsinger.world.lumar.LumarSeethe;
import io.github.drakonkinst.worldsinger.world.lumar.SporeParticleManager;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
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

    @Unique
    private static final Map<TagKey<Fluid>, StatusEffect> FLUID_TO_STATUS_EFFECT = ImmutableMap.of(
            ModFluidTags.VERDANT_SPORES, ModStatusEffects.VERDANT_SPORES,
            ModFluidTags.CRIMSON_SPORES, ModStatusEffects.CRIMSON_SPORES,
            ModFluidTags.ZEPHYR_SPORES, ModStatusEffects.ZEPHYR_SPORES,
            ModFluidTags.SUNLIGHT_SPORES, ModStatusEffects.SUNLIGHT_SPORES,
            ModFluidTags.ROSEITE_SPORES, ModStatusEffects.ROSEITE_SPORES,
            ModFluidTags.MIDNIGHT_SPORES, ModStatusEffects.MIDNIGHT_SPORES
    );

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
    private void allowWalkingOnSporesDuringRain(FluidState state,
            CallbackInfoReturnable<Boolean> cir) {
        // TODO: Not sure if this should be only still blocks or all blocks
        if (state.isIn(ModFluidTags.STILL_AETHER_SPORES)) {
            World world = this.getWorld();
            if (!LumarSeethe.areSporesFluidized(world)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        checkSporeSeaEffects();
    }

    @Inject(method = "tickMovement", at = @At("RETURN"))
    private void allowCustomFluidSwimming(CallbackInfo ci) {
        if (this.jumping && this.shouldSwimInFluids()) {
            double swimHeight = this.getSwimHeight();
            FluidState fluidState = this.getWorld().getFluidState(this.getBlockPos());

            // Swimming in liquid is the same velocity regardless of liquid, unless we decide to override the swimUpward function
            // Prevent swimUpwards() from being called more than once if in multiple distinct fluids
            if (this.canSwimUpwards(FluidTags.WATER, swimHeight, fluidState)
                    || this.canSwimUpwards(FluidTags.LAVA, swimHeight, fluidState)) {
                // Do nothing, already swimming upwards
            } else if (this.canSwimUpwards(ModFluidTags.AETHER_SPORES, swimHeight, fluidState)) {
                this.swimUpward(ModFluidTags.AETHER_SPORES);
            } else if (this.canSwimUpwards(ModFluidTags.SUNLIGHT, swimHeight, fluidState)) {
                this.swimUpward(ModFluidTags.SUNLIGHT);
            }
        }
    }

    private boolean canSwimUpwards(TagKey<Fluid> fluidTag, double swimHeight,
            FluidState fluidState) {
        double fluidHeight = this.getFluidHeight(fluidTag);
        if (fluidHeight > 0.0) {
            // During stillings, spore fluids do not act like a fluid and can climb out at any height
            boolean canSwim =
                    !this.isOnGround() || fluidHeight > swimHeight || this.canWalkOnFluid(
                            fluidState);
            return canSwim;
        }
        return false;
    }

    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isFallFlying()Z"), cancellable = true)
    private void injectCustomFluidPhysics(Vec3d movementInput, CallbackInfo ci) {
        FluidState fluidState = this.getWorld().getFluidState(this.getBlockPos());

        if (!this.shouldSwimInFluids()) {
            return;
        }

        if (EntityUtil.isTouchingSporeSea(this)) {
            boolean isFalling = this.getVelocity().y <= 0.0;
            double gravity = 0.08;
            if (isFalling && this.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                gravity = 0.01;
            }
            float horizontalMovementMultiplier = AetherSporeFluid.HORIZONTAL_DRAG_MULTIPLIER;
            float verticalMovementMultiplier = AetherSporeFluid.VERTICAL_DRAG_MULTIPLIER;
            if (this.canWalkOnFluid(fluidState)) {
                // Stuck in the spores!
                gravity = 0.0;
                isFalling = false;
                horizontalMovementMultiplier = 0.0f;
                movementInput = new Vec3d(0.0, movementInput.getY(), 0.0);
            }

            this.applyFluidPhysics(movementInput, horizontalMovementMultiplier,
                    verticalMovementMultiplier, gravity, isFalling);
            ci.cancel();
        } else if (EntityUtil.isTouchingFluid(this, ModFluidTags.SUNLIGHT)
                && !this.canWalkOnFluid(fluidState)) {
            boolean isFalling = this.getVelocity().y <= 0.0;
            double gravity = 0.08;
            if (isFalling && this.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
                gravity = 0.01;
            }

            float horizontalMovementMultiplier = SunlightFluid.HORIZONTAL_DRAG_MULTIPLIER;
            float verticalMovementMultiplier = SunlightFluid.VERTICAL_DRAG_MULTIPLIER;

            this.applyFluidPhysics(movementInput, horizontalMovementMultiplier,
                    verticalMovementMultiplier, gravity, isFalling);
            ci.cancel();
        }
    }

    @Unique
    private void applyFluidPhysics(Vec3d movementInput, float horizontalMovementMultiplier,
            float verticalMovementMultiplier, double gravity, boolean isFalling) {
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

        // Remainder of method
        updateLimbs(this instanceof Flutterer);
    }

    @Unique
    private void checkSporeSeaEffects() {
        if (EntityUtil.isSubmergedInSporeSea(this)) {
            if ((LivingEntity) (Object) this instanceof PlayerEntity playerEntity
                    && (playerEntity.isCreative() || playerEntity.isSpectator())) {
                return;
            }
            applySporeSeaEffects();

            // Also take suffocation damage, mainly for dead spores
            this.damage(ModDamageTypes.of(this.getWorld(), ModDamageTypes.DROWN_SPORE), 1.0f);
        }
    }

    @Unique
    private void applySporeSeaEffects() {
        if (this.getType().isIn(ModEntityTypeTags.SPORES_NEVER_AFFECT)) {
            return;
        }
        for (Map.Entry<TagKey<Fluid>, StatusEffect> entry : FLUID_TO_STATUS_EFFECT.entrySet()) {
            if (this.isSubmergedIn(entry.getKey())) {
                SporeParticleManager.applySporeEffect((LivingEntity) (Object) this,
                        entry.getValue(), SporeParticleManager.SPORE_EFFECT_DURATION_TICKS_DEFAULT);
            }
        }
    }

    @Shadow
    protected abstract boolean shouldSwimInFluids();

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

    @Shadow
    protected boolean jumping;

    @Shadow
    public abstract boolean damage(DamageSource source, float amount);
}
