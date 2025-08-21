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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.slot.Slot;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.class)
public abstract class ItemContainerMixin {

    @Shadow
    public abstract int getMaxUseTime(ItemStack stack, LivingEntity user);

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
        if (component == null || !component.shouldShowItemBar()) {
            return original.call(stack);
        }
        return component.getOccupancy().compareTo(Fraction.ZERO) > 0;
    }

    @WrapMethod(method = "getItemBarStep")
    private int getItemContainerBarStep(ItemStack stack, Operation<Integer> original) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component == null || !component.shouldShowItemBar()) {
            return original.call(stack);
        }
        return Math.min(1 + MathHelper.multiplyFraction(component.getOccupancy(), 12), 13);
    }

    @WrapMethod(method = "getItemBarColor")
    private int getItemContainerBarColor(ItemStack stack, Operation<Integer> original) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component == null || !component.shouldShowItemBar()) {
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

    @WrapMethod(method = "getMaxUseTime")
    private int getItemContainerUseTime(ItemStack stack, LivingEntity user,
            Operation<Integer> original) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component != null && !component.isEmpty()) {
            return ItemContainerInteractions.MAX_USE_TIME;
        }
        return original.call(stack, user);
    }

    @WrapMethod(method = "use")
    private ActionResult useItemContainer(World world, PlayerEntity user, Hand hand,
            Operation<ActionResult> original) {
        ItemStack stack = user.getStackInHand(hand);
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component != null) {
            if (user.isSneaking() && component.canToggleAutoPickup()) {
                if (ItemContainerInteractions.toggleAutoPickup(stack, component, user)) {
                    user.setCurrentHand(hand);
                    return ActionResult.CONSUME;
                }
            } else if (component.canQuickDeposit(user)) {
                user.setCurrentHand(hand);
                return ActionResult.SUCCESS;
            }
        }
        return original.call(world, user, hand);
    }

    @WrapMethod(method = "usageTick")
    private void itemContainerUsageTick(World world, LivingEntity user, ItemStack stack,
            int remainingUseTicks, Operation<Void> original) {
        ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
        if (component != null && component.canQuickDeposit(user)
                && user instanceof PlayerEntity playerEntity) {
            int maxUseTime = this.getMaxUseTime(stack, user);
            boolean usedMaxTime = remainingUseTicks == maxUseTime;
            if (usedMaxTime || remainingUseTicks < maxUseTime - 10 && remainingUseTicks % 2 == 0) {
                this.dropContentsOnUse(world, playerEntity, stack, component);
            }
        }
        original.call(world, user, stack, remainingUseTicks);
    }

    @Unique
    private void dropContentsOnUse(World world, PlayerEntity player, ItemStack stack,
            ItemContainerComponent component) {
        if (ItemContainerInteractions.dropFirstContainerStack(stack, player, component)) {
            ItemContainerInteractions.playDropContentsSound(world, player, component);
            player.incrementStat(Stats.USED.getOrCreateStat((Item) (Object) this));
        }
    }

}
