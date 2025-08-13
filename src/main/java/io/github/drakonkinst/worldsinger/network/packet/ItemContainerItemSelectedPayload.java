package io.github.drakonkinst.worldsinger.network.packet;

import io.github.drakonkinst.worldsinger.network.ModPayloadRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ItemContainerItemSelectedPayload(int slot, int selectedItemIndex) implements
        CustomPayload {

    public static final Id<ItemContainerItemSelectedPayload> ID = ModPayloadRegistry.id(
            "item_container_item_selected");
    public static final PacketCodec<RegistryByteBuf, ItemContainerItemSelectedPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, ItemContainerItemSelectedPayload::slot, PacketCodecs.VAR_INT,
            ItemContainerItemSelectedPayload::selectedItemIndex,
            ItemContainerItemSelectedPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
