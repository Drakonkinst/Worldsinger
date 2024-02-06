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

package io.github.drakonkinst.worldsinger.mixin.world;

import io.github.drakonkinst.worldsinger.world.PersistentByteData;
import io.github.drakonkinst.worldsinger.world.PersistentByteData.ByteDataType;
import io.github.drakonkinst.worldsinger.world.PersistentStateManagerAccess;
import java.io.File;
import java.util.Map;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

// Adds functionality to store byte arrays as persistent state, not just NBTs
// Byte arrays are good for structured data compared to NBT data
@Mixin(PersistentStateManager.class)
public abstract class PersistentStateManagerMixin implements PersistentStateManagerAccess {

    @Shadow
    @Final
    private Map<String, PersistentState> loadedStates;

    @Shadow
    protected abstract File getFile(String id);

    @Shadow
    public abstract void set(String id, PersistentState state);

    @Override
    public <T extends PersistentByteData> T worldsinger$getOrCreateFromBytes(ByteDataType<T> type,
            String id) {
        T persistentState = this.getFromBytes(type, id);
        if (persistentState == null) {
            persistentState = type.constructor().get();
            this.set(id, persistentState);
        }
        return persistentState;
    }

    @SuppressWarnings("unchecked")
    @Unique
    @Nullable
    private <T extends PersistentByteData> T getFromBytes(ByteDataType<T> type, String id) {
        PersistentState persistentState = loadedStates.get(id);
        if (persistentState == null && !loadedStates.containsKey(id)) {
            persistentState = this.readBytesFromFile(type, id);
            this.loadedStates.put(id, persistentState);
        }
        return (T) persistentState;
    }

    @Unique
    @Nullable
    private <T extends PersistentByteData> T readBytesFromFile(ByteDataType<T> type, String id) {
        T data = type.constructor().get();
        File file = this.getFile(id);
        if (file.exists()) {
            data.loadBytesFromFile(file);
            return data;
        }
        return null;
    }
}
