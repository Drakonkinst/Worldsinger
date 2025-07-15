package io.github.drakonkinst.worldsinger.mixin.sync;

import io.github.drakonkinst.worldsinger.event.CustomClickActionCallback;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.c2s.common.CustomClickActionC2SPacket;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler implements
        PlayStateFactories.PacketCodecModifierContext, ServerPlayPacketListener,
        PlayerAssociatedNetworkHandler, TickablePacketListener {

    @Shadow
    public ServerPlayerEntity player;

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection,
            ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Override
    public void onCustomClickAction(CustomClickActionC2SPacket packet) {
        super.onCustomClickAction(packet);
        CustomClickActionCallback.EVENT.invoker()
                .handleCustomClickAction(this.player, packet.id(), packet.payload());
    }
}
