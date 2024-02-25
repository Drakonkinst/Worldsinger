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

package io.github.drakonkinst.worldsinger.cosmere;

import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarLunagreeManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeManager.LunagreeLocation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

// Client-side record of the nearest lunagree locations
public class ClientLunagreeData {

    public static final double SPORE_FALL_PARTICLE_CHANCE = 0.1125;
    public static final int SPORE_FALL_RADIUS_FAR = 4;
    public static final int SPORE_FALL_RADIUS_CLOSE = 2;
    public static final float SPORE_FALL_PARTICLE_SIZE = 10.0f;

    private final ClientPlayerEntity player;
    private final List<LunagreeLocation> knownLunagreeLocations = new ArrayList<>();
    private @Nullable LunagreeLocation nearestLunagreeLocation = null;
    private boolean needsUpdate = true;
    private boolean underLunagree = false;

    public ClientLunagreeData(ClientPlayerEntity player) {
        this.player = player;
    }

    public List<LunagreeLocation> getKnownLunagreeLocations() {
        return knownLunagreeLocations;
    }

    public void setKnownLunagreeLocations(List<LunagreeLocation> locations) {
        knownLunagreeLocations.clear();
        knownLunagreeLocations.addAll(locations);
    }

    @Nullable
    public LunagreeLocation getNearestLunagreeLocation(int x, int z, int maxDistance) {
        if (nearestLunagreeLocation == null) {
            return null;
        }

        if (maxDistance > 0) {
            int deltaX = nearestLunagreeLocation.blockX() - x;
            int deltaZ = nearestLunagreeLocation.blockZ() - z;
            int distSq = deltaX * deltaX + deltaZ * deltaZ;
            if (distSq > maxDistance * maxDistance) {
                return null;
            }
        }

        return nearestLunagreeLocation;
    }

    public void update() {
        if (!CosmereWorldUtil.isLumar(player.getWorld())) {
            return;
        }
        if (!player.getVelocity().equals(Vec3d.ZERO) || needsUpdate) {
            updateNearestLunagreeLocation(player.getBlockX(), player.getBlockZ());
            needsUpdate = false;
        }
    }

    public boolean isUnderLunagree() {
        return underLunagree;
    }

    private void updateNearestLunagreeLocation(int playerX, int playerZ) {
        nearestLunagreeLocation = null;
        int minDistSq = Integer.MAX_VALUE;
        for (LunagreeLocation lunagreeLocation : knownLunagreeLocations) {
            int deltaX = lunagreeLocation.blockX() - playerX;
            int deltaZ = lunagreeLocation.blockZ() - playerZ;
            int distSq = deltaX * deltaX + deltaZ * deltaZ;
            if (distSq < minDistSq) {
                nearestLunagreeLocation = lunagreeLocation;
                minDistSq = distSq;
            }
        }
        underLunagree = minDistSq
                <= LumarLunagreeManager.SPORE_FALL_RADIUS * LumarLunagreeManager.SPORE_FALL_RADIUS;
    }
}
