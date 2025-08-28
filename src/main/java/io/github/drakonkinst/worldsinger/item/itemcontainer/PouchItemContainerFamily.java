package io.github.drakonkinst.worldsinger.item.itemcontainer;

import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.registry.tag.ModItemTags;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PouchItemContainerFamily implements ItemContainerFamily {

    public static final PouchItemContainerFamily INSTANCE = new PouchItemContainerFamily();

    private PouchItemContainerFamily() {}

    @Override
    public Item getEmptyItem() {
        return ModItems.POUCH;
    }

    @Override
    public Item getVariantFor(ItemStack firstEntry) {
        // Hardcode this for now, ways to make it data-driven later
        if (firstEntry.isIn(ModItemTags.SPHERES)) {
            return ModItems.POUCH_OF_SPHERES;
        }
        return null;
    }
}
