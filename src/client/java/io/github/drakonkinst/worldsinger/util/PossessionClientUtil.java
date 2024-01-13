package io.github.drakonkinst.worldsinger.util;

import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class PossessionClientUtil {

    // Don't need to check camera entity directly since we can trust possessionData to hold the
    // right value, if the camera entity is set due to possession.
    public static CameraPossessable getPossessedEntity() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return null;
        }
        return ModComponents.POSSESSION.get(player).getPossessionTarget();
    }

    private PossessionClientUtil() {}
}
