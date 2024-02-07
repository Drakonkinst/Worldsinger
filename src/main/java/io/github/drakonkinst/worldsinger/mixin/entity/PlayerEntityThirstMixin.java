package io.github.drakonkinst.worldsinger.mixin.entity;

import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityThirstMixin extends LivingEntity {

    protected PlayerEntityThirstMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;update(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void updateThirst(CallbackInfo ci) {
        this.getAttachedOrCreate(ModAttachmentTypes.THIRST).update(this);
    }

    @Inject(method = "addExhaustion", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V"))
    private void addThirstExhaustion(float exhaustion, CallbackInfo ci) {
        this.getAttachedOrCreate(ModAttachmentTypes.THIRST).addDehydration(exhaustion);
    }
}
