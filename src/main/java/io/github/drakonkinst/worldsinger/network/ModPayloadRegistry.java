package io.github.drakonkinst.worldsinger.network;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.network.packet.PossessAttackPayload;
import io.github.drakonkinst.worldsinger.network.packet.PossessSetPayload;
import io.github.drakonkinst.worldsinger.network.packet.PossessUpdatePayload;
import io.github.drakonkinst.worldsinger.network.packet.ShapeshiftAttackPayload;
import io.github.drakonkinst.worldsinger.network.packet.ShapeshiftSyncPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.packet.CustomPayload;

public final class ModPayloadRegistry {

    public static <T extends CustomPayload> CustomPayload.Id<T> id(String id) {
        return new CustomPayload.Id<>(Worldsinger.id(id));
    }

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(PossessSetPayload.ID, PossessSetPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PossessSetPayload.ID, PossessSetPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PossessUpdatePayload.ID, PossessUpdatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PossessAttackPayload.ID, PossessAttackPayload.CODEC);

        PayloadTypeRegistry.playS2C()
                .register(ShapeshiftSyncPayload.ID, ShapeshiftSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C()
                .register(ShapeshiftAttackPayload.ID, ShapeshiftAttackPayload.CODEC);
    }

    private ModPayloadRegistry() {}
}
