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

package io.github.drakonkinst.worldsinger.datagen;

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.registry.tag.ModConventionalBlockTags;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.BlockTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;

public class ModBlockTagGenerator extends BlockTagProvider {

    public ModBlockTagGenerator(FabricDataOutput output,
            CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(WrapperLookup wrapperLookup) {
        // Mineable
        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE).addOptionalTag(
                ModBlockTags.ALL_VERDANT_GROWTH);
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(ModBlocks.SALTSTONE)
                .add(ModBlocks.SILVER_ORE)
                .add(ModBlocks.DEEPSLATE_SILVER_ORE)
                .add(ModBlocks.SALTSTONE_SALT_ORE)
                .add(ModBlocks.SALT_BLOCK)
                .add(ModBlocks.RAW_SILVER_BLOCK)
                .add(ModBlocks.SILVER_BLOCK)
                .add(ModBlocks.ALUMINUM_BLOCK)
                .add(ModBlocks.ALUMINUM_SHEET)
                .add(ModBlocks.MAGMA_VENT)
                .addOptionalTag(ModBlockTags.ALL_CRIMSON_GROWTH)
                .addOptionalTag(ModBlockTags.ALL_ROSEITE_GROWTH);
        getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE).addOptionalTag(
                ModBlockTags.AETHER_SPORE_BLOCKS);

        // Other Vanilla
        getOrCreateTagBuilder(BlockTags.CAULDRONS).addOptionalTag(ModBlockTags.ALUMINUM_CAULDRONS)
                .add(ModBlocks.DEAD_SPORE_CAULDRON)
                .add(ModBlocks.VERDANT_SPORE_CAULDRON)
                .add(ModBlocks.CRIMSON_SPORE_CAULDRON)
                .add(ModBlocks.ZEPHYR_SPORE_CAULDRON)
                .add(ModBlocks.SUNLIGHT_SPORE_CAULDRON)
                .add(ModBlocks.ROSEITE_SPORE_CAULDRON)
                .add(ModBlocks.MIDNIGHT_SPORE_CAULDRON);
        getOrCreateTagBuilder(BlockTags.CLIMBABLE).add(ModBlocks.TWISTING_VERDANT_VINES,
                ModBlocks.DEAD_TWISTING_VERDANT_VINES, ModBlocks.TWISTING_VERDANT_VINES_PLANT,
                ModBlocks.DEAD_TWISTING_VERDANT_VINES_PLANT);
        getOrCreateTagBuilder(BlockTags.DEAD_BUSH_MAY_PLACE_ON).add(ModBlocks.SALTSTONE)
                .add(ModBlocks.SALTSTONE_SALT_ORE);
        getOrCreateTagBuilder(BlockTags.FLOWER_POTS).add(ModBlocks.POTTED_VERDANT_VINE_SNARE)
                .add(ModBlocks.POTTED_TWISTING_VERDANT_VINES)
                .add(ModBlocks.POTTED_DEAD_VERDANT_VINE_SNARE)
                .add(ModBlocks.POTTED_DEAD_TWISTING_VERDANT_VINES);
        getOrCreateTagBuilder(BlockTags.NEEDS_DIAMOND_TOOL).add(ModBlocks.MAGMA_VENT);
        getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.SILVER_ORE)
                .add(ModBlocks.DEEPSLATE_SILVER_ORE)
                .add(ModBlocks.RAW_SILVER_BLOCK)
                .add(ModBlocks.SILVER_BLOCK);
        getOrCreateTagBuilder(BlockTags.SWORD_EFFICIENT).addOptionalTag(
                        ModBlockTags.TWISTING_VERDANT_VINES)
                .addOptionalTag(ModBlockTags.VERDANT_VINE_SNARE)
                .addOptionalTag(ModBlockTags.ROSEITE_CLUSTER);

        // New conventional tags
        // Tags that other modders might implement without specific compatibilities for this mod.
        // This should be generally un-opinionated.
        getOrCreateTagBuilder(ModConventionalBlockTags.AIR).add(Blocks.AIR)
                .add(Blocks.CAVE_AIR)
                .add(Blocks.VOID_AIR);
        getOrCreateTagBuilder(ModConventionalBlockTags.WATER).add(Blocks.WATER)
                .add(Blocks.BUBBLE_COLUMN);
        getOrCreateTagBuilder(ModConventionalBlockTags.SILVER_ORES).add(ModBlocks.SILVER_ORE)
                .add(ModBlocks.DEEPSLATE_SILVER_ORE);
        getOrCreateTagBuilder(ModConventionalBlockTags.SALT_ORES).add(ModBlocks.SALTSTONE_SALT_ORE);
        getOrCreateTagBuilder(ModConventionalBlockTags.STORAGE_BLOCKS_STEEL).add(
                ModBlocks.STEEL_BLOCK);
        getOrCreateTagBuilder(ModConventionalBlockTags.STORAGE_BLOCKS_SILVER).add(
                ModBlocks.SILVER_BLOCK);
        getOrCreateTagBuilder(ModConventionalBlockTags.STORAGE_BLOCKS_ALUMINUM).add(
                ModBlocks.ALUMINUM_BLOCK);
        getOrCreateTagBuilder(ModConventionalBlockTags.BAMBOO_MOSAIC_BLOCKS).add(
                        Blocks.BAMBOO_MOSAIC)
                .add(Blocks.BAMBOO_MOSAIC_SLAB)
                .add(Blocks.BAMBOO_MOSAIC_STAIRS);
        getOrCreateTagBuilder(ModConventionalBlockTags.BAMBOO_PLANTS).add(Blocks.BAMBOO_SAPLING)
                .add(Blocks.BAMBOO);
        getOrCreateTagBuilder(ModConventionalBlockTags.CONCRETE_POWDER).add(
                        Blocks.WHITE_CONCRETE_POWDER)
                .add(Blocks.ORANGE_CONCRETE_POWDER)
                .add(Blocks.MAGENTA_CONCRETE_POWDER)
                .add(Blocks.LIGHT_BLUE_CONCRETE_POWDER)
                .add(Blocks.YELLOW_CONCRETE_POWDER)
                .add(Blocks.LIME_CONCRETE_POWDER)
                .add(Blocks.PINK_CONCRETE_POWDER)
                .add(Blocks.GRAY_CONCRETE_POWDER)
                .add(Blocks.LIGHT_GRAY_CONCRETE_POWDER)
                .add(Blocks.CYAN_CONCRETE_POWDER)
                .add(Blocks.PURPLE_CONCRETE_POWDER)
                .add(Blocks.BLUE_CONCRETE_POWDER)
                .add(Blocks.BROWN_CONCRETE_POWDER)
                .add(Blocks.GREEN_CONCRETE_POWDER)
                .add(Blocks.RED_CONCRETE_POWDER)
                .add(Blocks.BLACK_CONCRETE_POWDER);
        getOrCreateTagBuilder(ModConventionalBlockTags.DRIPLEAF_PLANTS).add(Blocks.BIG_DRIPLEAF)
                .add(Blocks.BIG_DRIPLEAF_STEM)
                .add(Blocks.SMALL_DRIPLEAF);
        getOrCreateTagBuilder(ModConventionalBlockTags.LANTERNS).add(Blocks.LANTERN)
                .add(Blocks.SOUL_LANTERN);
        getOrCreateTagBuilder(ModConventionalBlockTags.TORCHES).add(Blocks.TORCH)
                .add(Blocks.SOUL_TORCH)
                .add(Blocks.REDSTONE_TORCH);
        getOrCreateTagBuilder(ModConventionalBlockTags.GRAVEL).add(Blocks.GRAVEL)
                .add(Blocks.SUSPICIOUS_GRAVEL);
        getOrCreateTagBuilder(ModConventionalBlockTags.NETHERRACK).add(Blocks.NETHERRACK)
                .add(Blocks.WARPED_NYLIUM)
                .add(Blocks.CRIMSON_NYLIUM);

        // Merge conventional tags
        getOrCreateTagBuilder(ConventionalBlockTags.ORES).addOptionalTag(
                        ModConventionalBlockTags.SILVER_ORES)
                .addOptionalTag(ModConventionalBlockTags.SALT_ORES);
        getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS).addOptionalTag(
                        ModConventionalBlockTags.STORAGE_BLOCKS_STEEL)
                .addOptionalTag(ModConventionalBlockTags.STORAGE_BLOCKS_SILVER)
                .addOptionalTag(ModConventionalBlockTags.STORAGE_BLOCKS_ALUMINUM);

        // Mod block tags
        // Can be opinionated versions of vanilla tags for specific gameplay features

    }
}
