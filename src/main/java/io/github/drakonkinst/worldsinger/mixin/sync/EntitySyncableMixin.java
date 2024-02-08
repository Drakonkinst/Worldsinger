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

package io.github.drakonkinst.worldsinger.mixin.sync;

import io.github.drakonkinst.worldsinger.api.sync.SyncableAttachment;
import io.github.drakonkinst.worldsinger.api.sync.SyncableAttachmentTarget;
import io.github.drakonkinst.worldsinger.network.packet.AttachmentEntitySyncPayload;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("UnstableApiUsage")
@Mixin(Entity.class)
public abstract class EntitySyncableMixin implements SyncableAttachmentTarget {

    @Shadow
    private World world;

    @Shadow
    public abstract int getId();

    @Override
    public Iterable<ServerPlayerEntity> worldsinger$getRecipientsForSync() {
        Entity holder = (Entity) (Object) this;
        if (!this.world.isClient) {
            Deque<ServerPlayerEntity> watchers = new ArrayDeque<>(PlayerLookup.tracking(holder));
            if (holder instanceof ServerPlayerEntity player && player.networkHandler != null) {
                watchers.addFirst(player);
            }
            return watchers;
        }
        return Collections.emptyList();
    }

    @Override
    public <S extends SyncableAttachment> CustomPayload worldsinger$createSyncPacket(
            AttachmentType<S> attachmentType, SyncableAttachment attachment) {
        NbtCompound nbt = new NbtCompound();
        attachment.syncToNbt(nbt);
        return new AttachmentEntitySyncPayload(this.getId(), attachmentType.identifier(), nbt);
    }
}
