package io.github.drakonkinst.worldsinger.entity.ai;

import com.mojang.serialization.Codec;
import io.github.drakonkinst.worldsinger.Worldsinger;
import java.util.Optional;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

// @SuppressWarnings("UnqualifiedStaticUsage")
public class ModMemoryModuleTypes {

    // public static final MemoryModuleType<Boolean> HAS_CONTROLLER = register("has_controller");

    public static void initialize() {
    }

    private static <U> MemoryModuleType<U> register(String id, Codec<U> codec) {
        return Registry.register(Registries.MEMORY_MODULE_TYPE, Worldsinger.id(id),
                new MemoryModuleType<>(Optional.of(codec)));
    }

    private static <U> MemoryModuleType<U> register(String id) {
        return Registry.register(Registries.MEMORY_MODULE_TYPE, Worldsinger.id(id),
                new MemoryModuleType<>(Optional.empty()));
    }
}

