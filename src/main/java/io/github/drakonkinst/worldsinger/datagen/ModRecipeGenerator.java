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

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.recipe.SaltedFoodRecipe;
import io.github.drakonkinst.worldsinger.recipe.SilverLinedChestBoatRecipe;
import io.github.drakonkinst.worldsinger.recipe.SilverLinedItemRecipe;
import io.github.drakonkinst.worldsinger.recipe.SporeCannonballRecipe;
import io.github.drakonkinst.worldsinger.recipe.WaterCannonballRecipe;
import io.github.drakonkinst.worldsinger.registry.tag.ModConventionalItemTags;
import io.github.drakonkinst.worldsinger.registry.tag.ModItemTags;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.ComplexRecipeJsonBuilder;
import net.minecraft.data.server.recipe.CookingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;

public class ModRecipeGenerator extends FabricRecipeProvider {

    private static final int DEFAULT_SMELTING_TIME_SECONDS = 10;

    public ModRecipeGenerator(FabricDataOutput output,
            CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        offerStairsRecipe(exporter, ModBlocks.ROSEITE_STAIRS, true, ModBlocks.ROSEITE_BLOCK);
        offerSlabRecipe(exporter, ModBlocks.ROSEITE_SLAB, true, ModBlocks.ROSEITE_BLOCK);

        offerReversibleNuggetIngotRecipe(exporter, ModItems.STEEL_NUGGET, ModItems.STEEL_INGOT);
        offerReversibleIngotBlockRecipe(exporter, ModItems.STEEL_INGOT, ModBlocks.STEEL_BLOCK);
        offerReversibleNuggetIngotRecipe(exporter, ModItems.SILVER_NUGGET, ModItems.SILVER_INGOT);
        offerReversibleIngotBlockRecipe(exporter, ModItems.SILVER_INGOT, ModBlocks.SILVER_BLOCK);
        offerReversibleNuggetIngotRecipe(exporter, ModItems.ALUMINUM_NUGGET,
                ModItems.ALUMINUM_INGOT);
        offerReversibleIngotBlockRecipe(exporter, ModItems.ALUMINUM_INGOT,
                ModBlocks.ALUMINUM_BLOCK);
        offerReversibleIngotBlockRecipe(exporter, ModItems.SALT, ModBlocks.SALT_BLOCK);

        offerBlastingCompatibleRecipe(exporter, ModItems.SILVER_INGOT,
                List.of(ModBlocks.DEEPSLATE_SILVER_ORE, ModItems.RAW_SILVER, ModBlocks.SILVER_ORE),
                0.7f, DEFAULT_SMELTING_TIME_SECONDS);
        offerBlastingCompatibleRecipe(exporter, ModItems.SALT,
                List.of(ModBlocks.SALTSTONE_SALT_ORE), 0.7f, DEFAULT_SMELTING_TIME_SECONDS);
        offerBlastingCompatibleRecipe(exporter, ModItems.ALUMINUM_NUGGET,
                List.of(ModBlocks.ALUMINUM_SHEET), 0.1f, DEFAULT_SMELTING_TIME_SECONDS);
        offerBlastingOnlyRecipe(exporter, ModItems.STEEL_INGOT, List.of(ModItems.CRUDE_IRON), 0.7f,
                DEFAULT_SMELTING_TIME_SECONDS * 3);
        offerMeltingDownRecipe(exporter, ModItems.STEEL_NUGGET, new Item[] {
                ModItems.STEEL_PICKAXE,
                ModItems.STEEL_AXE,
                ModItems.STEEL_SWORD,
                ModItems.STEEL_SHOVEL,
                ModItems.STEEL_HOE,
                ModItems.STEEL_HELMET,
                ModItems.STEEL_CHESTPLATE,
                ModItems.STEEL_LEGGINGS,
                ModItems.STEEL_BOOTS
        }, 0.1f, DEFAULT_SMELTING_TIME_SECONDS);

        generateSpecialRecipes(exporter);
        generateShapedRecipes(exporter);
        generateShapelessRecipes(exporter);
    }

    private void generateSpecialRecipes(RecipeExporter exporter) {
        ComplexRecipeJsonBuilder.create(SaltedFoodRecipe::new).offerTo(exporter, "salted_food");
        ComplexRecipeJsonBuilder.create(SilverLinedChestBoatRecipe::new)
                .offerTo(exporter, "silver_lined_chest_boat");
        ComplexRecipeJsonBuilder.create(SilverLinedItemRecipe::new)
                .offerTo(exporter, "silver_lined_item");
        ComplexRecipeJsonBuilder.create(SporeCannonballRecipe::new)
                .offerTo(exporter, "spore_cannonball");
        ComplexRecipeJsonBuilder.create(WaterCannonballRecipe::new)
                .offerTo(exporter, "water_cannonball");
    }

    private void generateShapedRecipes(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.BREWING, ModBlocks.ALUMINUM_CAULDRON)
                .pattern(" # ")
                .pattern("#C#")
                .pattern(" # ")
                .input('#', ModBlocks.ALUMINUM_SHEET)
                .input('C', Blocks.CAULDRON)
                .criterion(FabricRecipeProvider.hasItem(ModBlocks.ALUMINUM_SHEET),
                        FabricRecipeProvider.conditionsFromItem(ModBlocks.ALUMINUM_SHEET))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.ALUMINUM_SHEET, 16)
                .pattern("###")
                .pattern("###")
                .input('#', ModConventionalItemTags.ALUMINUM_INGOTS)
                .criterion(FabricRecipeProvider.hasItem(ModItems.ALUMINUM_INGOT),
                        FabricRecipeProvider.conditionsFromItem(ModItems.ALUMINUM_INGOT))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.CERAMIC_CANNONBALL, 8)
                .pattern("###")
                .pattern("# #")
                .pattern("###")
                .input('#', Items.BRICK)
                .criterion(FabricRecipeProvider.hasItem(Items.BRICK),
                        FabricRecipeProvider.conditionsFromItem(Items.BRICK))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.BREWING, Items.GLASS_BOTTLE)
                .pattern("# #")
                .pattern(" # ")
                .input('#', ModItems.ROSEITE_CRYSTAL)
                .criterion(FabricRecipeProvider.hasItem(ModItems.ROSEITE_CRYSTAL),
                        FabricRecipeProvider.conditionsFromItem(ModItems.ROSEITE_CRYSTAL))
                .offerTo(exporter, Worldsinger.id("glass_bottle_from_roseite_crystal"));
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.ROSEITE_CORE)
                .pattern("###")
                .pattern("#B#")
                .pattern("###")
                .input('#', ModItems.ROSEITE_CRYSTAL)
                .input('B', Items.WATER_BUCKET)
                .criterion(FabricRecipeProvider.hasItem(ModItems.ROSEITE_CRYSTAL),
                        FabricRecipeProvider.conditionsFromItem(ModItems.ROSEITE_CRYSTAL))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.SILVER_KNIFE)
                .pattern("X")
                .pattern("#")
                .input('X', ModConventionalItemTags.SILVER_INGOTS)
                .input('#', Items.STICK)
                .criterion(FabricRecipeProvider.hasItem(ModItems.SILVER_INGOT),
                        FabricRecipeProvider.conditionsFromTag(
                                ModConventionalItemTags.SILVER_INGOTS))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.STEEL_ANVIL)
                .pattern("III")
                .pattern(" i ")
                .pattern("iii")
                .input('I', ModConventionalItemTags.STORAGE_BLOCKS_STEEL)
                .input('i', ModConventionalItemTags.STEEL_INGOTS)
                .criterion(FabricRecipeProvider.hasItem(ModBlocks.STEEL_BLOCK),
                        FabricRecipeProvider.conditionsFromTag(
                                ModConventionalItemTags.STORAGE_BLOCKS_STEEL))
                .offerTo(exporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.BREWING, Blocks.BREWING_STAND)
                .pattern(" B ")
                .pattern("###")
                .input('B', ModItems.CRIMSON_SPINE)
                .input('#', ItemTags.STONE_CRAFTING_MATERIALS)
                .criterion(FabricRecipeProvider.hasItem(ModItems.CRIMSON_SPINE),
                        FabricRecipeProvider.conditionsFromItem(ModItems.CRIMSON_SPINE))
                .offerTo(exporter);
        // Should replace vanilla recipe
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Blocks.COMPARATOR)
                .pattern(" # ")
                .pattern("#X#")
                .pattern("III")
                .input('#', Items.REDSTONE_TORCH)
                .input('I', Items.COMPARATOR)
                .input('X', ModItemTags.REPLACES_QUARTZ_IN_REDSTONE)
                .criterion(FabricRecipeProvider.hasItem(Items.QUARTZ),
                        FabricRecipeProvider.conditionsFromTag(
                                ModItemTags.REPLACES_QUARTZ_IN_REDSTONE))
                .offerTo(exporter);
        // Should replace vanilla recipe
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Blocks.DAYLIGHT_DETECTOR)
                .pattern("GGG")
                .pattern("QQQ")
                .pattern("WWW")
                .input('Q', ModItemTags.REPLACES_QUARTZ_IN_REDSTONE)
                .input('G', Blocks.GLASS)
                .input('W', ItemTags.WOODEN_SLABS)
                .criterion(FabricRecipeProvider.hasItem(Items.QUARTZ),
                        FabricRecipeProvider.conditionsFromTag(
                                ModItemTags.REPLACES_QUARTZ_IN_REDSTONE))
                .offerTo(exporter);
        // Should replace vanilla recipe
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, Blocks.OBSERVER)
                .pattern("###")
                .pattern("RRQ")
                .pattern("###")
                .input('Q', ModItemTags.REPLACES_QUARTZ_IN_REDSTONE)
                .input('R', Items.REDSTONE)
                .input('#', Blocks.COBBLESTONE)
                .criterion(FabricRecipeProvider.hasItem(Items.QUARTZ),
                        FabricRecipeProvider.conditionsFromTag(
                                ModItemTags.REPLACES_QUARTZ_IN_REDSTONE))
                .offerTo(exporter);
        offer2x2CompactingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS, ModBlocks.ROSEITE_BLOCK,
                ModItems.ROSEITE_CRYSTAL);
        offerDefaultArmorRecipes(exporter, ModConventionalItemTags.STEEL_INGOTS,
                ModItems.STEEL_INGOT, ModItems.STEEL_HELMET, ModItems.STEEL_CHESTPLATE,
                ModItems.STEEL_LEGGINGS, ModItems.STEEL_BOOTS);
        offerDefaultAxeRecipe(exporter, ModConventionalItemTags.STEEL_INGOTS, ModItems.STEEL_INGOT,
                ModItems.STEEL_AXE);
        offerDefaultHoeRecipe(exporter, ModConventionalItemTags.STEEL_INGOTS, ModItems.STEEL_INGOT,
                ModItems.STEEL_HOE);
        offerDefaultPickaxeRecipe(exporter, ModConventionalItemTags.STEEL_INGOTS,
                ModItems.STEEL_INGOT, ModItems.STEEL_PICKAXE);
        offerDefaultShovelRecipe(exporter, ModConventionalItemTags.STEEL_INGOTS,
                ModItems.STEEL_INGOT, ModItems.STEEL_SHOVEL);
        offerDefaultSwordRecipe(exporter, ModConventionalItemTags.STEEL_INGOTS,
                ModItems.STEEL_INGOT, ModItems.STEEL_SWORD);
    }

    private void generateShapelessRecipes(RecipeExporter exporter) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.CRUDE_IRON)
                .input(ConventionalItemTags.IRON_INGOTS)
                .input(ItemTags.COALS)
                .criterion(FabricRecipeProvider.hasItem(Items.IRON_INGOT),
                        FabricRecipeProvider.conditionsFromTag(ConventionalItemTags.IRON_INGOTS))
                .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.FLINT_AND_IRON)
                .input(ConventionalItemTags.IRON_INGOTS)
                .input(Items.FLINT)
                .criterion(FabricRecipeProvider.hasItem(Items.FLINT),
                        FabricRecipeProvider.conditionsFromItem(Items.FLINT))
                .criterion(FabricRecipeProvider.hasItem(Items.OBSIDIAN),
                        FabricRecipeProvider.conditionsFromItem(Blocks.OBSIDIAN))
                .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.QUARTZ_AND_IRON)
                .input(ConventionalItemTags.IRON_INGOTS)
                .input(Items.QUARTZ)
                .criterion(FabricRecipeProvider.hasItem(Items.QUARTZ),
                        FabricRecipeProvider.conditionsFromItem(Items.QUARTZ))
                .criterion(FabricRecipeProvider.hasItem(Items.OBSIDIAN),
                        FabricRecipeProvider.conditionsFromItem(Blocks.OBSIDIAN))
                .offerTo(exporter);
        // Should replace vanilla recipe
        ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.FLINT_AND_STEEL)
                .input(ModConventionalItemTags.STEEL_INGOTS)
                .input(Items.FLINT)
                .criterion(FabricRecipeProvider.hasItem(Items.FLINT),
                        FabricRecipeProvider.conditionsFromItem(Items.FLINT))
                .criterion(FabricRecipeProvider.hasItem(Items.OBSIDIAN),
                        FabricRecipeProvider.conditionsFromItem(Blocks.OBSIDIAN))
                .offerTo(exporter);
        ShapelessRecipeJsonBuilder.create(RecipeCategory.TOOLS, ModItems.QUARTZ_AND_STEEL)
                .input(ModConventionalItemTags.STEEL_INGOTS)
                .input(Items.QUARTZ)
                .criterion(FabricRecipeProvider.hasItem(Items.QUARTZ),
                        FabricRecipeProvider.conditionsFromItem(Items.QUARTZ))
                .criterion(FabricRecipeProvider.hasItem(Items.OBSIDIAN),
                        FabricRecipeProvider.conditionsFromItem(Blocks.OBSIDIAN))
                .offerTo(exporter);
    }

    private void offerDefaultArmorRecipes(RecipeExporter exporter, TagKey<Item> input,
            ItemConvertible unlockInput, Item helmet, Item chestplate, Item leggings, Item boots) {
        offerDefaultHelmetRecipe(exporter, input, unlockInput, helmet);
        offerDefaultChestplateRecipe(exporter, input, unlockInput, chestplate);
        offerDefaultLeggingsRecipe(exporter, input, unlockInput, leggings);
        offerDefaultBootsRecipe(exporter, input, unlockInput, boots);
    }

    private void offerDefaultHelmetRecipe(RecipeExporter exporter, TagKey<Item> input,
            ItemConvertible unlockInput, Item output) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, output)
                .pattern("XXX")
                .pattern("X X")
                .input('X', input)
                .criterion(FabricRecipeProvider.hasItem(unlockInput),
                        FabricRecipeProvider.conditionsFromTag(input))
                .offerTo(exporter);
    }

    private void offerDefaultChestplateRecipe(RecipeExporter exporter, TagKey<Item> input,
            ItemConvertible unlockInput, Item output) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, output)
                .pattern("X X")
                .pattern("XXX")
                .pattern("XXX")
                .input('X', input)
                .criterion(FabricRecipeProvider.hasItem(unlockInput),
                        FabricRecipeProvider.conditionsFromTag(input))
                .offerTo(exporter);
    }

    private void offerDefaultLeggingsRecipe(RecipeExporter exporter, TagKey<Item> input,
            ItemConvertible unlockInput, Item output) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, output)
                .pattern("XXX")
                .pattern("X X")
                .pattern("X X")
                .input('X', input)
                .criterion(FabricRecipeProvider.hasItem(unlockInput),
                        FabricRecipeProvider.conditionsFromTag(input))
                .offerTo(exporter);
    }

    private void offerDefaultBootsRecipe(RecipeExporter exporter, TagKey<Item> input,
            ItemConvertible unlockInput, Item output) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, output)
                .pattern("X X")
                .pattern("X X")
                .input('X', input)
                .criterion(FabricRecipeProvider.hasItem(unlockInput),
                        FabricRecipeProvider.conditionsFromTag(input))
                .offerTo(exporter);
    }

    private void offerDefaultSwordRecipe(RecipeExporter exporter, TagKey<Item> input,
            ItemConvertible unlockInput, Item output) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.COMBAT, output)
                .pattern("X")
                .pattern("X")
                .pattern("#")
                .input('X', input)
                .input('#', Items.STICK)
                .criterion(FabricRecipeProvider.hasItem(unlockInput),
                        FabricRecipeProvider.conditionsFromTag(input))
                .offerTo(exporter);
    }

    private void offerDefaultAxeRecipe(RecipeExporter exporter, TagKey<Item> input,
            ItemConvertible unlockInput, Item output) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, output)
                .pattern("XX")
                .pattern("X#")
                .pattern(" #")
                .input('X', input)
                .input('#', Items.STICK)
                .criterion(FabricRecipeProvider.hasItem(unlockInput),
                        FabricRecipeProvider.conditionsFromTag(input))
                .offerTo(exporter);
    }

    private void offerDefaultHoeRecipe(RecipeExporter exporter, TagKey<Item> input,
            ItemConvertible unlockInput, Item output) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, output)
                .pattern("XX")
                .pattern(" #")
                .pattern(" #")
                .input('X', input)
                .input('#', Items.STICK)
                .criterion(FabricRecipeProvider.hasItem(unlockInput),
                        FabricRecipeProvider.conditionsFromTag(input))
                .offerTo(exporter);
    }

    private void offerDefaultPickaxeRecipe(RecipeExporter exporter, TagKey<Item> input,
            ItemConvertible unlockInput, Item output) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, output)
                .pattern("XXX")
                .pattern(" # ")
                .pattern(" # ")
                .input('X', input)
                .input('#', Items.STICK)
                .criterion(FabricRecipeProvider.hasItem(unlockInput),
                        FabricRecipeProvider.conditionsFromTag(input))
                .offerTo(exporter);
    }

    private void offerDefaultShovelRecipe(RecipeExporter exporter, TagKey<Item> input,
            ItemConvertible unlockInput, Item output) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, output)
                .pattern("X")
                .pattern("#")
                .pattern("#")
                .input('X', input)
                .input('#', Items.STICK)
                .criterion(FabricRecipeProvider.hasItem(unlockInput),
                        FabricRecipeProvider.conditionsFromItem(unlockInput))
                .offerTo(exporter);
    }

    private void offerBlastingCompatibleRecipe(RecipeExporter exporter, Item output,
            List<ItemConvertible> input, float experience, int cookingTimeInSeconds) {
        String outputId = Registries.ITEM.getId(output).getPath();
        RecipeProvider.offerSmelting(exporter, input, RecipeCategory.MISC, output, experience,
                cookingTimeInSeconds * ModConstants.SECONDS_TO_TICKS, outputId);
        RecipeProvider.offerBlasting(exporter, input, RecipeCategory.MISC, output, experience,
                cookingTimeInSeconds * ModConstants.SECONDS_TO_TICKS / 2, outputId);
    }

    private void offerBlastingOnlyRecipe(RecipeExporter exporter, Item output,
            List<ItemConvertible> input, float experience, int cookingTimeInSeconds) {
        String outputId = Registries.ITEM.getId(output).getPath();
        RecipeProvider.offerBlasting(exporter, input, RecipeCategory.MISC, output, experience,
                cookingTimeInSeconds * ModConstants.SECONDS_TO_TICKS, outputId);
    }

    private void offerMeltingDownRecipe(RecipeExporter exporter, Item output, Item[] input,
            float experience, int cookingTimeInSeconds) {
        Ingredient ingredient = Ingredient.ofItems(input);
        CookingRecipeJsonBuilder smelting = CookingRecipeJsonBuilder.createSmelting(ingredient,
                RecipeCategory.MISC, output, experience,
                cookingTimeInSeconds * ModConstants.SECONDS_TO_TICKS);
        CookingRecipeJsonBuilder blasting = CookingRecipeJsonBuilder.createBlasting(ingredient,
                RecipeCategory.MISC, output, experience,
                cookingTimeInSeconds * ModConstants.SECONDS_TO_TICKS / 2);

        for (Item item : input) {
            String itemId = Registries.ITEM.getId(item).getPath();
            smelting.criterion("has_" + itemId, RecipeProvider.conditionsFromItem(item));
            blasting.criterion("has_" + itemId, RecipeProvider.conditionsFromItem(item));
        }

        smelting.offerTo(exporter, RecipeProvider.getSmeltingItemPath(output));
        blasting.offerTo(exporter, RecipeProvider.getBlastingItemPath(output));
    }

    private void offerReversibleNuggetIngotRecipe(RecipeExporter exporter, Item nugget,
            Item ingot) {
        String ingotId = Registries.ITEM.getId(ingot).getPath();
        RecipeProvider.offerReversibleCompactingRecipesWithCompactingRecipeGroup(exporter,
                RecipeCategory.MISC, nugget, RecipeCategory.MISC, ingot,
                Worldsinger.idStr(ingotId) + "_from_nuggets", ingotId);
    }

    private void offerReversibleIngotBlockRecipe(RecipeExporter exporter, Item ingot, Block block) {
        String ingotId = Registries.ITEM.getId(ingot).getPath();
        String blockId = Registries.ITEM.getId(block.asItem()).getPath();
        RecipeProvider.offerReversibleCompactingRecipesWithReverseRecipeGroup(exporter,
                RecipeCategory.MISC, ingot, RecipeCategory.BUILDING_BLOCKS, block,
                Worldsinger.idStr(ingotId) + "_from_" + blockId, ingotId);
    }

    private void offerStairsRecipe(RecipeExporter exporter, ItemConvertible stairsOutput,
            boolean addStonecutterRecipe, ItemConvertible... input) {
        CraftingRecipeJsonBuilder recipeBuilder = RecipeProvider.createStairsRecipe(stairsOutput,
                Ingredient.ofItems(input));
        for (ItemConvertible inputItem : input) {
            String criterionName = "has_" + Registries.ITEM.getId(inputItem.asItem()).getPath();
            recipeBuilder = recipeBuilder.criterion(criterionName,
                    RecipeProvider.conditionsFromItem(inputItem));
        }
        recipeBuilder.offerTo(exporter);

        if (addStonecutterRecipe) {
            for (ItemConvertible inputItem : input) {
                RecipeProvider.offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS,
                        stairsOutput, inputItem);
            }
        }
    }

    private void offerSlabRecipe(RecipeExporter exporter, ItemConvertible slabOutput,
            boolean addStonecutterRecipe, ItemConvertible... input) {
        CraftingRecipeJsonBuilder recipeBuilder = RecipeProvider.createSlabRecipe(
                RecipeCategory.BUILDING_BLOCKS, slabOutput, Ingredient.ofItems(input));
        for (ItemConvertible inputItem : input) {
            String criterionName = "has_" + Registries.ITEM.getId(inputItem.asItem()).getPath();
            recipeBuilder = recipeBuilder.criterion(criterionName,
                    RecipeProvider.conditionsFromItem(inputItem));
        }
        recipeBuilder.offerTo(exporter);

        if (addStonecutterRecipe) {
            for (ItemConvertible inputItem : input) {
                RecipeProvider.offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS,
                        slabOutput, inputItem, 2);
            }
        }
    }
}
