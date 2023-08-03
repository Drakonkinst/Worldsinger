package io.github.drakonkinst.examplemod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class WaterWalkMixin extends Entity {
    @Shadow
    protected abstract float getJumpVelocity();

    public WaterWalkMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
    public void examplemod$allowPlayersToWalkOnWater(FluidState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.isIn(FluidTags.WATER) && getWorld().isRaining()) {
            cir.setReturnValue(true);
        }
    }
}
