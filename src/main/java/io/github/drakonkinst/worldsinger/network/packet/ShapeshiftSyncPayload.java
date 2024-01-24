package io.github.drakonkinst.worldsinger.network.packet;

import io.github.drakonkinst.worldsinger.network.ModPayloadRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ShapeshiftSyncPayload(int entityId, String entityType,
                                    NbtCompound entityData) implements CustomPayload {

    public static final Id<ShapeshiftSyncPayload> ID = ModPayloadRegistry.id("shapeshift_sync");
    public static final PacketCodec<RegistryByteBuf, ShapeshiftSyncPayload> CODEC = CustomPayload.codecOf(
            ShapeshiftSyncPayload::write, ShapeshiftSyncPayload::new);

    private ShapeshiftSyncPayload(PacketByteBuf buf) {
        this(buf.readVarInt(), buf.readString(), buf.readNbt());
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeString(entityType);
        buf.writeNbt(entityData);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
