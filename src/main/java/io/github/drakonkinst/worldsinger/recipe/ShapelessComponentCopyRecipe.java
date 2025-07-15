package io.github.drakonkinst.worldsinger.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.IngredientPlacement;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShapelessComponentCopyRecipe implements CraftingRecipe {

    protected final String group;
    protected final CraftingRecipeCategory category;
    protected final ItemStack result;
    protected final List<Ingredient> ingredients;
    protected final Ingredient sourceIngredient;
    protected final List<ComponentType<?>> componentTypesToCopy;
    @Nullable
    private IngredientPlacement ingredientPlacement;

    public ShapelessComponentCopyRecipe(String group, CraftingRecipeCategory category,
            ItemStack result, List<Ingredient> ingredients, Ingredient sourceIngredient,
            List<ComponentType<?>> componentTypesToCopy) {
        this.group = group;
        this.category = category;
        this.result = result;
        this.ingredients = ingredients;
        this.sourceIngredient = sourceIngredient;
        this.componentTypesToCopy = componentTypesToCopy;
    }

    @Override
    public RecipeSerializer<ShapelessComponentCopyRecipe> getSerializer() {
        return ModRecipeSerializer.SHAPELESS_COMPONENT_COPY;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return this.category;
    }

    @Override
    public IngredientPlacement getIngredientPlacement() {
        if (this.ingredientPlacement == null) {
            this.ingredientPlacement = IngredientPlacement.forShapeless(this.ingredients);
        }

        return this.ingredientPlacement;
    }

    public boolean matches(CraftingRecipeInput craftingRecipeInput, World world) {
        if (craftingRecipeInput.getStackCount() != this.ingredients.size()) {
            return false;
        } else {
            return craftingRecipeInput.size() == 1 && this.ingredients.size() == 1
                    ? this.ingredients.getFirst().test(craftingRecipeInput.getStackInSlot(0))
                    : craftingRecipeInput.getRecipeMatcher().isCraftable(this, null);
        }
    }

    public ItemStack craft(CraftingRecipeInput craftingRecipeInput,
            RegistryWrapper.WrapperLookup wrapperLookup) {
        ItemStack sourceStack = ItemStack.EMPTY;
        // Find the source ingredient
        for (int i = 0; i < craftingRecipeInput.size(); ++i) {
            ItemStack stack = craftingRecipeInput.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (sourceIngredient.test(stack)) {
                sourceStack = stack;
                break;
            }
        }
        ItemStack resultStack = this.result.copy();
        for (ComponentType<?> componentType : componentTypesToCopy) {
            resultStack.copy(componentType, sourceStack);
        }
        return resultStack;
    }

    @Override
    public List<RecipeDisplay> getDisplays() {
        return List.of(new ShapelessCraftingRecipeDisplay(
                this.ingredients.stream().map(Ingredient::toDisplay).toList(),
                new SlotDisplay.StackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)));
    }

    public static class Serializer implements RecipeSerializer<ShapelessComponentCopyRecipe> {

        private static final MapCodec<ShapelessComponentCopyRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
                                CraftingRecipeCategory.CODEC.fieldOf("category")
                                        .orElse(CraftingRecipeCategory.MISC)
                                        .forGetter(recipe -> recipe.category),
                                ItemStack.VALIDATED_CODEC.fieldOf("result")
                                        .forGetter(recipe -> recipe.result), Ingredient.CODEC.listOf(1, 9)
                                        .fieldOf("ingredients")
                                        .forGetter(recipe -> recipe.ingredients),
                                Ingredient.CODEC.fieldOf("source_ingredient")
                                        .forGetter(recipe -> recipe.sourceIngredient),
                                ComponentType.CODEC.listOf()
                                        .fieldOf("copy_components")
                                        .forGetter(recipe -> recipe.componentTypesToCopy))
                        .apply(instance, ShapelessComponentCopyRecipe::new));
        public static final PacketCodec<RegistryByteBuf, ShapelessComponentCopyRecipe> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.STRING, recipe -> recipe.group, CraftingRecipeCategory.PACKET_CODEC,
                recipe -> recipe.category, ItemStack.PACKET_CODEC, recipe -> recipe.result,
                Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()),
                recipe -> recipe.ingredients, Ingredient.PACKET_CODEC,
                recipe -> recipe.sourceIngredient,
                ComponentType.PACKET_CODEC.collect(PacketCodecs.toList()),
                recipe -> recipe.componentTypesToCopy, ShapelessComponentCopyRecipe::new);

        @Override
        public MapCodec<ShapelessComponentCopyRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, ShapelessComponentCopyRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
