/*
 * MIT License
 *
 * Copyright (c) 2024 Drakonkinst
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

package io.github.drakonkinst.worldsinger.api;

import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarLunagreeGenerator;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeLocation;
import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

// Client-side record of the nearest lunagree locations
public class ClientLunagreeData {

    public static final double SPORE_FALL_PARTICLE_CHANCE = 0.1125;
    public static final int SPORE_FALL_RADIUS_FAR = 4;
    public static final int SPORE_FALL_RADIUS_CLOSE = 2;
    public static final float SPORE_FALL_PARTICLE_SIZE = 10.0f;
    public static final int MAX_KNOWN_LUNAGREE_LOCATIONS = 9;

    @SuppressWarnings("UnstableApiUsage")
    public static ClientLunagreeData get(World world) {
        return world.getAttachedOrCreate(ModClientAttachmentTypes.LUNAGREE_DATA);
    }

    // Associative arrays that have up to MAX_KNOWN_LUNAGREE_LOCATIONS values
    // The first null value marks the end of the list, if not full
    private final LunagreeLocation[] lunagreeLocations = new LunagreeLocation[MAX_KNOWN_LUNAGREE_LOCATIONS];

    private @Nullable LunagreeLocation nearestLunagreeLocation = null;
    private boolean needsUpdate = true;
    private boolean underLunagree = false;

    public void update(ClientWorld world, ClientPlayerEntity player) {
        if (!CosmerePlanet.isLumar(world)) {
            return;
        }
        if (!player.getVelocity().equals(Vec3d.ZERO) || needsUpdate) {
            updateNearestLunagreeLocation(player.getBlockX(), player.getBlockZ());
            needsUpdate = false;
        }
    }

    public LunagreeLocation[] getLunagreeLocations() {
        return lunagreeLocations;
    }

    public void setLunagreeLocations(List<LunagreeLocation> locations) {
        for (int i = 0; i < MAX_KNOWN_LUNAGREE_LOCATIONS; ++i) {
            if (i >= locations.size()) {
                lunagreeLocations[i] = null;
            } else {
                lunagreeLocations[i] = locations.get(i);
            }
        }
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

    public boolean isUnderLunagree() {
        return underLunagree;
    }

    private void updateNearestLunagreeLocation(int playerX, int playerZ) {
        nearestLunagreeLocation = null;
        int minDistSq = Integer.MAX_VALUE;
        for (LunagreeLocation lunagreeLocation : lunagreeLocations) {
            if (lunagreeLocation == null) {
                return;
            }
            int deltaX = lunagreeLocation.blockX() - playerX;
            int deltaZ = lunagreeLocation.blockZ() - playerZ;
            int distSq = deltaX * deltaX + deltaZ * deltaZ;
            if (distSq < minDistSq) {
                nearestLunagreeLocation = lunagreeLocation;
                minDistSq = distSq;
            }
        }
        underLunagree = minDistSq <= LumarLunagreeGenerator.SPORE_FALL_RADIUS
                * LumarLunagreeGenerator.SPORE_FALL_RADIUS;
    }
}
