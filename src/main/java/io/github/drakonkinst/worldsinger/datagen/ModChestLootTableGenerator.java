/*
 * MIT License
 *
 * Copyright (c) 2024 Drakonkinst
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

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballContent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballCore;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballShell;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import io.github.drakonkinst.worldsinger.registry.ModLootTables;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.function.EnchantRandomlyLootFunction;
import net.minecraft.loot.function.SetComponentsLootFunction;
import net.minecraft.loot.function.SetDamageLootFunction;
import net.minecraft.loot.function.SetPotionLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.potion.Potions;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class ModChestLootTableGenerator extends ModLootTableGenerator {

    public ModChestLootTableGenerator(FabricDataOutput output,
            CompletableFuture<WrapperLookup> registryLookup) {
        super(output, registryLookup, LootContextTypes.CHEST);
    }

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, Builder> consumer) {
        registerLumarLootTables(consumer);
    }

    private void registerLumarLootTables(BiConsumer<RegistryKey<LootTable>, Builder> consumer) {
        consumer.accept(ModLootTables.LUMAR_SALTSTONE_MINESHAFT_CHEST, LootTable.builder()
                // Common
                .pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(3, 6))
                        // Identical to normal Mineshaft chests
                        .with(itemEntry(Items.TORCH, 1, 16).weight(15))
                        .with(itemEntry(Items.BREAD, 1, 3).weight(15))
                        .with(itemEntry(Items.COAL, 3, 8).weight(10))
                        .with(itemEntry(Items.DIAMOND, 1, 2).weight(3))
                        .with(itemEntry(Items.LAPIS_LAZULI, 4, 9).weight(5))
                        .with(itemEntry(Items.GOLD_INGOT, 1, 3).weight(5))
                        .with(itemEntry(Items.IRON_INGOT, 1, 5).weight(10))
                        // Equal weight to gold ingots
                        .with(itemEntry(ModItems.SILVER_INGOT, 1, 3).weight(5))
                        .with(itemEntry(ModItems.STEEL_INGOT, 1, 3).weight(5))
                        .build())
                // Rare
                .pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(1, 2))
                        .with(itemEntry(Items.BOOK).weight(10)
                                // TODO: Add 15% chance to also gain Silk Touch using set_enchantments
                                .apply(EnchantRandomlyLootFunction.create()))
                        .with(itemEntry(Items.NAME_TAG).weight(30))
                        .with(itemEntry(ModItems.STEEL_PICKAXE).weight(2))));

        consumer.accept(ModLootTables.LUMAR_SHIPWRECK_CAPTAIN_CHEST, LootTable.builder()
                // Minor valuables
                .pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(2, 5))
                        .with(itemEntry(Items.LAPIS_LAZULI, 1, 10).weight(2))
                        .with(itemEntry(ModItems.SILVER_NUGGET, 1, 10).weight(5))
                        .with(itemEntry(ModItems.STEEL_NUGGET, 1, 10).weight(5))
                        .with(itemEntry(Items.IRON_NUGGET, 1, 10).weight(5))
                        .with(itemEntry(Items.GOLD_NUGGET, 1, 10).weight(5))
                        .build())
                // Major valuables
                .pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(3, 6))
                        .with(itemEntry(Items.IRON_INGOT, 1, 5).weight(18))
                        .with(itemEntry(ModItems.STEEL_INGOT, 1, 5).weight(2))
                        .with(itemEntry(ModItems.SILVER_INGOT, 1, 5).weight(10))
                        .with(itemEntry(Items.GOLD_INGOT, 1, 5).weight(2))
                        .with(itemEntry(Items.EMERALD, 1, 5).weight(8))
                        .with(itemEntry(Items.DIAMOND).weight(1))
                        .build())
                // Weapons
                .pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(0, 2))
                        .with(itemEntry(ModItems.STEEL_SWORD).weight(2)
                                .apply(SetDamageLootFunction.builder(
                                        UniformLootNumberProvider.create(0.5f, 1.0f))))
                        .with(itemEntry(Items.IRON_SWORD).weight(4)
                                .apply(SetDamageLootFunction.builder(
                                        UniformLootNumberProvider.create(0.5f, 1.0f))))
                        .build())
                // Navigation tools
                .pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(1, 2))
                        .with(itemEntry(Items.PAPER, 1, 5))
                        .with(itemEntry(Items.CLOCK))
                        .with(itemEntry(Items.COMPASS))
                        .with(itemEntry(Items.MAP))
                        .with(itemEntry(Items.SPYGLASS))
                        .build()));

        consumer.accept(ModLootTables.LUMAR_SHIPWRECK_SUPPLY_CHEST, LootTable.builder()
                // Farming
                .pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(2, 5))
                        .with(itemEntry(Items.BEETROOT_SEEDS, 2, 4).weight(2))
                        .with(itemEntry(Items.PUMPKIN, 1, 3).weight(2))
                        .with(itemEntry(Items.PUMPKIN_SEEDS, 2, 4).weight(2))
                        .with(itemEntry(Items.MELON_SEEDS, 2, 4).weight(2))
                        .with(itemEntry(Items.WHEAT, 4, 8).weight(7))
                        .with(itemEntry(Items.CARROT, 4, 8).weight(7))
                        .with(itemEntry(Items.POTATO, 2, 6).weight(7))
                        .with(itemEntry(Items.POISONOUS_POTATO, 1, 3).weight(7))
                        .with(itemEntry(Items.DIRT, 1, 4).weight(7)))
                // Misc Supplies
                .pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(3, 8))
                        .with(itemEntry(Items.INK_SAC, 1, 10).weight(10))
                        .with(itemEntry(Items.LEATHER_HELMET).weight(3)
                                .apply(SetDamageLootFunction.builder(
                                        UniformLootNumberProvider.create(0.5f, 1.0f)))
                                .apply(EnchantRandomlyLootFunction.create()))
                        .with(itemEntry(Items.WATER_BUCKET, 1, 2).weight(3))
                        .with(itemEntry(Items.COOKED_CHICKEN, 1, 5).weight(6))
                        .with(itemEntry(ModItems.VERDANT_VINE, 1, 10).weight(10))
                        .with(itemEntry(Items.COAL, 2, 8).weight(6))
                        .with(itemEntry(Items.BOOK, 1, 3).weight(5))
                        .with(itemEntry(Items.FEATHER, 1, 5).weight(10))
                        .with(itemEntry(Items.PAPER, 1, 10).weight(8))
                        .build()));

        consumer.accept(ModLootTables.LUMAR_SHIPWRECK_SPROUTER_CHEST, LootTable.builder()
                // Spores
                .pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(6, 10))
                        .with(itemEntry(Items.GLASS_BOTTLE, 1, 6).weight(250))
                        .with(itemEntry(ModItems.CRIMSON_SPINE, 1, 3).weight(200))
                        .with(itemEntry(ModItems.VERDANT_VINE, 1, 3).weight(200))
                        .with(itemEntry(ModItems.ROSEITE_CRYSTAL, 1, 6).weight(200))
                        .with(itemEntry(ModItems.ROSEITE_CORE, 1, 3).weight(150))
                        .with(itemEntry(Items.POTION, 1, 3).weight(120)
                                .apply(SetPotionLootFunction.builder(Potions.WATER)))
                        .with(itemEntry(Items.SPLASH_POTION).weight(90)
                                .apply(SetPotionLootFunction.builder(Potions.WATER)))
                        .with(itemEntry(ModItems.VERDANT_SPORES_BOTTLE, 1, 3).weight(100))
                        .with(itemEntry(ModItems.VERDANT_SPORES_SPLASH_BOTTLE).weight(25))
                        .with(itemEntry(ModItems.CRIMSON_SPORES_BOTTLE, 1, 3).weight(100))
                        .with(itemEntry(ModItems.CRIMSON_SPORES_SPLASH_BOTTLE).weight(25))
                        .with(itemEntry(ModItems.ZEPHYR_SPORES_BOTTLE, 1, 3).weight(150))
                        .with(itemEntry(ModItems.ZEPHYR_SPORES_SPLASH_BOTTLE).weight(50))
                        .with(itemEntry(ModItems.SUNLIGHT_SPORES_BOTTLE, 1, 3).weight(150))
                        .with(itemEntry(ModItems.SUNLIGHT_SPORES_SPLASH_BOTTLE).weight(50))
                        .with(itemEntry(ModItems.ROSEITE_SPORES_BOTTLE, 1, 3).weight(100))
                        .with(itemEntry(ModItems.ROSEITE_SPORES_SPLASH_BOTTLE).weight(25))
                        .with(itemEntry(ModItems.MIDNIGHT_SPORES_BOTTLE, 1, 3).weight(10))
                        .with(itemEntry(ModItems.MIDNIGHT_SPORES_SPLASH_BOTTLE).weight(7))
                        .with(itemEntry(ModItems.CERAMIC_CANNONBALL, 2, 4).weight(10)
                                .apply(SetComponentsLootFunction.builder(
                                        ModDataComponentTypes.CANNONBALL,
                                        new CannonballComponent(CannonballShell.CERAMIC,
                                                CannonballCore.ROSEITE, 1,
                                                List.of(CannonballContent.SUNLIGHT_SPORES)))))
                        .with(itemEntry(ModItems.CERAMIC_CANNONBALL, 4, 8).weight(25)
                                .apply(SetComponentsLootFunction.builder(
                                        ModDataComponentTypes.CANNONBALL,
                                        new CannonballComponent(CannonballShell.CERAMIC,
                                                CannonballCore.WATER, 0, Collections.emptyList()))))
                        .with(itemEntry(ModItems.CERAMIC_CANNONBALL, 4, 10).weight(50))
                        .with(itemEntry(Items.WATER_BUCKET).weight(10))
                        .with(itemEntry(ModItems.VERDANT_SPORES_BUCKET).weight(10))
                        .with(itemEntry(ModItems.CRIMSON_SPORES_BUCKET).weight(10))
                        .with(itemEntry(ModItems.ZEPHYR_SPORES_BUCKET).weight(10))
                        .with(itemEntry(ModItems.ROSEITE_SPORES_BUCKET).weight(10))
                        .with(itemEntry(ModItems.MIDNIGHT_SPORES_BUCKET).weight(1))
                        .with(itemEntry(ModItems.ALUMINUM_NUGGET, 9, 18).weight(100))
                        .with(itemEntry(ModBlocks.ALUMINUM_SHEET, 8, 24).weight(75))
                        .with(itemEntry(ModItems.ALUMINUM_INGOT, 2, 8).weight(25))
                        .build())
                // Sprouter tools
                .pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(0, 1))
                        .with(itemEntry(ModItems.STEEL_SHOVEL).weight(3)
                                .apply(SetDamageLootFunction.builder(
                                        UniformLootNumberProvider.create(0.3f, 0.8f))))
                        .with(itemEntry(Items.SHIELD).weight(3)
                                .apply(SetDamageLootFunction.builder(
                                        UniformLootNumberProvider.create(0.3f, 0.8f))))
                        .build()));
    }
}
