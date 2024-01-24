package io.github.drakonkinst.worldsinger.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
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

    public static final PacketCodec<RegistryByteBuf, SporeDustParticleEffect> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.VECTOR3F, effect -> effect.color, PacketCodecs.FLOAT,
            effect -> effect.scale, SporeDustParticleEffect::new);

    public static final ParticleEffect.Factory<SporeDustParticleEffect> PARAMETERS_FACTORY = (particleType, stringReader) -> {
        Vector3f vector3f = AbstractDustParticleEffect.readColor(stringReader);
        stringReader.expect(' ');
        float f = stringReader.readFloat();
        return new SporeDustParticleEffect(vector3f, f);
    };

    public SporeDustParticleEffect(Vector3f vector3f, float f) {
        super(vector3f, f);
    }

    public ParticleType<SporeDustParticleEffect> getType() {
        return ModParticleTypes.SPORE_DUST;
    }
}
