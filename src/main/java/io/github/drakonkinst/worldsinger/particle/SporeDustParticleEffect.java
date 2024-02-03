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

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.dynamic.Codecs;
import org.joml.Vector3f;

public class SporeDustParticleEffect extends AbstractDustParticleEffect {

    public static final Codec<SporeDustParticleEffect> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codecs.VECTOR_3F.fieldOf("color").forGetter(effect -> effect.color),
                            Codec.FLOAT.fieldOf("scale").forGetter(AbstractDustParticleEffect::getScale))
                    .apply(instance, SporeDustParticleEffect::new));
    public static final ParticleEffect.Factory<SporeDustParticleEffect> PARAMETERS_FACTORY = new ParticleEffect.Factory<>() {

        @Override
        public SporeDustParticleEffect read(ParticleType<SporeDustParticleEffect> particleType,
                StringReader stringReader) throws CommandSyntaxException {
            Vector3f vector3f = AbstractDustParticleEffect.readColor(stringReader);
            stringReader.expect(' ');
            float f = stringReader.readFloat();
            return new SporeDustParticleEffect(vector3f, f);
        }

        @Override
        public SporeDustParticleEffect read(ParticleType<SporeDustParticleEffect> particleType,
                PacketByteBuf packetByteBuf) {
            return new SporeDustParticleEffect(AbstractDustParticleEffect.readColor(packetByteBuf),
                    packetByteBuf.readFloat());
        }
    };

    public SporeDustParticleEffect(Vector3f vector3f, float f) {
        super(vector3f, f);
    }

    public ParticleType<SporeDustParticleEffect> getType() {
        return ModParticleTypes.SPORE_DUST;
    }
}
