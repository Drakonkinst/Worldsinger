package io.github.drakonkinst.worldsinger.entity.render.state;

import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.cosmere.SilverLiningLevel;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.entity.vehicle.ChestRaftEntity;
import net.minecraft.entity.vehicle.RaftEntity;

public interface BoatEntityRenderStateSilverLining {

    int CHEST_VALUE = 8;
    int RAFT_VALUE = 4;

    // Boat variant can be represented as a binary number
    // Chest = 1 bit, Raft = 1 bit, Silver Lining State = 2 bits
    // Returns a negative number if not silver-lined
    @SuppressWarnings("UnstableApiUsage")
    static int encodeBoatVariant(AbstractBoatEntity entity) {
        SilverLined silverData = entity.getAttached(ModAttachmentTypes.SILVER_LINED_BOAT);
        if (silverData == null) {
            return -1;
        }

        float durabilityFraction =
                (float) silverData.getSilverDurability() / silverData.getMaxSilverDurability();
        SilverLiningLevel level = SilverLiningLevel.fromDurability(durabilityFraction);
        if (level == SilverLiningLevel.NONE) {
            return -1;
        }

        boolean hasChest = entity instanceof ChestBoatEntity || entity instanceof ChestRaftEntity;
        boolean isRaft = entity instanceof RaftEntity || entity instanceof ChestRaftEntity;
        int silverLiningValue = level.ordinal() - 1;
        return (hasChest ? CHEST_VALUE : 0) + (isRaft ? RAFT_VALUE : 0) + silverLiningValue;
    }

    void worldsinger$setSilverLiningVariant(int value);

    int worldsinger$getSilverLiningVariant();
}
