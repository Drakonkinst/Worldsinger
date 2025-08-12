package io.github.drakonkinst.worldsinger.item;

import io.github.drakonkinst.worldsinger.item.component.ItemContainerComponent;
import io.github.drakonkinst.worldsinger.item.component.ItemContainerComponent.Builder;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.World;

public final class ItemContainerInteractions {

    public static final int FULL_ITEM_BAR_COLOR = ColorHelper.fromFloats(1.0F, 1.0F, 0.33F, 0.33F);
    public static final int ITEM_BAR_COLOR = ColorHelper.fromFloats(1.0F, 0.44F, 0.53F, 1.0F);

    private ItemContainerInteractions() {}

    public static float getAmountFilled(ItemStack stack) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component != null) {
            return component.getOccupancy().floatValue();
        }
        return 0.0f;
    }

    public static boolean onItemContainerStackClicked(ItemStack stack, Slot slot,
            ClickType clickType, PlayerEntity player) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component == null) {
            return false;
        }
        ItemStack slotStack = slot.getStack();
        ItemContainerComponent.Builder builder = new ItemContainerComponent.Builder(component);
        if (clickType == ClickType.LEFT && !slotStack.isEmpty()) {
            if (builder.add(slot, player) > 0) {
                playInsertSound(player);
            } else {
                playInsertFailSound(player);
            }

            stack.set(ModDataComponentTypes.ITEM_CONTAINER, builder.build());
            onContentChanged(player);
            return true;
        } else if (clickType == ClickType.RIGHT && slotStack.isEmpty()) {
            ItemStack removedStack = builder.removeSelected();
            if (!removedStack.isEmpty()) {
                ItemStack insertedStack = slot.insertStack(removedStack);
                if (insertedStack.getCount() > 0) {
                    builder.add(insertedStack);
                } else {
                    playRemoveOneSound(player);
                }
            }

            stack.set(ModDataComponentTypes.ITEM_CONTAINER, builder.build());
            onContentChanged(player);
            return true;
        }
        return false;
    }

    public static boolean onItemContainerClicked(ItemStack stack, ItemStack otherStack, Slot slot,
            ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.LEFT && otherStack.isEmpty()) {
            ItemContainerInteractions.setSelectedStackIndex(stack, -1);
            return false;
        }
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component == null) {
            return false;
        }
        ItemContainerComponent.Builder builder = new ItemContainerComponent.Builder(component);
        if (clickType == ClickType.LEFT && !otherStack.isEmpty()) {
            if (slot.canTakePartial(player) && builder.add(otherStack) > 0) {
                ItemContainerInteractions.playInsertSound(player);
            } else {
                ItemContainerInteractions.playInsertFailSound(player);
            }

            stack.set(ModDataComponentTypes.ITEM_CONTAINER, builder.build());
            onContentChanged(player);
            return true;
        } else if (clickType == ClickType.RIGHT && otherStack.isEmpty()) {
            if (slot.canTakePartial(player)) {
                ItemStack removedStack = builder.removeSelected();
                if (!removedStack.isEmpty()) {
                    ItemContainerInteractions.playRemoveOneSound(player);
                    cursorStackReference.set(removedStack);
                }
            }

            stack.set(ModDataComponentTypes.ITEM_CONTAINER, builder.build());
            onContentChanged(player);
            return true;
        } else {
            ItemContainerInteractions.setSelectedStackIndex(stack, -1);
            return false;
        }
    }

    private static void setSelectedStackIndex(ItemStack stack, int selectedStackIndex) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component != null) {
            ItemContainerComponent.Builder builder = new Builder(component);
            builder.setSelectedStackIndex(selectedStackIndex);
            stack.set(ModDataComponentTypes.ITEM_CONTAINER, builder.build());
        }
    }

    private static void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F,
                0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F,
                0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertFailSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
    }

    private static void playDropContentsSound(World world, Entity entity) {
        world.playSound(null, entity.getBlockPos(), SoundEvents.ITEM_BUNDLE_DROP_CONTENTS,
                SoundCategory.PLAYERS, 0.8F,
                0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private static void onContentChanged(PlayerEntity user) {
        ScreenHandler screenHandler = user.currentScreenHandler;
        if (screenHandler != null) {
            screenHandler.onContentChanged(user.getInventory());
        }
    }
}
