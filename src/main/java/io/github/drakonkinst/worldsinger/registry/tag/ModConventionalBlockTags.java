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
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class ModConventionalBlockTags {

    // A note on singular vs plural naming conventions.
    // "pumpkins" refers to very distinct blocks that contain pumpkins, so uses plural.
    // "gravel" refers to variants of gravel that functionally act the same and can be used
    // more or less interchangeably, so uses singular.
    // "_plants" describes tags that consist of non-full-block plants
    // "_blocks" describes tags that consist of full blocks
    // This is a bit subjective, but I think this lines up with vanilla naming conventions.

    // Groupings of vanilla blocks
    public static final TagKey<Block> AIR = ModConventionalBlockTags.of("air");
    public static final TagKey<Block> WATER = ModConventionalBlockTags.of("water");
    public static final TagKey<Block> BAMBOO_PLANTS = ModConventionalBlockTags.of("bamboo_plants");
    public static final TagKey<Block> BAMBOO_MOSAIC_BLOCKS = ModConventionalBlockTags.of(
            "bamboo_mosaic_blocks");
    public static final TagKey<Block> CAULDRONS = ModConventionalBlockTags.of("cauldrons");
    public static final TagKey<Block> CONCRETE_POWDER = ModConventionalBlockTags.of(
            "concrete_powder");
    public static final TagKey<Block> DRIPLEAF_PLANTS = ModConventionalBlockTags.of(
            "dripleaf_plants");
    public static final TagKey<Block> LANTERNS = ModConventionalBlockTags.of("lanterns");
    public static final TagKey<Block> TORCHES = ModConventionalBlockTags.of("torches");
    public static final TagKey<Block> NETHERRACK = ModConventionalBlockTags.of("netherrack");
    public static final TagKey<Block> GRAVEL = ModConventionalBlockTags.of("gravel");
    public static final TagKey<Block> PUMPKINS = ModConventionalBlockTags.of("pumpkins");
    public static final TagKey<Block> SPONGE = ModConventionalBlockTags.of("sponge");
    public static final TagKey<Block> SNOW_BLOCKS = ModConventionalBlockTags.of("snow_blocks");
    public static final TagKey<Block> STORAGE_BLOCKS_SALT = ModConventionalBlockTags.of(
            "storage_blocks/salt");
    public static final TagKey<Block> STORAGE_BLOCKS_RAW_SILVER = ModConventionalBlockTags.of(
            "storage_blocks/raw_silver");

    // New groupings for modded blocks
    public static final TagKey<Block> SILVER_ORES = ModConventionalBlockTags.of("ores/silver");
    public static final TagKey<Block> SALT_ORES = ModConventionalBlockTags.of("ores/salt");
    public static final TagKey<Block> STORAGE_BLOCKS_STEEL = ModConventionalBlockTags.of(
            "storage_blocks/steel");
    public static final TagKey<Block> STORAGE_BLOCKS_SILVER = ModConventionalBlockTags.of(
            "storage_blocks/silver");
    public static final TagKey<Block> STORAGE_BLOCKS_ALUMINUM = ModConventionalBlockTags.of(
            "storage_blocks/aluminum");

    private static TagKey<Block> of(String id) {
        return TagKey.of(RegistryKeys.BLOCK, Identifier.of(ModConstants.COMMON_ID, id));
    }

    private ModConventionalBlockTags() {}
}
