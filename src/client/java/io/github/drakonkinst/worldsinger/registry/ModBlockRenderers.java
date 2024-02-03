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

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;

public final class ModBlockRenderers {

    public static void register() {
        final Block[] cutoutBlocks = {
                ModBlocks.VERDANT_VINE_SNARE,
                ModBlocks.DEAD_VERDANT_VINE_SNARE,
                ModBlocks.TWISTING_VERDANT_VINES,
                ModBlocks.DEAD_TWISTING_VERDANT_VINES,
                ModBlocks.TWISTING_VERDANT_VINES_PLANT,
                ModBlocks.DEAD_TWISTING_VERDANT_VINES_PLANT,
                ModBlocks.CRIMSON_SPIKE,
                ModBlocks.DEAD_CRIMSON_SPIKE,
                ModBlocks.CRIMSON_SNARE,
                ModBlocks.DEAD_CRIMSON_SNARE,
                ModBlocks.CRIMSON_SPINES,
                ModBlocks.DEAD_CRIMSON_SPINES,
                ModBlocks.TALL_CRIMSON_SPINES,
                ModBlocks.DEAD_TALL_CRIMSON_SPINES,
                ModBlocks.ROSEITE_CLUSTER,
                ModBlocks.LARGE_ROSEITE_BUD,
                ModBlocks.MEDIUM_ROSEITE_BUD,
                ModBlocks.SMALL_ROSEITE_BUD
        };
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), cutoutBlocks);

        final Block[] translucentBlocks = {
                ModBlocks.ROSEITE_BLOCK, ModBlocks.ROSEITE_STAIRS, ModBlocks.ROSEITE_SLAB
        };
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(), translucentBlocks);
    }

    private ModBlockRenderers() {}
}
