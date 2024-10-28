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

package io.github.drakonkinst.worldsinger.entity.model;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.entity.rainline.RainlineEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.GeoModel;

public class RainlineEntityModel extends GeoModel<RainlineEntity> {

    private static final Identifier MODEL_PATH = Worldsinger.id("geo/rainline.geo.json");
    private static final Identifier TEXTURE_PATH = Worldsinger.id(
            "textures/entity/rainline/rainline.png");

    @Override
    public Identifier getModelResource(RainlineEntity animatable) {
        return MODEL_PATH;
    }

    @Override
    public Identifier getTextureResource(RainlineEntity animatable) {
        return TEXTURE_PATH;
    }

    @Override
    public Identifier getAnimationResource(RainlineEntity animatable) {
        // No animation
        return null;
    }

    @Override
    public @Nullable RenderLayer getRenderType(RainlineEntity animatable, Identifier texture) {
        return RenderLayer.getEntityTranslucent(texture);
    }
}
