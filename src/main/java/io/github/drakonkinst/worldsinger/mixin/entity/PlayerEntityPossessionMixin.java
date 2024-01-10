package io.github.drakonkinst.worldsinger.mixin.entity;

import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.component.PossessionComponent;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityPossessionMixin extends LivingEntity {

    @Shadow
    protected abstract boolean shouldDismount();

    protected PlayerEntityPossessionMixin(EntityType<? extends LivingEntity> entityType,
            World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void checkStopPossessing(CallbackInfo ci) {
        PossessionComponent possessionData = ModComponents.POSSESSION.get(this);
        if (possessionData.isPossessing()) {
            CameraPossessable possessedEntity = possessionData.getPossessedEntity();
            if (this.shouldDismount() || (possessedEntity != null
                    && !possessedEntity.shouldKeepPossessing((PlayerEntity) (Object) this))) {
                possessionData.resetPossessedEntity();
            }
        }
    }
}
