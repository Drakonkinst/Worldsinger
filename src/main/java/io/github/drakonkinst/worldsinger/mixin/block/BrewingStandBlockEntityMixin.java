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
package io.github.drakonkinst.worldsinger.mixin.block;

import io.github.drakonkinst.worldsinger.mixin.accessor.BrewingRecipeRegistryAccessor;
import io.github.drakonkinst.worldsinger.mixin.accessor.BrewingStandBlockEntityAccessor;
import io.github.drakonkinst.worldsinger.registry.tag.ModItemTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin extends LockableContainerBlockEntity implements
        SidedInventory {

    @Unique
    private static final int BREWING_FUEL_AMOUNT = 20;

    @Unique
    private static final int FUEL_SLOT = 4;

    @Unique
    private static final int INGREDIENT_SLOT = 3;

    @SuppressWarnings({ "ReferenceToMixin", "UnresolvedMixinReference" })
    @Inject(method = "tick", at = @At("HEAD"))
    private static void consumeCustomFuels(World world, BlockPos pos, BlockState state,
            BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
        DefaultedList<ItemStack> slots = ((BrewingStandBlockEntityAccessor) blockEntity).worldsinger$getInventory();
        ItemStack itemStack = slots.get(FUEL_SLOT);
        if (((BrewingStandBlockEntityAccessor) blockEntity).worldsinger$getFuel() <= 0
                && itemStack.isIn(ModItemTags.BREWING_STAND_FUELS)) {
            ((BrewingStandBlockEntityAccessor) blockEntity).worldsinger$setFuel(
                    BREWING_FUEL_AMOUNT);

            Item remainderItem = itemStack.getItem().getRecipeRemainder();
            itemStack.decrement(1);
            if (remainderItem != null) {
                ItemStack remainderStack = remainderItem.getDefaultStack();
                if (itemStack.isEmpty()) {
                    itemStack = remainderStack;
                } else {
                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), remainderStack);
                }
            }
            slots.set(FUEL_SLOT, itemStack);
            BrewingStandBlockEntity.markDirty(world, pos, state);
        }
    }

    // BrewingStandBlockEntity#craft does not properly handle items with recipe remainders
    // (specifically when there's only 1 in the stack) so fixing this
    @Inject(method = "craft", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"), cancellable = true)
    private static void craftCorrectly(World world, BlockPos pos, DefaultedList<ItemStack> slots,
            CallbackInfo ci) {
        ItemStack itemStack = slots.get(INGREDIENT_SLOT);
        Item remainderItem = itemStack.getItem().getRecipeRemainder();
        itemStack.decrement(1);
        if (remainderItem != null) {
            ItemStack remainderStack = remainderItem.getDefaultStack();
            if (itemStack.isEmpty()) {
                itemStack = remainderStack;
            } else {
                ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), remainderStack);
            }
        }
        slots.set(INGREDIENT_SLOT, itemStack);
        world.syncWorldEvent(WorldEvents.BREWING_STAND_BREWS, pos, 0);
        ci.cancel();
    }

    protected BrewingStandBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos,
            BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @SuppressWarnings({ "ReferenceToMixin", "UnresolvedMixinReference" })
    @Inject(method = "isValid", at = @At("RETURN"), cancellable = true)
    private void allowCustomPotionsAndFuels(int slot, ItemStack stack,
            CallbackInfoReturnable<Boolean> cir) {
        if (slot == 4) {
            cir.setReturnValue(cir.getReturnValue() || stack.isIn(ModItemTags.BREWING_STAND_FUELS));
            return;
        }
        if (slot == 3 || !this.getStack(slot).isEmpty()) {
            return;
        }
        World world = this.getWorld();
        if (world != null) {
            cir.setReturnValue(
                    ((BrewingRecipeRegistryAccessor) world.getBrewingRecipeRegistry()).worldsinger$isPotionType(
                            stack));
        }
    }
}
