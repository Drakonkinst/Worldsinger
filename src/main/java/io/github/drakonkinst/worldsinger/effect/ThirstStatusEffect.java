package io.github.drakonkinst.worldsinger.effect;

import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

public class ThirstStatusEffect extends StatusEffect {

    private final float drainMultiplier;

    protected ThirstStatusEffect(float drainMultiplier, int color) {
        super(StatusEffectCategory.HARMFUL, color);
        this.drainMultiplier = drainMultiplier;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity) {
            entity.getAttachedOrCreate(ModAttachmentTypes.THIRST)
                    .addDehydration(drainMultiplier * (float) (amplifier + 1));
        }
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
}
