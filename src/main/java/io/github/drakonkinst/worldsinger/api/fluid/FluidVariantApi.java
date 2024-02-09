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
package io.github.drakonkinst.worldsinger.api.fluid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

// A simple system to manage variants of blocks/items that don't support them too well
// Usually based around fluids, but can technically work for anything
// Specifically, glass bottles and cauldron variants
// Hides the internals so the map can be implemented as a Map<Pair, Pair> if we want
public class FluidVariantApi {

    // The first key is the base block or item
    // The second key is the original block that we want a variant for
    // The result is the variant of the original based on the base block
    private static final Map<Item, Map<Item, Item>> BOTTLE_VARIANT_MAP = new HashMap<>();
    private static final Map<Block, Map<Block, Block>> CAULDRON_VARIANT_MAP = new HashMap<>();

    public static void registerBottleVariants(Item baseItem, Map<Item, Item> bottleVariantMap) {
        Map<Item, Item> map = FluidVariantApi.getOrCreateBottleVariantMap(baseItem);
        map.putAll(bottleVariantMap);
    }

    public static void registerBottleVariant(Item baseItem, Item bottleOriginalItem,
            Item bottleVariantItem) {
        Map<Item, Item> map = FluidVariantApi.getOrCreateBottleVariantMap(baseItem);
        map.put(bottleOriginalItem, bottleVariantItem);
    }

    public static void registerCauldronVariants(Block baseBlock,
            Map<Block, Block> cauldronVariantMap) {
        Map<Block, Block> map = FluidVariantApi.getOrCreateCauldronVariantMap(baseBlock);
        map.putAll(cauldronVariantMap);
    }

    public static void registerCauldronVariant(Block baseBlock, Block cauldronOriginalBlock,
            Block cauldronVariantBlock) {
        Map<Block, Block> map = FluidVariantApi.getOrCreateCauldronVariantMap(baseBlock);
        map.put(cauldronOriginalBlock, cauldronVariantBlock);
    }

    private static Optional<Block> getCauldronVariant(CauldronVariantBlock cauldronVariantBlock,
            Block originalBlock) {
        Map<Block, Block> map = FluidVariantApi.CAULDRON_VARIANT_MAP.get(
                cauldronVariantBlock.worldsinger$getBaseBlock());
        if (map == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(map.get(originalBlock));
    }

    public static Optional<Block> getCauldronVariant(Block cauldronVariantBlock,
            Block originalBlock) {
        if (cauldronVariantBlock instanceof CauldronVariantBlock variant) {
            return FluidVariantApi.getCauldronVariant(variant, originalBlock);
        }
        return Optional.empty();
    }

    private static Map<Item, Item> getOrCreateBottleVariantMap(Item baseItem) {
        if (!(baseItem instanceof BottleVariantItem)) {
            throw new IllegalArgumentException(
                    "Item " + baseItem.getName().getString() + " is not a bottle variant");
        }
        return FluidVariantApi.BOTTLE_VARIANT_MAP.computeIfAbsent(baseItem, key -> new HashMap<>());
    }

    private static Map<Block, Block> getOrCreateCauldronVariantMap(Block baseBlock) {
        if (!(baseBlock instanceof CauldronVariantBlock)) {
            throw new IllegalArgumentException(
                    "Block " + baseBlock.getName().getString() + " is not a cauldron variant");
        }
        return FluidVariantApi.CAULDRON_VARIANT_MAP.computeIfAbsent(baseBlock,
                key -> new HashMap<>());
    }

    private FluidVariantApi() {}
}
