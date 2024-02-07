package io.github.drakonkinst.worldsinger.mixin.client.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.PossessionManager;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable.AttackOrigin;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityPossessionMixin extends AbstractClientPlayerEntity {

    public ClientPlayerEntityPossessionMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V"))
    private void tickClient(CallbackInfo ci) {
        PossessionManager possessionManager = this.getAttached(ModAttachmentTypes.POSSESSION);
        if (possessionManager != null) {
            possessionManager.clientTick();
        }
    }

    // TODO: Don't think this is needed anymore
    // // Player drifts on the client side when they start possessing, since in vanilla cases if
    // // the camera entity is not the player, the player doesn't exist in the world
    // // But since it does for our purposes, we need to stop their movement.
    // @Inject(method = "tickNewAi", at = @At("TAIL"))
    // private void stopMovementInputWhenStartPossessing(CallbackInfo ci) {
    //     // This is a better check than isCamera() because it doesn't interfere with other
    //     // uses of camera, and also if this is true then the camera should be set properly anyway
    //     if (ModComponents.POSSESSION.get(this).isPossessing()) {
    //         this.sidewaysSpeed = 0.0f;
    //         this.forwardSpeed = 0.0f;
    //         this.jumping = false;
    //     }
    // }

    @ModifyReturnValue(method = "canStartSprinting", at = @At("RETURN"))
    private boolean preventSprintingIfPossessing(boolean original) {
        CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
        return original && !(possessedEntity != null && !possessedEntity.canMoveSelf());
    }

    @Inject(method = "swingHand", at = @At("HEAD"), cancellable = true)
    private void preventSwingHandIfPossessing(Hand hand, CallbackInfo ci) {
        CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
        if (possessedEntity != null
                && possessedEntity.getEntityAttackOrigin() != AttackOrigin.POSSESSOR) {
            ci.cancel();
        }
    }
}
