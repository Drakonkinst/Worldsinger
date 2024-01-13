package io.github.drakonkinst.worldsinger.mixin.client.network;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientPossessionMixin {

    @WrapWithCondition(method = "handleInputEvents", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I", opcode = Opcodes.PUTFIELD))
    private boolean preventHotbarSwitchWithNumKeysIfPossessing(PlayerInventory instance,
            int value) {
        CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
        return possessedEntity == null || possessedEntity.canSwitchHotbarItem();
    }

    @WrapOperation(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;dropSelectedItem(Z)Z"))
    private boolean preventDroppingItemIfPossessing(ClientPlayerEntity instance,
            boolean entireStack, Operation<Boolean> original) {
        CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
        if (possessedEntity != null && !possessedEntity.canSwitchHotbarItem()) {
            return false;
        }
        return original.call(instance, entireStack);
    }

    @WrapWithCondition(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private boolean preventDroppingItemIfPossessing(ClientPlayNetworkHandler instance,
            Packet<?> packet) {
        if (packet instanceof PlayerActionC2SPacket actionPacket && actionPacket.getAction()
                == PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
            CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
            return possessedEntity == null || possessedEntity.canSwitchHotbarItem();
        }
        return true;
    }
}
