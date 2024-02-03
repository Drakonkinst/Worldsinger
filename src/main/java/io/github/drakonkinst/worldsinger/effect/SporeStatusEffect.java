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
package io.github.drakonkinst.worldsinger.effect;

import io.github.drakonkinst.worldsinger.block.SporeEmitting;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SunlightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.ZephyrSpores;
import io.github.drakonkinst.worldsinger.registry.ModDamageTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class SporeStatusEffect extends StatusEffect implements SporeEmitting {

    public static final float DEFAULT_DAMAGE = 15.0f;
    private static final int WATER_PER_ENTITY_BLOCK = 75;
    private final AetherSpores sporeType;
    private final RegistryKey<DamageType> damageType;
    private final float damageAmount;

    protected SporeStatusEffect(AetherSpores sporeType, RegistryKey<DamageType> damageType) {
        this(sporeType, DEFAULT_DAMAGE, damageType);
    }

    protected SporeStatusEffect(AetherSpores sporeType, float damageAmount,
            RegistryKey<DamageType> damageType) {
        super(StatusEffectCategory.HARMFUL, sporeType.getParticleColor());
        this.sporeType = sporeType;
        this.damageAmount = damageAmount;
        this.damageType = damageType;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!AetherSpores.sporesCanAffect(entity)) {
            return false;
        }

        if (sporeType.getId() == SunlightSpores.ID) {
            if (!entity.isFireImmune()) {
                entity.setOnFireFor(15);
            }
            entity.setFireTicks(entity.getFireTicks() + 1);
        }

        if (sporeType.getId() == ZephyrSpores.ID) {
            // Refill air
            if (entity.getAir() < entity.getMaxAir()) {
                entity.setAir(entity.getMaxAir());
            }
        }

        boolean wasDamaged = entity.damage(
                ModDamageTypes.createSource(entity.getWorld(), damageType), damageAmount);
        if (wasDamaged && sporeType.getId() == SunlightSpores.ID) {
            // Play lava damage sound
            entity.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4f,
                    2.0f + entity.getWorld().getRandom().nextFloat() * 0.4f);
        }
        if (wasDamaged && entity.isDead()) {
            onDeathEffect(entity);
        }
        return true;
    }

    private void onDeathEffect(LivingEntity entity) {
        World world = entity.getWorld();
        BlockPos pos = entity.getBlockPos();
        int waterAmount = MathHelper.ceil(
                entity.getWidth() * entity.getHeight() * WATER_PER_ENTITY_BLOCK);
        sporeType.onDeathFromStatusEffect(world, entity, pos, waterAmount);
    }

    public AetherSpores getSporeType() {
        return sporeType;
    }
}
