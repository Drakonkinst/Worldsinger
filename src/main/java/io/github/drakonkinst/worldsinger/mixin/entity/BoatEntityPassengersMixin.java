package io.github.drakonkinst.worldsinger.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BoatEntity.class)
public abstract class BoatEntityPassengersMixin extends VehicleEntity {

    public BoatEntityPassengersMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;isSmallerThanBoat(Lnet/minecraft/entity/Entity;)Z"))
    private boolean preventMidnightCreatureFromEnteringBoat(BoatEntity instance, Entity entity,
            Operation<Boolean> original) {
        return original.call(instance, entity) && !(entity instanceof MidnightCreatureEntity);
    }
}
