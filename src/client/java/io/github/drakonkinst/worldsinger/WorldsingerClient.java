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
package io.github.drakonkinst.worldsinger;

import io.github.drakonkinst.worldsinger.event.ModClientEventHandlers;
import io.github.drakonkinst.worldsinger.fluid.ModFluidRendering;
import io.github.drakonkinst.worldsinger.network.ClientNetworkHandler;
import io.github.drakonkinst.worldsinger.network.ClientProxy;
import io.github.drakonkinst.worldsinger.particle.ModParticleManager;
import io.github.drakonkinst.worldsinger.registry.ModBlockRendering;
import io.github.drakonkinst.worldsinger.registry.ModEntityRendering;
import io.github.drakonkinst.worldsinger.registry.ModItemRendering;
import io.github.drakonkinst.worldsinger.registry.ModModelPredicates;
import io.github.drakonkinst.worldsinger.world.ModDimensionRenderers;
import net.fabricmc.api.ClientModInitializer;

public class WorldsingerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Worldsinger.PROXY = new ClientProxy();

        ModFluidRendering.register();
        ModBlockRendering.register();
        ModEntityRendering.register();
        ModItemRendering.register();

        // Register particles
        ModParticleManager.register();

        ModModelPredicates.register();
        ModDimensionRenderers.initialize();

        ModClientEventHandlers.registerEventHandlers();
        ClientNetworkHandler.registerPacketHandlers();
    }
}