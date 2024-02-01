package io.github.drakonkinst.worldsinger.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.datatables.DataTableRegistry;
import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.registry.ModDataTables;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class ItemMixin {

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;canConsume(Z)Z"))
    private boolean allowEatingIfAffectsThirst(PlayerEntity instance, boolean ignoreHunger,
            Operation<Boolean> original) {
        if (original.call(instance, ignoreHunger)) {
            return true;
        }

        return !ModComponents.THIRST_MANAGER.get(instance).isFull()
                && DataTableRegistry.INSTANCE.get(ModDataTables.CONSUMABLE_HYDRATION)
                .contains((Item) (Object) this);
    }
}
