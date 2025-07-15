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

package io.github.drakonkinst.worldsinger.api.sync;

import com.google.common.collect.ImmutableList;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.entity.attachments.ModAttachmentTypes;
import java.util.List;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

@SuppressWarnings("UnstableApiUsage")
public final class AttachmentSync {

    public static final List<AttachmentType<? extends SyncableAttachment>> ENTITY_SYNCED_COMPONENTS = ImmutableList.of(
            ModAttachmentTypes.SILVER_LINED_BOAT, ModAttachmentTypes.THIRST,
            ModAttachmentTypes.MIDNIGHT_AETHER_BOND);

    public static <S extends SyncableAttachment> void sync(AttachmentTarget target,
            AttachmentType<S> attachmentType, SyncableAttachment attachment) {
        if (target instanceof SyncableAttachmentTarget syncableAttachmentTarget) {
            AttachmentSync.sync(syncableAttachmentTarget, attachmentType, attachment);
        } else {
            Worldsinger.LOGGER.warn("Cannot sync target " + target);
        }
    }

    private static <S extends SyncableAttachment> void sync(SyncableAttachmentTarget target,
            AttachmentType<S> attachmentType, SyncableAttachment attachment) {
        CustomPayload payload = target.worldsinger$createSyncPacket(attachmentType, attachment);
        for (ServerPlayerEntity player : target.worldsinger$getRecipientsForSync()) {
            if (attachment.shouldSyncWith(player)) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    public static void syncEntityAttachments(ServerPlayerEntity player, Entity target) {
        for (AttachmentType<? extends SyncableAttachment> type : ENTITY_SYNCED_COMPONENTS) {
            Object attachment = target.getAttached(type);
            if (attachment instanceof SyncableAttachment syncableAttachment
                    && syncableAttachment.shouldSyncWith(player)) {
                CustomPayload payload = ((SyncableAttachmentTarget) target).worldsinger$createSyncPacket(
                        type, syncableAttachment);
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    private AttachmentSync() {}
}
