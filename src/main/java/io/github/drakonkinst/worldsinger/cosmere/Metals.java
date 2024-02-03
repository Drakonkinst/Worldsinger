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
package io.github.drakonkinst.worldsinger.cosmere;

import io.github.drakonkinst.worldsinger.block.ModBlockTags;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypeTags;
import io.github.drakonkinst.worldsinger.item.ModItemTags;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;

public enum Metals implements Metal {
    IRON(ModBlockTags.HAS_IRON, ModItemTags.HAS_IRON, ModEntityTypeTags.HAS_IRON),
    STEEL(ModBlockTags.HAS_STEEL, ModItemTags.HAS_STEEL, ModEntityTypeTags.HAS_STEEL);

    private final TagKey<Block> blockTag;
    private final TagKey<Item> itemTag;
    private final TagKey<EntityType<?>> entityTypeTag;

    Metals(TagKey<Block> blockTag, TagKey<Item> itemTag, TagKey<EntityType<?>> entityTypeTag) {
        this.blockTag = blockTag;
        this.itemTag = itemTag;
        this.entityTypeTag = entityTypeTag;
    }

    @Override
    public TagKey<Block> getBlockTag() {
        return blockTag;
    }

    @Override
    public TagKey<Item> getItemTag() {
        return itemTag;
    }

    @Override
    public TagKey<EntityType<?>> getEntityTypeTag() {
        return entityTypeTag;
    }
}
