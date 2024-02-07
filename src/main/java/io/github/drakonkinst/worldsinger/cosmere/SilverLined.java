package io.github.drakonkinst.worldsinger.cosmere;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.api.ModApi;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.api.SyncableAttachment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;

@SuppressWarnings("UnstableApiUsage")
public interface SilverLined extends SyncableAttachment {

    static void transferDataFromEntityToItemStack(Entity entity, ItemStack itemStack) {
        SilverLined silverEntityData = entity.getAttached(ModAttachmentTypes.SILVER_LINED_BOAT);
        if (silverEntityData != null && silverEntityData.getSilverDurability() > 0) {
            SilverLined silverItemData = ModApi.SILVER_LINED_ITEM.find(itemStack, null);
            if (silverItemData != null) {
                silverItemData.setSilverDurability(silverEntityData.getSilverDurability());
            } else {
                Worldsinger.LOGGER.error("Expected to find silver data for new boat item");
            }
        }
    }

    static void transferDataFromItemStackToEntity(ItemStack itemStack, BoatEntity entity) {
        SilverLined silverItemData = ModApi.SILVER_LINED_ITEM.find(itemStack, null);
        if (silverItemData == null || silverItemData.getSilverDurability() <= 0) {
            return;
        }
        SilverLined silverEntityData = entity.getAttachedOrCreate(
                ModAttachmentTypes.SILVER_LINED_BOAT);
        silverEntityData.setSilverDurability(silverItemData.getSilverDurability());
    }

    void setSilverDurability(int durability);

    int getSilverDurability();

    int getMaxSilverDurability();
}
