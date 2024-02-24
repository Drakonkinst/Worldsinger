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
package io.github.drakonkinst.worldsinger.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class FallingSporeDustParticle extends AbstractSporeDustParticle {

    protected FallingSporeDustParticle(ClientWorld clientWorld, double x, double y, double z,
            double velocityX, double velocityY, double velocityZ,
            FallingSporeDustParticleEffect parameters, SpriteProvider spriteProvider) {
        super(clientWorld, x, y, z, velocityX, velocityY, velocityZ, parameters, spriteProvider);
        this.gravityStrength = 0.3f;
        this.velocityMultiplier = 0.85f;
        this.ascending = false;
        // Remove randomization in velocity
        this.setVelocity(velocityX, velocityY, velocityZ);
        scaleVelocity();
    }

    @Override
    protected void setRandomAge(float scale) {
        int i = (int) (12.0 / (this.random.nextDouble() * 0.8 + 0.2));
        this.maxAge = (int) Math.max((float) i * scale, 1.0F);
    }

    @Override
    public float getSize(float tickDelta) {
        return this.scale * MathHelper.clamp((this.age + tickDelta) / this.maxAge * 32.0F, 0.0F,
                1.0F);
    }

    public static class Factory implements ParticleFactory<FallingSporeDustParticleEffect> {

        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(FallingSporeDustParticleEffect parameters, ClientWorld world,
                double x, double y, double z, double velocityX, double velocityY,
                double velocityZ) {
            return new FallingSporeDustParticle(world, x, y, z, velocityX, velocityY, velocityZ,
                    parameters, this.spriteProvider);
        }
    }
}
