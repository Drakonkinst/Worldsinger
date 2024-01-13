package io.github.drakonkinst.worldsinger.mixin.client.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererPossessionMixin {

    // Just use the entity's block position at eye level to determine if it is suffocating
    // The getInWallBlockState() is specifically customized for the player's eyesight, which means
    // for other mobs it may treat it as suffocating even if just touching the block.
    @WrapOperation(method = "renderOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameOverlayRenderer;getInWallBlockState(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/block/BlockState;"))
    private static BlockState usePossessedEntitySimplifiedEyePos(PlayerEntity player,
            Operation<BlockState> original) {
        CameraPossessable possessionTarget = PossessionClientUtil.getPossessedEntity();
        if (possessionTarget != null) {
            LivingEntity possessedEntity = possessionTarget.toEntity();
            BlockPos eyePos = BlockPosUtil.toBlockPos(
                    new Vec3d(possessedEntity.getX(), possessedEntity.getEyeY(),
                            possessedEntity.getZ()));
            BlockState state = player.getWorld().getBlockState(eyePos);
            if (state.getRenderType() != BlockRenderType.INVISIBLE && state.shouldBlockVision(
                    player.getWorld(), eyePos)) {
                return state;
            } else {
                return null;
            }
        }
        return original.call(player);
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