package io.github.drakonkinst.worldsinger.mixin.entity.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.item.component.ItemContainerComponent;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class ItemEntityPickupMixin extends Entity implements Ownable {

    public ItemEntityPickupMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapOperation(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean autoSortIntoItemContainers(PlayerInventory instance, ItemStack stackToInsert,
            Operation<Boolean> original) {
        if (stackToInsert.isEmpty()) {
            return false;
        }

        boolean anyAccepted = false;
        for (ItemStack stack : instance) {
            ItemContainerComponent component = stack.get(ModDataComponentTypes.ITEM_CONTAINER);
            if (component == null || !component.shouldAutoPickup() || !component.canBeStored(
                    stackToInsert)) {
                continue;
            }
            ItemContainerComponent.Builder builder = new ItemContainerComponent.Builder(component);
            int numAdded = builder.add(stackToInsert);
            if (numAdded > 0) {
                anyAccepted = true;
            }
            stack.set(ModDataComponentTypes.ITEM_CONTAINER, builder.build());
            stackToInsert.decrement(numAdded);
            if (stackToInsert.isEmpty()) {
                break;
            }
        }
        if (!stackToInsert.isEmpty()) {
            boolean anyAcceptedToInventory = original.call(instance, stackToInsert);
            if (anyAcceptedToInventory) {
                anyAccepted = true;
            }
        }
        return anyAccepted;
    }
}
