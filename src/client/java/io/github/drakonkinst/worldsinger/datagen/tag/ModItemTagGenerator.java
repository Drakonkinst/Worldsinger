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
        valueLookupBuilder(ItemTags.AXES).addOptional(ModItems.STEEL_AXE);
        valueLookupBuilder(ItemTags.PICKAXES).addOptional(ModItems.STEEL_PICKAXE);
        valueLookupBuilder(ItemTags.SHOVELS).addOptional(ModItems.STEEL_SHOVEL);
        valueLookupBuilder(ItemTags.HOES).addOptional(ModItems.STEEL_HOE);
        valueLookupBuilder(ItemTags.SWORDS).addOptional(ModItems.STEEL_SWORD);
        valueLookupBuilder(ItemTags.HEAD_ARMOR).addOptional(ModItems.STEEL_HELMET);
        valueLookupBuilder(ItemTags.CHEST_ARMOR).addOptional(ModItems.STEEL_CHESTPLATE);
        valueLookupBuilder(ItemTags.LEG_ARMOR).addOptional(ModItems.STEEL_LEGGINGS);
        valueLookupBuilder(ItemTags.FOOT_ARMOR).addOptional(ModItems.STEEL_BOOTS);
        valueLookupBuilder(ItemTags.WEAPON_ENCHANTABLE).addOptional(ModItems.SILVER_KNIFE);
        valueLookupBuilder(ItemTags.CREEPER_IGNITERS).addOptional(ModItems.FLINT_AND_IRON)
                .addOptional(ModItems.QUARTZ_AND_IRON)
                .addOptional(ModItems.QUARTZ_AND_STEEL);
        valueLookupBuilder(ItemTags.DYEABLE).addOptional(ModItems.POUCH)
                .addOptional(ModItems.POUCH_OF_SPHERES);

        // Add mod conventional tags
        valueLookupBuilder(ModConventionalItemTags.STEEL_INGOTS).addOptional(ModItems.STEEL_INGOT);
        valueLookupBuilder(ModConventionalItemTags.SILVER_INGOTS).addOptional(
                ModItems.SILVER_INGOT);
        valueLookupBuilder(ModConventionalItemTags.ALUMINUM_INGOTS).addOptional(
                ModItems.ALUMINUM_INGOT);
        valueLookupBuilder(ModConventionalItemTags.STEEL_NUGGETS).addOptional(
                ModItems.STEEL_NUGGET);
        valueLookupBuilder(ModConventionalItemTags.SILVER_NUGGETS).addOptional(
                ModItems.SILVER_NUGGET);
        valueLookupBuilder(ModConventionalItemTags.ALUMINUM_NUGGETS).addOptional(
                ModItems.ALUMINUM_NUGGET);
        valueLookupBuilder(ModConventionalItemTags.SILVER_RAW_MATERIALS).addOptional(
                ModItems.RAW_SILVER);
        valueLookupBuilder(ModConventionalItemTags.SALT).addOptional(ModItems.SALT);
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
        valueLookupBuilder(ConventionalItemTags.QUARTZ_GEMS).addOptional(ModItems.ROSEITE_CRYSTAL);

        // Add mod tags
        valueLookupBuilder(ModItemTags.ALL_COOKED_MEAT).addOptionalTag(
                        ConventionalItemTags.COOKED_MEAT_FOODS)
                .addOptionalTag(ConventionalItemTags.COOKED_FISH_FOODS);
        valueLookupBuilder(ModItemTags.ALL_RAW_MEAT).addOptionalTag(
                        ConventionalItemTags.RAW_MEAT_FOODS)
                .addOptionalTag(ConventionalItemTags.RAW_FISH_FOODS);
        valueLookupBuilder(ModItemTags.ALL_CAULDRONS).addOptional(Items.CAULDRON)
                .addOptional(ModBlocks.ALUMINUM_CAULDRON.asItem());
        valueLookupBuilder(ModItemTags.ALWAYS_GIVE_THIRST).addOptional(Items.SPIDER_EYE)
                .addOptional(Items.PUFFERFISH);
        valueLookupBuilder(ModItemTags.BREWING_STAND_FUELS).addOptional(Items.GUNPOWDER)
                .addOptional(ModItems.SUNLIGHT_SPORES_BOTTLE);
        valueLookupBuilder(ModItemTags.CAN_BE_SALTED).addOptionalTag(ModItemTags.ALL_COOKED_MEAT)
                .addOptionalTag(ModItemTags.ALL_RAW_MEAT)
                .addOptional(Items.ROTTEN_FLESH)
                .addOptional(Items.CARROT)
                .addOptional(Items.POTATO)
                .addOptional(Items.POISONOUS_POTATO)
                .addOptional(Items.BEETROOT)
                .addOptional(Items.BAKED_POTATO)
                .addOptional(Items.BREAD)
                .addOptional(Items.SUSPICIOUS_STEW)
                .addOptional(Items.BEETROOT_SOUP)
                .addOptional(Items.MUSHROOM_STEW)
                .addOptional(Items.RABBIT_STEW)
                .addOptional(ModItems.VERDANT_VINE);
        valueLookupBuilder(ModItemTags.CHANCE_TO_GIVE_THIRST).addOptional(Items.POISONOUS_POTATO)
                .addOptional(Items.ROTTEN_FLESH);
        // Empty by default, can be modified to change functionality
        valueLookupBuilder(ModItemTags.EXCLUDE_SILVER_LINED);
        valueLookupBuilder(ModItemTags.FLINT_AND_STEEL_VARIANTS).addOptional(Items.FLINT_AND_STEEL)
                .addOptional(ModItems.FLINT_AND_IRON)
                .addOptional(ModItems.QUARTZ_AND_STEEL)
                .addOptional(ModItems.QUARTZ_AND_IRON);
        valueLookupBuilder(ModItemTags.KILLS_SPORE_GROWTHS).addOptional(ModItems.SILVER_KNIFE)
                .addOptionalTag(ItemTags.AXES);
        valueLookupBuilder(ModItemTags.TEMPTS_MIDNIGHT_CREATURES).addOptional(Items.POTION)
                .addOptionalTag(ConventionalItemTags.WATER_BUCKETS);
        valueLookupBuilder(ModItemTags.CAN_BE_QUIVERED).addOptional(Items.ARROW)
                .addOptional(Items.SPECTRAL_ARROW)
                .addOptional(Items.TIPPED_ARROW);
        valueLookupBuilder(ModItemTags.CAN_USE_IN_FABRIAL).addOptional(Items.AMETHYST_SHARD)
                .addOptional(Items.EMERALD)
                .addOptional(Items.DIAMOND);
        valueLookupBuilder(ModItemTags.SPHERES).addOptional(Items.HEART_OF_THE_SEA);
        valueLookupBuilder(ModItemTags.CAN_FIT_IN_POUCH).addOptionalTag(ModItemTags.SPHERES);

        copy(ModBlockTags.HAS_IRON, ModItemTags.HAS_IRON);
        valueLookupBuilder(ModItemTags.HAS_IRON).addOptional(Items.IRON_SHOVEL)
                .addOptional(Items.IRON_PICKAXE)
                .addOptional(Items.IRON_AXE)
                .addOptional(Items.IRON_HOE)
                .addOptional(Items.IRON_SWORD)
                .addOptional(Items.IRON_HELMET)
                .addOptional(Items.IRON_CHESTPLATE)
                .addOptional(Items.IRON_LEGGINGS)
                .addOptional(Items.IRON_BOOTS)
                .addOptional(Items.CHAINMAIL_HELMET)
                .addOptional(Items.CHAINMAIL_CHESTPLATE)
                .addOptional(Items.CHAINMAIL_LEGGINGS)
                .addOptional(Items.CHAINMAIL_BOOTS)
                .addOptional(Items.IRON_HORSE_ARMOR)
                .addOptionalTag(ConventionalItemTags.IRON_RAW_MATERIALS)
                .addOptionalTag(ConventionalItemTags.IRON_INGOTS)
                .addOptional(Items.IRON_NUGGET)
                .addOptional(ModItems.CRUDE_IRON)
                .addOptional(ModItems.FLINT_AND_IRON)
                .addOptional(ModItems.QUARTZ_AND_IRON)
                .addOptionalTag(ConventionalItemTags.BUCKETS)
                .addOptional(Items.COMPASS)
                .addOptional(Items.CROSSBOW)
                .addOptionalTag(ConventionalItemTags.SHEAR_TOOLS)
                .addOptional(Items.SHIELD);
        copy(ModBlockTags.HAS_STEEL, ModItemTags.HAS_STEEL);
        valueLookupBuilder(ModItemTags.HAS_STEEL).addOptional(Items.FLINT_AND_STEEL)
                .addOptional(ModItems.QUARTZ_AND_STEEL)
                .addOptionalTag(ModConventionalItemTags.STEEL_INGOTS)
                .addOptionalTag(ModConventionalItemTags.STEEL_NUGGETS)
                .addOptional(ModItems.STEEL_SWORD)
                .addOptional(ModItems.STEEL_AXE)
                .addOptional(ModItems.STEEL_PICKAXE)
                .addOptional(ModItems.STEEL_SHOVEL)
                .addOptional(ModItems.STEEL_HOE)
                .addOptional(ModItems.STEEL_HELMET)
                .addOptional(ModItems.STEEL_CHESTPLATE)
                .addOptional(ModItems.STEEL_LEGGINGS)
                .addOptional(ModItems.STEEL_BOOTS);

        copy(ModBlockTags.CONVERTS_TO_COARSE_DIRT_WHEN_SALTED,
                ModItemTags.CONVERTS_TO_COARSE_DIRT_WHEN_SALTED);
        copy(ModBlockTags.CONVERTS_TO_DIRT_WHEN_SALTED, ModItemTags.CONVERTS_TO_DIRT_WHEN_SALTED);
    }
}
