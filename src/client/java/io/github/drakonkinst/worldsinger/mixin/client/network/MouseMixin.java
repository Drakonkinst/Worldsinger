package io.github.drakonkinst.worldsinger.mixin.client.network;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.freelook.FreeLook;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Unique
    private static final float CHANGE_LOOK_MULTIPLIER = 0.15f;
    @Unique
    private static final float MIN_PITCH = -90.0f;
    @Unique
    private static final float MAX_PITCH = 90.0f;

    @WrapWithCondition(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"))
    private boolean preventHotbarSwitchWithScrollIfPossessing(PlayerInventory instance,
            double scrollAmount) {
        CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
        return possessedEntity == null || possessedEntity.canSwitchHotbarItem();
    }

    @WrapOperation(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
    private void changeFreeLookDirection(ClientPlayerEntity instance, double cursorDeltaX,
            double cursorDeltaY, Operation<Void> original) {
        FreeLook freeLookData = (FreeLook) instance;
        if (freeLookData.worldsinger$isFreeLookEnabled()) {
            // Set free look data instead
            float deltaYaw = (float) cursorDeltaX * CHANGE_LOOK_MULTIPLIER;
            float deltaPitch = (float) cursorDeltaY * CHANGE_LOOK_MULTIPLIER;
            freeLookData.worldsinger$setFreeLookYaw(
                    freeLookData.worldsinger$getFreeLookYaw() + deltaYaw);
            freeLookData.worldsinger$setFreeLookPitch(
                    MathHelper.clamp(freeLookData.worldsinger$getFreeLookPitch() + deltaPitch,
                            MIN_PITCH, MAX_PITCH));
        } else {
            original.call(instance, cursorDeltaX, cursorDeltaY);
            // Also sync free look, so it always starts from the current look direction when you
            // start free looking
            freeLookData.worldsinger$setFreeLookYaw(instance.getYaw());
            freeLookData.worldsinger$setFreeLookPitch(instance.getPitch());
        }

    }

}
