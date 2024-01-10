package io.github.drakonkinst.worldsinger.mixin.client.world;

import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.world.CameraPosAccess;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraPossessionMixin implements CameraPosAccess {

    @Shadow
    private Entity focusedEntity;

    @Shadow
    private float lastCameraY;

    @Shadow
    private float cameraY;

    @Inject(method = "update", at = @At("HEAD"))
    private void moveCameraInstantly(BlockView area, Entity focusedEntity, boolean thirdPerson,
            boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (focusedEntity == null || this.focusedEntity == null || this.focusedEntity.equals(
                focusedEntity)) {
            return;
        }

        if (focusedEntity instanceof CameraPossessable
                || this.focusedEntity instanceof CameraPossessable) {
            this.cameraY = focusedEntity.getStandingEyeHeight();
            this.lastCameraY = this.cameraY;
        }
    }
}
