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

import io.github.drakonkinst.worldsinger.util.ModConstants;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public enum CosmerePlanet {
    NONE(ModConstants.VANILLA_DAY_LENGTH), LUMAR(ModConstants.VANILLA_DAY_LENGTH * 2);

    private final long dayLength;

    CosmerePlanet(long dayLength) {
        this.dayLength = dayLength;
    }

    public long getDayLength() {
        return dayLength;
    }

    // Can switch to another system if ordinal is not enough
    public int getId() {
        return ordinal();
    }

    // Should only be used in world constructors when the planet field might not have been set yet.
    public static CosmerePlanet getPlanetFromKey(RegistryKey<World> worldKey) {
        if (worldKey.equals(ModDimensions.WORLD_LUMAR)) {
            return CosmerePlanet.LUMAR;
        }
        return CosmerePlanet.NONE;
    }

    // Should only be used after world construction
    public static CosmerePlanet getPlanet(@Nullable World world) {
        if (world == null) {
            return CosmerePlanet.NONE;
        }
        CosmereWorldAccess cosmereWorld = (CosmereWorldAccess) world;
        return cosmereWorld.worldsinger$getPlanet();
    }

    public static boolean isCosmerePlanet(@Nullable World world) {
        return CosmerePlanet.getPlanet(world) != CosmerePlanet.NONE;
    }

    public static boolean isPlanet(@Nullable World world, CosmerePlanet planet) {
        return CosmerePlanet.getPlanet(world) == planet;
    }

    public static boolean isLumar(@Nullable World world) {
        return CosmerePlanet.isPlanet(world, CosmerePlanet.LUMAR);
    }
}
