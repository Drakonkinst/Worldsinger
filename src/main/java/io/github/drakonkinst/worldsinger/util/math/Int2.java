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

package io.github.drakonkinst.worldsinger.util.math;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.stream.IntStream;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public record Int2(int x, int y) {

    public static final Codec<Int2> CODEC = Codec.INT_STREAM.comapFlatMap(
            stream -> Util.decodeFixedLengthArray(stream, 2)
                    .map(values -> new Int2(values[0], values[1])),
            pair -> IntStream.of(pair.x(), pair.y())).stable();
    public static final PacketCodec<ByteBuf, Int2> PACKET_CODEC = new PacketCodec<>() {
        public Int2 decode(ByteBuf byteBuf) {
            return Int2.fromLong(byteBuf.readLong());
        }

        public void encode(ByteBuf byteBuf, Int2 blockPos) {
            byteBuf.writeLong(blockPos.toLong());
        }
    };

    public static long toLong(Int2 value) {
        return ChunkPos.toLong(value.x, value.y);
    }

    public static Int2 fromLong(long value) {
        int x = ChunkPos.getPackedX(value);
        int y = ChunkPos.getPackedZ(value);
        return new Int2(x, y);
    }

    public static float distance(Int2 p0, Int2 p1) {
        return MathHelper.sqrt(Int2.distanceSquared(p0, p1));
    }

    public static float distanceSquared(Int2 p0, Int2 p1) {
        float deltaX = p0.x() - p1.x();
        float deltaY = p0.y() - p1.y();
        return deltaX * deltaX + deltaY * deltaY;
    }

    public long toLong() {
        return Int2.toLong(this);
    }

    @Override
    public @NotNull String toString() {
        return "(" + x + ", " + y + ")";
    }
}
