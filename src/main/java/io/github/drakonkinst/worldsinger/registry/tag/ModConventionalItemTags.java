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
package io.github.drakonkinst.worldsinger.registry.tag;

import io.github.drakonkinst.worldsinger.util.ModConstants;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class ModConventionalItemTags {

    public static final TagKey<Item> SALT = ModConventionalItemTags.of("salt");
    public static final TagKey<Item> STEEL_INGOTS = ModConventionalItemTags.of("ingots/steel");
    public static final TagKey<Item> SILVER_INGOTS = ModConventionalItemTags.of("ingots/silver");
    public static final TagKey<Item> ALUMINUM_INGOTS = ModConventionalItemTags.of(
            "ingots/aluminum");
    public static final TagKey<Item> STEEL_NUGGETS = ModConventionalItemTags.of("nuggets/steel");
    public static final TagKey<Item> SILVER_NUGGETS = ModConventionalItemTags.of("nuggets/silver");
    public static final TagKey<Item> ALUMINUM_NUGGETS = ModConventionalItemTags.of(
            "nuggets/aluminum");
    public static final TagKey<Item> SILVER_ORES = ModConventionalItemTags.of("ores/silver");
    public static final TagKey<Item> SALT_ORES = ModConventionalItemTags.of("ores/salt");
    public static final TagKey<Item> STORAGE_BLOCKS_STEEL = ModConventionalItemTags.of(
            "storage_blocks/steel");
    public static final TagKey<Item> STORAGE_BLOCKS_SILVER = ModConventionalItemTags.of(
            "storage_blocks/silver");
    public static final TagKey<Item> STORAGE_BLOCKS_ALUMINUM = ModConventionalItemTags.of(
            "storage_blocks/aluminum");

    private static TagKey<Item> of(String id) {
        return TagKey.of(RegistryKeys.ITEM, new Identifier(ModConstants.COMMON_ID, id));
    }

    private ModConventionalItemTags() {}
}
