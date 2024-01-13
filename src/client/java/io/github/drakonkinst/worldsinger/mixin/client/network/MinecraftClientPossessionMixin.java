package io.github.drakonkinst.worldsinger.mixin.client.network;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientPossessionMixin {

    @WrapWithCondition(method = "handleInputEvents", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I", opcode = Opcodes.PUTFIELD))
    private boolean preventHotbarSwitchIfPossessing(PlayerInventory instance, int value) {
        return !(MinecraftClient.getInstance()
                .getCameraEntity() instanceof CameraPossessable cameraPossessable)
                || cameraPossessable.canSwitchHotbarItem();
    }
}
