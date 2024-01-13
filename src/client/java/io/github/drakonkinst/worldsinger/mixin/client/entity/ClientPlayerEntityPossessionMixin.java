package io.github.drakonkinst.worldsinger.mixin.client.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
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

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityPossessionMixin extends AbstractClientPlayerEntity {

    public ClientPlayerEntityPossessionMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

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
