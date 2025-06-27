package io.github.drakonkinst.worldsinger.datagen.recipe;

import io.github.drakonkinst.worldsinger.recipe.ShapelessComponentCopyRecipe;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.component.ComponentType;
import net.minecraft.data.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;

// Based on ShapelessRecipeJsonBuilder
public class ShapelessComponentCopyRecipeJsonBuilder implements CraftingRecipeJsonBuilder {

    private final RegistryEntryLookup<Item> registryLookup;
    private final RecipeCategory category;
    private final ItemStack output;
    private final List<Ingredient> inputs = new ArrayList<>();
    private final Map<String, AdvancementCriterion<?>> advancementBuilder = new LinkedHashMap<>();
    private final List<ComponentType<?>> componentTypesToCopy = new ArrayList<>();
    @Nullable
    private String group;
    @Nullable
    private Ingredient sourceIngredient;

    private ShapelessComponentCopyRecipeJsonBuilder(RegistryEntryLookup<Item> registryLookup,
            RecipeCategory category, ItemStack output) {
        this.registryLookup = registryLookup;
        this.category = category;
        this.output = output;
    }

    public static ShapelessComponentCopyRecipeJsonBuilder create(
            RegistryEntryLookup<Item> registryLookup, RecipeCategory category, ItemStack output) {
        return new ShapelessComponentCopyRecipeJsonBuilder(registryLookup, category, output);
    }

    public static ShapelessComponentCopyRecipeJsonBuilder create(
            RegistryEntryLookup<Item> registryLookup, RecipeCategory category,
            ItemConvertible output) {
        return create(registryLookup, category, output, 1);
    }

    public static ShapelessComponentCopyRecipeJsonBuilder create(
            RegistryEntryLookup<Item> registryLookup, RecipeCategory category,
            ItemConvertible output, int count) {
        return new ShapelessComponentCopyRecipeJsonBuilder(registryLookup, category,
                output.asItem().getDefaultStack().copyWithCount(count));
    }

    public ShapelessComponentCopyRecipeJsonBuilder input(TagKey<Item> tag) {
        return this.input(Ingredient.ofTag(this.registryLookup.getOrThrow(tag)));
    }

    public ShapelessComponentCopyRecipeJsonBuilder input(ItemConvertible item) {
        return this.input(item, 1);
    }

    public ShapelessComponentCopyRecipeJsonBuilder input(ItemConvertible item, int amount) {
        for (int i = 0; i < amount; i++) {
            this.input(Ingredient.ofItem(item));
        }

        return this;
    }

    public ShapelessComponentCopyRecipeJsonBuilder input(Ingredient ingredient) {
        return this.input(ingredient, 1);
    }

    public ShapelessComponentCopyRecipeJsonBuilder input(Ingredient ingredient, int amount) {
        for (int i = 0; i < amount; i++) {
            this.inputs.add(ingredient);
        }

        return this;
    }

    public ShapelessComponentCopyRecipeJsonBuilder componentSource(Ingredient ingredient) {
        this.sourceIngredient = ingredient;
        return this;
    }

    public ShapelessComponentCopyRecipeJsonBuilder componentSource(ItemConvertible item) {
        return this.componentSource(Ingredient.ofItem(item));
    }

    public ShapelessComponentCopyRecipeJsonBuilder copyComponent(ComponentType<?> componentType) {
        this.componentTypesToCopy.add(componentType);
        return this;
    }

    public ShapelessComponentCopyRecipeJsonBuilder criterion(String string,
            AdvancementCriterion<?> advancementCriterion) {
        this.advancementBuilder.put(string, advancementCriterion);
        return this;
    }

    public ShapelessComponentCopyRecipeJsonBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }

    @Override
    public Item getOutputItem() {
        return this.output.getItem();
    }

    @Override
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        this.validate(recipeKey);
        Advancement.Builder builder = exporter.getAdvancementBuilder()
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey))
                .rewards(AdvancementRewards.Builder.recipe(recipeKey))
                .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        this.advancementBuilder.forEach(builder::criterion);
        ShapelessComponentCopyRecipe shapelessRecipe = new ShapelessComponentCopyRecipe(
                Objects.requireNonNullElse(this.group, ""),
                CraftingRecipeJsonBuilder.toCraftingCategory(this.category), this.output,
                this.inputs, this.sourceIngredient, this.componentTypesToCopy);
        exporter.accept(recipeKey, shapelessRecipe, builder.build(
                recipeKey.getValue().withPrefixedPath("recipes/" + this.category.getName() + "/")));
    }

    private void validate(RegistryKey<Recipe<?>> recipeKey) {
        if (this.advancementBuilder.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeKey.getValue());
        }
    }
}
