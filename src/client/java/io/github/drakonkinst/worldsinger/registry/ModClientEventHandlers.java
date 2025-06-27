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
package io.github.drakonkinst.worldsinger.registry;

import io.github.drakonkinst.worldsinger.api.ClientLunagreeData;
import io.github.drakonkinst.worldsinger.api.ClientRainlineData;
import io.github.drakonkinst.worldsinger.entity.PossessionClientUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.ActionResult;

public final class ModClientEventHandlers {

    public static void registerEventHandlers() {
        ModClientEventHandlers.registerSelfTargetingEventHandlers();
        ModClientEventHandlers.registerWorldTickEvents();
        PossessionClientUtil.registerPossessionEventHandlers();
        ModHudElements.registerHudEventHandlers();
    }

    private static void registerWorldTickEvents() {
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null) {
                return;
            }
            ClientLunagreeData.get(world).update(world, player);
            ClientRainlineData.get(world).update(world, player);
        });
    }

    private static void registerSelfTargetingEventHandlers() {
        // Explicitly prevent targeting yourself, which is possible in some possession cases
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity.equals(MinecraftClient.getInstance().player)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity.equals(MinecraftClient.getInstance().player)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    private ModClientEventHandlers() {}
}
