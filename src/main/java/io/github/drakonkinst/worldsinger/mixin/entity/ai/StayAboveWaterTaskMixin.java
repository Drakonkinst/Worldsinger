package io.github.drakonkinst.worldsinger.mixin.entity.ai;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StayAboveWaterTask.class)
public class StayAboveWaterTaskMixin {

    @ModifyReturnValue(method = "isUnderwater", at = @At("RETURN"))
    private static boolean checkForSporeFluid(boolean original, MobEntity entity) {
        return original || (SeetheManager.areSporesFluidized(entity.getWorld())
                && entity.getFluidHeight(ModFluidTags.AETHER_SPORES) > entity.getSwimHeight());
    }
}
