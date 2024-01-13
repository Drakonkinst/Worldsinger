package io.github.drakonkinst.worldsinger.mixin.client.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
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
    
    @ModifyExpressionValue(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z"), slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V")))
    private boolean disableHandRenderWhenPossessing(boolean original) {
        return original && PossessionClientUtil.getPossessedEntity() == null;
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
