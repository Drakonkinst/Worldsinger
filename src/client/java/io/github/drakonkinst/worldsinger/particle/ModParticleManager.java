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

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public final class ModParticleManager {

    public static void register() {
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();

        registry.register(ModParticleTypes.SPORE_DUST, SporeDustParticle.Factory::new);
        registry.register(ModParticleTypes.FALLING_SPORE_DUST,
                FallingSporeDustParticle.Factory::new);
        registry.register(ModParticleTypes.MIDNIGHT_ESSENCE, MidnightEssenceParticle.Factory::new);
        registry.register(ModParticleTypes.MIDNIGHT_TRAIL, MidnightTrailParticle.Factory::new);
    }

    private ModParticleManager() {}
}
