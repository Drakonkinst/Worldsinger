package io.github.drakonkinst.worldsinger.entity.attachments.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record PlayerOrigin(boolean hasPlayerSelectedOrigin, CosmerePlanet startingPlanet) {

    public static final Codec<PlayerOrigin> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(Codec.BOOL.fieldOf("hasPlayerSelectedOrigin")
                                    .forGetter(PlayerOrigin::hasPlayerSelectedOrigin),
                            CosmerePlanet.CODEC.fieldOf("startingPlanet")
                                    .forGetter(PlayerOrigin::startingPlanet))
                    .apply(instance, PlayerOrigin::new));
    public static final PacketCodec<ByteBuf, PlayerOrigin> PACKET_CODEC = PacketCodecs.codec(CODEC);

    public static final PlayerOrigin DEFAULT = new PlayerOrigin(false, CosmerePlanet.NONE);

    public PlayerOrigin setStartingPlanet(CosmerePlanet startingPlanet, boolean force) {
        if (hasPlayerSelectedOrigin && !force) {
            return this;
        }

        return new PlayerOrigin(true, startingPlanet);
    }

    public PlayerOrigin setStartingPlanet(CosmerePlanet startingPlanet) {
        return setStartingPlanet(startingPlanet, false);
    }
}
