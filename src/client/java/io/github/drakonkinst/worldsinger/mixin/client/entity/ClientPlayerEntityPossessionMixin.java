package io.github.drakonkinst.worldsinger.mixin.client.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityPossessionMixin extends AbstractClientPlayerEntity {

    public ClientPlayerEntityPossessionMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @ModifyReturnValue(method = "canStartSprinting", at = @At("RETURN"))
    private boolean preventSprintingIfPossessing(boolean original) {
        // TODO: Check if possessing
        return original && !(MinecraftClient.getInstance()
                .getCameraEntity() instanceof CameraPossessable cameraPossessable
                && !cameraPossessable.canMoveSelf());
    }

    @Inject(method = "swingHand", at = @At("HEAD"), cancellable = true)
    private void preventSwingHandIfPossessing(Hand hand, CallbackInfo ci) {
        // TODO: Check if possessing
        if (MinecraftClient.getInstance()
                .getCameraEntity() instanceof CameraPossessable cameraPossessable
                && !cameraPossessable.canPerformAttack()) {
            ci.cancel();
        }
    }

    // Run after super.tick()
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasVehicle()Z"))
    private void resetCameraWhenStopPossessing(CallbackInfo ci) {
        // PossessionComponent possessionData = ModComponents.POSSESSION.get(this);
        // if (!possessionData.isPossessing()) {
        //     Worldsinger.PROXY.resetRenderViewEntity();
        // }
    }
}
