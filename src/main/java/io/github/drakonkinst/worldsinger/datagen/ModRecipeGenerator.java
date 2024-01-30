package io.github.drakonkinst.worldsinger.datagen;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import java.util.List;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.block.Block;
import net.minecraft.data.server.recipe.CookingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;

public class ModRecipeGenerator extends FabricRecipeProvider {

    private static final int DEFAULT_SMELTING_TIME_SECONDS = 10;

    public ModRecipeGenerator(FabricDataOutput output) {
        super(output);
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
    }

    private void offerBlastingCompatibleRecipe(RecipeExporter exporter, Item output,
            List<ItemConvertible> input, float experience, int cookingTimeInSeconds) {
        String outputId = Registries.ITEM.getId(output).getPath();
        RecipeProvider.offerSmelting(exporter, input, RecipeCategory.MISC, output, experience,
                cookingTimeInSeconds * ModConstants.SECONDS_TO_TICKS, outputId);
        RecipeProvider.offerBlasting(exporter, input, RecipeCategory.MISC, output, experience,
                cookingTimeInSeconds * ModConstants.SECONDS_TO_TICKS / 2, outputId);
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
