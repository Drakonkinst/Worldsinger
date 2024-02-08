/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Drakonkinst
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.drakonkinst.worldsinger.util;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.PossessionManager;
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
        PossessionManager possessionManager = player.getAttached(ModAttachmentTypes.POSSESSION);
        if (possessionManager == null) {
            return null;
        }
        return possessionManager.getPossessionTarget();
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
