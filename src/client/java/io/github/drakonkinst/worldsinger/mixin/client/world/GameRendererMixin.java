/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Drakonkinst
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.drakonkinst.worldsinger.mixin.client.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import io.github.drakonkinst.worldsinger.util.PossessionClientUtil;
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

    @Unique
    private static final Identifier MIDNIGHT_CREATURE_OVERLAY = Worldsinger.id(
            "shaders/post/midnight_creature_overlay.json");

    @Shadow
    private @Nullable Identifier postProcessorId;

    @Shadow
    protected abstract void setPostProcessor(Identifier id);

    @ModifyExpressionValue(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/Perspective;isFirstPerson()Z"), slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V")))
    private boolean disableHandRenderWhenPossessing(boolean original) {
        return original && PossessionClientUtil.getPossessedEntity() == null;
    }

    @Inject(method = "onCameraEntitySet", at = @At("TAIL"))
    private void addCustomMobVisionTypes(@Nullable Entity entity, CallbackInfo ci) {
        if (this.postProcessorId != null) {
            return;
        }

        if (entity instanceof MidnightCreatureEntity) {
            this.setPostProcessor(MIDNIGHT_CREATURE_OVERLAY);
        }
    }
}
