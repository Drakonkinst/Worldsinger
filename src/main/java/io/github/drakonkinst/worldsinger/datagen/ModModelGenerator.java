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

import com.mojang.datafixers.util.Pair;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.item.ModItems;
import java.util.function.Function;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateModelGenerator.TintType;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.BlockStateVariantMap;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.MultipartBlockStateSupplier;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.data.client.VariantsBlockStateSupplier;
import net.minecraft.data.client.When;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

// Datagen is limited and does not work for the more complex items.
public class ModModelGenerator extends FabricModelProvider {

    public ModModelGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        generateBlockStatesOnly(blockStateModelGenerator);
        generateItemModelsOnly(blockStateModelGenerator);

        registerSimpleCubeBlocks(blockStateModelGenerator, new Block[] {
                ModBlocks.CRIMSON_GROWTH,
                ModBlocks.DEAD_CRIMSON_GROWTH,
                ModBlocks.DEAD_SPORE_BLOCK,
                ModBlocks.CRIMSON_SPORE_BLOCK,
                ModBlocks.DEEPSLATE_SILVER_ORE,
                ModBlocks.MAGMA_VENT,
                ModBlocks.MIDNIGHT_SPORE_BLOCK,
                ModBlocks.RAW_SILVER_BLOCK,
                ModBlocks.ROSEITE_SPORE_BLOCK,
                ModBlocks.SALT_BLOCK,
                ModBlocks.SALTSTONE,
                ModBlocks.SALTSTONE_SALT_ORE,
                ModBlocks.SILVER_BLOCK,
                ModBlocks.SILVER_ORE,
                ModBlocks.STEEL_BLOCK,
                ModBlocks.ALUMINUM_BLOCK,
                ModBlocks.SUNLIGHT_SPORE_BLOCK,
                ModBlocks.VERDANT_SPORE_BLOCK,
                ModBlocks.ZEPHYR_SPORE_BLOCK,
                ModBlocks.MIDNIGHT_ESSENCE
        });

        registerSimpleCrossBlocks(blockStateModelGenerator, new Block[] {
                ModBlocks.CRIMSON_SNARE, ModBlocks.DEAD_CRIMSON_SNARE
        });

        registerLeveledCauldron(blockStateModelGenerator, ModBlocks.DEAD_SPORE_CAULDRON,
                ModBlocks.DEAD_SPORE_BLOCK);
        registerLeveledCauldron(blockStateModelGenerator, ModBlocks.VERDANT_SPORE_CAULDRON,
                ModBlocks.VERDANT_SPORE_BLOCK);
        registerLeveledCauldron(blockStateModelGenerator, ModBlocks.CRIMSON_SPORE_CAULDRON,
                ModBlocks.CRIMSON_SPORE_BLOCK);
        registerLeveledCauldron(blockStateModelGenerator, ModBlocks.ZEPHYR_SPORE_CAULDRON,
                ModBlocks.ZEPHYR_SPORE_BLOCK);
        registerLeveledCauldron(blockStateModelGenerator, ModBlocks.SUNLIGHT_SPORE_CAULDRON,
                ModBlocks.SUNLIGHT_SPORE_BLOCK);
        registerLeveledCauldron(blockStateModelGenerator, ModBlocks.ROSEITE_SPORE_CAULDRON,
                ModBlocks.ROSEITE_SPORE_BLOCK);
        registerLeveledCauldron(blockStateModelGenerator, ModBlocks.MIDNIGHT_SPORE_CAULDRON,
                ModBlocks.MIDNIGHT_SPORE_BLOCK);

        registerUpFacingCrossBlock(blockStateModelGenerator, ModBlocks.CRIMSON_SPINES);
        registerUpFacingCrossBlock(blockStateModelGenerator, ModBlocks.DEAD_CRIMSON_SPINES);
        registerUpFacingCrossBlock(blockStateModelGenerator, ModBlocks.VERDANT_VINE_SNARE);
        registerUpFacingCrossBlock(blockStateModelGenerator, ModBlocks.DEAD_VERDANT_VINE_SNARE);

        blockStateModelGenerator.registerAnvil(ModBlocks.STEEL_ANVIL);
        blockStateModelGenerator.registerAnvil(ModBlocks.CHIPPED_STEEL_ANVIL);
        blockStateModelGenerator.registerAnvil(ModBlocks.DAMAGED_STEEL_ANVIL);

        // Currently uses the same blockstates and models as Amethyst, just with different textures.
        // Item model must be written by hand
        blockStateModelGenerator.registerAmethyst(ModBlocks.ROSEITE_CLUSTER);
        blockStateModelGenerator.registerAmethyst(ModBlocks.LARGE_ROSEITE_BUD);
        blockStateModelGenerator.registerAmethyst(ModBlocks.MEDIUM_ROSEITE_BUD);
        blockStateModelGenerator.registerAmethyst(ModBlocks.SMALL_ROSEITE_BUD);

        blockStateModelGenerator.registerDoubleBlock(ModBlocks.TALL_CRIMSON_SPINES,
                TintType.NOT_TINTED);
        blockStateModelGenerator.registerDoubleBlock(ModBlocks.DEAD_TALL_CRIMSON_SPINES,
                TintType.NOT_TINTED);

        BlockStateModelGenerator.BlockTexturePool roseiteTexturePool = blockStateModelGenerator.registerCubeAllModelTexturePool(
                ModBlocks.ROSEITE_BLOCK);
        roseiteTexturePool.stairs(ModBlocks.ROSEITE_STAIRS);
        roseiteTexturePool.slab(ModBlocks.ROSEITE_SLAB);

        registerAliasedModel(blockStateModelGenerator, ModBlocks.ALUMINUM_SHEET,
                ModBlocks.ALUMINUM_BLOCK);
        registerMultifaceBlock(blockStateModelGenerator, ModBlocks.ALUMINUM_SHEET);
    }

    private void generateBlockStatesOnly(BlockStateModelGenerator blockStateModelGenerator) {
        // registerSimpleState() generates ONLY the blockstate file, nothing else
        blockStateModelGenerator.registerSimpleState(ModBlocks.DEAD_SPORE_SEA);
        blockStateModelGenerator.registerSimpleState(ModBlocks.VERDANT_SPORE_SEA);
        blockStateModelGenerator.registerSimpleState(ModBlocks.CRIMSON_SPORE_SEA);
        blockStateModelGenerator.registerSimpleState(ModBlocks.ZEPHYR_SPORE_SEA);
        blockStateModelGenerator.registerSimpleState(ModBlocks.SUNLIGHT_SPORE_SEA);
        blockStateModelGenerator.registerSimpleState(ModBlocks.ROSEITE_SPORE_SEA);
        blockStateModelGenerator.registerSimpleState(ModBlocks.MIDNIGHT_SPORE_SEA);
        blockStateModelGenerator.registerSimpleState(ModBlocks.SUNLIGHT);
    }

    private void generateItemModelsOnly(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerParentedItemModel(ModItems.MIDNIGHT_CREATURE_SPAWN_EGG,
                ModelIds.getMinecraftNamespacedItem("template_spawn_egg"));
    }

    private void registerSimpleCubeBlocks(BlockStateModelGenerator blockStateModelGenerator,
            Block[] blocks) {
        for (Block block : blocks) {
            blockStateModelGenerator.registerSimpleCubeAll(block);
        }
    }

    private void registerAliasedModel(BlockStateModelGenerator blockStateModelGenerator,
            Block target, Block texture) {
        Item item = target.asItem();
        Models.GENERATED.upload(ModelIds.getItemModelId(item), TextureMap.layer0(texture),
                blockStateModelGenerator.modelCollector);
    }

    private void registerSimpleCrossBlocks(BlockStateModelGenerator blockStateModelGenerator,
            Block[] blocks) {
        for (Block block : blocks) {
            blockStateModelGenerator.registerTintableCross(block, TintType.NOT_TINTED);
        }
    }

    private void registerUpFacingCrossBlock(BlockStateModelGenerator blockStateModelGenerator,
            Block block) {
        blockStateModelGenerator.registerItemModel(block);
        blockStateModelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block,
                        BlockStateVariant.create()
                                .put(VariantSettings.MODEL,
                                        Models.CROSS.upload(block, TextureMap.cross(block),
                                                blockStateModelGenerator.modelCollector)))
                .coordinate(blockStateModelGenerator.createUpDefaultFacingVariantMap()));
    }

    private void registerLeveledCauldron(BlockStateModelGenerator blockStateModelGenerator,
            Block cauldronBlock, Block contentBlock) {
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(cauldronBlock)
                        .coordinate(BlockStateVariantMap.create(LeveledCauldronBlock.LEVEL)
                                .register(1, BlockStateVariant.create()
                                        .put(VariantSettings.MODEL,
                                                Models.TEMPLATE_CAULDRON_LEVEL1.upload(
                                                        cauldronBlock, "_level1",
                                                        TextureMap.cauldron(
                                                                TextureMap.getId(contentBlock)),
                                                        blockStateModelGenerator.modelCollector)))
                                .register(2, BlockStateVariant.create()
                                        .put(VariantSettings.MODEL,
                                                Models.TEMPLATE_CAULDRON_LEVEL2.upload(
                                                        cauldronBlock, "_level2",
                                                        TextureMap.cauldron(
                                                                TextureMap.getId(contentBlock)),
                                                        blockStateModelGenerator.modelCollector)))
                                .register(3, BlockStateVariant.create()
                                        .put(VariantSettings.MODEL,
                                                Models.TEMPLATE_CAULDRON_FULL.upload(cauldronBlock,
                                                        "_full", TextureMap.cauldron(
                                                                TextureMap.getId(contentBlock)),
                                                        blockStateModelGenerator.modelCollector)))));
    }

    private void registerMultifaceBlock(BlockStateModelGenerator blockStateModelGenerator,
            Block block) {
        Identifier identifier = ModelIds.getBlockModelId(block);
        MultipartBlockStateSupplier multipartBlockStateSupplier = MultipartBlockStateSupplier.create(
                block);
        When.PropertyCondition propertyCondition = Util.make(When.create(),
                propertyConditionx -> BlockStateModelGenerator.CONNECTION_VARIANT_FUNCTIONS.stream()
                        .map(Pair::getFirst)
                        .forEach(property -> {
                            if (block.getDefaultState().contains(property)) {
                                propertyConditionx.set(property, false);
                            }
                        }));

        for (Pair<BooleanProperty, Function<Identifier, BlockStateVariant>> pair : BlockStateModelGenerator.CONNECTION_VARIANT_FUNCTIONS) {
            BooleanProperty booleanProperty = pair.getFirst();
            Function<Identifier, BlockStateVariant> function = pair.getSecond();
            if (block.getDefaultState().contains(booleanProperty)) {
                multipartBlockStateSupplier.with(When.create().set(booleanProperty, true),
                        function.apply(identifier));
                multipartBlockStateSupplier.with(propertyCondition, function.apply(identifier));
            }
        }

        blockStateModelGenerator.blockStateCollector.accept(multipartBlockStateSupplier);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        registerGeneratedItems(itemModelGenerator, new ItemConvertible[] {
                ModItems.VERDANT_VINE,
                ModItems.CRUDE_IRON,
                ModItems.RAW_SILVER,
                ModItems.SILVER_NUGGET,
                ModItems.SILVER_INGOT,
                ModItems.SALT,
                ModItems.STEEL_NUGGET,
                ModItems.STEEL_INGOT,
                ModItems.ALUMINUM_INGOT,
                ModItems.ALUMINUM_NUGGET,
                ModItems.DEAD_SPORES_BUCKET,
                ModItems.VERDANT_SPORES_BUCKET,
                ModItems.CRIMSON_SPORES_BUCKET,
                ModItems.ZEPHYR_SPORES_BUCKET,
                ModItems.SUNLIGHT_SPORES_BUCKET,
                ModItems.ROSEITE_SPORES_BUCKET,
                ModItems.MIDNIGHT_SPORES_BUCKET,
                ModItems.FLINT_AND_IRON,
                ModItems.QUARTZ_AND_IRON,
                ModItems.QUARTZ_AND_STEEL,
                ModItems.ROSEITE_CRYSTAL,
                ModItems.ROSEITE_BEAD
        });
        registerHandheldItems(itemModelGenerator, new ItemConvertible[] {
                ModItems.CRIMSON_SPINE,
                ModItems.STEEL_AXE,
                ModItems.STEEL_PICKAXE,
                ModItems.STEEL_HOE,
                ModItems.STEEL_SHOVEL,
                ModItems.STEEL_SWORD,
                ModItems.SILVER_KNIFE
        });

        // Adds armor trim models as well, which we didn't have before
        itemModelGenerator.registerArmor((ArmorItem) ModItems.STEEL_HELMET);
        itemModelGenerator.registerArmor((ArmorItem) ModItems.STEEL_CHESTPLATE);
        itemModelGenerator.registerArmor((ArmorItem) ModItems.STEEL_LEGGINGS);
        itemModelGenerator.registerArmor((ArmorItem) ModItems.STEEL_BOOTS);
    }

    private void registerGeneratedItems(ItemModelGenerator itemModelGenerator,
            ItemConvertible[] items) {
        for (ItemConvertible item : items) {
            itemModelGenerator.register(item.asItem(), Models.GENERATED);
        }
    }

    private void registerHandheldItems(ItemModelGenerator itemModelGenerator,
            ItemConvertible[] items) {
        for (ItemConvertible item : items) {
            itemModelGenerator.register(item.asItem(), Models.HANDHELD);
        }
    }
}
