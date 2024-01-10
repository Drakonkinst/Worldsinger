package io.github.drakonkinst.worldsinger.network;

import com.mojang.authlib.GameProfile;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy {

    private static final Perspective[] PERSPECTIVES = Perspective.values();

    private Perspective previousPerspective = Perspective.FIRST_PERSON;
    private boolean usingCustomRenderView = false;

    @Override
    public LivingEntity createPlayerMorph(World world, UUID uuid, String playerName) {
        if (!world.isClient()) {
            // If running in singleplayer, this can override the instance. Revert to
            // normal behavior to avoid calling client code.
            return super.createPlayerMorph(world, uuid, playerName);
        }
        ClientWorld clientWorld = (ClientWorld) world;
        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
        if (handler == null) {
            return createDefaultPlayer(clientWorld, uuid, playerName);
        }

        PlayerListEntry playerListEntry = handler.getPlayerListEntry(uuid);
        if (playerListEntry == null) {
            return createDefaultPlayer(clientWorld, uuid, playerName);
        } else {
            return new OtherClientPlayerEntity(clientWorld, playerListEntry.getProfile());
        }
    }

    // TODO: We can also try fetching the skin data, to be even more accurate
    // But this works for now
    private LivingEntity createDefaultPlayer(ClientWorld world, UUID uuid, String playerName) {
        return new OtherClientPlayerEntity(world, new GameProfile(uuid, playerName));
    }

    public void setRenderViewEntity(Entity entity) {
        Worldsinger.LOGGER.info("Set render view to " + entity.getName().getString());
        Worldsinger.LOGGER.info(
                "UUID 1: " + ModComponents.POSSESSION.get(MinecraftClient.getInstance().player)
                        .getPossessedEntityUuid());
        previousPerspective = MinecraftClient.getInstance().options.getPerspective();
        MinecraftClient.getInstance().setCameraEntity(entity);
        Worldsinger.LOGGER.info("Camera entity is now " + MinecraftClient.getInstance()
                .getCameraEntity()
                .getName()
                .getString());

        // Set perspective of camera entity as well
        if (entity instanceof CameraPossessable cameraPossessable) {
            int perspectiveOrdinal = cameraPossessable.getDefaultPerspective();
            if (perspectiveOrdinal > -1) {
                MinecraftClient.getInstance().options.setPerspective(
                        PERSPECTIVES[perspectiveOrdinal]);
            }
        }
        usingCustomRenderView = true;
    }

    public boolean resetRenderViewEntity() {
        if (!usingCustomRenderView) {
            return false;
        }
        Worldsinger.LOGGER.info(
                "UUID 2: " + ModComponents.POSSESSION.get(MinecraftClient.getInstance().player)
                        .getPossessedEntityUuid());
        Worldsinger.LOGGER.info("RESET");
        MinecraftClient.getInstance().setCameraEntity(MinecraftClient.getInstance().player);
        MinecraftClient.getInstance().options.setPerspective(previousPerspective);
        usingCustomRenderView = false;
        return true;
    }
}
