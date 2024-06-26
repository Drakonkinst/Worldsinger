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
package io.github.drakonkinst.worldsinger.particle;

import com.mojang.serialization.MapCodec;
import io.github.drakonkinst.worldsinger.Worldsinger;
import java.util.function.Function;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

@SuppressWarnings("UnqualifiedStaticUsage")
public final class ModParticleTypes {

    public static final ParticleType<SporeDustParticleEffect> SPORE_DUST = register("spore_dust",
            true, type -> SporeDustParticleEffect.CODEC,
            type -> SporeDustParticleEffect.PACKET_CODEC);
    public static final ParticleType<FallingSporeDustParticleEffect> FALLING_SPORE_DUST = register(
            "falling_spore_dust", true, type -> FallingSporeDustParticleEffect.CODEC,
            type -> FallingSporeDustParticleEffect.PACKET_CODEC);

    public static final SimpleParticleType MIDNIGHT_ESSENCE = register("midnight_essence", false);
    public static final SimpleParticleType MIDNIGHT_TRAIL = register("midnight_trail", false);

    public static void initialize() {}

    private static SimpleParticleType register(String name, boolean alwaysShow) {
        return Registry.register(Registries.PARTICLE_TYPE, Worldsinger.id(name),
                FabricParticleTypes.simple(alwaysShow));
    }

    private static <T extends ParticleEffect> ParticleType<T> register(String name,
            boolean alwaysShow, Function<ParticleType<T>, MapCodec<T>> codecGetter,
            Function<ParticleType<T>, PacketCodec<? super RegistryByteBuf, T>> packetCodecGetter) {

        return Registry.register(Registries.PARTICLE_TYPE, Worldsinger.id(name),
                FabricParticleTypes.complex(alwaysShow, codecGetter, packetCodecGetter));
    }

    private ModParticleTypes() {}
}
