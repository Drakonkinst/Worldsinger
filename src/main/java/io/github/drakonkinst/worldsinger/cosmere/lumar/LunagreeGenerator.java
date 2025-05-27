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

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

// Generates and manages the access of lunagrees.
public interface LunagreeGenerator {

    LunagreeGenerator NULL = new NullLunagreeGenerator();

    // Lunagrees cover a wide area identified by a key. The rule used to generate the key
    // should be deterministic, but is otherwise hidden by implementation
    long getKeyForPos(int blockX, int blockZ);

    // Lunagrees can be adjacent to each other, with keys that are not necessarily contiguous.
    long[] getNeighborKeys(long centerKey);

    // Each lunagree contains a LunagreeLocation entry for a key, or null if it cannot be
    // created or does not exist.
    LunagreeLocation getLunagreeForKey(ServerWorld world, long key, boolean shouldCreate);

    // Called when a player's key changes
    void updateLunagreeDataForPlayer(ServerPlayerEntity player);

    // Helper method to grab the list of lunagrees at and/or around a key.
    default List<LunagreeLocation> getNeighboringLunagrees(ServerWorld world, long centerKey,
            boolean includeCenter, boolean shouldCreate) {
        long[] neighborKeys = getNeighborKeys(centerKey);
        List<LunagreeLocation> results = new ArrayList<>(
                neighborKeys.length + (includeCenter ? 1 : 0));
        if (includeCenter) {
            LunagreeLocation centerLocation = getLunagreeForKey(world, centerKey, shouldCreate);
            if (centerLocation != null) {
                results.add(centerLocation);
            }
        }
        for (int i = 0; i < neighborKeys.length; ++i) {
            long neighborKey = neighborKeys[i];
            LunagreeLocation neighborLocation = getLunagreeForKey(world, neighborKey, shouldCreate);
            if (neighborLocation != null) {
                results.add(neighborLocation);
            }
        }
        return results;
    }

    // Helper method to get all lunagrees near a position without generating any.
    default List<LunagreeLocation> getLunagreesNearPos(ServerWorld world, int blockX, int blockZ) {
        long key = getKeyForPos(blockX, blockZ);
        return getNeighboringLunagrees(world, key, true, false);
    }

    // Helper method to get the nearest lunagree, or null if it does not exist.
    // This will typically be the same lunagree as the current position, but not necessarily.
    @Nullable
    default LunagreeLocation getNearestLunagree(ServerWorld world, int blockX, int blockZ,
            int maxDistance) {
        List<LunagreeLocation> candidates = getLunagreesNearPos(world, blockX, blockZ);
        LunagreeLocation nearestLocation = null;
        int minDistSq = Integer.MAX_VALUE;
        for (LunagreeLocation location : candidates) {
            int deltaX = blockX - location.blockX();
            int deltaZ = blockZ - location.blockZ();
            int distSq = deltaX * deltaX + deltaZ * deltaZ;
            if (distSq < minDistSq && distSq < maxDistance * maxDistance) {
                nearestLocation = location;
                minDistSq = distSq;
            }
        }
        return nearestLocation;
    }

    default boolean isNull() {
        return false;
    }
}
