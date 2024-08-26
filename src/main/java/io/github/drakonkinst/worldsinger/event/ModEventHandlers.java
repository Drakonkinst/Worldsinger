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
package io.github.drakonkinst.worldsinger.event;

import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.api.sync.AttachmentSync;
import io.github.drakonkinst.worldsinger.block.LivingSporeGrowthBlock;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManagerAccess;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightAetherBondManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeKillingUtil;
import io.github.drakonkinst.worldsinger.effect.ModStatusEffects;
import io.github.drakonkinst.worldsinger.registry.tag.ModItemTags;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public final class ModEventHandlers {

    @SuppressWarnings("UnstableApiUsage")
    public static void initialize() {
        // Add Thirst-related effects when consuming an item
        FinishConsumingItemCallback.EVENT.register((entity, stack, foodComponent) -> {
            if (entity instanceof PlayerEntity player) {
                player.getAttachedOrCreate(ModAttachmentTypes.THIRST).drink(stack.getItem(), stack);

                // Status effects should only be added on server side
                if (!entity.getWorld().isClient()) {
                    if (stack.isIn(ModItemTags.ALWAYS_GIVE_THIRST)) {
                        player.addStatusEffect(
                                new StatusEffectInstance(ModStatusEffects.THIRST, 600, 0));
                    }
                    if (stack.isIn(ModItemTags.CHANCE_TO_GIVE_THIRST)
                            && entity.getWorld().getRandom().nextInt(5) != 0) {
                        player.addStatusEffect(
                                new StatusEffectInstance(ModStatusEffects.THIRST, 600, 0));
                    }
                }
            }
        });

        // Kill spore growth blocks when first mining them with a silver tool
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player.isSpectator()) {
                return ActionResult.PASS;
            }
            BlockState state = world.getBlockState(pos);
            if (!(state.getBlock() instanceof LivingSporeGrowthBlock sporeGrowth)) {
                return ActionResult.PASS;
            }

            if (SporeKillingUtil.killSporeGrowthUsingTool(world, sporeGrowth, state, pos, player,
                    hand)) {
                return ActionResult.success(true);
            }
            return ActionResult.PASS;
        });

        // Kill spore growth blocks when interacting with them with a silver tool
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSpectator()) {
                return ActionResult.PASS;
            }
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (!(state.getBlock() instanceof LivingSporeGrowthBlock sporeGrowth)) {
                return ActionResult.PASS;
            }

            // This automatically checks the item too
            if (SporeKillingUtil.killSporeGrowthUsingTool(world, sporeGrowth, state, pos, player,
                    hand)) {
                return ActionResult.success(true);
            }
            return ActionResult.PASS;
        });

        // When a player takes a successful melee attack from a silver tool, dispel their Midnight/
        // Luhel bonds.
        ServerPlayerHurtCallback.EVENT.register(
                (player, source, damageDealt, damageTaken, wasBlocked) -> {
                    // We assume that if the amount is greater than 0, and it was direct, then it was a
                    // successful (non-blocked) melee attack from the main hand
                    Entity attacker = source.getAttacker();
                    if (!wasBlocked && damageTaken > 0.0f && source.isDirect()
                            && attacker instanceof LivingEntity livingEntity) {
                        ItemStack attackingItem = livingEntity.getMainHandStack();

                        if (attackingItem.isIn(ModItemTags.KILLS_SPORE_GROWTHS)) {
                            MidnightAetherBondManager midnightAetherBond = player.getAttachedOrCreate(
                                    ModAttachmentTypes.MIDNIGHT_AETHER_BOND);
                            if (midnightAetherBond.hasAnyBonds()) {
                                midnightAetherBond.dispelAllBonds(player, true);
                            }
                        }
                    }
                });

        // Sync entity attachments
        StartTrackingEntityCallback.EVENT.register(AttachmentSync::syncEntityAttachments);
        PlayerSyncCallback.EVENT.register(
                (player -> AttachmentSync.syncEntityAttachments(player, player)));

        ServerTickEvents.END_WORLD_TICK.register(world -> {
            ((LumarManagerAccess) world).worldsinger$getLumarManager().serverTick(world);
        });
    }

    private ModEventHandlers() {}
}

