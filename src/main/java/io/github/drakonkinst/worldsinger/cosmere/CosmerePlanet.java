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

import com.mojang.serialization.Codec;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensions;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public enum CosmerePlanet implements StringIdentifiable {
    NONE(0, "overworld", ModConstants.VANILLA_DAY_LENGTH, World.OVERWORLD),
    LUMAR(1, "lumar", ModConstants.VANILLA_DAY_LENGTH * 2, ModDimensions.WORLD_LUMAR);

    public static final CosmerePlanet[] VALUES = CosmerePlanet.getOrderedPlanets();

    private static final IntFunction<CosmerePlanet> BY_ID = ValueLists.createIndexToValueFunction(
            CosmerePlanet::getId, values(), ValueLists.OutOfBoundsHandling.ZERO);
    public static final PacketCodec<ByteBuf, CosmerePlanet> PACKET_CODEC = PacketCodecs.indexed(
            BY_ID, CosmerePlanet::getId);
    public static final Codec<CosmerePlanet> CODEC = StringIdentifiable.createBasicCodec(
            CosmerePlanet::getOrderedPlanets);

    private final String translationKey;
    private final int id;
    private final long dayLength;
    private final RegistryKey<World> registryKey;

    CosmerePlanet(int id, String translationKey, long dayLength, RegistryKey<World> registryKey) {
        this.id = id;
        this.translationKey = translationKey;
        this.dayLength = dayLength;
        this.registryKey = registryKey;
    }

    public long getDayLength() {
        return dayLength;
    }

    public int getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    @Override
    public String asString() {
        return translationKey;
    }

    public RegistryKey<World> getRegistryKey() {
        return registryKey;
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

    // Only overrides default if it's actually a cosmere planet
    public static long getDayLengthOrDefault(@Nullable World world, long defaultValue) {
        CosmerePlanet planet = CosmerePlanet.getPlanet(world);
        if (planet == CosmerePlanet.NONE) {
            return defaultValue;
        }
        return planet.getDayLength();
    }

    public static float getDayLengthMultiplier(@Nullable World world) {
        CosmerePlanet planet = CosmerePlanet.getPlanet(world);
        if (planet == CosmerePlanet.NONE) {
            return 1.0f;
        }
        return (float) planet.getDayLength() / ModConstants.VANILLA_DAY_LENGTH;
    }

    // Determines the order of how this appears in dialogs and such.
    private static CosmerePlanet[] getOrderedPlanets() {
        return new CosmerePlanet[] {
                LUMAR, NONE
        };
    }
}
