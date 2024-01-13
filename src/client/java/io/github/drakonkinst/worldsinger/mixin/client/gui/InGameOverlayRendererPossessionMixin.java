package io.github.drakonkinst.worldsinger.mixin.client.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererPossessionMixin {

    @ModifyExpressionValue(method = "getInWallBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getX()D"))
    private static double usePossessedEntityX(double original) {
        CameraPossessable possessionTarget = PossessionClientUtil.getPossessedEntity();
        if (possessionTarget != null) {
            return possessionTarget.toEntity().getX();
        }
        return original;
    }

    @ModifyExpressionValue(method = "getInWallBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getEyeY()D"))
    private static double usePossessedEntityEyeY(double original) {
        CameraPossessable possessionTarget = PossessionClientUtil.getPossessedEntity();
        if (possessionTarget != null) {
            return possessionTarget.toEntity().getEyeY();
        }
        return original;
    }

    @ModifyExpressionValue(method = "getInWallBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getZ()D"))
    private static double usePossessedEntityZ(double original) {
        CameraPossessable possessionTarget = PossessionClientUtil.getPossessedEntity();
        if (possessionTarget != null) {
            return possessionTarget.toEntity().getZ();
        }
        return original;
    }

    @WrapOperation(method = "renderOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private static boolean renderUnderwaterOverlayFromPossessedEntityPerspective(
            ClientPlayerEntity instance, TagKey<Fluid> fluidTag, Operation<Boolean> original) {
        CameraPossessable possessionTarget = PossessionClientUtil.getPossessedEntity();
        if (possessionTarget != null) {
            return possessionTarget.toEntity().isSubmergedIn(fluidTag);
        }
        return original.call(instance, fluidTag);
    }

    @WrapOperation(method = "renderOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isOnFire()Z"))
    private static boolean renderFireOverlayFromPossessedEntityPerspective(
            ClientPlayerEntity instance, Operation<Boolean> original) {
        CameraPossessable possessionTarget = PossessionClientUtil.getPossessedEntity();
        if (possessionTarget != null) {
            return possessionTarget.toEntity().isOnFire();
        }
        return original.call(instance);
    }
}