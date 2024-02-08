package io.github.drakonkinst.worldsinger.mixin.entity;

import com.mojang.authlib.GameProfile;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
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
public abstract class ServerPlayerEntityMidnightAetherBondMixin extends PlayerEntity {

    public ServerPlayerEntityMidnightAetherBondMixin(World world, BlockPos pos, float yaw,
            GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void sendThirstUpdates(CallbackInfo ci) {
        this.getAttachedOrCreate(ModAttachmentTypes.MIDNIGHT_AETHER_BOND).serverTick(this);
    }
}
