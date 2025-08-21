package io.github.drakonkinst.worldsinger.item.itemcontainer;

import io.github.drakonkinst.worldsinger.item.component.ItemContainerComponent;
import io.github.drakonkinst.worldsinger.item.component.ItemContainerComponent.Builder;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.World;

public final class ItemContainerInteractions {

    public static final int FULL_ITEM_BAR_COLOR = ColorHelper.fromFloats(1.0F, 1.0F, 0.33F, 0.33F);
    public static final int ITEM_BAR_COLOR = ColorHelper.fromFloats(1.0F, 0.44F, 0.53F, 1.0F);
    public static final int MAX_USE_TIME = 200;

    private ItemContainerInteractions() {}

    public static boolean toggleAutoPickup(ItemStack stack, ItemContainerComponent component,
            PlayerEntity player) {
        if (component == null || !component.canToggleAutoPickup()) {
            return false;
        }
        ItemContainerComponent.Builder builder = new ItemContainerComponent.Builder(component);
        boolean isAutoPickupDisabled = builder.toggleAutoPickup();
        if (isAutoPickupDisabled) {
            player.sendMessage(
                    Text.translatable("item.worldsinger.item_container.auto_pickup_disabled"),
                    true);
        } else {
            player.sendMessage(
                    Text.translatable("item.worldsinger.item_container.auto_pickup_enabled"), true);
        }
        stack.set(ModDataComponentTypes.ITEM_CONTAINER, builder.build());
        return true;
    }

    public static boolean dropFirstContainerStack(ItemStack stack, PlayerEntity player,
            ItemContainerComponent component) {
        if (component != null && !component.isEmpty()) {
            Optional<ItemStack> optional = popFirstContainerStack(stack, player, component);
            if (optional.isPresent()) {
                player.dropItem(optional.get(), true);
                return true;
            }
            return false;
        }
        return false;
    }

    private static Optional<ItemStack> popFirstContainerStack(ItemStack stack, PlayerEntity player,
            ItemContainerComponent component) {
        ItemContainerComponent.Builder builder = new ItemContainerComponent.Builder(component);
        ItemStack itemStack = builder.removeSelected();
        if (itemStack != null) {
            playRemoveOneSound(player, component);
            stack.set(ModDataComponentTypes.ITEM_CONTAINER, builder.build());
            return Optional.of(itemStack);
        } else {
            return Optional.empty();
        }
    }

    public static float getAmountFilled(ItemStack stack) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component != null) {
            return component.getOccupancy().floatValue();
        }
        return 0.0f;
    }

    public static int getSelectedStackIndex(ItemStack stack) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component != null) {
            return component.getSelectedStackIndex();
        }
        return -1;
    }

    public static int getNumberOfStacksShown(ItemStack stack) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component != null) {
            return component.getNumberOfStacksShown();
        }
        return 0;
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
                playInsertSound(player, component);
            } else {
                playInsertFailSound(player, component);
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
                    playRemoveOneSound(player, component);
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
                ItemContainerInteractions.playInsertSound(player, component);
            } else {
                ItemContainerInteractions.playInsertFailSound(player, component);
            }

            stack.set(ModDataComponentTypes.ITEM_CONTAINER, builder.build());
            onContentChanged(player);
            return true;
        } else if (clickType == ClickType.RIGHT && otherStack.isEmpty()) {
            if (slot.canTakePartial(player)) {
                ItemStack removedStack = builder.removeSelected();
                if (!removedStack.isEmpty()) {
                    ItemContainerInteractions.playRemoveOneSound(player, component);
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

    public static void setSelectedStackIndex(ItemStack stack, int selectedStackIndex) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component != null) {
            ItemContainerComponent.Builder builder = new Builder(component);
            builder.setSelectedStackIndex(selectedStackIndex);
            stack.set(ModDataComponentTypes.ITEM_CONTAINER, builder.build());
        }
    }

    private static void playRemoveOneSound(Entity entity, ItemContainerComponent component) {
        entity.playSound(component.getSettings().getRemoveOneSound(), 0.8F,
                0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(Entity entity, ItemContainerComponent component) {
        entity.playSound(component.getSettings().getInsertSound(), 0.8F,
                0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertFailSound(Entity entity, ItemContainerComponent component) {
        entity.playSound(component.getSettings().getInsertFailSound(), 1.0F, 1.0F);
    }

    public static void playDropContentsSound(World world, Entity entity,
            ItemContainerComponent component) {
        world.playSound(null, entity.getBlockPos(), component.getSettings().getDropContentsSound(),
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
