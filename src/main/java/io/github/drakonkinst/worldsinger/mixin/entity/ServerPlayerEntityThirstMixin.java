package io.github.drakonkinst.worldsinger.mixin.entity;

import com.mojang.authlib.GameProfile;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.api.sync.AttachmentSync;
import io.github.drakonkinst.worldsinger.entity.PlayerThirstManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityThirstMixin extends PlayerEntity {

    @Unique
    private int syncedThirstLevel = -999999;

    public ServerPlayerEntityThirstMixin(World world, BlockPos pos, float yaw,
            GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "playerTick", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/server/network/ServerPlayerEntity;age:I"))
    private void sendThirstUpdates(CallbackInfo ci) {
        PlayerThirstManager thirstManager = this.getAttachedOrCreate(ModAttachmentTypes.THIRST);
        if (syncedThirstLevel != thirstManager.get()) {
            syncedThirstLevel = thirstManager.get();
            AttachmentSync.sync(this, ModAttachmentTypes.THIRST, thirstManager);
        }

    }
}
