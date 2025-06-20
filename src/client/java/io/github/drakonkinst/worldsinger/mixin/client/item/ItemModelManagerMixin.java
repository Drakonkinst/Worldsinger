package io.github.drakonkinst.worldsinger.mixin.client.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.item.ItemOverlay;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemModelManager.class)
public abstract class ItemModelManagerMixin {

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/model/ItemModel;update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/item/ItemModelManager;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/entity/LivingEntity;I)V"))
    private void applyItemOverlays(ItemModel instance, ItemRenderState renderState, ItemStack stack,
            ItemModelManager itemModelManager, ItemDisplayContext displayContext, ClientWorld world,
            LivingEntity entity, int seed, Operation<Void> original) {
        original.call(instance, renderState, stack, itemModelManager, displayContext, world, entity,
                seed);
        ItemOverlay.applyItemOverlays(renderState, stack, itemModelManager, displayContext, world,
                entity, seed);
    }
}
