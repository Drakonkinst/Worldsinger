package io.github.drakonkinst.worldsinger.event;

import java.util.Optional;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

@FunctionalInterface
public interface CustomClickActionCallback {

    Event<CustomClickActionCallback> EVENT = EventFactory.createArrayBacked(
            CustomClickActionCallback.class, (listeners) -> (player, id, payload) -> {
                for (CustomClickActionCallback listener : listeners) {
                    listener.handleCustomClickAction(player, id, payload);
                }
            });

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    void handleCustomClickAction(ServerPlayerEntity player, Identifier id,
            Optional<NbtElement> payload);
}
