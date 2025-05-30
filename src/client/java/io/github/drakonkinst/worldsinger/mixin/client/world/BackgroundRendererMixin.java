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
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.api.ClientRainlineData;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SunlightSpores;
import io.github.drakonkinst.worldsinger.fluid.AetherSporeFluid;
import io.github.drakonkinst.worldsinger.util.ColorUtil;
import io.github.drakonkinst.worldsinger.util.ModEnums;
import io.github.drakonkinst.worldsinger.world.CameraPosAccess;
import java.nio.ByteBuffer;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    @ModifyExpressionValue(method = "getFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/block/enums/CameraSubmersionType;", ordinal = 0))
    private static CameraSubmersionType skipExpensiveCalculation(CameraSubmersionType original) {
        // Pretend to be lava, skipping the expensive default "else" calculation
        if (original == ModEnums.CameraSubmersionType.SPORE_SEA) {
            return CameraSubmersionType.LAVA;
        }
        return original;
    }

    @Inject(method = "getFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;"))
    private static void correctCustomFluidColors(Camera camera, float tickDelta, ClientWorld world,
            int clampedViewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir,
            @Local(ordinal = 2) LocalFloatRef red, @Local(ordinal = 3) LocalFloatRef green,
            @Local(ordinal = 4) LocalFloatRef blue) {
        CameraSubmersionType cameraSubmersionType = camera.getSubmersionType();

        if (cameraSubmersionType != ModEnums.CameraSubmersionType.SPORE_SEA) {
            return;
        }

        // Set color based on the specific spore fluid
        CameraPosAccess cameraPos = (CameraPosAccess) camera;
        BlockState blockState = cameraPos.worldsinger$getBlockState();

        if (blockState.isOf(ModBlocks.SUNLIGHT)) {
            // Use Sunlight Spore colors for Sunlight blocks
            int color = SunlightSpores.getInstance().getColor();
            red.set(ColorUtil.getNormalizedRed(color));
            green.set(ColorUtil.getNormalizedGreen(color));
            blue.set(ColorUtil.getNormalizedBlue(color));
            return;
        }

        FluidState fluidState = ((CameraPosAccess) camera).worldsinger$getSubmersedFluidState();
        if (fluidState.getFluid() instanceof AetherSporeFluid aetherSporeFluid) {
            red.set(aetherSporeFluid.getFogRed());
            green.set(aetherSporeFluid.getFogGreen());
            blue.set(aetherSporeFluid.getFogBlue());
        } else {
            Worldsinger.LOGGER.error(
                    "Expected fluid to be an instance of AetherSporeFluid since Spore Sea submersion type is being used");
        }
    }

    @WrapOperation(method = "applyFog(Lnet/minecraft/client/render/Camera;Lorg/joml/Vector4f;FZF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;Lnet/minecraft/client/render/FogShape;FFFF)V"))
    private static void injectCustomFluidFogSettings(BackgroundRenderer instance, ByteBuffer buffer,
            int i, Vector4f fogColor, FogShape fogShape, float fogStart, float fogEnd, float skyEnd,
            float cloudEnd, Operation<Void> original, Camera camera, Vector4f fogColor2,
            float viewDistance, boolean thickenFog, float tickProgress) {
        if (camera.getSubmersionType() == ModEnums.CameraSubmersionType.SPORE_SEA) {
            float newFogStart;
            float newFogEnd;
            Entity entity = camera.getFocusedEntity();
            if (entity.isSpectator()) {
                // Match spectator mode settings for other fluids
                newFogStart = -8.0f;
                newFogEnd = viewDistance * 0.5f;
            } else {
                newFogStart = AetherSporeFluid.FOG_START;
                newFogEnd = AetherSporeFluid.FOG_END;
            }
            original.call(instance, buffer, i, fogColor, fogShape, newFogStart, newFogEnd, skyEnd,
                    cloudEnd);
            return;
        }
        original.call(instance, buffer, i, fogColor, fogShape, fogStart, fogEnd, skyEnd, cloudEnd);
    }

    @ModifyExpressionValue(method = "getFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"))
    private static float renderRainlines(float original, Camera camera, float tickDelta,
            ClientWorld world, int viewDistance, float skyDarkness) {
        ClientRainlineData rainlineData = ClientRainlineData.get(world);
        return Math.max(original, rainlineData.getRainlineGradient(true));
    }
}
