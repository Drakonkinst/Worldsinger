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
import io.github.drakonkinst.worldsinger.entity.rainline.RainlineEntity;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.util.ModEnums;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Precipitation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererLumarMixin {

    @Shadow
    private @Nullable ClientWorld world;

    @Unique
    @Nullable
    private RainlineEntity nearestRainlineEntity = null;

    @Inject(method = "render", at = @At("HEAD"))
    private void trackNearbyRainlineEntities(RenderTickCounter tickCounter,
            boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
            LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2,
            CallbackInfo ci) {
        if (this.world == null) {
            return;
        }
        nearestRainlineEntity = RainlineEntity.getNearestRainlineEntity(this.world, camera.getPos(),
                RainlineEntity.RAINLINE_GRADIENT_RADIUS);
    }

    @WrapOperation(method = "tickRainSplashing", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;getHeight(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V")))
    private boolean makeLavaRainEffectsDataDriven(BlockState instance, Block block,
            Operation<Boolean> original) {
        if (original.call(instance, block)) {
            return true;
        }
        return instance.isIn(ModBlockTags.SMOKES_IN_RAIN);
    }

    @ModifyExpressionValue(method = "tickRainSplashing", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"))
    private float addRainlineEffects1(float original, Camera camera) {
        if (nearestRainlineEntity != null) {
            return Math.max(RainlineEntity.getRainlineGradient(this.world, camera.getPos(), false),
                    original);
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"))
    private float renderRainlines1(float original, Matrix4f matrix4f, Matrix4f projectionMatrix,
            float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback) {
        if (nearestRainlineEntity != null) {
            return Math.max(RainlineEntity.getRainlineGradient(this.world, camera.getPos(), true),
                    original);
        }
        return original;
    }

    @WrapOperation(method = "tickRainSplashing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getPrecipitation(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome$Precipitation;"))
    private Precipitation addRainlineEffects2(Biome instance, BlockPos pos,
            Operation<Precipitation> original) {
        if (nearestRainlineEntity != null) {
            return Precipitation.RAIN;
        }
        return original.call(instance, pos);
    }

    @WrapOperation(method = "renderWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getPrecipitation(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome$Precipitation;"))
    private Precipitation renderRainlines2(Biome instance, BlockPos pos,
            Operation<Precipitation> original) {
        if (nearestRainlineEntity != null) {
            return Precipitation.RAIN;
        }
        return original.call(instance, pos);
    }

    @ModifyExpressionValue(method = "renderWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"))
    private float renderRainlines3(float original, LightmapTextureManager manager, float tickDelta,
            double cameraX, double cameraY, double cameraZ) {
        if (nearestRainlineEntity != null) {
            return Math.max(RainlineEntity.getRainlineGradient(this.world,
                    new Vec3d(cameraX, cameraY, cameraZ), false), original);
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;hasPrecipitation()Z"))
    private boolean renderRainlines4(boolean original, LightmapTextureManager manager,
            float tickDelta, double cameraX, double cameraY, double cameraZ) {
        if (original) {
            return original;
        }
        return nearestRainlineEntity != null;
    }

    @ModifyExpressionValue(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getSubmersionType()Lnet/minecraft/block/enums/CameraSubmersionType;"))
    private CameraSubmersionType skipRenderingSkyInSporeFluid(CameraSubmersionType original) {
        // Treat spore sea camera like lava, so that it skips sky rendering when submerged
        if (original == ModEnums.CameraSubmersionType.SPORE_SEA) {
            return CameraSubmersionType.LAVA;
        }
        return original;
    }

}
