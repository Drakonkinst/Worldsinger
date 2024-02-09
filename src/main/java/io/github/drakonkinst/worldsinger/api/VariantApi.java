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
package io.github.drakonkinst.worldsinger.api;

import it.unimi.dsi.fastutil.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

// A simple system to manage variants of blocks/items that don't support them too well
// Using this system, you can change between related block/item variants smoothly
// Specifically, glass bottles and cauldron variants
// Usage of variants must be specifically coded, but allows other things to not be duplicated
public class VariantApi {

    // The first key is the base block or item
    // The second key is the original block that we want a variant for
    // The result is the variant of the original based on the base block/item
    // Each item/block can only belong to at most one variant map due to this
    private static final Map<Pair<Item, Item>, Item> ITEM_VARIANT_MAP = new HashMap<>();
    private static final Map<Pair<Block, Block>, Block> BLOCK_VARIANT_MAP = new HashMap<>();
    private static final Map<Item, Item> BASE_ITEM_MAP = new HashMap<>();
    private static final Map<Block, Block> BASE_BLOCK_MAP = new HashMap<>();

    public static void registerItemVariants(Item baseItem, Map<Item, Item> itemVariantMap) {
        for (Map.Entry<Item, Item> entry : itemVariantMap.entrySet()) {
            ITEM_VARIANT_MAP.put(Pair.of(baseItem, entry.getKey()), entry.getValue());
            BASE_ITEM_MAP.put(entry.getValue(), baseItem);
        }
    }

    public static void registerItemVariant(Item baseItem, Item originalItem, Item variantItem) {
        ITEM_VARIANT_MAP.put(Pair.of(baseItem, originalItem), variantItem);
        BASE_ITEM_MAP.put(variantItem, baseItem);
    }

    public static Optional<Item> getItemVariant(Item variantItem, Item originalItem) {
        Item baseItem = BASE_ITEM_MAP.get(variantItem);
        if (baseItem == null) {
            // The variant MUST be registered in the base map, which helps eliminate some
            // wrong-argument issues which can get quite tricky.
            return Optional.empty();
        }
        Pair<Item, Item> key = Pair.of(baseItem, originalItem);
        return Optional.ofNullable(ITEM_VARIANT_MAP.get(key));
    }

    public static void registerBlockVariants(Block baseBlock, Map<Block, Block> blockVariantMap) {
        for (Map.Entry<Block, Block> entry : blockVariantMap.entrySet()) {
            BLOCK_VARIANT_MAP.put(Pair.of(baseBlock, entry.getKey()), entry.getValue());
            BASE_BLOCK_MAP.put(entry.getValue(), baseBlock);
        }
    }

    public static void registerBlockVariant(Block baseBlock, Block originalBlock,
            Block variantBlock) {
        BLOCK_VARIANT_MAP.put(Pair.of(baseBlock, originalBlock), variantBlock);
        BASE_BLOCK_MAP.put(variantBlock, baseBlock);
    }

    // Most operations require 2 lookups, which is a tradeoff to not dynamically set the base block
    // in the block class itself which is more invasive
    public static Optional<Block> getBlockVariant(Block variantBlock, Block originalBlock) {
        Block baseBlock = BASE_BLOCK_MAP.get(variantBlock);
        if (baseBlock == null) {
            // The variant MUST be registered in the base map, which helps eliminate some
            // wrong-argument issues which can get quite tricky.
            return Optional.empty();
        }
        Pair<Block, Block> key = Pair.of(baseBlock, originalBlock);
        return Optional.ofNullable(BLOCK_VARIANT_MAP.get(key));
    }

    private VariantApi() {}
}
