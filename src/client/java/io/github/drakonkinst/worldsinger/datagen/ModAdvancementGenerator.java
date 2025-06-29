package io.github.drakonkinst.worldsinger.datagen;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballCore;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballShell;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensions;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.ImpossibleCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.text.Text;

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

        // Cosmere Advancements
        AdvancementEntry cosmere = Advancement.Builder.create()
                .display(ModBlocks.VERDANT_VINE_SNARE,
                        Text.translatable("advancements.worldsinger.cosmere.root.title"),
                        Text.translatable("advancements.worldsinger.cosmere.root.description"),
                        Worldsinger.id("block/saltstone"), AdvancementFrame.TASK, false, false,
                        false)
                // Gain the advancement by entering any cosmere world
                .criterion("entered_lumar", TickCriterion.Conditions.createLocation(
                        LocationPredicate.Builder.createDimension(ModDimensions.WORLD_LUMAR)))
                .build(consumer, Worldsinger.idStr("cosmere/root"));
        AdvancementEntry obtainFullSteel = Advancement.Builder.create()
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
        AdvancementEntry enterLumar = Advancement.Builder.create()
                .parent(cosmere)
                .display(ModItems.VERDANT_VINE,
                        Text.translatable("advancements.worldsinger.lumar.enter_lumar.title"),
                        Text.translatable("advancements.worldsinger.lumar.enter_lumar.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                .criterion("entered_lumar", TickCriterion.Conditions.createLocation(
                        LocationPredicate.Builder.createDimension(ModDimensions.WORLD_LUMAR)))
                .build(consumer, Worldsinger.idStr("lumar/enter_lumar"));
        AdvancementEntry killSpores = Advancement.Builder.create()
                .parent(enterLumar)
                .display(ModItems.SALT,
                        Text.translatable("advancements.worldsinger.lumar.kill_spores.title"),
                        Text.translatable("advancements.worldsinger.lumar.kill_spores.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/kill_spores"));
        AdvancementEntry useSilverLinedBoat = Advancement.Builder.create()
                .parent(killSpores)
                .display(silverLinedBoat, Text.translatable(
                                "advancements.worldsinger.lumar.use_silver_lined_boat.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.use_silver_lined_boat.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/use_silver_lined_boat"));
        AdvancementEntry obtainSaltstone = Advancement.Builder.create()
                .parent(killSpores)
                .display(ModBlocks.SALTSTONE,
                        Text.translatable("advancements.worldsinger.lumar.obtain_saltstone.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.obtain_saltstone.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/obtain_saltstone"));
        // Can move this around later
        AdvancementEntry obtainSaltedFood = Advancement.Builder.create()
                .parent(obtainSaltstone)
                .display(saltedChicken, Text.translatable(
                                "advancements.worldsinger.cosmere.obtain_salted_food.title"),
                        Text.translatable(
                                "advancements.worldsinger.cosmere.obtain_salted_food.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("cosmere/obtain_salted_food"));
        AdvancementEntry findLunagree = Advancement.Builder.create()
                .parent(useSilverLinedBoat)
                .display(ModItems.ROSEITE_SPORES_BUCKET,
                        Text.translatable("advancements.worldsinger.lumar.find_lunagree.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.find_lunagree.description"), null,
                        AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/find_lunagree"));
        AdvancementEntry lootShipwreck = Advancement.Builder.create()
                .parent(enterLumar)
                .display(Items.OAK_PLANKS,
                        Text.translatable("advancements.worldsinger.lumar.loot_shipwreck.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.loot_shipwreck.description"), null,
                        AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/loot_shipwreck"));
        AdvancementEntry obtainAllSporeBuckets = Advancement.Builder.create()
                .parent(useSilverLinedBoat)
                .display(ModItems.ZEPHYR_SPORES_BUCKET, Text.translatable(
                                "advancements.worldsinger.lumar.obtain_all_spore_buckets.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.obtain_all_spore_buckets.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/obtain_all_spore_buckets"));
        AdvancementEntry obtainAllSporeGrowths = Advancement.Builder.create()
                .parent(obtainAllSporeBuckets)
                .display(ModBlocks.LARGE_ROSEITE_BUD, Text.translatable(
                                "advancements.worldsinger.lumar.obtain_all_spore_growths.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.obtain_all_spore_growths.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/obtain_all_spore_growths"));
        AdvancementEntry obtainSporeCannonball = Advancement.Builder.create()
                .parent(obtainAllSporeBuckets)
                .display(sporeCannonball, Text.translatable(
                                "advancements.worldsinger.lumar.obtain_spore_cannonball.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.obtain_spore_cannonball.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/obtain_spore_cannonball"));
        AdvancementEntry brewSporeSplashBottle = Advancement.Builder.create()
                .parent(obtainAllSporeBuckets)
                .display(ModItems.SUNLIGHT_SPORES_SPLASH_BOTTLE, Text.translatable(
                                "advancements.worldsinger.lumar.brew_spore_splash_bottle.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.brew_spore_splash_bottle.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/brew_spore_splash_bottle"));
        AdvancementEntry obtainMagmaVent = Advancement.Builder.create()
                .parent(obtainAllSporeBuckets)
                .display(ModBlocks.MAGMA_VENT,
                        Text.translatable("advancements.worldsinger.lumar.obtain_magma_vent.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.obtain_magma_vent.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/obtain_magma_vent"));
        AdvancementEntry enterCrimsonSea = Advancement.Builder.create()
                .parent(useSilverLinedBoat)
                .display(ModBlocks.CRIMSON_SPORE_BLOCK,
                        Text.translatable("advancements.worldsinger.lumar.enter_crimson_sea.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.enter_crimson_sea.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/enter_crimson_sea"));
        AdvancementEntry enterMidnightSea = Advancement.Builder.create()
                .parent(enterCrimsonSea)
                .display(ModBlocks.MIDNIGHT_SPORE_BLOCK, Text.translatable(
                                "advancements.worldsinger.lumar.enter_midnight_sea.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.enter_midnight_sea.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/enter_midnight_sea"));
        AdvancementEntry tameMidnightCreature = Advancement.Builder.create()
                .parent(enterMidnightSea)
                .display(ModBlocks.MIDNIGHT_ESSENCE, Text.translatable(
                                "advancements.worldsinger.lumar.tame_midnight_creature.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.tame_midnight_creature.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/tame_midnight_creature"));
        AdvancementEntry findRainline = Advancement.Builder.create()
                .parent(lootShipwreck)
                .display(Items.FILLED_MAP,
                        Text.translatable("advancements.worldsinger.lumar.find_rainline.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.find_rainline.description"), null,
                        AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/find_rainline"));
        AdvancementEntry exploreLumar = Advancement.Builder.create()
                .parent(findRainline)
                .display(silverLinedBoat,
                        Text.translatable("advancements.worldsinger.lumar.explore_lumar.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.explore_lumar.description"), null,
                        AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/explore_lumar"));
        AdvancementEntry sailInSpores = Advancement.Builder.create()
                .parent(useSilverLinedBoat)
                .display(ModBlocks.VERDANT_SPORE_BLOCK,
                        Text.translatable("advancements.worldsinger.lumar.sail_in_spores.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.sail_in_spores.description"), null,
                        AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/sail_in_spores"));
        AdvancementEntry walkOnSporeSea = Advancement.Builder.create()
                .parent(sailInSpores)
                .display(Items.LEATHER_BOOTS,
                        Text.translatable("advancements.worldsinger.lumar.walk_on_spore_sea.title"),
                        Text.translatable(
                                "advancements.worldsinger.lumar.walk_on_spore_sea.description"),
                        null, AdvancementFrame.TASK, true, true, false)
                // TODO: Criterion
                .criterion("impossible",
                        Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, Worldsinger.idStr("lumar/walk_on_spore_sea"));

    }
}
