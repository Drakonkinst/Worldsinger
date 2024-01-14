package io.github.drakonkinst.worldsinger.mixin.client.network;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerPossessionMixin {

    @ModifyReturnValue(method = "hasExperienceBar", at = @At("RETURN"))
    private boolean preventRenderExperienceBarIfPossessing(boolean original) {
        return original && PossessionClientUtil.getPossessedEntity() == null;
    }

    @ModifyReturnValue(method = "hasStatusBars", at = @At("RETURN"))
    private boolean preventRenderStatusBarsIfPossessing(boolean original) {
        return original && PossessionClientUtil.getPossessedEntity() == null;
    }
}
