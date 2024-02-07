package io.github.drakonkinst.worldsinger.mixin.entity;

import com.mojang.authlib.GameProfile;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.PossessionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityPossessionMixin extends PlayerEntity {

    public ServerPlayerEntityPossessionMixin(World world, BlockPos pos, float yaw,
            GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickServer(CallbackInfo ci) {
        PossessionManager possessionManager = this.getAttached(ModAttachmentTypes.POSSESSION);
        if (possessionManager != null) {
            possessionManager.serverTick();
        }
    }
}
