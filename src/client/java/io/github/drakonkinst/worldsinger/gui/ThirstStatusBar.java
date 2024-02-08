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
package io.github.drakonkinst.worldsinger.gui;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.effect.ModStatusEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ThirstStatusBar {

    private static final int HOTBAR_HEIGHT = 39;
    private static final int STATUS_BAR_HEIGHT = 10;
    private static final int THIRST_BAR_OFFSET_X = 91;
    private static final int ICON_SIZE = 9;

    private static final Identifier WATER_EMPTY_TEXTURE = Worldsinger.id("hud/water_empty");
    private static final Identifier WATER_EMPTY_THIRST_TEXTURE = Worldsinger.id(
            "hud/water_empty_thirst");
    private static final Identifier WATER_HALF_TEXTURE = Worldsinger.id("hud/water_half");
    private static final Identifier WATER_HALF_THIRST_TEXTURE = Worldsinger.id(
            "hud/water_half_thirst");
    private static final Identifier WATER_FULL_TEXTURE = Worldsinger.id("hud/water_full");
    private static final Identifier WATER_FULL_THIRST_TEXTURE = Worldsinger.id(
            "hud/water_full_thirst");

    public static void renderThirstStatusBar(MinecraftClient client, DrawContext context,
            PlayerEntity player) {
        final int height = client.getWindow().getScaledHeight();
        final int halfWidth = client.getWindow().getScaledWidth() / 2;

        int thirstLevel = player.getAttachedOrCreate(ModAttachmentTypes.THIRST).get();
        int yPos = height - HOTBAR_HEIGHT - STATUS_BAR_HEIGHT;

        for (int i = 0; i < 10; ++i) {
            Identifier emptyTexture;
            Identifier halfTexture;
            Identifier fullTexture;
            if (player.hasStatusEffect(ModStatusEffects.THIRST)) {
                emptyTexture = WATER_EMPTY_THIRST_TEXTURE;
                halfTexture = WATER_HALF_THIRST_TEXTURE;
                fullTexture = WATER_FULL_THIRST_TEXTURE;
            } else {
                emptyTexture = WATER_EMPTY_TEXTURE;
                halfTexture = WATER_HALF_TEXTURE;
                fullTexture = WATER_FULL_TEXTURE;
            }

            int xPos = halfWidth + THIRST_BAR_OFFSET_X - i * (ICON_SIZE - 1) - ICON_SIZE;
            context.drawGuiTexture(emptyTexture, xPos, yPos, ICON_SIZE, ICON_SIZE);
            if (i * 2 + 1 < thirstLevel) {
                context.drawGuiTexture(fullTexture, xPos, yPos, ICON_SIZE, ICON_SIZE);
            } else if (i * 2 + 1 == thirstLevel) {
                context.drawGuiTexture(halfTexture, xPos, yPos, ICON_SIZE, ICON_SIZE);
            }
        }
    }

    // TODO: Add an option for players to show it all the time
    // TODO: Should typically show when equipping a water-based power
    // TODO: Or maybe when holding an item that changes thirst?
    public static boolean shouldRenderThirstBar(PlayerEntity player) {
        return player.getAttachedOrCreate(ModAttachmentTypes.THIRST).isCritical() ||
                player.getAttachedOrCreate(ModAttachmentTypes.MIDNIGHT_AETHER_BOND).getBondCount()
                        > 0;
    }
}
