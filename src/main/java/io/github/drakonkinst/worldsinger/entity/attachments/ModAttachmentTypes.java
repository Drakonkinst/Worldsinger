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

package io.github.drakonkinst.worldsinger.entity.attachments;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.entity.attachments.player.PlayerMidnightAetherBondManager;
import io.github.drakonkinst.worldsinger.entity.attachments.player.PlayerOrigin;
import io.github.drakonkinst.worldsinger.entity.attachments.player.PlayerPossessionManager;
import io.github.drakonkinst.worldsinger.entity.attachments.player.PlayerThirstManager;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

// I think these are only used for entities now
@SuppressWarnings("UnstableApiUsage")
public final class ModAttachmentTypes {

    // Player persistent data
    public static final AttachmentType<PlayerThirstManager> THIRST = AttachmentRegistry.create(
            Worldsinger.id("thirst"), builder -> builder.persistent(PlayerThirstManager.CODEC)
                    .initializer(PlayerThirstManager::new));
    public static final AttachmentType<PlayerOrigin> PLAYER_ORIGIN = AttachmentRegistry.create(
            Worldsinger.id("player_origin"), builder -> builder.persistent(PlayerOrigin.CODEC)
                    .initializer(() -> PlayerOrigin.DEFAULT));

    // Player non-persistent data
    public static final AttachmentType<PlayerMidnightAetherBondManager> MIDNIGHT_AETHER_BOND = AttachmentRegistry.createDefaulted(
            Worldsinger.id("midnight_aether_bond"), PlayerMidnightAetherBondManager::new);
    public static final AttachmentType<PlayerPossessionManager> POSSESSION = AttachmentRegistry.create(
            Worldsinger.id("possession"));
    // Other entity data
    public static final AttachmentType<SilverLinedBoatData> SILVER_LINED_BOAT = AttachmentRegistry.create(
            Worldsinger.id("silver_lined_boat"),
            builder -> builder.persistent(SilverLinedBoatData.CODEC)
                    .initializer(() -> new SilverLinedBoatData(0)));

    private ModAttachmentTypes() {}
}
