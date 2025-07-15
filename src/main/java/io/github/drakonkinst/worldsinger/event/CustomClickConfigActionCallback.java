package io.github.drakonkinst.worldsinger.event;

import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

@FunctionalInterface
public interface CustomClickConfigActionCallback {

    @FunctionalInterface
    interface ConfigAction {

        void setup(ServerPlayerEntity player);
    }

    Event<CustomClickConfigActionCallback> EVENT = EventFactory.createArrayBacked(
            CustomClickConfigActionCallback.class,
            (listeners) -> (handler, configActions, id, payload) -> {
                for (CustomClickConfigActionCallback listener : listeners) {
                    listener.handleCustomClickAction(handler, configActions, id, payload);
                }
            });

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    void handleCustomClickAction(ServerConfigurationNetworkHandler handler,
            Map<Identifier, ConfigAction> configActions, Identifier id,
            Optional<NbtElement> payload);
}
