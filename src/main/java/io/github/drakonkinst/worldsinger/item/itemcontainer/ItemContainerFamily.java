package io.github.drakonkinst.worldsinger.item.itemcontainer;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface ItemContainerFamily {

    Item getEmptyItem();

    Item getVariantFor(ItemStack firstEntry);
}
