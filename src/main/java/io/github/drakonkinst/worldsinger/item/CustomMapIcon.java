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

package io.github.drakonkinst.worldsinger.item;

import com.mojang.serialization.Codec;
import io.github.drakonkinst.worldsinger.mixin.accessor.ValueListsInvoker;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

public record CustomMapIcon(CustomMapIcon.Type type, byte x, byte z, byte rotation,
                            Optional<Text> name) {

    public enum Type implements StringIdentifiable {
        RAINLINE(631, "rainline");

        public static final IntFunction<CustomMapIcon.Type> INDEX_TO_TYPE = ValueListsInvoker.createIdToValueFunction(
                CustomMapIcon.Type::getIndex, Type.values());
        public static final Codec<CustomMapIcon.Type> CODEC = StringIdentifiable.createCodec(
                CustomMapIcon.Type::values);
        public static final PacketCodec<ByteBuf, CustomMapIcon.Type> PACKET_CODEC = PacketCodecs.indexed(
                INDEX_TO_TYPE, CustomMapIcon.Type::getIndex);

        private final int index;
        private final String name;

        Type(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String asString() {
            return name;
        }
    }
}
