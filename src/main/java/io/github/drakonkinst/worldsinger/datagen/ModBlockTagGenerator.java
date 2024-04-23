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
        getOrCreateTagBuilder(ModConventionalBlockTags.STORAGE_BLOCKS_SALT).add(
                ModBlocks.SALT_BLOCK);
        getOrCreateTagBuilder(ModConventionalBlockTags.STORAGE_BLOCKS_RAW_SILVER).add(
                ModBlocks.RAW_SILVER_BLOCK);
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
        getOrCreateTagBuilder(ModConventionalBlockTags.PUMPKINS).add(Blocks.PUMPKIN)
                .add(Blocks.CARVED_PUMPKIN)
                .add(Blocks.JACK_O_LANTERN);
        getOrCreateTagBuilder(ModConventionalBlockTags.SPONGE).add(Blocks.SPONGE)
                .add(Blocks.WET_SPONGE);
        getOrCreateTagBuilder(ModConventionalBlockTags.SNOW_BLOCKS).add(Blocks.SNOW_BLOCK)
                .add(Blocks.POWDER_SNOW);
        getOrCreateTagBuilder(ModConventionalBlockTags.CAULDRONS).add(Blocks.CAULDRON)
                .add(Blocks.WATER_CAULDRON)
                .add(Blocks.LAVA_CAULDRON)
                .add(Blocks.POWDER_SNOW_CAULDRON)
                .add(ModBlocks.DEAD_SPORE_CAULDRON)
                .add(ModBlocks.VERDANT_SPORE_CAULDRON)
                .add(ModBlocks.CRIMSON_SPORE_CAULDRON)
                .add(ModBlocks.ZEPHYR_SPORE_CAULDRON)
                .add(ModBlocks.SUNLIGHT_SPORE_CAULDRON)
                .add(ModBlocks.ROSEITE_SPORE_CAULDRON)
                .add(ModBlocks.MIDNIGHT_SPORE_CAULDRON);

        // Merge conventional tags
        getOrCreateTagBuilder(ConventionalBlockTags.ORES).addOptionalTag(
                        ModConventionalBlockTags.SILVER_ORES)
                .addOptionalTag(ModConventionalBlockTags.SALT_ORES);
        getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS).addOptionalTag(
                        ModConventionalBlockTags.STORAGE_BLOCKS_STEEL)
                .addOptionalTag(ModConventionalBlockTags.STORAGE_BLOCKS_SILVER)
                .addOptionalTag(ModConventionalBlockTags.STORAGE_BLOCKS_RAW_SILVER)
                .addOptionalTag(ModConventionalBlockTags.STORAGE_BLOCKS_ALUMINUM)
                .addOptionalTag(ModConventionalBlockTags.STORAGE_BLOCKS_SALT);

        // Mod block tags
        // Can be opinionated versions of vanilla tags for specific gameplay features
        getOrCreateTagBuilder(ModBlockTags.AETHER_SPORE_BLOCKS).add(ModBlocks.DEAD_SPORE_BLOCK)
                .add(ModBlocks.VERDANT_SPORE_BLOCK)
                .add(ModBlocks.CRIMSON_SPORE_BLOCK)
                .add(ModBlocks.ZEPHYR_SPORE_BLOCK)
                .add(ModBlocks.SUNLIGHT_SPORE_BLOCK)
                .add(ModBlocks.ROSEITE_SPORE_BLOCK)
                .add(ModBlocks.MIDNIGHT_SPORE_BLOCK);
        getOrCreateTagBuilder(ModBlockTags.AETHER_SPORE_SEA_BLOCKS).add(ModBlocks.DEAD_SPORE_SEA)
                .add(ModBlocks.VERDANT_SPORE_SEA)
                .add(ModBlocks.CRIMSON_SPORE_SEA)
                .add(ModBlocks.ZEPHYR_SPORE_SEA)
                .add(ModBlocks.SUNLIGHT_SPORE_SEA)
                .add(ModBlocks.ROSEITE_SPORE_SEA)
                .add(ModBlocks.MIDNIGHT_SPORE_SEA);
        getOrCreateTagBuilder(ModBlockTags.AFFECTED_BY_RAIN).add(Blocks.FARMLAND);
        getOrCreateTagBuilder(ModBlockTags.ALUMINUM_CAULDRONS).add(ModBlocks.ALUMINUM_CAULDRON)
                .add(ModBlocks.ALUMINUM_WATER_CAULDRON)
                .add(ModBlocks.ALUMINUM_LAVA_CAULDRON)
                .add(ModBlocks.ALUMINUM_POWDER_SNOW_CAULDRON)
                .add(ModBlocks.ALUMINUM_DEAD_SPORE_CAULDRON)
                .add(ModBlocks.ALUMINUM_VERDANT_SPORE_CAULDRON)
                .add(ModBlocks.ALUMINUM_CRIMSON_SPORE_CAULDRON)
                .add(ModBlocks.ALUMINUM_ZEPHYR_SPORE_CAULDRON)
                .add(ModBlocks.ALUMINUM_SUNLIGHT_SPORE_CAULDRON)
                .add(ModBlocks.ALUMINUM_ROSEITE_SPORE_CAULDRON)
                .add(ModBlocks.ALUMINUM_MIDNIGHT_SPORE_CAULDRON);
        getOrCreateTagBuilder(ModBlockTags.BLOCKS_INVESTITURE).addOptionalTag(
                ModBlockTags.HAS_ALUMINUM);
        getOrCreateTagBuilder(ModBlockTags.CRIMSON_GROWTH).add(ModBlocks.CRIMSON_GROWTH)
                .add(ModBlocks.DEAD_CRIMSON_GROWTH);
        getOrCreateTagBuilder(ModBlockTags.CRIMSON_SPIKE).add(ModBlocks.CRIMSON_SPIKE)
                .add(ModBlocks.DEAD_CRIMSON_SPIKE);
        getOrCreateTagBuilder(ModBlockTags.CRIMSON_SPINES).add(ModBlocks.CRIMSON_SPINES)
                .add(ModBlocks.DEAD_CRIMSON_SPINES);
        getOrCreateTagBuilder(ModBlockTags.FLUIDS_CANNOT_BREAK).addOptionalTag(BlockTags.DOORS)
                .addOptionalTag(BlockTags.SIGNS)
                .add(Blocks.LADDER)
                .add(Blocks.SUGAR_CANE)
                .add(Blocks.BUBBLE_COLUMN)
                .add(Blocks.NETHER_PORTAL)
                .add(Blocks.NETHER_PORTAL)
                .add(Blocks.END_PORTAL)
                .add(Blocks.END_GATEWAY)
                .add(Blocks.STRUCTURE_VOID)
                .add(ModBlocks.TWISTING_VERDANT_VINES)
                .add(ModBlocks.TWISTING_VERDANT_VINES_PLANT)
                .add(ModBlocks.SUNLIGHT);
        getOrCreateTagBuilder(ModBlockTags.FUNGI_BLOCKS).addOptionalTag(BlockTags.WART_BLOCKS)
                .add(Blocks.SHROOMLIGHT)
                .add(Blocks.BROWN_MUSHROOM_BLOCK)
                .add(Blocks.RED_MUSHROOM_BLOCK)
                .add(Blocks.MUSHROOM_STEM);
        getOrCreateTagBuilder(ModBlockTags.FUNGI_PLANTS).add(Blocks.CRIMSON_FUNGUS)
                .add(Blocks.WARPED_FUNGUS)
                .add(Blocks.RED_MUSHROOM)
                .add(Blocks.BROWN_MUSHROOM)
                .add(Blocks.NETHER_SPROUTS);
        getOrCreateTagBuilder(ModBlockTags.GRASS_PLANTS).add(Blocks.SHORT_GRASS)
                .add(Blocks.TALL_GRASS)
                .add(Blocks.LARGE_FERN)
                .add(Blocks.FERN);
        // Salt and silver halt the killer
        getOrCreateTagBuilder(ModBlockTags.KILLS_SPORES).addOptionalTag(ModBlockTags.HAS_SALT)
                .addOptionalTag(ModBlockTags.HAS_SILVER);
        getOrCreateTagBuilder(ModBlockTags.OPAQUE_FOR_LIGHTING).add(ModBlocks.ROSEITE_STAIRS)
                .add(ModBlocks.ROSEITE_SLAB);
        getOrCreateTagBuilder(ModBlockTags.PLANTS).addOptionalTag(BlockTags.SAPLINGS)
                .addOptionalTag(BlockTags.CROPS)
                .addOptionalTag(ModConventionalBlockTags.DRIPLEAF_PLANTS)
                .add(Blocks.ATTACHED_PUMPKIN_STEM)
                .add(Blocks.ATTACHED_PUMPKIN_STEM)
                .add(Blocks.ATTACHED_MELON_STEM)
                .addOptionalTag(BlockTags.FLOWERS)
                .addOptionalTag(ModBlockTags.VINES)
                .addOptionalTag(ModBlockTags.FUNGI_PLANTS)
                .addOptionalTag(ModBlockTags.ROOTS)
                .addOptionalTag(ModBlockTags.GRASS_PLANTS)
                .addOptionalTag(ModBlockTags.SEA_PLANTS)
                .add(Blocks.DEAD_BUSH)
                .add(Blocks.GLOW_LICHEN)
                .add(Blocks.SPORE_BLOSSOM)
                .add(Blocks.SUGAR_CANE)
                .add(Blocks.SWEET_BERRY_BUSH);
        getOrCreateTagBuilder(ModBlockTags.ROOTS).add(Blocks.HANGING_ROOTS)
                .add(Blocks.WARPED_ROOTS)
                .add(Blocks.CRIMSON_ROOTS);
        getOrCreateTagBuilder(ModBlockTags.ROSEITE_CLUSTER).add(ModBlocks.ROSEITE_CLUSTER)
                .add(ModBlocks.LARGE_ROSEITE_BUD)
                .add(ModBlocks.MEDIUM_ROSEITE_BUD)
                .add(ModBlocks.SMALL_ROSEITE_BUD);
        getOrCreateTagBuilder(ModBlockTags.ROSEITE_GROWABLE).add(ModBlocks.ROSEITE_STAIRS)
                .add(ModBlocks.ROSEITE_SLAB)
                .addOptionalTag(ModBlockTags.ROSEITE_CLUSTER);
        getOrCreateTagBuilder(ModBlockTags.SALTSTONE).add(ModBlocks.SALTSTONE)
                .add(ModBlocks.SALTSTONE_SALT_ORE);
        getOrCreateTagBuilder(ModBlockTags.SCULK_BLOCKS).add(Blocks.SCULK)
                .add(Blocks.SCULK_CATALYST)
                .add(Blocks.CALIBRATED_SCULK_SENSOR)
                .add(Blocks.SCULK_CATALYST)
                .add(Blocks.SCULK_SHRIEKER);
        getOrCreateTagBuilder(ModBlockTags.SEA_PLANTS).add(Blocks.SEAGRASS)
                .add(Blocks.KELP)
                .add(Blocks.KELP_PLANT)
                .add(Blocks.SEA_PICKLE)
                .add(Blocks.TALL_SEAGRASS);
        getOrCreateTagBuilder(ModBlockTags.SEAGULLS_SPAWNABLE_ON).addOptionalTag(
                        BlockTags.ANIMALS_SPAWNABLE_ON)
                .addOptionalTag(ModBlockTags.SALTSTONE)
                .add(Blocks.STONE);
        getOrCreateTagBuilder(ModBlockTags.SILVER_WALKABLE).addOptionalTag(
                        ModConventionalBlockTags.STORAGE_BLOCKS_RAW_SILVER)
                .addOptionalTag(ModConventionalBlockTags.STORAGE_BLOCKS_SILVER)
                .addOptionalTag(ModConventionalBlockTags.SILVER_ORES);
        getOrCreateTagBuilder(ModBlockTags.SMOKES_IN_RAIN).add(ModBlocks.SUNLIGHT);
        getOrCreateTagBuilder(ModBlockTags.SOFT_EARTH).addOptionalTag(BlockTags.SAND)
                .addOptionalTag(ModConventionalBlockTags.GRAVEL)
                .addOptionalTag(BlockTags.DIRT)
                .add(Blocks.CLAY)
                .add(Blocks.FARMLAND)
                .add(Blocks.DIRT_PATH)
                .add(Blocks.SOUL_SAND)
                .add(Blocks.SOUL_SOIL);
        getOrCreateTagBuilder(ModBlockTags.SPORES_CAN_BREAK).addOptionalTag(BlockTags.LOGS)
                .addOptionalTag(BlockTags.PLANKS)
                .addOptionalTag(BlockTags.LEAVES)
                .addOptionalTag(ModBlockTags.WOOD_TYPE)
                .addOptionalTag(ModConventionalBlockTags.BAMBOO_PLANTS)
                .addOptionalTag(BlockTags.BAMBOO_BLOCKS)
                .addOptionalTag(ModConventionalBlockTags.BAMBOO_MOSAIC_BLOCKS)
                .addOptionalTag(ModBlockTags.ALL_GLASS_TYPE)
                .addOptionalTag(ModBlockTags.SOFT_EARTH)
                .addOptionalTag(ModConventionalBlockTags.CONCRETE_POWDER)
                .addOptionalTag(ModConventionalBlockTags.SNOW_BLOCKS)
                .addOptionalTag(BlockTags.WOOL)
                .addOptionalTag(BlockTags.BEDS)
                .addOptionalTag(BlockTags.BANNERS)
                .addOptionalTag(BlockTags.ICE)
                .addOptionalTag(BlockTags.FLOWER_POTS)
                .addOptionalTag(ModConventionalBlockTags.NETHERRACK)
                .addOptionalTag(ModBlockTags.FUNGI_BLOCKS)
                .addOptionalTag(ModConventionalBlockTags.LANTERNS)
                .addOptionalTag(ModConventionalBlockTags.SPONGE)
                .addOptionalTag(ModConventionalBlockTags.PUMPKINS)
                .add(Blocks.HAY_BLOCK)
                .add(Blocks.TARGET)
                .add(Blocks.DRIED_KELP_BLOCK)
                .add(Blocks.MANGROVE_ROOTS)
                .add(Blocks.NETHERRACK)
                .add(Blocks.LILY_PAD)
                .add(Blocks.SCAFFOLDING)
                .add(Blocks.CACTUS)
                .add(Blocks.MELON);
        getOrCreateTagBuilder(ModBlockTags.SPORES_CAN_GROW).addOptionalTag(
                        ModConventionalBlockTags.AIR)
                .addOptionalTag(ModConventionalBlockTags.WATER)
                .addOptionalTag(ModBlockTags.PLANTS)
                .addOptionalTag(BlockTags.CORALS)
                .addOptionalTag(BlockTags.WOOL_CARPETS)
                .addOptionalTag(ModConventionalBlockTags.TORCHES)
                .addOptionalTag(BlockTags.CANDLES)
                .addOptionalTag(BlockTags.FIRE)
                .add(Blocks.LAVA)
                .add(Blocks.SCULK_VEIN)
                .add(Blocks.SNOW)
                .add(Blocks.LIGHT)
                .add(Blocks.STRUCTURE_VOID);
        getOrCreateTagBuilder(ModBlockTags.STEEL_ANVIL).add(ModBlocks.STEEL_ANVIL)
                .add(ModBlocks.CHIPPED_STEEL_ANVIL)
                .add(ModBlocks.DAMAGED_STEEL_ANVIL);
        getOrCreateTagBuilder(ModBlockTags.TALL_CRIMSON_SPINES).add(ModBlocks.TALL_CRIMSON_SPINES)
                .add(ModBlocks.DEAD_TALL_CRIMSON_SPINES);
        getOrCreateTagBuilder(ModBlockTags.TWISTING_VERDANT_VINES).add(
                        ModBlocks.TWISTING_VERDANT_VINES)
                .add(ModBlocks.TWISTING_VERDANT_VINES_PLANT)
                .add(ModBlocks.DEAD_TWISTING_VERDANT_VINES)
                .add(ModBlocks.DEAD_TWISTING_VERDANT_VINES_PLANT);
        getOrCreateTagBuilder(ModBlockTags.VERDANT_VINE_BLOCK).add(ModBlocks.VERDANT_VINE_BLOCK)
                .add(ModBlocks.DEAD_VERDANT_VINE_BLOCK);
        getOrCreateTagBuilder(ModBlockTags.VERDANT_VINE_BRANCH).add(ModBlocks.VERDANT_VINE_BRANCH)
                .add(ModBlocks.DEAD_VERDANT_VINE_BRANCH);
        getOrCreateTagBuilder(ModBlockTags.VERDANT_VINE_SNARE).add(ModBlocks.VERDANT_VINE_SNARE)
                .add(ModBlocks.DEAD_VERDANT_VINE_SNARE);
        getOrCreateTagBuilder(ModBlockTags.VINES).add(Blocks.VINE)
                .addOptionalTag(BlockTags.CAVE_VINES)
                .add(Blocks.WEEPING_VINES)
                .add(Blocks.WEEPING_VINES_PLANT)
                .add(Blocks.TWISTING_VINES)
                .add(Blocks.TWISTING_VINES_PLANT);
        getOrCreateTagBuilder(ModBlockTags.WOOD_TYPE).addOptionalTag(BlockTags.WOODEN_FENCES)
                .addOptionalTag(BlockTags.FENCE_GATES)
                .addOptionalTag(BlockTags.WOODEN_TRAPDOORS)
                .addOptionalTag(BlockTags.WOODEN_DOORS)
                .addOptionalTag(BlockTags.WOODEN_STAIRS)
                .addOptionalTag(BlockTags.WOODEN_SLABS)
                .addOptionalTag(BlockTags.WOODEN_PRESSURE_PLATES)
                .addOptionalTag(BlockTags.WOODEN_BUTTONS)
                .addOptionalTag(BlockTags.ALL_SIGNS);

        // Worldgen
        getOrCreateTagBuilder(ModBlockTags.INFINIBURN_LUMAR).addOptionalTag(
                BlockTags.INFINIBURN_OVERWORLD);
        getOrCreateTagBuilder(ModBlockTags.LUMAR_CARVER_REPLACEABLES).addOptionalTag(
                        BlockTags.BASE_STONE_OVERWORLD)
                .addOptionalTag(BlockTags.DIRT)
                .addOptionalTag(BlockTags.SAND)
                .addOptionalTag(BlockTags.TERRACOTTA)
                .addOptionalTag(BlockTags.IRON_ORES)
                .addOptionalTag(BlockTags.COPPER_ORES)
                .addOptionalTag(ModConventionalBlockTags.GRAVEL)
                .add(Blocks.SANDSTONE)
                .add(Blocks.RED_SANDSTONE)
                .add(Blocks.CALCITE)
                .add(Blocks.SNOW)
                .add(Blocks.PACKED_ICE)
                .addOptionalTag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_IRON)
                .addOptionalTag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_COPPER);

        // Things that contain materials
        getOrCreateTagBuilder(ModBlockTags.HAS_ALUMINUM).add(ModBlocks.ALUMINUM_BLOCK)
                .add(ModBlocks.ALUMINUM_SHEET)
                .addOptionalTag(ModBlockTags.ALUMINUM_CAULDRONS);
        getOrCreateTagBuilder(ModBlockTags.HAS_IRON).addOptionalTag(BlockTags.ANVIL)
                .addOptionalTag(ConventionalBlockTags.STORAGE_BLOCKS_IRON)
                .addOptionalTag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_IRON)
                .addOptionalTag(ModBlockTags.ALL_CAULDRONS)
                .add(Blocks.HOPPER)
                .add(Blocks.IRON_DOOR)
                .add(Blocks.IRON_TRAPDOOR)
                .add(Blocks.IRON_BARS)
                .add(Blocks.IRON_ORE)
                .add(Blocks.DEEPSLATE_IRON_ORE)
                .add(Blocks.BLAST_FURNACE)
                .add(Blocks.SMITHING_TABLE)
                .add(Blocks.STONECUTTER)
                .addOptionalTag(ModConventionalBlockTags.LANTERNS)
                // Does NOT include Powered Rails because those are made of gold
                .add(Blocks.RAIL)
                .add(Blocks.ACTIVATOR_RAIL)
                .add(Blocks.DETECTOR_RAIL)
                .add(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .add(Blocks.PISTON)
                .add(Blocks.STICKY_PISTON)
                .add(Blocks.TRIPWIRE_HOOK);
        getOrCreateTagBuilder(ModBlockTags.HAS_SALT).addOptionalTag(ModBlockTags.SALTSTONE)
                .addOptionalTag(ModConventionalBlockTags.SALT_ORES)
                .addOptionalTag(ModConventionalBlockTags.STORAGE_BLOCKS_SALT);
        getOrCreateTagBuilder(ModBlockTags.HAS_SILVER).addOptionalTag(
                        ModConventionalBlockTags.SILVER_ORES)
                .addOptionalTag(ModConventionalBlockTags.STORAGE_BLOCKS_RAW_SILVER)
                .addOptionalTag(ModConventionalBlockTags.STORAGE_BLOCKS_SILVER);
        getOrCreateTagBuilder(ModBlockTags.HAS_STEEL).addOptionalTag(
                        ModConventionalBlockTags.STORAGE_BLOCKS_STEEL)
                .addOptionalTag(ModBlockTags.STEEL_ANVIL);

        // Mod grouping tags
        getOrCreateTagBuilder(ModBlockTags.ALL_CAULDRONS).addOptionalTag(
                ModConventionalBlockTags.CAULDRONS).addOptionalTag(ModBlockTags.ALUMINUM_CAULDRONS);
        getOrCreateTagBuilder(ModBlockTags.ALL_GLASS_TYPE).addOptionalTag(
                        ConventionalBlockTags.GLASS_BLOCKS)
                .addOptionalTag(ConventionalBlockTags.GLASS_PANES);
        getOrCreateTagBuilder(ModBlockTags.ALL_CRIMSON_GROWTH).addOptionalTag(
                        ModBlockTags.CRIMSON_GROWTH)
                .addOptionalTag(ModBlockTags.CRIMSON_SPIKE)
                .addOptionalTag(ModBlockTags.CRIMSON_SNARE)
                .addOptionalTag(ModBlockTags.TALL_CRIMSON_SPINES)
                .addOptionalTag(ModBlockTags.CRIMSON_SPINES);
        getOrCreateTagBuilder(ModBlockTags.ALL_ROSEITE_GROWTH).add(ModBlocks.ROSEITE_BLOCK)
                .addOptionalTag(ModBlockTags.ROSEITE_GROWABLE);
        getOrCreateTagBuilder(ModBlockTags.ALL_VERDANT_GROWTH).addOptionalTag(
                        ModBlockTags.VERDANT_VINE_BLOCK)
                .addOptionalTag(ModBlockTags.VERDANT_VINE_BRANCH)
                .addOptionalTag(ModBlockTags.VERDANT_VINE_SNARE)
                .addOptionalTag(ModBlockTags.TWISTING_VERDANT_VINES);
    }
}
