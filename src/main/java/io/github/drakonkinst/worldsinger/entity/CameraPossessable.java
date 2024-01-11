package io.github.drakonkinst.worldsinger.entity;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

// Indicates a mob that can be possessed by the player, which changes the camera client-side only.
// Should use PossessionComponent
public interface CameraPossessable {

    int FIRST_PERSON = 0;
    int THIRD_PERSON_BACK = 1;
    int THIRD_PERSON_FRONT = 2;

    enum AttackOrigin {
        POSSESSOR, POSSESSED, DISABLED
    }

    Identifier POSSESS_UPDATE_PACKET_ID = Worldsinger.id("possession_update");
    Identifier POSSESS_ATTACK_PACKET_ID = Worldsinger.id("possession_attack");

    static PacketByteBuf createSyncPacket(float headYaw, float bodyYaw, float pitch,
            float forwardSpeed, float sidewaysSpeed, boolean jumping, boolean sprinting) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeFloat(headYaw);
        buf.writeFloat(bodyYaw);
        buf.writeFloat(pitch);
        buf.writeFloat(forwardSpeed);
        buf.writeFloat(sidewaysSpeed);
        buf.writeBoolean(jumping);
        buf.writeBoolean(sprinting);
        return buf;
    }

    static PacketByteBuf createAttackPacket(Entity target) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(target.getId());
        return buf;
    }

    void commandMovement(float headYaw, float bodyYaw, float pitch, float forwardSpeed,
            float sidewaysSpeed, boolean jumping, boolean sprinting);

    void onStartPossessing(PlayerEntity possessor);

    void onStopPossessing(PlayerEntity possessor);

    boolean shouldKeepPossessing(PlayerEntity possessor);

    boolean isBeingPossessed();

    LivingEntity toEntity();

    default int getDefaultPerspective() {
        return FIRST_PERSON;
    }

    // Enables any left-click actions, including attacking entities and breaking blocks
    default boolean canPerformAttack() {
        return false;
    }

    default AttackOrigin getEntityAttackOrigin() {
        return AttackOrigin.DISABLED;
    }

    default boolean canPickBlock() {
        return false;
    }

    // Includes all left-click block interaction
    default boolean canBreakBlock() {
        return false;
    }

    default boolean canInteractWithEntities() {
        return false;
    }

    default boolean canSwitchHotbarItem() {
        return false;
    }

    default boolean canMoveSelf() {
        return false;
    }

    default boolean canInteractWithBlocks() {
        return false;
    }

    default boolean canSwitchPerspectives() {
        return false;
    }
}
