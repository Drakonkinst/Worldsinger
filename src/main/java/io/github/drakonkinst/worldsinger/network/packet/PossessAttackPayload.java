package io.github.drakonkinst.worldsinger.network.packet;

import io.github.drakonkinst.worldsinger.network.ModPayloadRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record PossessAttackPayload(int targetEntityId) implements CustomPayload {

    public static final Id<PossessAttackPayload> ID = ModPayloadRegistry.id("possess_attack");
    public static final PacketCodec<RegistryByteBuf, PossessAttackPayload> CODEC = PacketCodecs.VAR_INT.xmap(
            PossessAttackPayload::new, PossessAttackPayload::targetEntityId).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
