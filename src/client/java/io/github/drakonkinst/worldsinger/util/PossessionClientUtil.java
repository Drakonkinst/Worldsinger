package io.github.drakonkinst.worldsinger.util;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public final class PossessionClientUtil {

    private static final String ON_POSSESS_START_TRANSLATION_KEY = Util.createTranslationKey(
            "action", Worldsinger.id("possess.on_start"));

    // Don't need to check camera entity directly since we can trust possessionData to hold the
    // right value, if the camera entity is set due to possession.
    public static CameraPossessable getPossessedEntity() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return null;
        }
        return ModComponents.POSSESSION.get(player).getPossessionTarget();
    }

    public static void displayPossessStartText() {
        MinecraftClient client = MinecraftClient.getInstance();
        Text text = Text.translatable(ON_POSSESS_START_TRANSLATION_KEY,
                client.options.sneakKey.getBoundKeyLocalizedText());
        client.inGameHud.setOverlayMessage(text, false);
        client.getNarratorManager().narrate(text);
    }

    private PossessionClientUtil() {}
}
