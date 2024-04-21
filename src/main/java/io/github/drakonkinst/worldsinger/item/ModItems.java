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
package io.github.drakonkinst.worldsinger.item;

import static io.github.drakonkinst.worldsinger.registry.ModArmorMaterials.STEEL_DURABILITY_MULTIPLIER;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.CrimsonSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.DeadSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RoseiteSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SunlightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.VerdantSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.ZephyrSpores;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypes;
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import io.github.drakonkinst.worldsinger.registry.ModArmorMaterials;
import io.github.drakonkinst.worldsinger.registry.ModFoodComponents;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import io.github.drakonkinst.worldsinger.registry.ModToolMaterials;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@SuppressWarnings("UnqualifiedStaticUsage")
public final class ModItems {

    // Items

    // Spore Buckets
    public static final Item DEAD_SPORES_BUCKET = register("dead_spores_bucket",
            createSporeBucketItem(ModBlocks.DEAD_SPORE_BLOCK, ModFluids.DEAD_SPORES));
    public static final Item VERDANT_SPORES_BUCKET = register("verdant_spores_bucket",
            createSporeBucketItem(ModBlocks.VERDANT_SPORE_BLOCK, ModFluids.VERDANT_SPORES));
    public static final Item CRIMSON_SPORES_BUCKET = register("crimson_spores_bucket",
            createSporeBucketItem(ModBlocks.CRIMSON_SPORE_BLOCK, ModFluids.CRIMSON_SPORES));
    public static final Item ZEPHYR_SPORES_BUCKET = register("zephyr_spores_bucket",
            createSporeBucketItem(ModBlocks.ZEPHYR_SPORE_BLOCK, ModFluids.ZEPHYR_SPORES));
    public static final Item SUNLIGHT_SPORES_BUCKET = register("sunlight_spores_bucket",
            createSporeBucketItem(ModBlocks.SUNLIGHT_SPORE_BLOCK, ModFluids.SUNLIGHT_SPORES));
    public static final Item ROSEITE_SPORES_BUCKET = register("roseite_spores_bucket",
            createSporeBucketItem(ModBlocks.ROSEITE_SPORE_BLOCK, ModFluids.ROSEITE_SPORES));
    public static final Item MIDNIGHT_SPORES_BUCKET = register("midnight_spores_bucket",
            createSporeBucketItem(ModBlocks.MIDNIGHT_SPORE_BLOCK, ModFluids.MIDNIGHT_SPORES));

    // Spore Bottles
    public static final Item DEAD_SPORES_BOTTLE = register("dead_spores_bottle",
            createSporeBottleItem(DeadSpores.getInstance()));
    public static final Item VERDANT_SPORES_BOTTLE = register("verdant_spores_bottle",
            createSporeBottleItem(VerdantSpores.getInstance()));
    public static final Item CRIMSON_SPORES_BOTTLE = register("crimson_spores_bottle",
            createSporeBottleItem(CrimsonSpores.getInstance()));
    public static final Item ZEPHYR_SPORES_BOTTLE = register("zephyr_spores_bottle",
            createSporeBottleItem(ZephyrSpores.getInstance()));
    public static final Item SUNLIGHT_SPORES_BOTTLE = register("sunlight_spores_bottle",
            createSporeBottleItem(SunlightSpores.getInstance()));
    public static final Item ROSEITE_SPORES_BOTTLE = register("roseite_spores_bottle",
            createSporeBottleItem(RoseiteSpores.getInstance()));
    public static final Item MIDNIGHT_SPORES_BOTTLE = register("midnight_spores_bottle",
            createSporeBottleItem(MidnightSpores.getInstance()));
    public static final Item DEAD_SPORES_SPLASH_BOTTLE = register("dead_spores_splash_bottle",
            createSporeSplashBottleItem(DeadSpores.getInstance()));
    public static final Item VERDANT_SPORES_SPLASH_BOTTLE = register("verdant_spores_splash_bottle",
            createSporeSplashBottleItem(VerdantSpores.getInstance()));
    public static final Item CRIMSON_SPORES_SPLASH_BOTTLE = register("crimson_spores_splash_bottle",
            createSporeSplashBottleItem(CrimsonSpores.getInstance()));
    public static final Item ZEPHYR_SPORES_SPLASH_BOTTLE = register("zephyr_spores_splash_bottle",
            createSporeSplashBottleItem(ZephyrSpores.getInstance()));
    public static final Item SUNLIGHT_SPORES_SPLASH_BOTTLE = register(
            "sunlight_spores_splash_bottle",
            createSporeSplashBottleItem(SunlightSpores.getInstance()));
    public static final Item ROSEITE_SPORES_SPLASH_BOTTLE = register("roseite_spores_splash_bottle",
            createSporeSplashBottleItem(RoseiteSpores.getInstance()));
    public static final Item MIDNIGHT_SPORES_SPLASH_BOTTLE = register(
            "midnight_spores_splash_bottle",
            createSporeSplashBottleItem(MidnightSpores.getInstance()));

    public static final Item VERDANT_VINE = register("verdant_vine",
            new Item(new Item.Settings().food(ModFoodComponents.VERDANT_VINE)));
    public static final Item CRIMSON_SPINE = register("crimson_spine",
            new Item(new Item.Settings()));
    public static final Item ROSEITE_CRYSTAL = register("roseite_crystal",
            new Item(new Item.Settings()));
    public static final Item ROSEITE_BEAD = register("roseite_bead", new Item(new Item.Settings()));
    public static final Item SALT = register("salt",
            new SaltItem(new Item.Settings().food(ModFoodComponents.SALT)));

    // Silver
    public static final Item RAW_SILVER = register("raw_silver", new Item(new Item.Settings()));
    public static final Item SILVER_INGOT = register("silver_ingot", new Item(new Item.Settings()));
    public static final Item SILVER_NUGGET = register("silver_nugget",
            new Item(new Item.Settings()));

    // Steel
    public static final Item CRUDE_IRON = register("crude_iron", new Item(new Item.Settings()));
    public static final Item STEEL_INGOT = register("steel_ingot", new Item(new Item.Settings()));
    public static final Item STEEL_NUGGET = register("steel_nugget", new Item(new Item.Settings()));
    public static final Item STEEL_HELMET = register("steel_helmet",
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.HELMET,
                    new Item.Settings().maxDamage(
                            ArmorItem.Type.HELMET.getMaxDamage(STEEL_DURABILITY_MULTIPLIER))));
    public static final Item STEEL_CHESTPLATE = register("steel_chestplate",
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.CHESTPLATE,
                    new Item.Settings().maxDamage(
                            ArmorItem.Type.CHESTPLATE.getMaxDamage(STEEL_DURABILITY_MULTIPLIER))));
    public static final Item STEEL_LEGGINGS = register("steel_leggings",
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.LEGGINGS,
                    new Item.Settings().maxDamage(
                            ArmorItem.Type.LEGGINGS.getMaxDamage(STEEL_DURABILITY_MULTIPLIER))));
    public static final Item STEEL_BOOTS = register("steel_boots",
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.BOOTS,
                    new Item.Settings().maxDamage(
                            ArmorItem.Type.BOOTS.getMaxDamage(STEEL_DURABILITY_MULTIPLIER))));
    public static final Item STEEL_SWORD = register("steel_sword",
            new SwordItem(ModToolMaterials.STEEL, new Item.Settings().attributeModifiers(
                    SwordItem.createAttributeModifiers(ModToolMaterials.STEEL, 3, -2.4f))));
    public static final Item STEEL_PICKAXE = register("steel_pickaxe",
            new PickaxeItem(ModToolMaterials.STEEL, new Item.Settings().attributeModifiers(
                    PickaxeItem.createAttributeModifiers(ModToolMaterials.STEEL, 1.0f, -2.8f))));
    public static final Item STEEL_AXE = register("steel_axe", new AxeItem(ModToolMaterials.STEEL,
            new Item.Settings().attributeModifiers(
                    AxeItem.createAttributeModifiers(ModToolMaterials.STEEL, 6.0f, -3.1f))));
    public static final Item STEEL_SHOVEL = register("steel_shovel",
            new ShovelItem(ModToolMaterials.STEEL, new Item.Settings().attributeModifiers(
                    ShovelItem.createAttributeModifiers(ModToolMaterials.STEEL, 1.5f, -3.0f))));
    public static final Item STEEL_HOE = register("steel_hoe", new HoeItem(ModToolMaterials.STEEL,
            new Item.Settings().attributeModifiers(
                    HoeItem.createAttributeModifiers(ModToolMaterials.STEEL, -2.0f, -1.0f))));

    // Aluminum
    public static final Item ALUMINUM_INGOT = register("aluminum_ingot",
            new Item(new Item.Settings()));
    public static final Item ALUMINUM_NUGGET = register("aluminum_nugget",
            new Item(new Item.Settings()));

    // Tools
    public static final Item QUARTZ_AND_STEEL = register("quartz_and_steel",
            new FlintAndSteelItem(new Item.Settings().maxDamage(88)));
    public static final Item FLINT_AND_IRON = register("flint_and_iron",
            new FaultyFirestarterItem(0.33f, new Item.Settings().maxDamage(64)));
    public static final Item QUARTZ_AND_IRON = register("quartz_and_iron",
            new FaultyFirestarterItem(0.33f, new Item.Settings().maxDamage(88)));
    public static final Item SILVER_KNIFE = register("silver_knife", new SilverKnifeItem(
            new Item.Settings().attributeModifiers(
                    KnifeItem.createAttributeModifiers(ModToolMaterials.SILVER, 1.0f, -2.0f))));

    // Admin
    public static final Item MIDNIGHT_CREATURE_SPAWN_EGG = register("midnight_creature_spawn_egg",
            new SpawnEggItem(ModEntityTypes.MIDNIGHT_CREATURE, 0x000000, 0x111111,
                    new Item.Settings()));

    // Item Groups
    private static final ItemGroup WORLDSINGER_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModBlocks.VERDANT_VINE_SNARE))
            .displayName(Text.translatable("itemGroup.worldsinger.worldsinger"))
            .build();

    private static Item createSporeBucketItem(Block sporeBlock, FlowableFluid sporeFluid) {
        return new AetherSporeBucketItem(sporeBlock, sporeFluid,
                ModSoundEvents.BLOCK_SPORE_BLOCK_PLACE,
                new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1));
    }

    private static Item createSporeBottleItem(AetherSpores sporeType) {
        return new SporeBottleItem(sporeType,
                new Item.Settings().recipeRemainder(Items.GLASS_BOTTLE).maxCount(16));
    }

    private static Item createSporeSplashBottleItem(AetherSpores sporeType) {
        return new SplashSporeBottleItem(sporeType,
                new Item.Settings().maxCount(Items.SPLASH_POTION.getMaxCount()));
    }

    public static <T extends Item> T register(String id, T item) {
        Identifier itemId = Worldsinger.id(id);
        return Registry.register(Registries.ITEM, itemId, item);
    }

    public static void initialize() {
        // Custom item group
        Identifier moddedItemsIdentifier = Worldsinger.id("worldsinger");
        Registry.register(Registries.ITEM_GROUP, moddedItemsIdentifier, WORLDSINGER_ITEM_GROUP);
        RegistryKey<ItemGroup> moddedItemsItemGroupKey = RegistryKey.of(RegistryKeys.ITEM_GROUP,
                moddedItemsIdentifier);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register((itemGroup) -> {
            itemGroup.addBefore(Items.GOLD_BLOCK, ModBlocks.STEEL_BLOCK);
            itemGroup.addBefore(Items.REDSTONE_BLOCK, ModBlocks.SILVER_BLOCK,
                    ModBlocks.ALUMINUM_BLOCK, ModBlocks.ALUMINUM_SHEET);
            itemGroup.addAfter(Items.NETHERITE_BLOCK, ModBlocks.SALT_BLOCK);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register((itemGroup) -> {
            itemGroup.addAfter(Items.DEEPSLATE, ModBlocks.SALTSTONE);
            itemGroup.addAfter(Items.DEEPSLATE_COAL_ORE, ModBlocks.SALTSTONE_SALT_ORE);
            itemGroup.addAfter(Items.DEEPSLATE_GOLD_ORE, ModBlocks.SILVER_ORE,
                    ModBlocks.DEEPSLATE_SILVER_ORE);
            // Spore Growth Blocks
            itemGroup.addAfter(Items.WET_SPONGE, new ItemConvertible[] {
                    ModBlocks.VERDANT_VINE_BLOCK,
                    ModBlocks.VERDANT_VINE_BRANCH,
                    ModBlocks.VERDANT_VINE_SNARE,
                    ModBlocks.TWISTING_VERDANT_VINES,
                    ModBlocks.DEAD_VERDANT_VINE_BLOCK,
                    ModBlocks.DEAD_VERDANT_VINE_BRANCH,
                    ModBlocks.DEAD_VERDANT_VINE_SNARE,
                    ModBlocks.DEAD_TWISTING_VERDANT_VINES,
                    ModBlocks.CRIMSON_GROWTH,
                    ModBlocks.CRIMSON_SPIKE,
                    ModBlocks.CRIMSON_SNARE,
                    ModBlocks.TALL_CRIMSON_SPINES,
                    ModBlocks.CRIMSON_SPINES,
                    ModBlocks.DEAD_CRIMSON_GROWTH,
                    ModBlocks.DEAD_CRIMSON_SPIKE,
                    ModBlocks.DEAD_CRIMSON_SNARE,
                    ModBlocks.DEAD_TALL_CRIMSON_SPINES,
                    ModBlocks.DEAD_CRIMSON_SPINES,
                    ModBlocks.ROSEITE_BLOCK,
                    ModBlocks.ROSEITE_STAIRS,
                    ModBlocks.ROSEITE_SLAB,
                    ModBlocks.SMALL_ROSEITE_BUD,
                    ModBlocks.MEDIUM_ROSEITE_BUD,
                    ModBlocks.LARGE_ROSEITE_BUD,
                    ModBlocks.ROSEITE_CLUSTER,
                    ModBlocks.MIDNIGHT_ESSENCE
            });
            itemGroup.addAfter(Items.RAW_GOLD_BLOCK, ModBlocks.RAW_SILVER_BLOCK);
            itemGroup.addAfter(Blocks.MAGMA_BLOCK, ModBlocks.MAGMA_VENT);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((itemGroup) -> {
            itemGroup.addAfter(Items.DAMAGED_ANVIL, ModBlocks.STEEL_ANVIL,
                    ModBlocks.CHIPPED_STEEL_ANVIL, ModBlocks.DAMAGED_STEEL_ANVIL);
            itemGroup.addAfter(Blocks.MAGMA_BLOCK, ModBlocks.MAGMA_VENT);
            itemGroup.addAfter(Items.END_CRYSTAL, ModBlocks.MIDNIGHT_ESSENCE);
            // Only added here, not in Redstone tab, because it is not a distinct Redstone component
            itemGroup.addAfter(Blocks.CAULDRON, ModBlocks.ALUMINUM_CAULDRON);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((itemGroup) -> {
            // Steel Tools
            itemGroup.addAfter(Items.IRON_HOE, new ItemConvertible[] {
                    ModItems.STEEL_SHOVEL,
                    ModItems.STEEL_PICKAXE,
                    ModItems.STEEL_AXE,
                    ModItems.STEEL_HOE
            });
            itemGroup.addAfter(Items.FLINT_AND_STEEL, ModItems.QUARTZ_AND_STEEL,
                    ModItems.FLINT_AND_IRON, ModItems.QUARTZ_AND_IRON);
            // Spore Buckets
            itemGroup.addAfter(Items.MILK_BUCKET, new ItemConvertible[] {
                    ModItems.VERDANT_SPORES_BUCKET,
                    ModItems.CRIMSON_SPORES_BUCKET,
                    ModItems.ZEPHYR_SPORES_BUCKET,
                    ModItems.SUNLIGHT_SPORES_BUCKET,
                    ModItems.ROSEITE_SPORES_BUCKET,
                    ModItems.MIDNIGHT_SPORES_BUCKET,
                    ModItems.DEAD_SPORES_BUCKET
            });
            itemGroup.addAfter(Items.FISHING_ROD, ModItems.SILVER_KNIFE);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register((itemGroup) -> {
            itemGroup.addAfter(Items.IRON_SWORD, ModItems.STEEL_SWORD);
            itemGroup.addAfter(Items.IRON_AXE, ModItems.STEEL_AXE);
            itemGroup.addAfter(Items.IRON_BOOTS, new ItemConvertible[] {
                    ModItems.STEEL_HELMET,
                    ModItems.STEEL_CHESTPLATE,
                    ModItems.STEEL_LEGGINGS,
                    ModItems.STEEL_BOOTS
            });
            itemGroup.addAfter(Items.TRIDENT, ModItems.SILVER_KNIFE);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register((itemGroup) -> {
            itemGroup.addAfter(Items.DRIED_KELP, ModItems.VERDANT_VINE);
            // Spore Bottles
            itemGroup.addBefore(Items.MILK_BUCKET, new ItemConvertible[] {
                    ModItems.VERDANT_SPORES_BOTTLE,
                    ModItems.VERDANT_SPORES_SPLASH_BOTTLE,
                    ModItems.CRIMSON_SPORES_BOTTLE,
                    ModItems.CRIMSON_SPORES_SPLASH_BOTTLE,
                    ModItems.ZEPHYR_SPORES_BOTTLE,
                    ModItems.ZEPHYR_SPORES_SPLASH_BOTTLE,
                    ModItems.SUNLIGHT_SPORES_BOTTLE,
                    ModItems.SUNLIGHT_SPORES_SPLASH_BOTTLE,
                    ModItems.ROSEITE_SPORES_BOTTLE,
                    ModItems.ROSEITE_SPORES_SPLASH_BOTTLE,
                    ModItems.MIDNIGHT_SPORES_BOTTLE,
                    ModItems.MIDNIGHT_SPORES_SPLASH_BOTTLE,
                    ModItems.DEAD_SPORES_BOTTLE,
                    ModItems.DEAD_SPORES_SPLASH_BOTTLE
            });
            itemGroup.addAfter(Items.SPIDER_EYE, ModItems.SALT);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> {
            itemGroup.addAfter(Items.RAW_GOLD, ModItems.RAW_SILVER);
            itemGroup.addAfter(Items.IRON_NUGGET, ModItems.STEEL_NUGGET);
            itemGroup.addAfter(Items.IRON_INGOT, ModItems.CRUDE_IRON, ModItems.STEEL_INGOT);
            itemGroup.addAfter(Items.GOLD_NUGGET, ModItems.SILVER_NUGGET, ModItems.ALUMINUM_NUGGET);
            itemGroup.addAfter(Items.GOLD_INGOT, ModItems.SILVER_INGOT, ModItems.ALUMINUM_INGOT);
            itemGroup.addAfter(Items.SUGAR, ModItems.SALT);
            itemGroup.addBefore(Items.WHITE_DYE, ModItems.VERDANT_VINE, ModItems.CRIMSON_SPINE,
                    ModItems.ROSEITE_CRYSTAL, ModItems.ROSEITE_BEAD);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register((itemGroup) -> {
            // Alphabetized
            itemGroup.addAfter(Items.MAGMA_CUBE_SPAWN_EGG, ModItems.MIDNIGHT_CREATURE_SPAWN_EGG);
        });

        ItemGroupEvents.modifyEntriesEvent(moddedItemsItemGroupKey).register((itemGroup) -> {
            // For now, this item group should only contain important items

            // Spore Buckets
            itemGroup.add(ModItems.VERDANT_SPORES_BUCKET);
            itemGroup.add(ModItems.CRIMSON_SPORES_BUCKET);
            itemGroup.add(ModItems.ZEPHYR_SPORES_BUCKET);
            itemGroup.add(ModItems.SUNLIGHT_SPORES_BUCKET);
            itemGroup.add(ModItems.ROSEITE_SPORES_BUCKET);
            itemGroup.add(ModItems.MIDNIGHT_SPORES_BUCKET);
            itemGroup.add(ModItems.DEAD_SPORES_BUCKET);

            // Spore Growth Blocks
            itemGroup.add(ModBlocks.VERDANT_VINE_BLOCK);
            itemGroup.add(ModBlocks.VERDANT_VINE_BRANCH);
            itemGroup.add(ModBlocks.VERDANT_VINE_SNARE);
            itemGroup.add(ModBlocks.TWISTING_VERDANT_VINES);
            itemGroup.add(ModBlocks.CRIMSON_GROWTH);
            itemGroup.add(ModBlocks.CRIMSON_SPIKE);
            itemGroup.add(ModBlocks.CRIMSON_SNARE);
            itemGroup.add(ModBlocks.TALL_CRIMSON_SPINES);
            itemGroup.add(ModBlocks.CRIMSON_SPINES);
            itemGroup.add(ModBlocks.ROSEITE_BLOCK);
            itemGroup.add(ModBlocks.ROSEITE_STAIRS);
            itemGroup.add(ModBlocks.ROSEITE_SLAB);
            itemGroup.add(ModBlocks.ROSEITE_CLUSTER);
            itemGroup.add(ModBlocks.LARGE_ROSEITE_BUD);
            itemGroup.add(ModBlocks.MEDIUM_ROSEITE_BUD);
            itemGroup.add(ModBlocks.SMALL_ROSEITE_BUD);
            itemGroup.add(ModBlocks.MIDNIGHT_ESSENCE);

            // Metal Blocks
            itemGroup.add(ModBlocks.SILVER_BLOCK);
            itemGroup.add(ModBlocks.STEEL_BLOCK);
            itemGroup.add(ModBlocks.ALUMINUM_BLOCK);
            itemGroup.add(ModBlocks.ALUMINUM_SHEET);

            // Blocks
            itemGroup.add(ModBlocks.SALTSTONE);
            itemGroup.add(ModBlocks.MAGMA_VENT);

            // Tools
            itemGroup.add(ModItems.SILVER_KNIFE);

            // Ingredients
            itemGroup.add(ModItems.VERDANT_VINE);
            itemGroup.add(ModItems.CRIMSON_SPINE);
            itemGroup.add(ModItems.SALT);
            itemGroup.add(ModItems.SILVER_INGOT);
            itemGroup.add(ModItems.STEEL_INGOT);
            itemGroup.add(ModItems.ALUMINUM_INGOT);
            itemGroup.add(ModItems.ROSEITE_CRYSTAL);
            itemGroup.add(ModItems.ROSEITE_BEAD);

            // Spore Bottles
            itemGroup.add(ModItems.VERDANT_SPORES_BOTTLE);
            itemGroup.add(ModItems.VERDANT_SPORES_SPLASH_BOTTLE);
            itemGroup.add(ModItems.CRIMSON_SPORES_BOTTLE);
            itemGroup.add(ModItems.CRIMSON_SPORES_SPLASH_BOTTLE);
            itemGroup.add(ModItems.ZEPHYR_SPORES_BOTTLE);
            itemGroup.add(ModItems.ZEPHYR_SPORES_SPLASH_BOTTLE);
            itemGroup.add(ModItems.SUNLIGHT_SPORES_BOTTLE);
            itemGroup.add(ModItems.SUNLIGHT_SPORES_SPLASH_BOTTLE);
            itemGroup.add(ModItems.ROSEITE_SPORES_BOTTLE);
            itemGroup.add(ModItems.ROSEITE_SPORES_SPLASH_BOTTLE);
            itemGroup.add(ModItems.MIDNIGHT_SPORES_BOTTLE);
            itemGroup.add(ModItems.MIDNIGHT_SPORES_SPLASH_BOTTLE);
            itemGroup.add(ModItems.DEAD_SPORES_BOTTLE);
            itemGroup.add(ModItems.DEAD_SPORES_SPLASH_BOTTLE);

            // Spawn Eggs
            itemGroup.add(ModItems.MIDNIGHT_CREATURE_SPAWN_EGG);
        });
    }

    private ModItems() {}
}
