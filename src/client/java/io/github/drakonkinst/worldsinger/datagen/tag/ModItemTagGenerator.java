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

package io.github.drakonkinst.worldsinger.datagen.tag;

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
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
        valueLookupBuilder(ItemTags.AXES).add(ModItems.STEEL_AXE);
        valueLookupBuilder(ItemTags.PICKAXES).add(ModItems.STEEL_PICKAXE);
        valueLookupBuilder(ItemTags.SHOVELS).add(ModItems.STEEL_SHOVEL);
        valueLookupBuilder(ItemTags.HOES).add(ModItems.STEEL_HOE);
        valueLookupBuilder(ItemTags.SWORDS).add(ModItems.STEEL_SWORD);
        valueLookupBuilder(ItemTags.HEAD_ARMOR).add(ModItems.STEEL_HELMET);
        valueLookupBuilder(ItemTags.CHEST_ARMOR).add(ModItems.STEEL_CHESTPLATE);
        valueLookupBuilder(ItemTags.LEG_ARMOR).add(ModItems.STEEL_LEGGINGS);
        valueLookupBuilder(ItemTags.FOOT_ARMOR).add(ModItems.STEEL_BOOTS);
        valueLookupBuilder(ItemTags.WEAPON_ENCHANTABLE).add(ModItems.SILVER_KNIFE);
        valueLookupBuilder(ItemTags.CREEPER_IGNITERS).add(ModItems.FLINT_AND_IRON)
                .add(ModItems.QUARTZ_AND_IRON)
                .add(ModItems.QUARTZ_AND_STEEL);

        // Add mod conventional tags
        valueLookupBuilder(ModConventionalItemTags.STEEL_INGOTS).add(ModItems.STEEL_INGOT);
        valueLookupBuilder(ModConventionalItemTags.SILVER_INGOTS).add(ModItems.SILVER_INGOT);
        valueLookupBuilder(ModConventionalItemTags.ALUMINUM_INGOTS).add(ModItems.ALUMINUM_INGOT);
        valueLookupBuilder(ModConventionalItemTags.STEEL_NUGGETS).add(ModItems.STEEL_NUGGET);
        valueLookupBuilder(ModConventionalItemTags.SILVER_NUGGETS).add(ModItems.SILVER_NUGGET);
        valueLookupBuilder(ModConventionalItemTags.ALUMINUM_NUGGETS).add(ModItems.ALUMINUM_NUGGET);
        valueLookupBuilder(ModConventionalItemTags.SILVER_RAW_MATERIALS).add(ModItems.RAW_SILVER);
        valueLookupBuilder(ModConventionalItemTags.SALT).add(ModItems.SALT);
        copy(ModConventionalBlockTags.SILVER_ORES, ModConventionalItemTags.SILVER_ORES);
        copy(ModConventionalBlockTags.SALT_ORES, ModConventionalItemTags.SALT_ORES);
        copy(ModConventionalBlockTags.STORAGE_BLOCKS_STEEL,
                ModConventionalItemTags.STORAGE_BLOCKS_STEEL);
        copy(ModConventionalBlockTags.STORAGE_BLOCKS_SILVER,
                ModConventionalItemTags.STORAGE_BLOCKS_SILVER);
        copy(ModConventionalBlockTags.STORAGE_BLOCKS_ALUMINUM,
                ModConventionalItemTags.STORAGE_BLOCKS_ALUMINUM);
        copy(ModConventionalBlockTags.STORAGE_BLOCKS_SALT,
                ModConventionalItemTags.STORAGE_BLOCKS_SALT);
        copy(ModConventionalBlockTags.STORAGE_BLOCKS_RAW_SILVER,
                ModConventionalItemTags.STORAGE_BLOCKS_RAW_SILVER);
        valueLookupBuilder(ModConventionalItemTags.STEEL_TOOL_MATERIALS).addOptionalTag(
                ModConventionalItemTags.STEEL_INGOTS);
        valueLookupBuilder(ModConventionalItemTags.SILVER_TOOL_MATERIALS).addOptionalTag(
                ModConventionalItemTags.SILVER_INGOTS);

        // Merge fabric and mod conventional tags
        valueLookupBuilder(ConventionalItemTags.INGOTS).addOptionalTag(
                        ModConventionalItemTags.STEEL_INGOTS)
                .addOptionalTag(ModConventionalItemTags.SILVER_INGOTS)
                .addOptionalTag(ModConventionalItemTags.ALUMINUM_INGOTS);
        valueLookupBuilder(ConventionalItemTags.NUGGETS).addOptionalTag(
                        ModConventionalItemTags.STEEL_NUGGETS)
                .addOptionalTag(ModConventionalItemTags.SILVER_NUGGETS)
                .addOptionalTag(ModConventionalItemTags.ALUMINUM_NUGGETS);
        valueLookupBuilder(ConventionalItemTags.RAW_MATERIALS).addOptionalTag(
                ModConventionalItemTags.SILVER_RAW_MATERIALS);
        copy(ConventionalBlockTags.ORES, ConventionalItemTags.ORES);
        copy(ConventionalBlockTags.STORAGE_BLOCKS, ConventionalItemTags.STORAGE_BLOCKS);
        copy(ConventionalBlockTags.STORAGE_BLOCKS_RAW_COPPER, ConventionalItemTags.STORAGE_BLOCKS);

        // Add mod tags
        valueLookupBuilder(ModItemTags.ALL_COOKED_MEAT).addOptionalTag(
                        ConventionalItemTags.COOKED_MEAT_FOODS)
                .addOptionalTag(ConventionalItemTags.COOKED_FISH_FOODS);
        valueLookupBuilder(ModItemTags.ALL_RAW_MEAT).addOptionalTag(
                        ConventionalItemTags.RAW_MEAT_FOODS)
                .addOptionalTag(ConventionalItemTags.RAW_FISH_FOODS);
        valueLookupBuilder(ModItemTags.ALL_CAULDRONS).add(Items.CAULDRON,
                ModBlocks.ALUMINUM_CAULDRON.asItem());
        valueLookupBuilder(ModItemTags.ALWAYS_GIVE_THIRST).add(Items.SPIDER_EYE)
                .add(Items.PUFFERFISH);
        valueLookupBuilder(ModItemTags.BREWING_STAND_FUELS).add(Items.GUNPOWDER)
                .add(ModItems.SUNLIGHT_SPORES_BOTTLE);
        valueLookupBuilder(ModItemTags.CAN_BE_SALTED).addOptionalTag(ModItemTags.ALL_COOKED_MEAT)
                .addOptionalTag(ModItemTags.ALL_RAW_MEAT)
                .add(Items.ROTTEN_FLESH)
                .add(Items.CARROT)
                .add(Items.POTATO)
                .add(Items.POISONOUS_POTATO)
                .add(Items.BEETROOT)
                .add(Items.BAKED_POTATO)
                .add(Items.BREAD)
                .add(Items.SUSPICIOUS_STEW)
                .add(Items.BEETROOT_SOUP)
                .add(Items.MUSHROOM_STEW)
                .add(Items.RABBIT_STEW)
                .add(ModItems.VERDANT_VINE);
        valueLookupBuilder(ModItemTags.CHANCE_TO_GIVE_THIRST).add(Items.POISONOUS_POTATO)
                .add(Items.ROTTEN_FLESH);
        // Empty by default, can be modified to change functionality
        valueLookupBuilder(ModItemTags.EXCLUDE_SILVER_LINED);
        valueLookupBuilder(ModItemTags.FLINT_AND_STEEL_VARIANTS).add(Items.FLINT_AND_STEEL)
                .add(ModItems.FLINT_AND_IRON)
                .add(ModItems.QUARTZ_AND_STEEL)
                .add(ModItems.QUARTZ_AND_IRON);
        valueLookupBuilder(ModItemTags.KILLS_SPORE_GROWTHS).add(ModItems.SILVER_KNIFE)
                .addOptionalTag(ItemTags.AXES);
        valueLookupBuilder(ModItemTags.TEMPTS_MIDNIGHT_CREATURES).add(Items.POTION)
                .addOptionalTag(ConventionalItemTags.WATER_BUCKETS);
        valueLookupBuilder(ModItemTags.REPLACES_QUARTZ_IN_REDSTONE).add(Items.QUARTZ,
                ModItems.ROSEITE_CRYSTAL);

        copy(ModBlockTags.HAS_IRON, ModItemTags.HAS_IRON);
        valueLookupBuilder(ModItemTags.HAS_IRON).add(Items.IRON_SHOVEL)
                .add(Items.IRON_PICKAXE)
                .add(Items.IRON_AXE)
                .add(Items.IRON_HOE)
                .add(Items.IRON_SWORD)
                .add(Items.IRON_HELMET)
                .add(Items.IRON_CHESTPLATE)
                .add(Items.IRON_LEGGINGS)
                .add(Items.IRON_BOOTS)
                .add(Items.CHAINMAIL_HELMET)
                .add(Items.CHAINMAIL_CHESTPLATE)
                .add(Items.CHAINMAIL_LEGGINGS)
                .add(Items.CHAINMAIL_BOOTS)
                .add(Items.IRON_HORSE_ARMOR)
                .addOptionalTag(ConventionalItemTags.IRON_RAW_MATERIALS)
                .addOptionalTag(ConventionalItemTags.IRON_INGOTS)
                .add(Items.IRON_NUGGET)
                .add(ModItems.CRUDE_IRON)
                .add(ModItems.FLINT_AND_IRON)
                .add(ModItems.QUARTZ_AND_IRON)
                .addOptionalTag(ConventionalItemTags.BUCKETS)
                .add(Items.COMPASS)
                .add(Items.CROSSBOW)
                .addOptionalTag(ConventionalItemTags.SHEAR_TOOLS)
                .add(Items.SHIELD);
        copy(ModBlockTags.HAS_STEEL, ModItemTags.HAS_STEEL);
        valueLookupBuilder(ModItemTags.HAS_STEEL).add(Items.FLINT_AND_STEEL)
                .add(ModItems.QUARTZ_AND_STEEL)
                .addOptionalTag(ModConventionalItemTags.STEEL_INGOTS)
                .addOptionalTag(ModConventionalItemTags.STEEL_NUGGETS)
                .add(ModItems.STEEL_SWORD)
                .add(ModItems.STEEL_AXE)
                .add(ModItems.STEEL_PICKAXE)
                .add(ModItems.STEEL_SHOVEL)
                .add(ModItems.STEEL_HOE)
                .add(ModItems.STEEL_HELMET)
                .add(ModItems.STEEL_CHESTPLATE)
                .add(ModItems.STEEL_LEGGINGS)
                .add(ModItems.STEEL_BOOTS);
    }
}
