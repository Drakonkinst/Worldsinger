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

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballCore;
import net.minecraft.block.Blocks;
import net.minecraft.client.data.TextureKey;
import net.minecraft.client.data.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public final class ModTextureMaps {

    public static TextureMap aluminumCauldron(Identifier content) {
        return new TextureMap().put(TextureKey.PARTICLE,
                        TextureMap.getSubId(Blocks.CAULDRON, "_side"))
                .put(TextureKey.SIDE, TextureMap.getSubId(ModBlocks.ALUMINUM_CAULDRON, "_side"))
                .put(TextureKey.TOP, TextureMap.getSubId(Blocks.CAULDRON, "_top"))
                .put(TextureKey.BOTTOM, TextureMap.getSubId(Blocks.CAULDRON, "_bottom"))
                .put(TextureKey.INSIDE, TextureMap.getSubId(Blocks.CAULDRON, "_inner"))
                .put(ModTextureKeys.ALUMINUM, TextureMap.getId(ModBlocks.ALUMINUM_BLOCK))
                .put(TextureKey.CONTENT, content);
    }

    public static TextureMap cannonball(Item baseItem, CannonballCore core, int fuse) {
        // TODO: Map core and fuse to the proper textures in ModItemRendering
        TextureMap map = new TextureMap().put(TextureKey.LAYER0, TextureMap.getId(baseItem))
                .put(ModTextureKeys.CORE, null)
                .put(ModTextureKeys.FUSE, null)
                .put(ModTextureKeys.CONTENTS_1, null)
                .put(ModTextureKeys.CONTENTS_2, null)
                .put(ModTextureKeys.CONTENTS_3, null);
        if (core != null) {
            return map;
        }
    }
}
