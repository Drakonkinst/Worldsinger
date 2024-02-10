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
package io.github.drakonkinst.worldsinger.config;

import io.github.drakonkinst.worldsinger.compat.Compat;
import io.github.drakonkinst.worldsinger.compat.Compat.Mod;
import io.github.drakonkinst.worldsinger.compat.yacl.ModYACLConfigScreen;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import java.net.URI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

// Optional dependency on YACL. If it does not exist, you are prompted to install it.
public final class ModConfigScreen {

    private static final String YACL_URL = "https://modrinth.com/mod/yacl/versions";

    public static Screen create(Screen parent) {
        if (!Compat.isModLoaded(Mod.YACL)) {
            return new ConfirmScreen((result) -> {
                if (result) {
                    Util.getOperatingSystem().open(URI.create(YACL_URL));
                }
                MinecraftClient.getInstance().setScreen(parent);
            }, ModConfigScreen.getConfigText("missing"),
                    ModConfigScreen.getConfigText("missing.message"), ScreenTexts.YES,
                    ScreenTexts.NO);
        }

        if (!Compat.isModUpToDate(Mod.YACL)) {
            return new ConfirmScreen((result) -> {
                if (result) {
                    Util.getOperatingSystem().open(URI.create(YACL_URL));
                }
                MinecraftClient.getInstance().setScreen(parent);
            }, ModConfigScreen.getConfigText("outdated"),
                    ModConfigScreen.getConfigText("outdated.message"), ScreenTexts.YES,
                    ScreenTexts.NO);
        }

        return ModYACLConfigScreen.generateConfigScreen(parent);
    }

    private static Text getConfigText(String key) {
        return Text.translatable("config." + ModConstants.MOD_ID + ".yacl." + key);
    }

    private ModConfigScreen() {}
}
