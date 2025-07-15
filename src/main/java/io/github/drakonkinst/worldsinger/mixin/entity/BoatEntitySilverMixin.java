/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Drakonkinst
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.drakonkinst.worldsinger.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.worldsinger.entity.attachments.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.api.sync.AttachmentSync;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.entity.SilverLinedEntityData;
import io.github.drakonkinst.worldsinger.item.component.SilverLinedComponent;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import io.github.drakonkinst.worldsinger.registry.tag.ModConventionalItemTags;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnstableApiUsage")
@Mixin(AbstractBoatEntity.class)
public abstract class BoatEntitySilverMixin extends VehicleEntity {

    public BoatEntitySilverMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void addSilverLining(PlayerEntity player, Hand hand,
            CallbackInfoReturnable<ActionResult> cir) {
        SilverLinedEntityData silverData = this.getAttachedOrCreate(
                ModAttachmentTypes.SILVER_LINED_BOAT);
        ItemStack itemStack = player.getStackInHand(hand);
        int silverDurability = silverData.getSilverDurability();
        if (itemStack.isIn(ModConventionalItemTags.SILVER_INGOTS)
                && silverDurability < silverData.getMaxSilverDurability()) {
            // Set data
            silverData.repair();
            if (!this.getWorld().isClient()) {
                AttachmentSync.sync(this, ModAttachmentTypes.SILVER_LINED_BOAT, silverData);
            }

            // Play sound
            float pitch = 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f;
            this.playSound(ModSoundEvents.ENTITY_BOAT_LINE_SILVER, 1.0f, pitch);

            // Consume silver
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @ModifyReturnValue(method = "getPickBlockStack", at = @At("RETURN"))
    private ItemStack pickBlockWithSilverData(ItemStack itemStack) {
        return this.addSilverData(itemStack);
    }

    @Unique
    private ItemStack addSilverData(ItemStack itemStack) {
        SilverLined.transferDataFromEntityToItemStack(this, itemStack);
        return itemStack;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T get(ComponentType<? extends T> type) {
        if (type == ModDataComponentTypes.SILVER_DURABILITY) {
            SilverLinedEntityData silverData = this.getAttachedOrCreate(
                    ModAttachmentTypes.SILVER_LINED_BOAT);
            return castComponentValue((ComponentType<T>) ModDataComponentTypes.SILVER_DURABILITY,
                    new SilverLinedComponent(silverData.getSilverDurability()));
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, ModDataComponentTypes.SILVER_DURABILITY);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == ModDataComponentTypes.SILVER_DURABILITY) {
            SilverLinedEntityData silverData = this.getAttachedOrCreate(
                    ModAttachmentTypes.SILVER_LINED_BOAT);
            silverData.setSilverDurability(
                    castComponentValue(ModDataComponentTypes.SILVER_DURABILITY, value).value());
            return true;
        }
        return super.setApplicableComponent(type, value);
    }
}
