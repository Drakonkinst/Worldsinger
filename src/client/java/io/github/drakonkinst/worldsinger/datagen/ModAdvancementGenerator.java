package io.github.drakonkinst.worldsinger.datagen;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.advancement.BondEntityCriterion;
import io.github.drakonkinst.worldsinger.advancement.FindIconOnMapCriterion;
import io.github.drakonkinst.worldsinger.advancement.SailedInSporeSeaCriterion;
import io.github.drakonkinst.worldsinger.advancement.SailedNearLunagreeCriterion;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.CrimsonSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarLunagreeGenerator;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RoseiteSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SunlightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.VerdantSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.ZephyrSpores;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypes;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballCore;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballShell;
import io.github.drakonkinst.worldsinger.loot.condition.SporeSeaLocationCheckLootCondition;
import io.github.drakonkinst.worldsinger.predicate.component.CannonballPredicate;
import io.github.drakonkinst.worldsinger.predicate.component.ModComponentPredicateTypes;
import io.github.drakonkinst.worldsinger.predicate.component.SilverLinedPredicate;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import io.github.drakonkinst.worldsinger.registry.ModLootTables;
import io.github.drakonkinst.worldsinger.registry.ModMapDecorationTypes;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.worldgen.biome.ModBiomeKeys;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensions;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRequirements.CriterionMerger;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.ItemCriterion;
import net.minecraft.advancement.criterion.PlayerGeneratesContainerLootCriterion;
import net.minecraft.advancement.criterion.PlayerInteractedWithEntityCriterion;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.advancement.criterion.TickCriterion.Conditions;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.loot.condition.LocationCheckLootCondition;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.component.ComponentMapPredicate;
import net.minecraft.predicate.component.ComponentsPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityPredicate.Builder;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.text.Text;
import net.minecraft.world.biome.Biome;

public class ModAdvancementGenerator extends FabricAdvancementProvider {

    protected ModAdvancementGenerator(FabricDataOutput output,
            CompletableFuture<WrapperLookup> registryLookup) {
        super(output, registryLookup);
    }

    @SuppressWarnings("unused")
    @Override
    public void generateAdvancement(WrapperLookup wrapperLookup,
            Consumer<AdvancementEntry> consumer) {
        // Icons
        ItemStack saltedChicken = Items.COOKED_CHICKEN.getDefaultStack();
        saltedChicken.set(ModDataComponentTypes.SALTED, true);
        ItemStack silverLinedBoat = SilverLined.repairSilverDurability(
                Items.OAK_BOAT.getDefaultStack(), 4);
        ItemStack sporeCannonball = ModItems.CERAMIC_CANNONBALL.getDefaultStack();
        sporeCannonball.set(ModDataComponentTypes.CANNONBALL,
                new CannonballComponent(CannonballShell.CERAMIC, CannonballCore.ROSEITE, 3,
                        Collections.emptyList()));
        // Registries
        RegistryWrapper<EntityType<?>> entityTypeLookup = wrapperLookup.getOrThrow(
                RegistryKeys.ENTITY_TYPE);
        RegistryWrapper<Item> itemLookup = wrapperLookup.getOrThrow(RegistryKeys.ITEM);
        RegistryWrapper<Block> blockLookup = wrapperLookup.getOrThrow(RegistryKeys.BLOCK);
        RegistryWrapper<Biome> biomeLookup = wrapperLookup.getOrThrow(RegistryKeys.BIOME);
        RegistryWrapper<MapDecorationType> mapDecorationLookup = wrapperLookup.getOrThrow(
                RegistryKeys.MAP_DECORATION_TYPE);

        // Cosmere Advancements
        AdvancementEntry cosmere = Advancement.Builder.createUntelemetered()
                .display(ModBlocks.VERDANT_VINE_SNARE,
                        Text.translatable("advancements.worldsinger.cosmere.root.title"),
                        Text.translatable("advancements.worldsinger.cosmere.root.description"),
                        Worldsinger.id("block/saltstone"), AdvancementFrame.TASK, false, false,
                        false)
                // Gain the advancement by entering any cosmere world
                .criterion("entered_lumar", TickCriterion.Conditions.createLocation(
                        LocationPredicate.Builder.createDimension(ModDimensions.WORLD_LUMAR)))
                .build(consumer, Worldsinger.idStr("cosmere/root"));
        AdvancementEntry obtainFullSteel = Advancement.Builder.createUntelemetered()
                .parent(cosmere)
                .display(ModItems.STEEL_CHESTPLATE, Text.translatable(
                                "advancements.worldsinger.cosmere.obtain_full_steel.title"),
                        Text.translatable(
                                "advancements.worldsinger.cosmere.obtain_full_steel.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criteriaMerger(AdvancementRequirements.CriterionMerger.AND)
                .criterion("steel_helmet",
                        InventoryChangedCriterion.Conditions.items(ModItems.STEEL_HELMET))
                .criterion("steel_chestplate",
                        InventoryChangedCriterion.Conditions.items(ModItems.STEEL_CHESTPLATE))
                .criterion("steel_leggings",
                        InventoryChangedCriterion.Conditions.items(ModItems.STEEL_LEGGINGS))
                .criterion("steel_boots",
                        InventoryChangedCriterion.Conditions.items(ModItems.STEEL_BOOTS))
                .build(consumer, Worldsinger.idStr("cosmere/obtain_full_steel"));

        // Lumar Advancements
        AdvancementEntry enterLumar = Advancement.Builder.createUntelemetered()
                .parent(cosmere)
                .display(ModItems.VERDANT_VINE,
                        Text.translatable("advancements.worldsinger.lumar.enter_lumar.title"),
                        Text.translatable("advancements.worldsinger.lumar.enter_lumar.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criterion("entered_lumar", TickCriterion.Conditions.createLocation(
                        LocationPredicate.Builder.createDimension(ModDimensions.WORLD_LUMAR)))
                .build(consumer, Worldsinger.idStr("lumar/enter_lumar"));
        // Turns out, detecting whether you are responsible for killing spores is REALLY HARD
        // Instead, we'll just go off placing certain blocks
        AdvancementEntry killSpores = Advancement.Builder.createUntelemetered()
                .parent(enterLumar)
                .display(ModItems.SALT,
                        Text.translatable("advancements.worldsinger.lumar.kill_spores.title"),
                        Text.translatable("advancements.worldsinger.lumar.kill_spores.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criterion("place_spore_killing_block_on_lumar",
                        ItemCriterion.Conditions.createPlacedBlock(
                                LocationCheckLootCondition.builder(
                                        LocationPredicate.Builder.create()
                                                .dimension(ModDimensions.WORLD_LUMAR)
                                                .block(BlockPredicate.Builder.create()
                                                        .tag(blockLookup,
                                                                ModBlockTags.KILLS_SPORES)))))
                .build(consumer, Worldsinger.idStr("lumar/kill_spores"));
        AdvancementEntry useSilverLinedBoat = Advancement.Builder.createUntelemetered()
                .parent(killSpores)
                .display(silverLinedBoat, Text.translatable(
                                "advancements.worldsinger.lumar.use_silver_lined_boat.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.use_silver_lined_boat.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criterion("interact_with_silver_lined_boat",
                        PlayerInteractedWithEntityCriterion.Conditions.create(
                                ItemPredicate.Builder.create(), Optional.of(
                                        EntityPredicate.contextPredicateFromEntityPredicate(
                                                EntityPredicate.Builder.create()
                                                        .type(entityTypeLookup,
                                                                ConventionalEntityTypeTags.BOATS)
                                                        .components(
                                                                ComponentsPredicate.Builder.create()
                                                                        .partial(
                                                                                ModComponentPredicateTypes.SILVER_LINED,
                                                                                new SilverLinedPredicate(
                                                                                        NumberRange.IntRange.atLeast(
                                                                                                1)))
                                                                        .build())))))
                .build(consumer, Worldsinger.idStr("lumar/use_silver_lined_boat"));
        AdvancementEntry obtainSaltstone = Advancement.Builder.createUntelemetered()
                .parent(killSpores)
                .display(ModBlocks.SALTSTONE,
                        Text.translatable("advancements.worldsinger.lumar.obtain_saltstone.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.obtain_saltstone.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criterion("saltstone",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.SALTSTONE))
                .build(consumer, Worldsinger.idStr("lumar/obtain_saltstone"));
        // Can move this around later
        AdvancementEntry obtainSaltedFood = Advancement.Builder.createUntelemetered()
                .parent(obtainSaltstone)
                .display(saltedChicken, Text.translatable(
                                "advancements.worldsinger.cosmere.obtain_salted_food.title"),
                        Text.translatable(
                                "advancements.worldsinger.cosmere.obtain_salted_food.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criterion("salted_food", InventoryChangedCriterion.Conditions.items(
                        ItemPredicate.Builder.create()
                                .components(ComponentsPredicate.Builder.create()
                                        .exact(ComponentMapPredicate.of(
                                                ModDataComponentTypes.SALTED, true))
                                        .build())))
                .build(consumer, Worldsinger.idStr("cosmere/obtain_salted_food"));
        AdvancementEntry findLunagree = Advancement.Builder.createUntelemetered()
                .parent(useSilverLinedBoat)
                .display(ModItems.ROSEITE_SPORES_BUCKET,
                        Text.translatable("advancements.worldsinger.lumar.find_lunagree.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.find_lunagree.description"), null,
                        AdvancementFrame.TASK, true, true, false)
                .criterion("sail_near_lunagree", SailedNearLunagreeCriterion.Conditions.create(
                        LumarLunagreeGenerator.SPORE_FALL_RADIUS + 75.0))
                .build(consumer, Worldsinger.idStr("lumar/find_lunagree"));
        AdvancementEntry lootShipwreck = Advancement.Builder.createUntelemetered()
                .parent(enterLumar)
                .display(Items.OAK_PLANKS,
                        Text.translatable("advancements.worldsinger.lumar.loot_shipwreck.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.loot_shipwreck.description"), null,
                        AdvancementFrame.TASK, true, true, false)
                .criteriaMerger(CriterionMerger.OR)
                .criterion("loot_sprouter_chest",
                        PlayerGeneratesContainerLootCriterion.Conditions.create(
                                ModLootTables.LUMAR_SHIPWRECK_SPROUTER_CHEST))
                .criterion("loot_captain_chest",
                        PlayerGeneratesContainerLootCriterion.Conditions.create(
                                ModLootTables.LUMAR_SHIPWRECK_CAPTAIN_CHEST))
                .criterion("loot_supply_chest",
                        PlayerGeneratesContainerLootCriterion.Conditions.create(
                                ModLootTables.LUMAR_SHIPWRECK_SUPPLY_CHEST))
                .build(consumer, Worldsinger.idStr("lumar/loot_shipwreck"));
        AdvancementEntry obtainAllSporeBuckets = Advancement.Builder.createUntelemetered()
                .parent(useSilverLinedBoat)
                .display(ModItems.ZEPHYR_SPORES_BUCKET, Text.translatable(
                                "advancements.worldsinger.lumar.obtain_all_spore_buckets.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.obtain_all_spore_buckets.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criteriaMerger(CriterionMerger.AND)
                .criterion("verdant_spores_bucket",
                        InventoryChangedCriterion.Conditions.items(ModItems.VERDANT_SPORES_BUCKET))
                .criterion("crimson_spores_bucket",
                        InventoryChangedCriterion.Conditions.items(ModItems.CRIMSON_SPORES_BUCKET))
                .criterion("midnight_spores_bucket",
                        InventoryChangedCriterion.Conditions.items(ModItems.MIDNIGHT_SPORES_BUCKET))
                .criterion("zephyr_spores_bucket",
                        InventoryChangedCriterion.Conditions.items(ModItems.ZEPHYR_SPORES_BUCKET))
                .criterion("roseite_spores_bucket",
                        InventoryChangedCriterion.Conditions.items(ModItems.ROSEITE_SPORES_BUCKET))
                .criterion("sunlight_spores_bucket",
                        InventoryChangedCriterion.Conditions.items(ModItems.SUNLIGHT_SPORES_BUCKET))
                .build(consumer, Worldsinger.idStr("lumar/obtain_all_spore_buckets"));
        AdvancementEntry obtainAllSporeGrowths = Advancement.Builder.createUntelemetered()
                .parent(obtainAllSporeBuckets)
                .display(ModBlocks.LARGE_ROSEITE_BUD, Text.translatable(
                                "advancements.worldsinger.lumar.obtain_all_spore_growths.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.obtain_all_spore_growths.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criteriaMerger(CriterionMerger.AND)
                .criterion("verdant_vine_block",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.VERDANT_VINE_BLOCK,
                                ModBlocks.DEAD_VERDANT_VINE_BLOCK))
                .criterion("verdant_vine_branch",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.VERDANT_VINE_BRANCH,
                                ModBlocks.DEAD_VERDANT_VINE_BRANCH))
                .criterion("verdant_vine_snare",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.VERDANT_VINE_SNARE,
                                ModBlocks.DEAD_VERDANT_VINE_SNARE))
                .criterion("twisting_verdant_vines",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.TWISTING_VERDANT_VINES,
                                ModBlocks.DEAD_TWISTING_VERDANT_VINES))
                .criterion("crimson_growth",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.CRIMSON_GROWTH,
                                ModBlocks.DEAD_CRIMSON_GROWTH))
                .criterion("crimson_snare",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.CRIMSON_SNARE,
                                ModBlocks.DEAD_CRIMSON_SNARE))
                .criterion("tall_crimson_spines",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.TALL_CRIMSON_SPINES,
                                ModBlocks.DEAD_TALL_CRIMSON_SPINES))
                .criterion("crimson_spines",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.CRIMSON_SPINES,
                                ModBlocks.DEAD_CRIMSON_SPINES))
                .criterion("crimson_spike",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.CRIMSON_SPIKE,
                                ModBlocks.DEAD_CRIMSON_SPIKE))
                .criterion("roseite_block",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.ROSEITE_BLOCK))
                .criterion("roseite_stairs",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.ROSEITE_STAIRS))
                .criterion("roseite_slab",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.ROSEITE_SLAB))
                .criterion("roseite_cluster",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.ROSEITE_CLUSTER))
                .criterion("large_roseite_bud",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.LARGE_ROSEITE_BUD))
                .criterion("medium_roseite_bud",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.MEDIUM_ROSEITE_BUD))
                .criterion("small_roseite_bud",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.SMALL_ROSEITE_BUD))
                .build(consumer, Worldsinger.idStr("lumar/obtain_all_spore_growths"));
        AdvancementEntry obtainSporeCannonball = Advancement.Builder.createUntelemetered()
                .parent(obtainAllSporeBuckets)
                .display(sporeCannonball, Text.translatable(
                                "advancements.worldsinger.lumar.obtain_spore_cannonball.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.obtain_spore_cannonball.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criterion("spore_cannonball", InventoryChangedCriterion.Conditions.items(
                        ItemPredicate.Builder.create()
                                .items(itemLookup, ModItems.CERAMIC_CANNONBALL)
                                .components(ComponentsPredicate.Builder.create()
                                        .partial(ModComponentPredicateTypes.CANNONBALL,
                                                new CannonballPredicate(
                                                        Optional.of(CannonballCore.ROSEITE),
                                                        NumberRange.IntRange.atLeast(1),
                                                        NumberRange.IntRange.ANY))
                                        .build())))
                .build(consumer, Worldsinger.idStr("lumar/obtain_spore_cannonball"));
        AdvancementEntry brewSporeSplashBottle = Advancement.Builder.createUntelemetered()
                .parent(obtainAllSporeBuckets)
                .display(ModItems.SUNLIGHT_SPORES_SPLASH_BOTTLE, Text.translatable(
                                "advancements.worldsinger.lumar.brew_spore_splash_bottle.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.brew_spore_splash_bottle.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criterion("obtain_spore_splash_bottle", InventoryChangedCriterion.Conditions.items(
                        ModItems.CRIMSON_SPORES_SPLASH_BOTTLE,
                        ModItems.VERDANT_SPORES_SPLASH_BOTTLE,
                        ModItems.ROSEITE_SPORES_SPLASH_BOTTLE,
                        ModItems.MIDNIGHT_SPORES_SPLASH_BOTTLE,
                        ModItems.ZEPHYR_SPORES_SPLASH_BOTTLE,
                        ModItems.SUNLIGHT_SPORES_SPLASH_BOTTLE))
                .build(consumer, Worldsinger.idStr("lumar/brew_spore_splash_bottle"));
        AdvancementEntry obtainMagmaVent = Advancement.Builder.createUntelemetered()
                .parent(obtainAllSporeBuckets)
                .display(ModBlocks.MAGMA_VENT,
                        Text.translatable("advancements.worldsinger.lumar.obtain_magma_vent.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.obtain_magma_vent.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criterion("magma_vent",
                        InventoryChangedCriterion.Conditions.items(ModBlocks.MAGMA_VENT))
                .build(consumer, Worldsinger.idStr("lumar/obtain_magma_vent"));
        AdvancementEntry enterCrimsonSea = Advancement.Builder.createUntelemetered()
                .parent(useSilverLinedBoat)
                .display(ModBlocks.CRIMSON_SPORE_BLOCK,
                        Text.translatable("advancements.worldsinger.lumar.enter_crimson_sea.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.enter_crimson_sea.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criterion("entered_crimson_sea",
                        createEnterSporeSeaCriterion(CrimsonSpores.getInstance()))
                .build(consumer, Worldsinger.idStr("lumar/enter_crimson_sea"));
        AdvancementEntry enterMidnightSea = Advancement.Builder.createUntelemetered()
                .parent(enterCrimsonSea)
                .display(ModBlocks.MIDNIGHT_SPORE_BLOCK, Text.translatable(
                                "advancements.worldsinger.lumar.enter_midnight_sea.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.enter_midnight_sea.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criterion("entered_midnight_sea",
                        createEnterSporeSeaCriterion(MidnightSpores.getInstance()))
                .build(consumer, Worldsinger.idStr("lumar/enter_midnight_sea"));
        AdvancementEntry tameMidnightCreature = Advancement.Builder.createUntelemetered()
                .parent(enterMidnightSea)
                .display(ModBlocks.MIDNIGHT_ESSENCE, Text.translatable(
                                "advancements.worldsinger.lumar.tame_midnight_creature.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.tame_midnight_creature.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criterion("bonded_midnight_creature", BondEntityCriterion.Conditions.create(
                        new Builder().type(entityTypeLookup, ModEntityTypes.MIDNIGHT_CREATURE)))
                .build(consumer, Worldsinger.idStr("lumar/tame_midnight_creature"));
        AdvancementEntry findRainline = Advancement.Builder.createUntelemetered()
                .parent(lootShipwreck)
                .display(Items.FILLED_MAP,
                        Text.translatable("advancements.worldsinger.lumar.find_rainline.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.find_rainline.description"), null,
                        AdvancementFrame.TASK, true, true, false)
                .criterion("found_rainline_on_map",
                        FindIconOnMapCriterion.Conditions.create(ModMapDecorationTypes.RAINLINE))
                .build(consumer, Worldsinger.idStr("lumar/find_rainline"));
        AdvancementEntry exploreLumar = Advancement.Builder.createUntelemetered()
                .parent(findRainline)
                .display(silverLinedBoat,
                        Text.translatable("advancements.worldsinger.lumar.explore_lumar.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.explore_lumar.description"), null,
                        AdvancementFrame.TASK, true, true, false)
                .criteriaMerger(CriterionMerger.AND)
                .criterion("entered_verdant_sea",
                        createEnterSporeSeaCriterion(VerdantSpores.getInstance()))
                .criterion("entered_zephyr_sea",
                        createEnterSporeSeaCriterion(ZephyrSpores.getInstance()))
                .criterion("entered_sunlight_sea",
                        createEnterSporeSeaCriterion(SunlightSpores.getInstance()))
                .criterion("entered_crimson_sea",
                        createEnterSporeSeaCriterion(CrimsonSpores.getInstance()))
                .criterion("entered_roseite_sea",
                        createEnterSporeSeaCriterion(RoseiteSpores.getInstance()))
                .criterion("entered_midnight_sea",
                        createEnterSporeSeaCriterion(MidnightSpores.getInstance()))
                // TODO: Make this even more data-driven later
                .criterion("deep_spore_sea",
                        createEnterBiomeCriterion(biomeLookup, ModBiomeKeys.DEEP_SPORE_SEA))
                .criterion("lumar_forest",
                        createEnterBiomeCriterion(biomeLookup, ModBiomeKeys.LUMAR_FOREST))
                .criterion("lumar_grasslands",
                        createEnterBiomeCriterion(biomeLookup, ModBiomeKeys.LUMAR_GRASSLANDS))
                .criterion("lumar_peaks",
                        createEnterBiomeCriterion(biomeLookup, ModBiomeKeys.LUMAR_PEAKS))
                .criterion("lumar_rocks",
                        createEnterBiomeCriterion(biomeLookup, ModBiomeKeys.LUMAR_ROCKS))
                .criterion("saltstone_island",
                        createEnterBiomeCriterion(biomeLookup, ModBiomeKeys.SALTSTONE_ISLAND))
                .criterion("spore_sea",
                        createEnterBiomeCriterion(biomeLookup, ModBiomeKeys.SPORE_SEA))
                .build(consumer, Worldsinger.idStr("lumar/explore_lumar"));
        AdvancementEntry sailInSpores = Advancement.Builder.createUntelemetered()
                .parent(useSilverLinedBoat)
                .display(ModBlocks.VERDANT_SPORE_BLOCK,
                        Text.translatable("advancements.worldsinger.lumar.sail_in_spores.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.sail_in_spores.description"), null,
                        AdvancementFrame.TASK, true, true, false)
                .criterion("sail_in_spores_during_stilling",
                        SailedInSporeSeaCriterion.Conditions.create(false))
                .build(consumer, Worldsinger.idStr("lumar/sail_in_spores"));
        AdvancementEntry walkOnSporeSea = Advancement.Builder.createUntelemetered()
                .parent(sailInSpores)
                .display(Items.LEATHER_BOOTS,
                        Text.translatable("advancements.worldsinger.lumar.walk_on_spore_sea.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.walk_on_spore_sea.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criterion("walk_on_spore_sea", TickCriterion.Conditions.createLocation(
                        EntityPredicate.Builder.create()
                                .steppingOn(LocationPredicate.Builder.create()
                                        .block(BlockPredicate.Builder.create()
                                                .tag(blockLookup,
                                                        ModBlockTags.AETHER_SPORE_SEA_BLOCKS)))))
                .build(consumer, Worldsinger.idStr("lumar/walk_on_spore_sea"));

    }

    private AdvancementCriterion<Conditions> createEnterSporeSeaCriterion(AetherSpores sporeType) {
        return Criteria.LOCATION.create(new TickCriterion.Conditions(Optional.of(
                LootContextPredicate.create(
                        SporeSeaLocationCheckLootCondition.builder(sporeType).build()))));
    }

    private AdvancementCriterion<Conditions> createEnterBiomeCriterion(
            RegistryWrapper<Biome> biomeLookup, RegistryKey<Biome> biome) {
        return TickCriterion.Conditions.createLocation(
                LocationPredicate.Builder.createBiome(biomeLookup.getOrThrow(biome)));
    }
}
