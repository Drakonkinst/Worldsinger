package io.github.drakonkinst.worldsinger.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VehicleEntity.class)
public abstract class VehicleEntityMixin {

    @WrapOperation(method = "killAndDropItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/VehicleEntity;dropStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/ItemEntity;"))
    private ItemEntity dropStackWithSilverData(VehicleEntity instance, ItemStack itemStack,
            Operation<ItemEntity> original) {
        if (instance instanceof BoatEntity boatEntity) {
            // Modify the item stack to include silver data
            SilverLined.transferDataFromEntityToItemStack(boatEntity, itemStack);
        }
        return original.call(instance, itemStack);
    }

}
