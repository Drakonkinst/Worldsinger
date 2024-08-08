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
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.entity.RainlineEntity;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.util.ModEnums;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Precipitation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererLumarMixin {

    @Shadow
    private @Nullable ClientWorld world;

    // @WrapOperation(method = "renderWeather", at = @At(value = "INVOKE", target = "getPrecipitation"))

    @WrapOperation(method = "tickRainSplashing", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;getHeight(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V")))
    private boolean makeLavaRainEffectsDataDriven(BlockState instance, Block block,
            Operation<Boolean> original) {
        if (original.call(instance, block)) {
            return true;
        }
        return instance.isIn(ModBlockTags.SMOKES_IN_RAIN);
    }

    @ModifyExpressionValue(method = "tickRainSplashing", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"))
    private float addRainlineEffects1(float original) {
        if (CosmerePlanet.isLumar(this.world)) {
            return 1.0f;
        }
        return original;
    }

    @WrapOperation(method = "tickRainSplashing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getPrecipitation(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome$Precipitation;"))
    private Precipitation addRainlineEffects2(Biome instance, BlockPos pos,
            Operation<Precipitation> original) {
        if (CosmerePlanet.isLumar(this.world) && RainlineEntity.isRainlineOver(world,
                pos.toCenterPos())) {
            return Precipitation.RAIN;
        }
        return original.call(instance, pos);
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
