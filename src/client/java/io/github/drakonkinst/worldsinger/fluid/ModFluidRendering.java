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
package io.github.drakonkinst.worldsinger.fluid;

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.fluid.Fluid;

@SuppressWarnings("UnqualifiedStaticUsage")
public final class ModFluidRendering {

    public static void register() {
        registerFluidRenderer(ModFluids.DEAD_SPORES, ModFluids.FLOWING_DEAD_SPORES,
                "block/dead_spore_block", "block/dead_spore_sea_flow");
        registerFluidRenderer(ModFluids.VERDANT_SPORES, ModFluids.FLOWING_VERDANT_SPORES,
                "block/verdant_spore_block", "block/verdant_spore_sea_flow");
        registerFluidRenderer(ModFluids.CRIMSON_SPORES, ModFluids.FLOWING_CRIMSON_SPORES,
                "block/crimson_spore_block", "block/crimson_spore_sea_flow");
        registerFluidRenderer(ModFluids.ZEPHYR_SPORES, ModFluids.FLOWING_ZEPHYR_SPORES,
                "block/zephyr_spore_block", "block/zephyr_spore_sea_flow");
        registerFluidRenderer(ModFluids.SUNLIGHT_SPORES, ModFluids.FLOWING_SUNLIGHT_SPORES,
                "block/sunlight_spore_block", "block/sunlight_spore_sea_flow");
        registerFluidRenderer(ModFluids.ROSEITE_SPORES, ModFluids.FLOWING_ROSEITE_SPORES,
                "block/roseite_spore_block", "block/roseite_spore_sea_flow");
        registerFluidRenderer(ModFluids.MIDNIGHT_SPORES, ModFluids.FLOWING_MIDNIGHT_SPORES,
                "block/midnight_spore_block", "block/midnight_spore_sea_flow");
        registerFluidRenderer(ModFluids.SUNLIGHT, "block/sunlight", false);
    }

    private static void registerFluidRenderer(Fluid still, Fluid flow, String stillTexturePath,
            String flowTexturePath) {
        // Register render handler
        FluidRenderHandlerRegistry.INSTANCE.register(still, flow,
                new SimpleFluidRenderHandler(Worldsinger.id(stillTexturePath),
                        Worldsinger.id(flowTexturePath)));

        // Register fluid render layer as translucent (needed to make boats cull properly)
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), still, flow);
    }

    private static void registerFluidRenderer(Fluid fluid, String texturePath, boolean shaded) {
        // Register render handler
        FluidRenderHandlerRegistry.INSTANCE.register(fluid,
                new StillFluidRenderHandler(Worldsinger.id(texturePath), shaded));
    }

    private ModFluidRendering() {}
}
