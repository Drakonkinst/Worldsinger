/*
 * MIT License
 *
 * Copyright (c) 2024 Drakonkinst
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

import io.github.drakonkinst.worldsinger.cosmere.WaterReactionManager;
import io.github.drakonkinst.worldsinger.cosmere.WaterReactive;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin extends Item {

    @Unique
    private static final int WATER_AMOUNT = 75;

    public PotionItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void triggerSpores(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();
        PotionContentsComponent potionContentsComponent = itemStack.getOrDefault(
                DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        BlockState blockState = world.getBlockState(blockPos);
        WaterReactive waterReactive = WaterReactionManager.getIfWaterReactive(blockPos, blockState);

        if (playerEntity != null && context.getSide() != Direction.DOWN && waterReactive != null
                && potionContentsComponent.matches(Potions.WATER)) {
            world.playSound(null, blockPos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS,
                    1.0F, 1.0F);
            playerEntity.setStackInHand(context.getHand(),
                    ItemUsage.exchangeStack(itemStack, playerEntity,
                            new ItemStack(Items.GLASS_BOTTLE)));
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(itemStack.getItem()));
            if (!world.isClient) {
                ServerWorld serverWorld = (ServerWorld) world;
                for (int i = 0; i < 5; i++) {
                    serverWorld.spawnParticles(ParticleTypes.SPLASH,
                            (double) blockPos.getX() + world.random.nextDouble(),
                            (double) (blockPos.getY() + 1),
                            (double) blockPos.getZ() + world.random.nextDouble(), 1, 0.0, 0.0, 0.0,
                            1.0);
                }
            }
            world.playSound(null, blockPos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS,
                    1.0F, 1.0F);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, blockPos);
            waterReactive.reactToWater(world, blockPos, WATER_AMOUNT);
            cir.setReturnValue(ActionResult.success(world.isClient));
        }
    }
}
