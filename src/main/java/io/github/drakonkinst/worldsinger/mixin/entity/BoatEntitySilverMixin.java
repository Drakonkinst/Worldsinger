package io.github.drakonkinst.worldsinger.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.item.ModItemTags;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnstableApiUsage")
@Mixin(BoatEntity.class)
public abstract class BoatEntitySilverMixin extends VehicleEntity {

    @Unique
    private static final int SILVER_REPAIR_AMOUNT = 625;

    public BoatEntitySilverMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void addSilverLining(PlayerEntity player, Hand hand,
            CallbackInfoReturnable<ActionResult> cir) {
        SilverLined silverData = this.getAttachedOrCreate(ModAttachmentTypes.SILVER_LINED_BOAT);
        ItemStack itemStack = player.getStackInHand(hand);
        int silverDurability = silverData.getSilverDurability();
        if (itemStack.isIn(ModItemTags.SILVER_INGOTS)
                && silverDurability < silverData.getMaxSilverDurability()) {
            // Set data
            silverData.setSilverDurability(silverDurability + SILVER_REPAIR_AMOUNT);
            silverData.sync();

            // Play sound
            float pitch = 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f;
            this.playSound(ModSoundEvents.ENTITY_BOAT_LINE_SILVER, 1.0f, pitch);

            // Consume silver
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
            cir.setReturnValue(ActionResult.success(this.getWorld().isClient()));
        }
    }

    @ModifyReturnValue(method = "getPickBlockStack", at = @At("RETURN"))
    private ItemStack dropWithSilverData(ItemStack itemStack) {
        return this.addSilverData(itemStack);
    }

    @Unique
    private ItemStack addSilverData(ItemStack itemStack) {
        SilverLined.transferDataFromEntityToItemStack(this, itemStack);
        return itemStack;
    }
}
