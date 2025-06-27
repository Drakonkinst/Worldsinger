package io.github.drakonkinst.worldsinger.event.thirst;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface ThirstEvents {

    Event<ThirstVisiblePredicateCallback> VISIBLE_PREDICATE = EventFactory.createArrayBacked(
            ThirstVisiblePredicateCallback.class, (listeners) -> (player) -> {
                for (ThirstVisiblePredicateCallback listener : listeners) {
                    boolean result = listener.shouldBeVisible(player);
                    if (result) {
                        return true;
                    }
                }
                return false;
            });
}
