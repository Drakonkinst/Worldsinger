package io.github.drakonkinst.worldsinger.item;

import io.github.drakonkinst.worldsinger.entity.SilverLinedBoatData;
import net.minecraft.item.ItemStack;

public class SilverLinedBoatItemData extends SilverLinedItemData {

    public SilverLinedBoatItemData(ItemStack stack) {
        super(stack);
    }

    @Override
    public int getMaxSilverDurability() {
        return SilverLinedBoatData.MAX_DURABILITY;
    }

    @Override
    public void sync() {
        // Item does not sync
    }
}
