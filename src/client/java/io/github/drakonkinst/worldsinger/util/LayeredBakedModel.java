/*
 * MIT License
 *
 * Copyright (c) 2011-2017 mortuusars
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

package io.github.drakonkinst.worldsinger.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class LayeredBakedModel implements BakedModel {

    private static final List<LayeredBakedModelCache> CACHES = new ArrayList<>();

    public static LayeredBakedModelCache registerCache(LayeredBakedModelCache cache) {
        CACHES.add(cache);
        return cache;
    }

    public static void clearCaches() {
        for (LayeredBakedModelCache cache : CACHES) {
            cache.clear();
        }
    }

    private final List<BakedModel> layers;

    public LayeredBakedModel(List<BakedModel> layers) {
        this.layers = layers;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face,
            Random random) {
        List<BakedQuad> quads = new ArrayList<>();

        for (BakedModel layer : layers) {
            List<BakedQuad> layerQuads = layer.getQuads(state, face, random);
            quads.addAll(layerQuads);
        }

        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return layers.get(0).useAmbientOcclusion();
    }

    @Override
    public boolean hasDepth() {
        return layers.get(0).hasDepth();
    }

    @Override
    public boolean isSideLit() {
        return layers.get(0).isSideLit();
    }

    @Override
    public boolean isBuiltin() {
        return layers.get(0).isBuiltin();
    }

    @Override
    public Sprite getParticleSprite() {
        return layers.get(0).getParticleSprite();
    }

    @Override
    public ModelTransformation getTransformation() {
        return layers.get(0).getTransformation();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return layers.get(0).getOverrides();
    }
}
