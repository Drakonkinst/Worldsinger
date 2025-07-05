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
import io.github.drakonkinst.worldsinger.command.WorldhopCommand;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.PossessionManager;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManagerAccess;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightAetherBondManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeKillingUtil;
import io.github.drakonkinst.worldsinger.dialog.ModDialogs;
import io.github.drakonkinst.worldsinger.effect.ModStatusEffects;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import io.github.drakonkinst.worldsinger.registry.tag.ModItemTags;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerPickItemEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.Nullable;

public final class ModEventHandlers {

    private static void registerRegistryHandlers() {
        // Furnace fuels
        FuelRegistryEvents.BUILD.register((builder, context) -> {
            // // Using the time required to cook most items
            int numItemsToTicks = 20 * 10;
            builder.add(ModItems.SUNLIGHT_SPORES_BOTTLE, 8 * numItemsToTicks);
            builder.add(ModItems.SUNLIGHT_SPORES_BUCKET, 100 * numItemsToTicks);
        });

        // TODO: Move this to item_components, but hopefully add some datagen first
        // Modify default item components
        DefaultItemComponentEvents.MODIFY.register(context -> {
            // TODO: I'd like to make this properly data-driven one day, but tags are not supported here
            List<Item> boats = List.of(Items.ACACIA_BOAT, Items.BIRCH_BOAT, Items.CHERRY_BOAT,
                    Items.DARK_OAK_BOAT, Items.JUNGLE_BOAT, Items.MANGROVE_BOAT, Items.OAK_BOAT,
                    Items.SPRUCE_BOAT, Items.BAMBOO_RAFT, Items.ACACIA_CHEST_BOAT,
                    Items.BIRCH_CHEST_BOAT, Items.CHERRY_CHEST_BOAT, Items.DARK_OAK_CHEST_BOAT,
                    Items.JUNGLE_CHEST_BOAT, Items.MANGROVE_CHEST_BOAT, Items.OAK_CHEST_BOAT,
                    Items.SPRUCE_CHEST_BOAT, Items.BAMBOO_CHEST_RAFT, Items.PALE_OAK_BOAT,
                    Items.PALE_OAK_CHEST_BOAT);
            List<Item> axes = List.of(Items.WOODEN_AXE, Items.GOLDEN_AXE, Items.STONE_AXE,
                    Items.IRON_AXE, ModItems.STEEL_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE);

            // Add silver-lined to boats
            context.modify(boats, (builder, item) -> {
                builder.add(ModDataComponentTypes.MAX_SILVER_DURABILITY,
                        SilverLined.BOAT_MAX_DURABILITY);
                builder.add(ModDataComponentTypes.SILVER_DURABILITY_DISPLAY_FACTOR,
                        SilverLined.BOAT_VISUAL_SCALE_FACTOR);
            });

            // Add silver-lined to axes
            context.modify(axes, ((builder, item) -> {
                int maxDurability = builder.getOrDefault(DataComponentTypes.MAX_DAMAGE, 1);
                builder.add(ModDataComponentTypes.MAX_SILVER_DURABILITY, maxDurability);
            }));
        });

        CustomClickActionCallback.EVENT.register((player, id, payload) -> {
            if (id.equals(ModDialogs.WORLDHOP_ID) && payload.isPresent()) {
                handleWorldhopAction(player, payload.get().asCompound().orElse(null));
            }
        });
    }

    private static void handleWorldhopAction(ServerPlayerEntity player,
            @Nullable NbtCompound compound) {
        if (compound == null) {
            return;
        }
        compound.getString(ModDialogs.WORLDHOP_PAYLOAD_KEY).ifPresent(planetId -> {
            // TODO: At some point we might want to prevent players from sending this action more than once
            CosmerePlanet targetPlanet = null;
            for (CosmerePlanet planet : CosmerePlanet.VALUES) {
                if (planet.getTranslationKey().equals(planetId)) {
                    targetPlanet = planet;
                }
            }
            if (targetPlanet != null) {
                // It's valid
                TeleportTarget target = WorldhopCommand.createTeleportTarget(player.getWorld(),
                        targetPlanet.getRegistryKey());
                if (target != null) {
                    player.teleportTo(target);
                }
            }
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void registerThirstHandlers() {
        // Add Thirst-related effects when consuming an item
        FinishConsumingItemCallback.EVENT.register((entity, stack) -> {
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
    }

    private static void registerBlockInteractionEvents() {
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
                return ActionResult.SUCCESS;
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
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }

    private static void registerPossessionHandlers() {
        // Prevent entity/block picking while possessing an entity
        PlayerPickItemEvents.BLOCK.register((player, pos, state, requestIncludeData) -> {
            PossessionManager possessionManager = player.getAttached(ModAttachmentTypes.POSSESSION);
            if (possessionManager == null) {
                return null;
            }
            CameraPossessable possessedEntity = possessionManager.getPossessionTarget();
            if (possessedEntity != null && !possessedEntity.canPickBlock()) {
                // Prevent picking block
                return ItemStack.EMPTY;
            }
            // Use default behavior
            return null;
        });

        PlayerPickItemEvents.ENTITY.register((player, pos, requestIncludeData) -> {
            PossessionManager possessionManager = player.getAttached(ModAttachmentTypes.POSSESSION);
            if (possessionManager == null) {
                return null;
            }
            CameraPossessable possessedEntity = possessionManager.getPossessionTarget();
            if (possessedEntity != null && !possessedEntity.canPickBlock()) {
                // Prevent picking entity
                return ItemStack.EMPTY;
            }
            // Use default behavior
            return null;
        });
    }

    private static void registerEntityHandlers() {
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
    }

    private static void registerWorldHandlers() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            ((LumarManagerAccess) world).worldsinger$getLumarManager().serverTick(world);
        });
    }

    public static void initialize() {
        registerRegistryHandlers();
        registerThirstHandlers();
        registerBlockInteractionEvents();
        registerPossessionHandlers();
        registerEntityHandlers();
        registerWorldHandlers();
    }

    private ModEventHandlers() {}
}

