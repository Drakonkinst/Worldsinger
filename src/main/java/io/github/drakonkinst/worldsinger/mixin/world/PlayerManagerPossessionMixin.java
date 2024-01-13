package io.github.drakonkinst.worldsinger.mixin.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.component.PossessionComponent;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Makes the server send packets to the player as if they were in the possessed entity's location
// This helps fix sounds, etc. that they wouldn't normally hear.
@Mixin(PlayerManager.class)
public abstract class PlayerManagerPossessionMixin {

    @WrapOperation(method = "sendToAround", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getX()D"))
    private double modifyXCoordinateIfPossessing(ServerPlayerEntity instance,
            Operation<Double> original) {
        PossessionComponent possessionData = ModComponents.POSSESSION.get(instance);
        CameraPossessable possessionTarget = possessionData.getPossessionTarget();
        if (possessionTarget != null && possessionTarget.redirectWorldEvents()) {
            return possessionTarget.toEntity().getX();
        }
        return original.call(instance);
    }

    @WrapOperation(method = "sendToAround", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getY()D"))
    private double modifyYCoordinateIfPossessing(ServerPlayerEntity instance,
            Operation<Double> original) {
        PossessionComponent possessionData = ModComponents.POSSESSION.get(instance);
        CameraPossessable possessionTarget = possessionData.getPossessionTarget();
        if (possessionTarget != null && possessionTarget.redirectWorldEvents()) {
            return possessionTarget.toEntity().getY();
        }
        return original.call(instance);
    }

    @WrapOperation(method = "sendToAround", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getZ()D"))
    private double modifyZCoordinateIfPossessing(ServerPlayerEntity instance,
            Operation<Double> original) {
        PossessionComponent possessionData = ModComponents.POSSESSION.get(instance);
        CameraPossessable possessionTarget = possessionData.getPossessionTarget();
        if (possessionTarget != null && possessionTarget.redirectWorldEvents()) {
            return possessionTarget.toEntity().getZ();
        }
        return original.call(instance);
    }
}
