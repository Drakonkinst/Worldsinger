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

import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.registry.tag.ModConventionalBlockTags;
import io.github.drakonkinst.worldsinger.registry.tag.ModConventionalItemTags;
import io.github.drakonkinst.worldsinger.registry.tag.ModItemTags;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.ItemTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.ItemTags;
import org.jetbrains.annotations.Nullable;

// https://github.com/FabricMC/fabric/tree/1.20.5/fabric-convention-tags-v2/src/generated/resources/data/c/tags/items
// https://maven.fabricmc.net/docs/fabric-api-0.97.4+1.20.5/net/fabricmc/fabric/api/tag/convention/v2/ConventionalItemTags.html
public class ModItemTagGenerator extends ItemTagProvider {

    public ModItemTagGenerator(FabricDataOutput output,
            CompletableFuture<WrapperLookup> completableFuture,
            @Nullable BlockTagProvider blockTagProvider) {
        super(output, completableFuture, blockTagProvider);
    }

    @Override
    protected void configure(WrapperLookup lookup) {
        // Add modded items to vanilla tags
        getOrCreateTagBuilder(ItemTags.AXES).add(ModItems.STEEL_AXE);
        getOrCreateTagBuilder(ItemTags.PICKAXES).add(ModItems.STEEL_PICKAXE);
        getOrCreateTagBuilder(ItemTags.SHOVELS).add(ModItems.STEEL_SHOVEL);
        getOrCreateTagBuilder(ItemTags.HOES).add(ModItems.STEEL_HOE);
        getOrCreateTagBuilder(ItemTags.SWORDS).add(ModItems.STEEL_SWORD);
        getOrCreateTagBuilder(ItemTags.HEAD_ARMOR).add(ModItems.STEEL_HELMET);
        getOrCreateTagBuilder(ItemTags.CHEST_ARMOR).add(ModItems.STEEL_CHESTPLATE);
        getOrCreateTagBuilder(ItemTags.LEG_ARMOR).add(ModItems.STEEL_LEGGINGS);
        getOrCreateTagBuilder(ItemTags.FOOT_ARMOR).add(ModItems.STEEL_BOOTS);
        getOrCreateTagBuilder(ItemTags.WEAPON_ENCHANTABLE).add(ModItems.SILVER_KNIFE);
        getOrCreateTagBuilder(ItemTags.CREEPER_IGNITERS).add(ModItems.FLINT_AND_IRON)
                .add(ModItems.QUARTZ_AND_IRON)
                .add(ModItems.QUARTZ_AND_STEEL);

        // Add mod conventional tags
        getOrCreateTagBuilder(ModConventionalItemTags.STEEL_INGOTS).add(ModItems.STEEL_INGOT);
        getOrCreateTagBuilder(ModConventionalItemTags.SILVER_INGOTS).add(ModItems.SILVER_INGOT);
        getOrCreateTagBuilder(ModConventionalItemTags.ALUMINUM_INGOTS).add(ModItems.ALUMINUM_INGOT);
        getOrCreateTagBuilder(ModConventionalItemTags.STEEL_NUGGETS).add(ModItems.STEEL_NUGGET);
        getOrCreateTagBuilder(ModConventionalItemTags.SILVER_NUGGETS).add(ModItems.SILVER_NUGGET);
        getOrCreateTagBuilder(ModConventionalItemTags.ALUMINUM_NUGGETS).add(
                ModItems.ALUMINUM_NUGGET);
        copy(ModConventionalBlockTags.SILVER_ORES, ModConventionalItemTags.SILVER_ORES);
        copy(ModConventionalBlockTags.SALT_ORES, ModConventionalItemTags.SALT_ORES);
        copy(ModConventionalBlockTags.STORAGE_BLOCKS_STEEL,
                ModConventionalItemTags.STORAGE_BLOCKS_STEEL);
        copy(ModConventionalBlockTags.STORAGE_BLOCKS_SILVER,
                ModConventionalItemTags.STORAGE_BLOCKS_SILVER);
        copy(ModConventionalBlockTags.STORAGE_BLOCKS_ALUMINUM,
                ModConventionalItemTags.STORAGE_BLOCKS_ALUMINUM);

        // Merge fabric and mod conventional tags
        getOrCreateTagBuilder(ConventionalItemTags.INGOTS).addOptionalTag(
                        ModConventionalItemTags.STEEL_INGOTS)
                .addOptionalTag(ModConventionalItemTags.SILVER_INGOTS)
                .addOptionalTag(ModConventionalItemTags.ALUMINUM_INGOTS);
        getOrCreateTagBuilder(ConventionalItemTags.NUGGETS).addOptionalTag(
                        ModConventionalItemTags.STEEL_NUGGETS)
                .addOptionalTag(ModConventionalItemTags.SILVER_NUGGETS)
                .addOptionalTag(ModConventionalItemTags.ALUMINUM_NUGGETS);
        copy(ConventionalBlockTags.ORES, ConventionalItemTags.ORES);
        copy(ConventionalBlockTags.STORAGE_BLOCKS, ConventionalItemTags.STORAGE_BLOCKS);

        // Add mod tags
        getOrCreateTagBuilder(ModItemTags.ALL_COOKED_MEAT).add(Items.COOKED_BEEF)
                .add(Items.COOKED_CHICKEN)
                .add(Items.COOKED_MUTTON)
                .add(Items.COOKED_RABBIT)
                // Not in the API for some reason
                .add(Items.COOKED_PORKCHOP)
                .add(Items.COOKED_COD)
                .add(Items.COOKED_SALMON)
                .addOptionalTag(ConventionalItemTags.COOKED_MEATS_FOODS)
                .addOptionalTag(ConventionalItemTags.COOKED_FISHES_FOODS);
        getOrCreateTagBuilder(ModItemTags.ALL_RAW_MEAT).add(Items.BEEF)
                .add(Items.CHICKEN)
                .add(Items.MUTTON)
                .add(Items.RABBIT)
                // Not in the API for some reason
                .add(Items.COOKED_PORKCHOP)
                .add(Items.COOKED_COD)
                .add(Items.COOKED_SALMON)
                .addOptionalTag(ConventionalItemTags.RAW_MEATS_FOODS)
                .addOptionalTag(ConventionalItemTags.RAW_FISHES_FOODS);
    }
}
