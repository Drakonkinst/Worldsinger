package io.github.drakonkinst.worldsinger.mixin.client.world;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    // TODO: Using one of the Super Secret Shaders for now, but should eventually make a custom one
    @Unique
    private static final Identifier MIDNIGHT_CREATURE_OVERLAY = new Identifier(
            "shaders/post/desaturate.json");

    @Shadow
    @Nullable PostEffectProcessor postProcessor;

    @Shadow
    abstract void loadPostProcessor(Identifier id);

    @WrapWithCondition(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;F)V"))
    private boolean disableHandRenderWhenPossessing(GameRenderer instance, MatrixStack matrices,
            Camera camera, float tickDelta) {
        CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
        return possessedEntity == null;
    }

    @Inject(method = "onCameraEntitySet", at = @At("TAIL"))
    private void addCustomMobVisionTypes(@Nullable Entity entity, CallbackInfo ci) {
        if (this.postProcessor != null) {
            return;
        }

        if (entity instanceof MidnightCreatureEntity) {
            this.loadPostProcessor(MIDNIGHT_CREATURE_OVERLAY);
        }
    }
}
