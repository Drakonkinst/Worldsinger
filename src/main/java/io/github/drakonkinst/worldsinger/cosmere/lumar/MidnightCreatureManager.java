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
package io.github.drakonkinst.worldsinger.cosmere.lumar;

import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;

public final class MidnightCreatureManager {

    public static final int MIN_DRAIN_INTERVAL_TICKS = 20;
    public static final int MAX_DRAIN_INTERVAL_TICKS = 80;

    public static final double DEFAULT_MOVEMENT_SPEED = 0.25;
    public static final double DEFAULT_MAX_HEALTH = 20.0;
    private static final double MAX_HEALTH_SIZE_MULTIPLIER = 17;
    private static final double ATTACK_DAMAGE_SIZE_MULTIPLIER = 2.5;
    private static final float MIN_MAX_HEALTH = 8.0f;       // Same as Silverfish
    private static final float MAX_MAX_HEALTH = 100.0f;     // Same as Ravager
    private static final float MIN_ATTACK_DAMAGE = 3.0f;    // Same as Zombie
    private static final float MAX_ATTACK_DAMAGE = 12.0f;   // Same as Ravager
    private static final int DRAIN_INTERVAL_MULTIPLIER = -8;

    public static DefaultAttributeContainer.Builder createMidnightCreatureAttributes() {
        // Before transforming, should not be able to move or attack
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MOVEMENT_SPEED, 0.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 0.0)
                .add(EntityAttributes.MAX_HEALTH, DEFAULT_MAX_HEALTH);
    }

    public static double getMaxHealthForSize(float size) {
        double value = size * MAX_HEALTH_SIZE_MULTIPLIER;
        return MathHelper.clamp(Math.round(value), MIN_MAX_HEALTH, MAX_MAX_HEALTH);
    }

    public static double getAttackDamageForSize(float size) {
        double value = size * ATTACK_DAMAGE_SIZE_MULTIPLIER;
        return MathHelper.clamp(Math.round(value), MIN_ATTACK_DAMAGE, MAX_ATTACK_DAMAGE);
    }

    public static int getDrainIntervalForSize(float size) {
        int sizeStage = MathHelper.floor(size * 2.0f);
        return Math.max(MAX_DRAIN_INTERVAL_TICKS + DRAIN_INTERVAL_MULTIPLIER * sizeStage,
                MIN_DRAIN_INTERVAL_TICKS);
    }

    public static int getMinBribeForSize(float size) {
        return MathHelper.floor(size * 2.0f);
    }

    public static int getWaterAmountPerUnit(ItemStack stack) {
        if (stack.isOf(Items.POTION)) {
            return MidnightCreatureEntity.POTION_BRIBE;
        }
        if (stack.isIn(ConventionalItemTags.WATER_BUCKETS)) {
            return MidnightCreatureEntity.WATER_BUCKET_BRIBE;
        }
        return 0;
    }

    public static ItemStack getStackAfterDraining(ItemStack stack) {
        if (stack.isOf(Items.POTION)) {
            return Items.GLASS_BOTTLE.getDefaultStack();
        }
        return stack.getRecipeRemainder();
    }

    private MidnightCreatureManager() {}
}
