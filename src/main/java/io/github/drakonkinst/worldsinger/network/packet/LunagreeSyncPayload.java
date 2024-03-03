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

package io.github.drakonkinst.worldsinger.network.packet;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeLocation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record LunagreeSyncPayload(List<LunagreeLocation> locations) implements CustomPayload {

    public static final Id<LunagreeSyncPayload> ID = new CustomPayload.Id<>(
            Worldsinger.id("lunagree_sync"));
    public static final PacketCodec<RegistryByteBuf, LunagreeSyncPayload> CODEC = CustomPayload.codecOf(
            LunagreeSyncPayload::write, LunagreeSyncPayload::new);

    private LunagreeSyncPayload(PacketByteBuf buf) {
        this(new ArrayList<>());
        int numLocations = buf.readVarInt();
        for (int i = 0; i < numLocations; ++i) {
            LunagreeLocation location = LunagreeLocation.fromPacket(buf);
            locations.add(location);
        }
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(locations.size());
        for (LunagreeLocation location : locations) {
            LunagreeLocation.writePacket(location, buf);
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
