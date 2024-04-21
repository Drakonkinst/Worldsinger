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

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public abstract class AbstractSporeDustParticle extends SpriteBillboardParticle {

    protected static final float VELOCITY_MULTIPLIER = 0.1f;

    protected final SpriteProvider spriteProvider;

    protected AbstractSporeDustParticle(ClientWorld world, double x, double y, double z,
            double velocityX, double velocityY, double velocityZ,
            AbstractSporeDustParticleEffect parameters, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.scale *= 0.75F * parameters.getScale();
        setColor(Vec3d.unpackRgb(parameters.getSporeType().getParticleColor()).toVector3f());
        setRandomAge(parameters.getScale());
        this.setSpriteForAge(spriteProvider);
    }

    private void setColor(Vector3f color) {
        float f = this.random.nextFloat() * 0.4F + 0.6F;
        this.red = color.x * f;
        this.green = color.y * f;
        this.blue = color.z * f;
    }

    protected void scaleVelocity() {
        this.velocityX *= VELOCITY_MULTIPLIER;
        this.velocityY *= VELOCITY_MULTIPLIER;
        this.velocityZ *= VELOCITY_MULTIPLIER;
    }

    protected void setRandomAge(float scale) {
        int i = (int) (8.0 / (this.random.nextDouble() * 0.8 + 0.2));
        this.maxAge = (int) Math.max((float) i * scale, 1.0F);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getSize(float tickDelta) {
        return this.scale * MathHelper.clamp(
                ((float) this.age + tickDelta) / (float) this.maxAge * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteForAge(this.spriteProvider);
    }
}
