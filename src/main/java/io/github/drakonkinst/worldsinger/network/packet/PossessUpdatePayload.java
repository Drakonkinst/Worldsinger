package io.github.drakonkinst.worldsinger.network.packet;

import io.github.drakonkinst.worldsinger.network.ModPayloadRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record PossessUpdatePayload(float yaw, float pitch, float forwardSpeed, float sidewaysSpeed,
                                   boolean jumping, boolean sprinting) implements CustomPayload {

    public static final Id<PossessUpdatePayload> ID = ModPayloadRegistry.id("possess_update");
    public static final PacketCodec<RegistryByteBuf, PossessUpdatePayload> CODEC = CustomPayload.codecOf(
            PossessUpdatePayload::write, PossessUpdatePayload::new);

    private PossessUpdatePayload(PacketByteBuf buf) {
        this(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readBoolean(),
                buf.readBoolean());
    }

    private void write(PacketByteBuf buf) {
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeFloat(forwardSpeed);
        buf.writeFloat(sidewaysSpeed);
        buf.writeBoolean(jumping);
        buf.writeBoolean(sprinting);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
