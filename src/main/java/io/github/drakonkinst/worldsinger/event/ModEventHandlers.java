package io.github.drakonkinst.worldsinger.event;

import io.github.drakonkinst.worldsinger.block.LivingSporeGrowthBlock;
import io.github.drakonkinst.worldsinger.component.MidnightAetherBondComponent;
import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeKillingManager;
import io.github.drakonkinst.worldsinger.effect.ModStatusEffects;
import io.github.drakonkinst.worldsinger.item.ModItemTags;
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

    public static void initialize() {
        // Add Thirst-related effects when consuming an item
        FinishConsumingItemCallback.EVENT.register((entity, stack) -> {
            if (entity instanceof PlayerEntity player) {
                ModComponents.THIRST_MANAGER.get(player).drink(stack.getItem(), stack);

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

            if (SporeKillingManager.killSporeGrowthUsingTool(world, sporeGrowth, state, pos, player,
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

            if (SporeKillingManager.killSporeGrowthUsingTool(world, sporeGrowth, state, pos, player,
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
                    if (!wasBlocked && damageTaken > 0.0f && !source.isIndirect()
                            && attacker instanceof LivingEntity livingEntity) {
                        ItemStack attackingItem = livingEntity.getMainHandStack();

                        if (attackingItem.isIn(ModItemTags.KILLS_SPORE_GROWTHS)) {
                            MidnightAetherBondComponent midnightAetherBond = ModComponents.MIDNIGHT_AETHER_BOND.get(
                                    player);
                            if (midnightAetherBond.hasAnyBonds()) {
                                midnightAetherBond.dispelAllBonds(true);
                            }
                        }
                    }
                });
    }

    private ModEventHandlers() {}
}

