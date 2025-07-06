package io.github.drakonkinst.worldsinger.mixin.sync;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import io.github.drakonkinst.worldsinger.event.CustomClickConfigActionCallback;
import io.github.drakonkinst.worldsinger.event.CustomClickConfigActionCallback.ConfigAction;
import io.github.drakonkinst.worldsinger.network.UUIDProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.FabricServerConfigurationNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ServerConfigurationPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.c2s.common.CustomClickActionC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class ServerConfigurationNetworkHandlerMixin extends
        ServerCommonNetworkHandler implements ServerConfigurationPacketListener,
        TickablePacketListener, FabricServerConfigurationNetworkHandler, UUIDProvider {

    @Shadow
    @Final
    private GameProfile profile;
    @Unique
    private final Map<Identifier, ConfigAction> configActions = new HashMap<>();

    public ServerConfigurationNetworkHandlerMixin(MinecraftServer server,
            ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Override
    public void onCustomClickAction(CustomClickActionC2SPacket packet) {
        super.onCustomClickAction(packet);
        CustomClickConfigActionCallback.EVENT.invoker()
                .handleCustomClickAction((ServerConfigurationNetworkHandler) (Object) this,
                        configActions, packet.id(), packet.payload());
    }

    @WrapOperation(method = "onReady", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/network/ConnectedClientData;)V"))
    private void executeConfigActions(PlayerManager instance, ClientConnection connection,
            ServerPlayerEntity player, ConnectedClientData clientData, Operation<Void> original) {
        // Transition to play handler first
        original.call(instance, connection, player, clientData);

        for (ConfigAction action : configActions.values()) {
            action.setup(player);
        }
    }

    @Override
    public UUID worldsinger$getUuid() {
        return this.profile.getId();
    }
}
