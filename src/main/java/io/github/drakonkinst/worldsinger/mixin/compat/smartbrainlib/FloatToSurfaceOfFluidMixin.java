package io.github.drakonkinst.worldsinger.mixin.compat.smartbrainlib;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

// Same change as StayAboveWaterTask
@Pseudo
@Mixin(FloatToSurfaceOfFluid.class)
public abstract class FloatToSurfaceOfFluidMixin<E extends MobEntity> extends ExtendedBehaviour<E> {

    @ModifyReturnValue(method = "checkExtraStartConditions", at = @At("RETURN"))
    private boolean checkForSporeFluid(boolean original, ServerWorld world, E entity) {
        return original || (SeetheManager.areSporesFluidized(entity.getWorld())
                && entity.getFluidHeight(ModFluidTags.AETHER_SPORES) > entity.getSwimHeight());
    }

}
