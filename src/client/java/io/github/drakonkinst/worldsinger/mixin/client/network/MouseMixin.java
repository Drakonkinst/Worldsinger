package io.github.drakonkinst.worldsinger.mixin.client.network;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public abstract class MouseMixin {

    @WrapWithCondition(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"))
    private boolean preventHotbarSwitchWithScrollIfPossessing(PlayerInventory instance,
            double scrollAmount) {
        CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
        return possessedEntity == null || possessedEntity.canSwitchHotbarItem();
    }
}
