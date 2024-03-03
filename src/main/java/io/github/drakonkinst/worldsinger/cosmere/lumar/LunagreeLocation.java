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

import io.github.drakonkinst.worldsinger.util.math.Int2;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;

public record LunagreeLocation(int blockX, int blockZ, int sporeId, Int2[] rainlineNodes) {

    private static final String KEY_X = "blockX";
    private static final String KEY_Z = "blockZ";
    private static final String KEY_ID = "id";
    private static final String KEY_RAINLINE = "rainlinePath";

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
            Int2 rainlineNode = location.rainlineNodes()[i];
            buf.writeVarInt(rainlineNode.x());
            buf.writeVarInt(rainlineNode.y());
        }
    }

    public static LunagreeLocation fromNbt(NbtCompound nbt) {
        int sporeId = nbt.getInt(KEY_ID);
        int x = nbt.getInt(KEY_X);
        int z = nbt.getInt(KEY_Z);
        NbtList rainlineNodeData = nbt.getList(KEY_RAINLINE, NbtElement.LIST_TYPE);
        Int2[] rainlineNodes = new Int2[RainlinePath.RAINLINE_NODE_COUNT];
        for (int i = 0; i < RainlinePath.RAINLINE_NODE_COUNT; ++i) {
            if (i >= rainlineNodeData.size()) {
                break;
            }
            int[] coords = rainlineNodeData.getIntArray(i);
            rainlineNodes[i] = new Int2(coords[0], coords[1]);
        }
        return new LunagreeLocation(x, z, sporeId, rainlineNodes);
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putInt(KEY_ID, sporeId);
        nbt.putInt(KEY_X, blockX);
        nbt.putInt(KEY_Z, blockZ);
        NbtList rainlineNodeData = new NbtList();
        for (int i = 0; i < RainlinePath.RAINLINE_NODE_COUNT; ++i) {
            rainlineNodeData.add(
                    new NbtIntArray(new int[] { rainlineNodes[i].x(), rainlineNodes[i].y() }));
        }
        nbt.put(KEY_RAINLINE, rainlineNodeData);
    }

    public double distSqTo(double x, double z) {
        final double deltaX = blockX - x;
        final double deltaZ = blockZ - z;
        return deltaX * deltaX + deltaZ * deltaZ;
    }
}
