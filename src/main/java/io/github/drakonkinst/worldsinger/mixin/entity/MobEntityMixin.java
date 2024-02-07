package io.github.drakonkinst.worldsinger.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
import io.github.drakonkinst.worldsinger.util.ModEnums;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {

    // Walkable, but should probably still be cautious
    @Unique
    private static final float STILLING_PENALTY = 8.0f;

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapOperation(method = "getPathfindingPenalty", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/pathing/PathNodeType;getDefaultPenalty()F"))
    private float allowWalkingOnSporeSeaDuringStilling(PathNodeType instance,
            Operation<Float> original) {
        if (instance != ModEnums.PathNodeType.AETHER_SPORE_SEA) {
            return original.call(instance);
        }

        if (SeetheManager.areSporesFluidized(this.getWorld())) {
            return original.call(instance);
        }
        return STILLING_PENALTY;
    }
}
