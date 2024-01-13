package io.github.drakonkinst.worldsinger.mixin.client.network;

import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {

    @Inject(method = "setPerspective", at = @At("HEAD"), cancellable = true)
    private void preventSwitchingPerspectiveWhilePossessing(Perspective perspective,
            CallbackInfo ci) {
        CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
        if (possessedEntity != null && !possessedEntity.canSwitchPerspectives()) {
            ci.cancel();
        }
    }
}
