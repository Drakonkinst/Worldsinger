package io.github.drakonkinst.worldsinger.item;

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
import io.github.drakonkinst.worldsinger.material.ModArmorMaterials;
import io.github.drakonkinst.worldsinger.material.ModToolMaterials;
import io.github.drakonkinst.worldsinger.registry.ModFoodComponents;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
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
            createSporeBucketItem(ModBlocks.DEAD_SPORE_BLOCK));
    public static final Item VERDANT_SPORES_BUCKET = register("verdant_spores_bucket",
            createSporeBucketItem(ModBlocks.VERDANT_SPORE_BLOCK));
    public static final Item CRIMSON_SPORES_BUCKET = register("crimson_spores_bucket",
            createSporeBucketItem(ModBlocks.CRIMSON_SPORE_BLOCK));
    public static final Item ZEPHYR_SPORES_BUCKET = register("zephyr_spores_bucket",
            createSporeBucketItem(ModBlocks.ZEPHYR_SPORE_BLOCK));
    public static final Item SUNLIGHT_SPORES_BUCKET = register("sunlight_spores_bucket",
            createSporeBucketItem(ModBlocks.SUNLIGHT_SPORE_BLOCK));
    public static final Item ROSEITE_SPORES_BUCKET = register("roseite_spores_bucket",
            createSporeBucketItem(ModBlocks.ROSEITE_SPORE_BLOCK));
    public static final Item MIDNIGHT_SPORES_BUCKET = register("midnight_spores_bucket",
            createSporeBucketItem(ModBlocks.MIDNIGHT_SPORE_BLOCK));

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
            new Item(new FabricItemSettings().food(new FoodComponent.Builder()
                    // Same as Dried Kelp
                    .hunger(1).saturationModifier(0.3f).snack().build())));
    public static final Item CRIMSON_SPINE = register("crimson_spine",
            new Item(new FabricItemSettings()));
    public static final Item ROSEITE_CRYSTAL = register("roseite_crystal",
            new Item(new FabricItemSettings()));
    public static final Item ROSEITE_BEAD = register("roseite_bead",
            new Item(new FabricItemSettings()));
    public static final Item SALT = register("salt",
            new SaltItem(new FabricItemSettings().food(ModFoodComponents.SALT)));

    // Silver
    public static final Item RAW_SILVER = register("raw_silver",
            new Item(new FabricItemSettings()));
    public static final Item SILVER_INGOT = register("silver_ingot",
            new Item(new FabricItemSettings()));
    public static final Item SILVER_NUGGET = register("silver_nugget",
            new Item(new FabricItemSettings()));

    // Steel
    public static final Item CRUDE_IRON = register("crude_iron",
            new Item(new FabricItemSettings()));
    public static final Item STEEL_INGOT = register("steel_ingot",
            new Item(new FabricItemSettings()));
    public static final Item STEEL_NUGGET = register("steel_nugget",
            new Item(new FabricItemSettings()));
    public static final Item STEEL_HELMET = register("steel_helmet",
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.HELMET,
                    new FabricItemSettings()));
    public static final Item STEEL_CHESTPLATE = register("steel_chestplate",
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.CHESTPLATE,
                    new FabricItemSettings()));
    public static final Item STEEL_LEGGINGS = register("steel_leggings",
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.LEGGINGS,
                    new FabricItemSettings()));
    public static final Item STEEL_BOOTS = register("steel_boots",
            new ArmorItem(ModArmorMaterials.STEEL, ArmorItem.Type.BOOTS, new FabricItemSettings()));
    public static final Item STEEL_SWORD = register("steel_sword",
            new SwordItem(ModToolMaterials.STEEL, 3, -2.4f, new FabricItemSettings()));
    public static final Item STEEL_PICKAXE = register("steel_pickaxe",
            new PickaxeItem(ModToolMaterials.STEEL, 1, -2.8f, new FabricItemSettings()));
    public static final Item STEEL_AXE = register("steel_axe",
            new AxeItem(ModToolMaterials.STEEL, 6.0f, -3.1f, new FabricItemSettings()));
    public static final Item STEEL_SHOVEL = register("steel_shovel",
            new ShovelItem(ModToolMaterials.STEEL, 1.5f, -3.0f, new FabricItemSettings()));
    public static final Item STEEL_HOE = register("steel_hoe",
            new HoeItem(ModToolMaterials.STEEL, -2, -1.0f, new FabricItemSettings()));

    // Aluminum
    public static final Item ALUMINUM_INGOT = register("aluminum_ingot",
            new Item(new FabricItemSettings()));

    // Tools
    public static final Item QUARTZ_AND_STEEL = register("quartz_and_steel",
            new FlintAndSteelItem(new FabricItemSettings().maxDamage(88)));
    public static final Item FLINT_AND_IRON = register("flint_and_iron",
            new FaultyFirestarterItem(0.33f, new FabricItemSettings().maxDamage(64)));
    public static final Item QUARTZ_AND_IRON = register("quartz_and_iron",
            new FaultyFirestarterItem(0.33f, new FabricItemSettings().maxDamage(88)));
    public static final Item SILVER_KNIFE = register("silver_knife",
            new SilverKnifeItem(1.0f, -2.0f, new FabricItemSettings()));

    // Admin
    public static final Item MIDNIGHT_CREATURE_SPAWN_EGG = register("midnight_creature_spawn_egg",
            new SpawnEggItem(ModEntityTypes.MIDNIGHT_CREATURE, 0x000000, 0x111111,
                    new FabricItemSettings()));

    // Item Groups
    private static final ItemGroup WORLDSINGER_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModBlocks.VERDANT_VINE_SNARE))
            .displayName(Text.translatable("itemGroup.worldsinger.worldsinger"))
            .build();

    private static Item createSporeBucketItem(Block sporeBlock) {
        return new AetherSporeBucketItem(sporeBlock, ModSoundEvents.BLOCK_SPORE_BLOCK_PLACE,
                new FabricItemSettings().recipeRemainder(Items.BUCKET).maxCount(1));
    }

    private static Item createSporeBottleItem(AetherSpores sporeType) {
        return new SporeBottleItem(sporeType,
                new FabricItemSettings().recipeRemainder(Items.GLASS_BOTTLE)
                        .maxCount(Items.POTION.getMaxCount()));
    }

    private static Item createSporeSplashBottleItem(AetherSpores sporeType) {
        return new SplashSporeBottleItem(sporeType,
                new FabricItemSettings().maxCount(Items.SPLASH_POTION.getMaxCount()));
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

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register((itemGroup) -> {
            itemGroup.addAfter(Items.IRON_SWORD, ModItems.STEEL_SWORD);
            itemGroup.addAfter(Items.IRON_AXE, ModItems.STEEL_AXE);
            itemGroup.addAfter(Items.IRON_BOOTS, ModItems.STEEL_HELMET, ModItems.STEEL_CHESTPLATE,
                    ModItems.STEEL_LEGGINGS, ModItems.STEEL_BOOTS);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((itemGroup) -> {
            itemGroup.addAfter(Items.IRON_HOE, ModItems.STEEL_SHOVEL, ModItems.STEEL_PICKAXE,
                    ModItems.STEEL_AXE, ModItems.STEEL_HOE);
            itemGroup.addAfter(Items.FLINT_AND_STEEL, ModItems.QUARTZ_AND_STEEL,
                    ModItems.FLINT_AND_IRON, ModItems.QUARTZ_AND_IRON);
            itemGroup.addAfter(Items.MILK_BUCKET, ModItems.DEAD_SPORES_BUCKET,
                    ModItems.VERDANT_SPORES_BUCKET, ModItems.CRIMSON_SPORES_BUCKET,
                    ModItems.ZEPHYR_SPORES_BUCKET, ModItems.SUNLIGHT_SPORES_BUCKET,
                    ModItems.ROSEITE_SPORES_BUCKET, ModItems.MIDNIGHT_SPORES_BUCKET);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register((itemGroup) -> {
            itemGroup.addAfter(Items.DAMAGED_ANVIL, ModBlocks.STEEL_ANVIL,
                    ModBlocks.CHIPPED_STEEL_ANVIL, ModBlocks.DAMAGED_STEEL_ANVIL);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register((itemGroup) -> {
            itemGroup.addAfter(Items.DEEPSLATE, ModBlocks.SALTSTONE);
            itemGroup.addAfter(Items.DEEPSLATE_COAL_ORE, ModBlocks.SALTSTONE_SALT_ORE);
            itemGroup.addAfter(Items.DEEPSLATE_GOLD_ORE, ModBlocks.SILVER_ORE,
                    ModBlocks.DEEPSLATE_SILVER_ORE);
            itemGroup.addAfter(Items.WET_SPONGE, ModBlocks.VERDANT_VINE_BLOCK,
                    ModBlocks.VERDANT_VINE_BRANCH, ModBlocks.VERDANT_VINE_SNARE,
                    ModBlocks.TWISTING_VERDANT_VINES, ModBlocks.DEAD_VERDANT_VINE_BLOCK,
                    ModBlocks.DEAD_VERDANT_VINE_BRANCH, ModBlocks.DEAD_VERDANT_VINE_SNARE,
                    ModBlocks.DEAD_TWISTING_VERDANT_VINES);
            itemGroup.addAfter(Items.RAW_GOLD_BLOCK, ModBlocks.RAW_SILVER_BLOCK);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> {
            itemGroup.addAfter(Items.RAW_GOLD, ModItems.RAW_SILVER);
            itemGroup.addAfter(Items.IRON_NUGGET, ModItems.STEEL_NUGGET);
            itemGroup.addAfter(Items.IRON_INGOT, ModItems.STEEL_INGOT);
            itemGroup.addAfter(Items.GOLD_NUGGET, ModItems.SILVER_NUGGET);
            itemGroup.addAfter(Items.GOLD_INGOT, ModItems.SILVER_INGOT);
            itemGroup.addBefore(Items.NETHERITE_SCRAP, ModItems.CRUDE_IRON);
            itemGroup.addAfter(Items.SUGAR, ModItems.SALT);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register((itemGroup) -> {
            itemGroup.addBefore(Items.GOLD_BLOCK, ModBlocks.STEEL_BLOCK);
            itemGroup.addBefore(Items.REDSTONE_BLOCK, ModBlocks.SILVER_BLOCK);
            itemGroup.addAfter(Items.NETHERITE_BLOCK, ModBlocks.SALT_BLOCK);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register((itemGroup) -> {
            itemGroup.addAfter(Items.DRIED_KELP, ModItems.VERDANT_VINE);
        });

        ItemGroupEvents.modifyEntriesEvent(moddedItemsItemGroupKey).register((itemGroup) -> {
            // Only include the highlight items/blocks here
            itemGroup.add(ModItems.DEAD_SPORES_BUCKET);
            itemGroup.add(ModItems.VERDANT_SPORES_BUCKET);
            itemGroup.add(ModItems.CRIMSON_SPORES_BUCKET);
            itemGroup.add(ModItems.ZEPHYR_SPORES_BUCKET);
            itemGroup.add(ModItems.SUNLIGHT_SPORES_BUCKET);
            itemGroup.add(ModItems.ROSEITE_SPORES_BUCKET);
            itemGroup.add(ModItems.MIDNIGHT_SPORES_BUCKET);

            itemGroup.add(ModBlocks.VERDANT_VINE_BLOCK);
            itemGroup.add(ModBlocks.VERDANT_VINE_BRANCH);
            itemGroup.add(ModBlocks.VERDANT_VINE_SNARE);
            itemGroup.add(ModBlocks.TWISTING_VERDANT_VINES);
            itemGroup.add(ModItems.VERDANT_VINE);
            itemGroup.add(ModItems.CRIMSON_SPINE);
            itemGroup.add(ModItems.SALT);
            itemGroup.add(ModItems.SILVER_INGOT);
            itemGroup.add(ModItems.STEEL_INGOT);
            itemGroup.add(ModItems.ALUMINUM_INGOT);
            itemGroup.add(ModBlocks.SALTSTONE);
            itemGroup.add(ModBlocks.SILVER_BLOCK);
            itemGroup.add(ModBlocks.STEEL_BLOCK);

            itemGroup.add(ModItems.DEAD_SPORES_BOTTLE);
            itemGroup.add(ModItems.DEAD_SPORES_SPLASH_BOTTLE);
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

            itemGroup.add(ModBlocks.CRIMSON_GROWTH);
            itemGroup.add(ModBlocks.CRIMSON_SPIKE);
            itemGroup.add(ModBlocks.CRIMSON_SNARE);
            itemGroup.add(ModBlocks.TALL_CRIMSON_SPINES);
            itemGroup.add(ModBlocks.CRIMSON_SPINES);
            itemGroup.add(ModBlocks.DEAD_CRIMSON_GROWTH);
            itemGroup.add(ModBlocks.DEAD_CRIMSON_SPIKE);
            itemGroup.add(ModBlocks.DEAD_CRIMSON_SNARE);
            itemGroup.add(ModBlocks.DEAD_TALL_CRIMSON_SPINES);
            itemGroup.add(ModBlocks.DEAD_CRIMSON_SPINES);
            itemGroup.add(ModBlocks.MAGMA_VENT);
            itemGroup.add(ModBlocks.ROSEITE_BLOCK);
            itemGroup.add(ModBlocks.ROSEITE_STAIRS);
            itemGroup.add(ModBlocks.ROSEITE_SLAB);
            itemGroup.add(ModBlocks.ROSEITE_CLUSTER);
            itemGroup.add(ModBlocks.LARGE_ROSEITE_BUD);
            itemGroup.add(ModBlocks.MEDIUM_ROSEITE_BUD);
            itemGroup.add(ModBlocks.SMALL_ROSEITE_BUD);
            itemGroup.add(ModItems.ROSEITE_CRYSTAL);
            itemGroup.add(ModItems.ROSEITE_BEAD);

            itemGroup.add(ModItems.MIDNIGHT_CREATURE_SPAWN_EGG);
            itemGroup.add(ModBlocks.MIDNIGHT_ESSENCE);

            itemGroup.add(ModItems.SILVER_KNIFE);
        });
    }

    private ModItems() {}
}
