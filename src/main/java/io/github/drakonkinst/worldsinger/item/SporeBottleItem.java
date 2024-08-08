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
package io.github.drakonkinst.worldsinger.item;

import io.github.drakonkinst.worldsinger.block.SporeEmitting;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleManager;
import io.github.drakonkinst.worldsinger.event.FinishConsumingItemCallback;
import io.github.drakonkinst.worldsinger.registry.ModDamageTypes;
import java.util.List;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class SporeBottleItem extends PotionItem implements SporeEmitting {

    private static final float SPORE_DEFAULT_DAMAGE = 4.0f;

    private final AetherSpores sporeType;

    public SporeBottleItem(AetherSpores sporeType, Settings settings) {
        super(settings);
        this.sporeType = sporeType;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        FinishConsumingItemCallback.EVENT.invoker()
                .onConsume(user, stack, stack.get(DataComponentTypes.FOOD));

        PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity) user : null;
        if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
        }
        if (!world.isClient) {
            RegistryEntry<StatusEffect> statusEffect = sporeType.getStatusEffect();
            if (statusEffect == null) {
                user.damage(ModDamageTypes.createSource(world, ModDamageTypes.DROWN_SPORE),
                        SPORE_DEFAULT_DAMAGE);
            } else {
                SporeParticleManager.applySporeEffect(user, statusEffect,
                        SporeParticleManager.SPORE_EFFECT_DURATION_TICKS_DEFAULT);
            }
        }
        if (playerEntity != null) {
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!playerEntity.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }
        if (playerEntity == null || !playerEntity.getAbilities().creativeMode) {
            if (stack.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }
            if (playerEntity != null) {
                playerEntity.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
            }
        }
        user.emitGameEvent(GameEvent.DRINK);
        return stack;
    }

    public AetherSpores getSporeType() {
        return sporeType;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }

    // All code below used to overwrite potion behavior & hopefully avoid crashes.

    @Override
    public String getTranslationKey(ItemStack stack) {
        // Reset to default
        return this.getTranslationKey();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip,
            TooltipType type) {
        // Reset to default
    }

    @Override
    public ItemStack getDefaultStack() {
        // Reset to default
        return new ItemStack(this);
    }
}
