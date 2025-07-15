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
package io.github.drakonkinst.worldsinger.mixin.item;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.block.AetherSporeBlock;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.fluid.AetherSporeFluid;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlassBottleItem.class)
public abstract class GlassBottleItemMixin extends Item {

    public GlassBottleItemMixin(Settings settings) {
        super(settings);
    }

    @Shadow
    protected abstract ItemStack fill(ItemStack stack, PlayerEntity player, ItemStack outputStack);

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"), cancellable = true)
    private void fillSporeBottles(World world, PlayerEntity user, Hand hand,
            CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult blockHitResult = Item.raycast(world, user,
                RaycastContext.FluidHandling.SOURCE_ONLY);

        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos blockPos = blockHitResult.getBlockPos();
        FluidState fluidState = world.getFluidState(blockPos);
        if (fluidState.isIn(ModFluidTags.AETHER_SPORES)) {
            if (fluidState.getFluid() instanceof AetherSporeFluid aetherSporeFluid) {
                this.fillWithSporeBottle(world, user, itemStack, blockPos,
                        aetherSporeFluid.getSporeType(), cir);
            } else {
                Worldsinger.LOGGER.error(
                        "Expected aether spore fluid to extend AetherSporeFluid class");
            }
            return;
        }

        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.isIn(ModBlockTags.AETHER_SPORE_BLOCKS)) {
            if (blockState.getBlock() instanceof AetherSporeBlock aetherSporeBlock) {
                this.fillWithSporeBottle(world, user, itemStack, blockPos,
                        aetherSporeBlock.getSporeType(), cir);
            } else {
                Worldsinger.LOGGER.error(
                        "Expected aether spore block to extend AetherSporeBlock class");
            }
        }
    }

    @Unique
    private void fillWithSporeBottle(World world, PlayerEntity user, ItemStack itemStack,
            BlockPos blockPos, AetherSpores sporeType, CallbackInfoReturnable<ActionResult> cir) {
        world.playSound(user, user.getX(), user.getY(), user.getZ(),
                ModSoundEvents.ITEM_BOTTLE_FILL_AETHER_SPORE, SoundCategory.NEUTRAL, 1.0f, 1.0f);
        world.emitGameEvent(user, GameEvent.FLUID_PICKUP, blockPos);
        cir.setReturnValue(ActionResult.SUCCESS.withNewHandStack(
                this.fill(itemStack, user, sporeType.getBottledItem().getDefaultStack())));
    }
}
