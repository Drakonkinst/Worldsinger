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
package io.github.drakonkinst.worldsinger.network;

import com.mojang.authlib.GameProfile;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy {

    private static final Perspective[] PERSPECTIVES = Perspective.values();

    private Perspective previousPerspective = Perspective.FIRST_PERSON;
    private boolean usingCustomRenderView = false;

    @Override
    public LivingEntity createPlayerMorph(World world, UUID uuid, String playerName) {
        if (!world.isClient()) {
            // If running in singleplayer, this can override the instance. Revert to
            // normal behavior to avoid calling client code.
            return super.createPlayerMorph(world, uuid, playerName);
        }
        ClientWorld clientWorld = (ClientWorld) world;
        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
        if (handler == null) {
            return createDefaultPlayer(clientWorld, uuid, playerName);
        }

        PlayerListEntry playerListEntry = handler.getPlayerListEntry(uuid);
        if (playerListEntry == null) {
            return createDefaultPlayer(clientWorld, uuid, playerName);
        } else {
            return new OtherClientPlayerEntity(clientWorld, playerListEntry.getProfile());
        }
    }

    // TODO: We can also try fetching the skin data, to be even more accurate
    // But this works for now
    private LivingEntity createDefaultPlayer(ClientWorld world, UUID uuid, String playerName) {
        return new OtherClientPlayerEntity(world, new GameProfile(uuid, playerName));
    }

    public void setRenderViewEntity(Entity entity) {
        if (entity.equals(MinecraftClient.getInstance().getCameraEntity())) {
            return;
        }
        previousPerspective = MinecraftClient.getInstance().options.getPerspective();
        MinecraftClient.getInstance().setCameraEntity(entity);

        // Set perspective of camera entity as well
        if (entity instanceof CameraPossessable cameraPossessable) {
            int perspectiveOrdinal = cameraPossessable.getDefaultPerspective();
            if (perspectiveOrdinal > -1) {
                MinecraftClient.getInstance().options.setPerspective(
                        PERSPECTIVES[perspectiveOrdinal]);
            }
        }
        usingCustomRenderView = true;
    }

    public void resetRenderViewEntity() {
        if (!usingCustomRenderView) {
            return;
        }
        MinecraftClient.getInstance().setCameraEntity(MinecraftClient.getInstance().player);
        MinecraftClient.getInstance().options.setPerspective(previousPerspective);
        usingCustomRenderView = false;
    }
}
