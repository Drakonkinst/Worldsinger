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
package io.github.drakonkinst.worldsinger.registry;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypes;
import io.github.drakonkinst.worldsinger.entity.model.RainlineEntityModel;
import io.github.drakonkinst.worldsinger.entity.render.MidnightCreatureEntityRenderer;
import io.github.drakonkinst.worldsinger.entity.render.RainlineEntityRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public final class ModEntityRendering {

    public static final EntityModelLayer MODEL_RAINLINE_LAYER = new EntityModelLayer(
            Worldsinger.id("rainline"), "main");

    public static void register() {
        EntityRendererRegistry.register(ModEntityTypes.SPORE_BOTTLE, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.CANNONBALL, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.MIDNIGHT_CREATURE,
                MidnightCreatureEntityRenderer::new);
        // TODO: Can render as a dark cloud later
        EntityRendererRegistry.register(ModEntityTypes.RAINLINE, RainlineEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(MODEL_RAINLINE_LAYER,
                RainlineEntityModel::getTexturedModelData);
    }

    private ModEntityRendering() {}
}