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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.util.math.Int2;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.dynamic.Codecs;

public record LunagreeLocation(int blockX, int blockZ, int sporeId, List<Int2> rainlineNodes) {

    public static final MapCodec<LunagreeLocation> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codec.INT.fieldOf("block_x").forGetter(LunagreeLocation::blockX),
                            Codec.INT.fieldOf("block_z").forGetter(LunagreeLocation::blockZ),
                            Codecs.NON_NEGATIVE_INT.fieldOf("spore_id")
                                    .forGetter(LunagreeLocation::sporeId), Int2.CODEC.listOf()
                                    .optionalFieldOf("rainline_path", Collections.emptyList())
                                    .forGetter(LunagreeLocation::rainlineNodes))
                    .apply(instance, LunagreeLocation::new));
    // TODO: Packet codec which only saves lunagree position, not path data?

    public static LunagreeLocation fromPacket(PacketByteBuf buf) {
        int blockX = buf.readVarInt();
        int blockZ = buf.readVarInt();
        byte sporeId = buf.readByte();
        Int2[] rainlineNodes = new Int2[RainlinePath.RAINLINE_NODE_COUNT];
        for (int i = 0; i < RainlinePath.RAINLINE_NODE_COUNT; ++i) {
            int x = buf.readVarInt();
            int y = buf.readVarInt();
            rainlineNodes[i] = new Int2(x, y);
        }
        return new LunagreeLocation(blockX, blockZ, sporeId, rainlineNodes);
    }

    public static void writePacket(LunagreeLocation location, PacketByteBuf buf) {
        buf.writeVarInt(location.blockX);
        buf.writeVarInt(location.blockZ);
        buf.writeByte(location.sporeId);
        for (int i = 0; i < RainlinePath.RAINLINE_NODE_COUNT; ++i) {
            Int2 rainlineNode = location.rainlineNodes[i];
            buf.writeVarInt(rainlineNode.x());
            buf.writeVarInt(rainlineNode.y());
        }
    }

    public double distSqTo(double x, double z) {
        final double deltaX = blockX - x;
        final double deltaZ = blockZ - z;
        return deltaX * deltaX + deltaZ * deltaZ;
    }
}
