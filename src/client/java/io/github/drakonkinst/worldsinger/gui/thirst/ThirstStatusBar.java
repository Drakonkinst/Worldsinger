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
package io.github.drakonkinst.worldsinger.gui.thirst;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.ThirstManager;
import io.github.drakonkinst.worldsinger.effect.ModStatusEffects;
import io.github.drakonkinst.worldsinger.event.thirst.ThirstEvents;
import io.github.drakonkinst.worldsinger.registry.ModHudElements;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;

@SuppressWarnings("UnstableApiUsage")
public final class ThirstStatusBar {

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

    private static final Random random = Random.create();

    private static long keepThirstBarVisibleUntil = 0;
    private static boolean isThirstBarVisible = false;

    // We can assume that the conditions for showing hunger bar are already met, and don't need to re-check these
    public static void renderThirstBar(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        assert client.world != null;
        boolean shouldShowThirstBar = ThirstEvents.VISIBLE_PREDICATE.invoker()
                .shouldBeVisible(client.player);
        if (shouldShowThirstBar) {
            keepThirstBarVisibleUntil = client.world.getTime()
                    + ModHudElements.hideThirstAfterSeconds * ModConstants.SECONDS_TO_TICKS;
        }
        isThirstBarVisible =
                shouldShowThirstBar || keepThirstBarVisibleUntil > client.world.getTime();
        if (isThirstBarVisible) {
            Profiler profiler = Profilers.get();
            profiler.push("thirst");
            drawThirstBar(client, context, client.player);
            profiler.pop();
        }
    }

    public static void drawThirstBar(MinecraftClient client, DrawContext context,
            PlayerEntity player) {
        final int height = client.getWindow().getScaledHeight();
        final int halfWidth = client.getWindow().getScaledWidth() / 2;

        ThirstManager thirstComponent = player.getAttachedOrCreate(ModAttachmentTypes.THIRST);
        int thirstLevel = thirstComponent.get();
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

            int modifiedYPos = yPos;
            if (thirstComponent.isCritical()
                    && client.inGameHud.getTicks() % (thirstLevel * 3 + 1) == 0) {
                modifiedYPos += random.nextInt(3) - 1;
            }

            int xPos = halfWidth + THIRST_BAR_OFFSET_X - i * (ICON_SIZE - 1) - ICON_SIZE;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, emptyTexture, xPos, modifiedYPos,
                    ICON_SIZE, ICON_SIZE);
            if (i * 2 + 1 < thirstLevel) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, fullTexture, xPos,
                        modifiedYPos, ICON_SIZE, ICON_SIZE);
            } else if (i * 2 + 1 == thirstLevel) {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, halfTexture, xPos,
                        modifiedYPos, ICON_SIZE, ICON_SIZE);
            }
        }
    }

    public static boolean isThirstBarVisible() {
        return isThirstBarVisible;
    }

    private ThirstStatusBar() {}
}
