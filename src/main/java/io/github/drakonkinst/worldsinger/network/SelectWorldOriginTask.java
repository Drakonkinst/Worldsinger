package io.github.drakonkinst.worldsinger.network;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.dialog.ModDialogs;
import java.util.function.Consumer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.ShowDialogS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerConfigurationTask;

public record SelectWorldOriginTask(MinecraftServer server) implements
        ServerPlayerConfigurationTask {

    public static final Key KEY = new Key(Worldsinger.idStr("select_world_origin"));

    @Override
    public void sendPacket(Consumer<Packet<?>> sender) {
        sender.accept(new ShowDialogS2CPacket(server.getRegistryManager()
                .getOrThrow(RegistryKeys.DIALOG)
                .getOrThrow(ModDialogs.WORLDHOP_CONFIG)));
    }

    @Override
    public Key getKey() {
        return KEY;
    }
}
