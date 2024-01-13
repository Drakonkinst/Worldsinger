package io.github.drakonkinst.worldsinger.entity.data;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.component.PossessionComponent;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.mixin.accessor.PlayerEntityInvoker;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class PossessionPlayerData implements PossessionComponent {

    private final PlayerEntity player;
    private CameraPossessable possessionTarget;
    private boolean shouldResetCamera = false;

    public PossessionPlayerData(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void setPossessionTarget(CameraPossessable entity) {
        if (!entity.equals(possessionTarget)) {
            entity.onStartPossessing(player);
        }
        this.possessionTarget = entity;
        // Send packet to client
        if (player instanceof ServerPlayerEntity serverPlayerEntity) {
            ServerPlayNetworking.send(serverPlayerEntity, CameraPossessable.POSSESS_SET_PACKET_ID,
                    CameraPossessable.createSetPacket(possessionTarget));
        }
    }

    @Override
    public void resetPossessionTarget() {
        if (possessionTarget != null) {
            possessionTarget.onStopPossessing(player);
        }
        this.possessionTarget = null;

        // Send packet to client
        if (player instanceof ServerPlayerEntity serverPlayerEntity) {
            ServerPlayNetworking.send(serverPlayerEntity, CameraPossessable.POSSESS_SET_PACKET_ID,
                    CameraPossessable.createSetPacket(null));
        } else if (player.getWorld().isClient()) {
            // Since this method can be called from events, need to defer camera changes to the render thread
            shouldResetCamera = true;
        }
    }

    @Override
    @Nullable
    public CameraPossessable getPossessionTarget() {
        return possessionTarget;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        // Intentionally empty
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        // Intentionally empty
    }

    @Override
    public void clientTick() {
        // Force the camera to follow the possessed entity
        if (possessionTarget != null && !possessionTarget.toEntity().isRemoved()) {
            Worldsinger.PROXY.setRenderViewEntity(possessionTarget.toEntity());
        }

        // Reset the camera
        if (shouldResetCamera) {
            Worldsinger.PROXY.resetRenderViewEntity();
            shouldResetCamera = false;
        }
    }

    @Override
    public void serverTick() {
        if (!isPossessing() || possessionTarget == null) {
            return;
        }

        LivingEntity possessedEntity = possessionTarget.toEntity();
        if (possessedEntity.isDead() || doesPlayerWantToExit() || !isInRange(possessedEntity)
                || !possessionTarget.shouldKeepPossessing(player)) {
            resetPossessionTarget();
        }
    }

    private boolean doesPlayerWantToExit() {
        return ((PlayerEntityInvoker) player).worldsinger$shouldDismount();
    }

    private boolean isInRange(LivingEntity entity) {
        final float maxPossessionDistance = possessionTarget.getMaxPossessionDistance();
        return entity.squaredDistanceTo(player) <= maxPossessionDistance * maxPossessionDistance;
    }

    @Override
    public void tick() {

    }
}
