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

package io.github.drakonkinst.worldsinger.cosmere.lumar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.util.math.Int2;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Codecs;

public record LunagreeLocation(int blockX, int blockZ, int sporeId, List<Int2> rainlineNodes) {

    public static final Codec<LunagreeLocation> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.INT.fieldOf("block_x").forGetter(LunagreeLocation::blockX),
                            Codec.INT.fieldOf("block_z").forGetter(LunagreeLocation::blockZ),
                            Codecs.NON_NEGATIVE_INT.fieldOf("spore_id")
                                    .forGetter(LunagreeLocation::sporeId), Int2.CODEC.listOf()
                                    .optionalFieldOf("rainline_path", Collections.emptyList())
                                    .forGetter(LunagreeLocation::rainlineNodes))
                    .apply(instance, LunagreeLocation::new));
    // Never send the rainline node data to the client
    public static final PacketCodec<RegistryByteBuf, LunagreeLocation> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, LunagreeLocation::blockX, PacketCodecs.VAR_INT,
            LunagreeLocation::blockZ, PacketCodecs.VAR_INT, LunagreeLocation::sporeId,
            PacketCodec.unit(Collections.emptyList()), LunagreeLocation::rainlineNodes,
            LunagreeLocation::new);

    public double distSqTo(double x, double z) {
        final double deltaX = blockX - x;
        final double deltaZ = blockZ - z;
        return deltaX * deltaX + deltaZ * deltaZ;
    }
}
