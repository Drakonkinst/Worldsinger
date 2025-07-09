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

import static io.github.drakonkinst.worldsinger.datagen.ModLootTableGenerator.itemEntry;

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.SlabType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.TableBonusLootCondition;
import net.minecraft.loot.condition.ValueCheckLootCondition;
import net.minecraft.loot.context.LootContext.EntityTarget;
import net.minecraft.loot.entry.AlternativeEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.LimitCountLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityTypePredicate;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.state.property.Properties;

public class ModBlockLootTableGenerator extends FabricBlockLootTableProvider {

    protected ModBlockLootTableGenerator(FabricDataOutput dataOutput,
            CompletableFuture<WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        // TODO: Figure out how to add random_sequence back

        // Drops itself
        addDrop(ModBlocks.ALUMINUM_BLOCK);
        addDrop(ModBlocks.ALUMINUM_CAULDRON);
        addDrop(ModBlocks.STEEL_ANVIL);
        addDrop(ModBlocks.CHIPPED_STEEL_ANVIL);
        addDrop(ModBlocks.DAMAGED_STEEL_ANVIL);
        addDrop(ModBlocks.RAW_SILVER_BLOCK);
        addDrop(ModBlocks.MAGMA_VENT);
        addDrop(ModBlocks.SALT_BLOCK);
        addDrop(ModBlocks.SILVER_BLOCK);

        // Silk touch
        addDropWithSilkTouch(ModBlocks.DEAD_CRIMSON_GROWTH);
        addDropWithSilkTouch(ModBlocks.DEAD_CRIMSON_SNARE);
        addDropWithSilkTouch(ModBlocks.DEAD_CRIMSON_SPIKE);
        addDropWithSilkTouch(ModBlocks.DEAD_CRIMSON_SPINES);
        addDropWithSilkTouch(ModBlocks.DEAD_TALL_CRIMSON_SPINES);
        addDropWithSilkTouch(ModBlocks.DEAD_TWISTING_VERDANT_VINES);
        addDropWithSilkTouch(ModBlocks.DEAD_TWISTING_VERDANT_VINES_PLANT,
                ModBlocks.DEAD_TWISTING_VERDANT_VINES);
        addDropWithSilkTouch(ModBlocks.DEAD_VERDANT_VINE_BLOCK);
        addDropWithSilkTouch(ModBlocks.DEAD_VERDANT_VINE_BRANCH);
        addDropWithSilkTouch(ModBlocks.DEAD_VERDANT_VINE_SNARE);
        addDropWithSilkTouch(ModBlocks.LARGE_ROSEITE_BUD);
        addDropWithSilkTouch(ModBlocks.MEDIUM_ROSEITE_BUD);
        addDropWithSilkTouch(ModBlocks.SMALL_ROSEITE_BUD);

        // Drops another block
        addDrop(ModBlocks.ALUMINUM_WATER_CAULDRON, ModBlocks.ALUMINUM_CAULDRON);
        addDrop(ModBlocks.ALUMINUM_LAVA_CAULDRON, ModBlocks.ALUMINUM_CAULDRON);
        addDrop(ModBlocks.ALUMINUM_POWDER_SNOW_CAULDRON, ModBlocks.ALUMINUM_CAULDRON);
        addDrop(ModBlocks.ALUMINUM_DEAD_SPORE_CAULDRON, ModBlocks.ALUMINUM_CAULDRON);
        addDrop(ModBlocks.ALUMINUM_VERDANT_SPORE_CAULDRON, ModBlocks.ALUMINUM_CAULDRON);
        addDrop(ModBlocks.ALUMINUM_CRIMSON_SPORE_CAULDRON, ModBlocks.ALUMINUM_CAULDRON);
        addDrop(ModBlocks.ALUMINUM_ZEPHYR_SPORE_CAULDRON, ModBlocks.ALUMINUM_CAULDRON);
        addDrop(ModBlocks.ALUMINUM_SUNLIGHT_SPORE_CAULDRON, ModBlocks.ALUMINUM_CAULDRON);
        addDrop(ModBlocks.ALUMINUM_ROSEITE_SPORE_CAULDRON, ModBlocks.ALUMINUM_CAULDRON);
        addDrop(ModBlocks.ALUMINUM_MIDNIGHT_SPORE_CAULDRON, ModBlocks.ALUMINUM_CAULDRON);
        addDrop(ModBlocks.DEAD_SPORE_CAULDRON, Blocks.CAULDRON);
        addDrop(ModBlocks.VERDANT_SPORE_CAULDRON, Blocks.CAULDRON);
        addDrop(ModBlocks.CRIMSON_SPORE_CAULDRON, Blocks.CAULDRON);
        addDrop(ModBlocks.ZEPHYR_SPORE_CAULDRON, Blocks.CAULDRON);
        addDrop(ModBlocks.SUNLIGHT_SPORE_CAULDRON, Blocks.CAULDRON);
        addDrop(ModBlocks.ROSEITE_SPORE_CAULDRON, Blocks.CAULDRON);
        addDrop(ModBlocks.MIDNIGHT_SPORE_CAULDRON, Blocks.CAULDRON);

        // Other
        addDrop(ModBlocks.ALUMINUM_SHEET, block -> this.multifaceGrowthDrops(block,
                // Always passes condition
                ValueCheckLootCondition.builder(ConstantLootNumberProvider.create(0),
                        BoundedIntUnaryOperator.create(0))));
        addDrop(ModBlocks.SALTSTONE, block -> this.dropsWithSilkTouch(block,
                this.addSurvivesExplosionCondition(block, ItemEntry.builder(ModItems.SALT)
                        // Identical to gravel/flint logic
                        .conditionally(TableBonusLootCondition.builder(
                                ModLootTableGenerator.getEnchantment(this.registries,
                                        Enchantments.FORTUNE), 0.1F, 0.14285715F, 0.25F, 1.0F))
                        .alternatively(ItemEntry.builder(block)))));

        // Ores
        addDrop(ModBlocks.SILVER_ORE, block -> oreDrops(block, ModItems.RAW_SILVER));
        addDrop(ModBlocks.DEEPSLATE_SILVER_ORE, block -> oreDrops(block, ModItems.RAW_SILVER));
        addDrop(ModBlocks.SALTSTONE_SALT_ORE, block -> this.dropsWithSilkTouch(block,
                this.applyExplosionDecay(block, ItemEntry.builder(ModItems.SALT)
                        .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(4, 5)))
                        .apply(ApplyBonusLootFunction.uniformBonusCount(
                                ModLootTableGenerator.getEnchantment(this.registries,
                                        Enchantments.FORTUNE))))));

        // Potted plants
        addPottedPlantDrops(ModBlocks.POTTED_DEAD_TWISTING_VERDANT_VINES);
        addPottedPlantDrops(ModBlocks.POTTED_DEAD_VERDANT_VINE_SNARE);
        addPottedPlantDrops(ModBlocks.POTTED_TWISTING_VERDANT_VINES);
        addPottedPlantDrops(ModBlocks.POTTED_VERDANT_VINE_SNARE);

        // Spore growths
        addSporeGrowthDrop(ModBlocks.VERDANT_VINE_BLOCK, itemEntry(ModItems.VERDANT_VINE, 3, 5));
        addSporeGrowthDrop(ModBlocks.VERDANT_VINE_BRANCH, itemEntry(ModItems.VERDANT_VINE, 1, 3));
        addSporeGrowthDrop(ModBlocks.VERDANT_VINE_SNARE, itemEntry(ModItems.VERDANT_VINE, 0, 2));
        addSporeGrowthDrop(ModBlocks.TWISTING_VERDANT_VINES,
                itemEntry(ModItems.VERDANT_VINE, 0, 1));
        addSporeGrowthDrop(ModBlocks.TWISTING_VERDANT_VINES_PLANT,
                itemEntry(ModBlocks.TWISTING_VERDANT_VINES),
                itemEntry(ModItems.VERDANT_VINE, 0, 1));
        addSporeGrowthDrop(ModBlocks.CRIMSON_GROWTH, itemEntry(ModItems.CRIMSON_SPINE, 2, 4));
        addSporeGrowthDrop(ModBlocks.CRIMSON_SNARE, itemEntry(ModItems.CRIMSON_SPINE, 1, 3));
        addSporeGrowthDrop(ModBlocks.CRIMSON_SPIKE, itemEntry(ModItems.CRIMSON_SPINE, 1, 3));
        addSporeGrowthDrop(ModBlocks.TALL_CRIMSON_SPINES, itemEntry(ModItems.CRIMSON_SPINE, 0, 2));
        addSporeGrowthDrop(ModBlocks.CRIMSON_SPINES, itemEntry(ModItems.CRIMSON_SPINE, 0, 2));
        addSporeGrowthDrop(ModBlocks.ROSEITE_BLOCK,
                itemEntry(ModItems.ROSEITE_CRYSTAL, 2, 4).apply(limitToMax(4)));
        addSporeGrowthDrop(ModBlocks.ROSEITE_STAIRS,
                itemEntry(ModItems.ROSEITE_CRYSTAL, 1, 3).apply(limitToMax(3)));
        addSporeGrowthDrop(ModBlocks.ROSEITE_SLAB, itemEntry(ModBlocks.ROSEITE_SLAB).apply(
                        SetCountLootFunction.builder(ConstantLootNumberProvider.create(2))
                                .conditionally(getDoubleSlabCondition(ModBlocks.ROSEITE_SLAB))),
                itemEntry(ModItems.ROSEITE_CRYSTAL, 0, 2).apply(limitToMax(2))
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(2),
                                        true)
                                .conditionally(getDoubleSlabCondition(ModBlocks.ROSEITE_SLAB))));
        addSporeGrowthDrop(ModBlocks.ROSEITE_CLUSTER,
                itemEntry(ModItems.ROSEITE_CRYSTAL, 0, 1).apply(limitToMax(1)));
    }

    private LootCondition.Builder getDoubleSlabCondition(Block block) {
        return BlockStatePropertyLootCondition.builder(block)
                .properties(StatePredicate.Builder.create()
                        .exactMatch(Properties.SLAB_TYPE, SlabType.DOUBLE));
    }

    private LootFunction.Builder limitToMax(int count) {
        return LimitCountLootFunction.builder(BoundedIntUnaryOperator.createMax(count));
    }

    private void addSporeGrowthDrop(Block block, LeafEntry.Builder<?> normalDrop) {
        addSporeGrowthDrop(block, ItemEntry.builder(block), normalDrop);
    }

    private void addSporeGrowthDrop(Block block, LeafEntry.Builder<?> silkTouchDrop,
            LeafEntry.Builder<?> normalDrop) {
        addDrop(block, LootTable.builder()
                .pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(EntityPropertiesLootCondition.builder(EntityTarget.THIS,
                                EntityPredicate.Builder.create()
                                        .type(EntityTypePredicate.create(this.registries.getOrThrow(
                                                RegistryKeys.ENTITY_TYPE), EntityType.PLAYER))))
                        .conditionally(BlockStatePropertyLootCondition.builder(block)
                                .properties(StatePredicate.Builder.create()
                                        .exactMatch(ModProperties.CATALYZED, true)))
                        .with(AlternativeEntry.builder(
                                silkTouchDrop.conditionally(this.createSilkTouchCondition()),
                                normalDrop.apply(ApplyBonusLootFunction.uniformBonusCount(
                                        ModLootTableGenerator.getEnchantment(this.registries,
                                                Enchantments.FORTUNE)))))
                        .build()));
    }
}
