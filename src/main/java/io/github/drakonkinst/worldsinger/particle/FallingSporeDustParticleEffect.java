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

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public class FallingSporeDustParticleEffect extends AbstractSporeDustParticleEffect {

    public static final MapCodec<FallingSporeDustParticleEffect> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            AetherSpores.CODEC.fieldOf("sporeType").forGetter(effect -> effect.sporeType),
                            Codec.FLOAT.fieldOf("scale").forGetter(AbstractDustParticleEffect::getScale))
                    .apply(instance, FallingSporeDustParticleEffect::new));

    public static final PacketCodec<RegistryByteBuf, FallingSporeDustParticleEffect> PACKET_CODEC = PacketCodec.tuple(
            AetherSpores.PACKET_CODEC, effect -> effect.sporeType, PacketCodecs.FLOAT,
            effect -> effect.scale, FallingSporeDustParticleEffect::new);

    public static final ParticleEffect.Factory<FallingSporeDustParticleEffect> PARAMETERS_FACTORY = (particleType, stringReader, wrapperLookup) -> {
        stringReader.expect(' ');
        String sporeName = stringReader.readString();
        AetherSpores sporeType = AetherSpores.getAetherSporeTypeFromString(sporeName)
                .orElseThrow(
                        () -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument()
                                .createWithContext(stringReader));
        stringReader.expect(' ');
        float scale = stringReader.readFloat();
        return new FallingSporeDustParticleEffect(sporeType, scale);
    };

    public FallingSporeDustParticleEffect(AetherSpores sporeType, float scale) {
        super(sporeType, scale);
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticleTypes.FALLING_SPORE_DUST;
    }
}
