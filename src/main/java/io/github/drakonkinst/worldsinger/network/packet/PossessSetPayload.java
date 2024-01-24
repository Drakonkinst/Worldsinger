package io.github.drakonkinst.worldsinger.network.packet;

import io.github.drakonkinst.worldsinger.network.ModPayloadRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record PossessSetPayload(int entityId) implements CustomPayload {

    public static final CustomPayload.Id<PossessSetPayload> ID = ModPayloadRegistry.id(
            "possess_set");
    public static final PacketCodec<RegistryByteBuf, PossessSetPayload> CODEC = PacketCodecs.VAR_INT.xmap(
            PossessSetPayload::new, PossessSetPayload::entityId).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
