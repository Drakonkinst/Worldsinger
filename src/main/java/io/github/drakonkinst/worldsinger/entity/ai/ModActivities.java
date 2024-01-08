package io.github.drakonkinst.worldsinger.entity.ai;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

// @SuppressWarnings("UnqualifiedStaticUsage")
public final class ModActivities {

    // public static final Activity FIGHT_TAMED = register("fight_tamed");
    // public static final Activity IDLE_TAMED = register("idle_tamed");

    public static void initialize() {

    }

    private static Activity register(String id) {
        return Registry.register(Registries.ACTIVITY, id, new Activity(id));
    }
}
