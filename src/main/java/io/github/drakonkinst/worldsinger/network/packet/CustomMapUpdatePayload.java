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
import io.github.drakonkinst.worldsinger.item.map.CustomMapDecoration;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;

// Encloses a MapUpdateS2CPacket, allowing new data to be synced
public record CustomMapUpdatePayload(MapIdComponent mapId, byte scale, boolean locked,
                                     Optional<List<MapDecoration>> decorations,
                                     Optional<MapState.UpdateData> updateData,
                                     Optional<List<CustomMapDecoration>> customDecorations) implements
        CustomPayload {

    public static final Id<CustomMapUpdatePayload> ID = new Id<>(
            Worldsinger.id("custom_map_update"));
    public static final PacketCodec<RegistryByteBuf, CustomMapUpdatePayload> CODEC = PacketCodec.tuple(
            MapIdComponent.PACKET_CODEC, CustomMapUpdatePayload::mapId, PacketCodecs.BYTE,
            CustomMapUpdatePayload::scale, PacketCodecs.BOOLEAN, CustomMapUpdatePayload::locked,
            MapDecoration.CODEC.collect(PacketCodecs.toList()).collect(PacketCodecs::optional),
            CustomMapUpdatePayload::decorations, MapState.UpdateData.CODEC,
            CustomMapUpdatePayload::updateData,
            CustomMapDecoration.CODEC.collect(PacketCodecs.toList())
                    .collect(PacketCodecs::optional), CustomMapUpdatePayload::customDecorations,
            CustomMapUpdatePayload::new);

    public CustomMapUpdatePayload(MapUpdateS2CPacket mapUpdateS2CPacket,
            List<CustomMapDecoration> customIcons) {
        this(mapUpdateS2CPacket.mapId(), mapUpdateS2CPacket.scale(), mapUpdateS2CPacket.locked(),
                mapUpdateS2CPacket.decorations(), mapUpdateS2CPacket.updateData(),
                Optional.ofNullable(customIcons));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
