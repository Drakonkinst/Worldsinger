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

import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;

public abstract class LunagreeManager extends PersistentState {

    public static final String NAME = "lunagrees";

    private static final String KEY_X = "x";
    private static final String KEY_Z = "z";
    private static final String KEY_ID = "id";

    public record LunagreeLocation(int blockX, int blockZ, int sporeId) {

        public static LunagreeLocation fromPacket(PacketByteBuf buf) {
            int blockX = buf.readVarInt();
            int blockZ = buf.readVarInt();
            byte sporeId = buf.readByte();
            return new LunagreeLocation(blockX, blockZ, sporeId);
        }

        public static void writePacket(LunagreeLocation location, PacketByteBuf buf) {
            buf.writeVarInt(location.blockX);
            buf.writeVarInt(location.blockZ);
            buf.writeByte(location.sporeId);
        }

        public static LunagreeLocation fromNbt(NbtCompound nbt) {
            int sporeId = nbt.getInt(KEY_ID);
            int x = nbt.getInt(KEY_X);
            int z = nbt.getInt(KEY_Z);
            return new LunagreeLocation(x, z, sporeId);
        }

        public void writeNbt(NbtCompound nbt) {
            nbt.putInt(KEY_ID, sporeId);
            nbt.putInt(KEY_X, blockX);
            nbt.putInt(KEY_Z, blockZ);
        }

        public double distSqTo(double x, double z) {
            final double deltaX = blockX - x;
            final double deltaZ = blockZ - z;
            return deltaX * deltaX + deltaZ * deltaZ;
        }
    }

    public abstract void updateLunagreeDataForPlayer(ServerPlayerEntity player);

    public abstract Optional<LunagreeLocation> getNearestLunagree(int blockX, int blockZ,
            int maxDistance);

    public abstract long getKeyForPos(int blockX, int blockZ);

    public boolean isNull() {
        return false;
    }
}
