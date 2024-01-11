package io.github.drakonkinst.worldsinger.mixin.client.network;

import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    private void preventInteractWithSelf(PlayerEntity player, Entity entity, Hand hand,
            CallbackInfoReturnable<ActionResult> cir) {
        if (entity.equals(MinecraftClient.getInstance().player)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "interactEntity", at = @At("HEAD"), cancellable = true)
    private void preventInteractIfPossessing(PlayerEntity player, Entity entity, Hand hand,
            CallbackInfoReturnable<ActionResult> cir) {
        if (MinecraftClient.getInstance()
                .getCameraEntity() instanceof CameraPossessable cameraPossessable
                && !cameraPossessable.canInteractWithEntities()) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "interactEntityAtLocation", at = @At("HEAD"), cancellable = true)
    private void preventInteractAtLocationWithSelf(PlayerEntity player, Entity entity,
            EntityHitResult hitResult, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (entity.equals(MinecraftClient.getInstance().player)) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "interactEntityAtLocation", at = @At("HEAD"), cancellable = true)
    private void preventInteractAtLocationIfPossessing(PlayerEntity player, Entity entity,
            EntityHitResult hitResult, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (MinecraftClient.getInstance()
                .getCameraEntity() instanceof CameraPossessable cameraPossessable
                && !cameraPossessable.canInteractWithEntities()) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void preventBlockInteractionIfPossessing(ClientPlayerEntity player, Hand hand,
            BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (MinecraftClient.getInstance()
                .getCameraEntity() instanceof CameraPossessable cameraPossessable
                && !cameraPossessable.canInteractWithBlocks()) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}
