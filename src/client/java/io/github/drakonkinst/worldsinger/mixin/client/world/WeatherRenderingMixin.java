/*
 * MIT License
 *
 * Copyright (c) 2024 Drakonkinst
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
import io.github.drakonkinst.worldsinger.api.ClientRainlineData;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.Precipitation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WeatherRendering.class)
public abstract class WeatherRenderingMixin {

    @ModifyExpressionValue(method = "getPrecipitationAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getPrecipitation(Lnet/minecraft/util/math/BlockPos;I)Lnet/minecraft/world/biome/Biome$Precipitation;"))
    private Precipitation showRainlinePrecipitation(Precipitation original, World world,
            BlockPos pos) {
        ClientRainlineData rainlineData = ClientRainlineData.get(world);
        if (rainlineData.isUnderNearestRainline(pos)) {
            return Precipitation.RAIN;
        }
        return original;
    }

    @ModifyExpressionValue(method = "renderPrecipitation(Lnet/minecraft/world/World;Lnet/minecraft/client/render/VertexConsumerProvider;IFLnet/minecraft/util/math/Vec3d;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRainGradient(F)F"))
    private float renderPrecipitationIfRainlineNearby(float original, World world,
            VertexConsumerProvider vertexConsumers, int ticks, float delta, Vec3d pos) {
        ClientRainlineData rainlineData = ClientRainlineData.get(world);
        if (!rainlineData.isRainlineNearby()) {
            return original;
        }
        float gradient = rainlineData.getRainlineGradient(false);
        return Math.max(original, gradient);
    }

    @WrapOperation(method = "addParticlesAndSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private boolean makeLavaRainEffectsDataDriven(BlockState instance, Block block,
            Operation<Boolean> original) {
        if (original.call(instance, block)) {
            return true;
        }
        return instance.isIn(ModBlockTags.SMOKES_IN_RAIN);
    }

    @ModifyExpressionValue(method = "addParticlesAndSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"))
    private float addParticlesAndSoundsIfRainlineNearby(float original, ClientWorld world,
            Camera camera, int ticks, ParticlesMode particlesMode) {
        ClientRainlineData rainlineData = ClientRainlineData.get(world);
        if (!rainlineData.isRainlineNearby()) {
            return original;
        }
        float gradient = rainlineData.getRainlineGradient(false);
        return Math.max(original, gradient);
    }

}
