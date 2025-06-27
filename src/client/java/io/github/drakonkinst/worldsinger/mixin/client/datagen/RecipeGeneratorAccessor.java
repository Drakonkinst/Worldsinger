package io.github.drakonkinst.worldsinger.mixin.client.datagen;

import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryEntryLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeGenerator.class)
public interface RecipeGeneratorAccessor {

    @Accessor("itemLookup")
    RegistryEntryLookup<Item> worldsinger$getItemLookup();
}
