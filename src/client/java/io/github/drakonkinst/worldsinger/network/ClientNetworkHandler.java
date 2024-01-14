package io.github.drakonkinst.worldsinger.network;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.component.PossessionComponent;
import io.github.drakonkinst.worldsinger.cosmere.ShapeshiftingManager;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.Shapeshifter;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

@SuppressWarnings("UnqualifiedStaticUsage")
public final class ClientNetworkHandler {

    public static void registerPacketHandlers() {
        registerShapeshiftingPacketHandlers();
        registerPossessionPacketHandlers();
    }

    private static void registerShapeshiftingPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(
                ShapeshiftingManager.SHAPESHIFTER_SYNC_PACKET_ID,
                (client, handler, buf, responseSender) -> handleShapeshifterSyncPacket(client,
                        buf));
        ClientPlayNetworking.registerGlobalReceiver(
                ShapeshiftingManager.SHAPESHIFTER_ATTACK_PACKET_ID,
                (client, handler, buf, responseSender) -> handleShapeshifterAttackPacket(client,
                        buf));
    }

    private static void registerPossessionPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(CameraPossessable.POSSESS_SET_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    if (client.world == null || client.player == null) {
                        return;
                    }

                    final int entityIdToPossess = buf.readVarInt();
                    PossessionComponent possessionData = ModComponents.POSSESSION.get(
                            client.player);
                    if (entityIdToPossess < 0) {
                        possessionData.resetPossessionTarget();
                    } else {
                        // Display dismount prompt, regardless of whether entity is already set
                        PossessionClientUtil.displayPossessStartText();

                        CameraPossessable currentPossessedEntity = possessionData.getPossessionTarget();
                        if (currentPossessedEntity != null
                                && currentPossessedEntity.toEntity().getId() == entityIdToPossess) {
                            // Already set
                            return;
                        }
                        Entity entity = client.world.getEntityById(entityIdToPossess);
                        if (entity instanceof CameraPossessable cameraPossessable) {
                            possessionData.setPossessionTarget(cameraPossessable);
                        }
                    }
                });
    }

    private static void handleShapeshifterSyncPacket(MinecraftClient client, PacketByteBuf buf) {
        final int id = buf.readVarInt();
        final String entityId = buf.readString();
        final NbtCompound entityNbt = buf.readNbt();

        if (client.world == null) {
            Worldsinger.LOGGER.warn("Failed to process sync packet since world is unloaded");
            return;
        }

        Entity entity = client.world.getEntityById(id);
        if (entity == null) {
            Worldsinger.LOGGER.warn(
                    "Failed to process sync packet since entity with ID " + id + " does not exist");
            return;
        }
        if (!(entity instanceof Shapeshifter shapeshifter)) {
            Worldsinger.LOGGER.warn("Failed to process sync packet since entity with ID " + id
                    + " exists but is not a shapeshifter");
            return;
        }

        if (entityId.equals(ShapeshiftingManager.EMPTY_MORPH)) {
            shapeshifter.updateMorph(null);
            return;
        }

        if (entityNbt != null) {
            entityNbt.putString(Entity.ID_KEY, entityId);
            ShapeshiftingManager.createMorphFromNbt(shapeshifter, entityNbt, true);
        }
    }

    private static void handleShapeshifterAttackPacket(MinecraftClient client, PacketByteBuf buf) {
        final int id = buf.readVarInt();

        if (client.world == null) {
            Worldsinger.LOGGER.warn("Failed to process attack packet since world is unloaded");
            return;
        }

        Entity entity = client.world.getEntityById(id);
        if (entity == null) {
            Worldsinger.LOGGER.warn("Failed to process attack packet since entity with ID " + id
                    + " does not exist");
            return;
        }
        if (!(entity instanceof Shapeshifter shapeshifter)) {
            Worldsinger.LOGGER.warn("Failed to process attack packet since entity with ID " + id
                    + " exists but is not a shapeshifter");
            return;
        }
        ShapeshiftingManager.onAttackClient(shapeshifter);
    }

    private ClientNetworkHandler() {}
}
