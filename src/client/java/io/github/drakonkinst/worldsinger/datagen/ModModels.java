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
package io.github.drakonkinst.worldsinger.datagen;

import io.github.drakonkinst.worldsinger.Worldsinger;
import java.util.Optional;
import net.minecraft.client.data.Model;
import net.minecraft.client.data.TextureKey;

public final class ModModels {

    public static final Model TEMPLATE_ALUMINUM_CAULDRON_LEVEL1 = ModModels.block(
            "template_aluminum_cauldron_level1", TextureKey.CONTENT, TextureKey.INSIDE,
            TextureKey.PARTICLE, TextureKey.TOP, TextureKey.BOTTOM, TextureKey.SIDE);
    public static final Model TEMPLATE_ALUMINUM_CAULDRON_LEVEL2 = ModModels.block(
            "template_aluminum_cauldron_level2", TextureKey.CONTENT, TextureKey.INSIDE,
            TextureKey.PARTICLE, TextureKey.TOP, TextureKey.BOTTOM, TextureKey.SIDE);
    public static final Model TEMPLATE_ALUMINUM_CAULDRON_FULL = ModModels.block(
            "template_aluminum_cauldron_full", TextureKey.CONTENT, TextureKey.INSIDE,
            TextureKey.PARTICLE, TextureKey.TOP, TextureKey.BOTTOM, TextureKey.SIDE);

    private static Model block(String parent, TextureKey... requiredTextureKeys) {
        return new Model(Optional.of(Worldsinger.id("block/" + parent)), Optional.empty(),
                requiredTextureKeys);
    }

    private ModModels() {}
}
