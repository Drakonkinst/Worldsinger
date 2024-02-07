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

package io.github.drakonkinst.worldsinger.api;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.entity.PlayerThirstManager;
import io.github.drakonkinst.worldsinger.entity.SilverLinedBoatData;
import io.github.drakonkinst.worldsinger.entity.data.PlayerPossessionManager;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

@SuppressWarnings("UnstableApiUsage")
public class ModAttachmentTypes {

    public static final AttachmentType<SilverLinedBoatData> SILVER_LINED_BOAT = AttachmentRegistry.<SilverLinedBoatData>builder()
            .persistent(SilverLinedBoatData.CODEC)
            .initializer(() -> new SilverLinedBoatData(0))
            .buildAndRegister(Worldsinger.id("silver_lined_boat"));
    public static final AttachmentType<PlayerPossessionManager> POSSESSION = AttachmentRegistry.create(
            Worldsinger.id("possession"));
    public static final AttachmentType<PlayerThirstManager> THIRST = AttachmentRegistry.<PlayerThirstManager>builder()
            .persistent(PlayerThirstManager.CODEC)
            .initializer(PlayerThirstManager::new)
            .buildAndRegister(Worldsinger.id("thirst"));

}
