package io.github.drakonkinst.worldsinger.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightAetherBondManager;
import io.github.drakonkinst.worldsinger.entity.data.PlayerMidnightAetherBondManager;

@SuppressWarnings("UnqualifiedStaticUsage")
public final class ModComponents implements EntityComponentInitializer {

    public static final ComponentKey<MidnightAetherBondManager> MIDNIGHT_AETHER_BOND = register(
            "midnight_aether_bond", MidnightAetherBondManager.class);

    private static <T extends Component> ComponentKey<T> register(String id, Class<T> clazz) {
        return ComponentRegistry.getOrCreate(Worldsinger.id(id), clazz);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // Custom handling upon death
        registry.registerForPlayers(MIDNIGHT_AETHER_BOND, PlayerMidnightAetherBondManager::new,
                RespawnCopyStrategy.ALWAYS_COPY);
    }
}
