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

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class ModBlockTags {

    public static final TagKey<Block> AETHER_SPORE_BLOCKS = ModBlockTags.of("aether_spore_blocks");
    public static final TagKey<Block> AETHER_SPORE_SEA_BLOCKS = ModBlockTags.of(
            "aether_spore_sea_blocks");
    // Blocks that randomly tick in rain, which should also be affected by rainlines
    public static final TagKey<Block> AFFECTED_BY_RAIN = ModBlockTags.of("affected_by_rain");
    public static final TagKey<Block> ALL_CAULDRONS = ModBlockTags.of("all_cauldrons");
    public static final TagKey<Block> ALL_CRIMSON_GROWTH = ModBlockTags.of("all_crimson_growth");
    public static final TagKey<Block> ALL_GLASS_TYPE = ModBlockTags.of("all_glass_type");
    public static final TagKey<Block> ALL_ROSEITE_GROWTH = ModBlockTags.of("all_roseite_growth");
    public static final TagKey<Block> ALL_VERDANT_GROWTH = ModBlockTags.of("all_verdant_growth");
    public static final TagKey<Block> ALUMINUM_CAULDRONS = ModBlockTags.of("aluminum_cauldrons");
    public static final TagKey<Block> BLOCKS_INVESTITURE = ModBlockTags.of("blocks_investiture");
    public static final TagKey<Block> CRIMSON_GROWTH = ModBlockTags.of("crimson_growth");
    public static final TagKey<Block> CRIMSON_SNARE = ModBlockTags.of("crimson_snare");
    public static final TagKey<Block> CRIMSON_SPIKE = ModBlockTags.of("crimson_spike");
    public static final TagKey<Block> CRIMSON_SPINES = ModBlockTags.of("crimson_spines");
    public static final TagKey<Block> FLUIDS_CANNOT_BREAK = ModBlockTags.of("fluids_cannot_break");
    public static final TagKey<Block> FUNGI_BLOCKS = ModBlockTags.of("fungi_blocks");
    // The kind of blocks M-Bot would enjoy.
    public static final TagKey<Block> FUNGI_PLANTS = ModBlockTags.of("fungi_plants");
    public static final TagKey<Block> GRASS_PLANTS = ModBlockTags.of("grass_plants");
    public static final TagKey<Block> HAS_ALUMINUM = ModBlockTags.of("has_aluminum");
    public static final TagKey<Block> HAS_IRON = ModBlockTags.of("has_iron");
    public static final TagKey<Block> HAS_SALT = ModBlockTags.of("has_salt");
    public static final TagKey<Block> HAS_SILVER = ModBlockTags.of("has_silver");
    public static final TagKey<Block> HAS_STEEL = ModBlockTags.of("has_steel");
    public static final TagKey<Block> INFINIBURN_LUMAR = ModBlockTags.of("infiniburn_lumar");
    public static final TagKey<Block> KILLS_SPORES = ModBlockTags.of("kills_spores");
    public static final TagKey<Block> LUMAR_CARVER_REPLACEABLES = ModBlockTags.of(
            "lumar_carver_replaceables");
    // Marks blocks that are not opaque, but should be treated as opaque for lighting.
    // Needed for non-full blocks similar to Tinted Glass to block light.
    public static final TagKey<Block> OPAQUE_FOR_LIGHTING = ModBlockTags.of("opaque_for_lighting");
    public static final TagKey<Block> PLANTS = ModBlockTags.of("plants");
    public static final TagKey<Block> ROOTS = ModBlockTags.of("roots");
    public static final TagKey<Block> ROSEITE_CLUSTER = ModBlockTags.of("roseite_cluster");
    public static final TagKey<Block> ROSEITE_GROWABLE = ModBlockTags.of("roseite_growable");
    public static final TagKey<Block> SALTSTONE = ModBlockTags.of("saltstone");
    public static final TagKey<Block> SCULK_BLOCKS = ModBlockTags.of("sculk_blocks");
    public static final TagKey<Block> SEA_PLANTS = ModBlockTags.of("sea_plants");
    public static final TagKey<Block> SEAGULLS_SPAWNABLE_ON = ModBlockTags.of(
            "seagulls_spawnable_on");
    public static final TagKey<Block> SILVER_WALKABLE = ModBlockTags.of("silver_walkable");
    public static final TagKey<Block> SMOKES_IN_RAIN = ModBlockTags.of("smokes_in_rain");
    public static final TagKey<Block> SOFT_EARTH = ModBlockTags.of("soft_earth");
    public static final TagKey<Block> SPORES_CAN_BREAK = ModBlockTags.of("spores_can_break");
    public static final TagKey<Block> SPORES_CAN_GROW = ModBlockTags.of("spores_can_grow");
    public static final TagKey<Block> STEEL_ANVIL = ModBlockTags.of("steel_anvil");
    public static final TagKey<Block> TALL_CRIMSON_SPINES = ModBlockTags.of("tall_crimson_spines");
    public static final TagKey<Block> TWISTING_VERDANT_VINES = ModBlockTags.of(
            "twisting_verdant_vines");
    public static final TagKey<Block> VERDANT_VINE_BLOCK = ModBlockTags.of("verdant_vine_block");
    public static final TagKey<Block> VERDANT_VINE_BRANCH = ModBlockTags.of("verdant_vine_branch");
    public static final TagKey<Block> VERDANT_VINE_SNARE = ModBlockTags.of("verdant_vine_snare");
    public static final TagKey<Block> VINES = ModBlockTags.of("vines");
    public static final TagKey<Block> WOOD_TYPE = ModBlockTags.of("wood_type");

    // Tools
    public static final TagKey<Block> INCORRECT_FOR_STEEL_TOOL = ModBlockTags.of(
            "incorrect_for_steel_tool");
    public static final TagKey<Block> INCORRECT_FOR_SILVER_TOOL = ModBlockTags.of(
            "incorrect_for_silver_tool");

    private static TagKey<Block> of(String id) {
        return TagKey.of(RegistryKeys.BLOCK, Worldsinger.id(id));
    }

    private ModBlockTags() {}
}
