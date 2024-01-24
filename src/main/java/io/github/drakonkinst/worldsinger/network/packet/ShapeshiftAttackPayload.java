package io.github.drakonkinst.worldsinger.network.packet;

import io.github.drakonkinst.worldsinger.network.ModPayloadRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ShapeshiftAttackPayload(int entityId) implements CustomPayload {

    public static final Id<ShapeshiftAttackPayload> ID = ModPayloadRegistry.id("shapeshift_attack");
    public static final PacketCodec<RegistryByteBuf, ShapeshiftAttackPayload> CODEC = PacketCodecs.VAR_INT.xmap(
            ShapeshiftAttackPayload::new, ShapeshiftAttackPayload::entityId).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
