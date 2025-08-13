package io.github.drakonkinst.worldsinger.mixin.item;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.drakonkinst.worldsinger.item.component.ItemContainerComponent;
import io.github.drakonkinst.worldsinger.item.itemcontainer.ItemContainerInteractions;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class ItemContainerMixin {

    @WrapMethod(method = "onClicked")
    private boolean onItemContainerClicked(ItemStack stack, ItemStack otherStack, Slot slot,
            ClickType clickType, PlayerEntity player, StackReference cursorStackReference,
            Operation<Boolean> original) {
        if (stack.contains(ModDataComponentTypes.ITEM_CONTAINER)) {
            return ItemContainerInteractions.onItemContainerClicked(stack, otherStack, slot,
                    clickType, player, cursorStackReference);
        }
        return original.call(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    @WrapMethod(method = "onStackClicked")
    private boolean onItemContainerStackClicked(ItemStack stack, Slot slot, ClickType clickType,
            PlayerEntity player, Operation<Boolean> original) {
        if (stack.contains(ModDataComponentTypes.ITEM_CONTAINER)) {
            return ItemContainerInteractions.onItemContainerStackClicked(stack, slot, clickType,
                    player);
        }
        return original.call(stack, slot, clickType, player);
    }

    @WrapMethod(method = "isItemBarVisible")
    private boolean isItemContainerBarVisible(ItemStack stack, Operation<Boolean> original) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component == null) {
            return original.call(stack);
        }
        return component.getOccupancy().compareTo(Fraction.ZERO) > 0;
    }

    @WrapMethod(method = "getItemBarStep")
    private int getItemContainerBarStep(ItemStack stack, Operation<Integer> original) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component == null) {
            return original.call(stack);
        }
        return Math.min(1 + MathHelper.multiplyFraction(component.getOccupancy(), 12), 13);
    }

    @WrapMethod(method = "getItemBarColor")
    private int getItemContainerBarColor(ItemStack stack, Operation<Integer> original) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component == null) {
            return original.call(stack);
        }
        return component.getOccupancy().compareTo(Fraction.ONE) >= 0
                ? ItemContainerInteractions.FULL_ITEM_BAR_COLOR
                : ItemContainerInteractions.ITEM_BAR_COLOR;
    }

    @WrapMethod(method = "getTooltipData")
    private Optional<TooltipData> getTooltipData(ItemStack stack,
            Operation<Optional<TooltipData>> original) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component == null) {
            return original.call(stack);
        }
        TooltipDisplayComponent tooltipDisplayComponent = stack.getOrDefault(
                DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT);
        return !tooltipDisplayComponent.shouldDisplay(ModDataComponentTypes.ITEM_CONTAINER)
                ? Optional.empty() : Optional.of(component);
    }

    @WrapMethod(method = "onItemEntityDestroyed")
    private void onItemContainerEntityDestroyed(ItemEntity entity, Operation<Void> original) {
        original.call(entity);
        ItemContainerComponent component = entity.getStack()
                .get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component != null) {
            ItemUsage.spawnItemContents(entity, component.iterateCopy());
        }

    }

}
