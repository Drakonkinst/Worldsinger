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
import io.github.drakonkinst.worldsinger.item.CannonballCoreProperty;
import io.github.drakonkinst.worldsinger.item.CannonballFuseProperty;
import io.github.drakonkinst.worldsinger.item.ItemOverlay;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.item.SporeBottleTintSource;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballCore;
import io.github.drakonkinst.worldsinger.mixin.client.accessor.BlockStateModelGeneratorAccessor;
import io.github.drakonkinst.worldsinger.registry.ModEquipmentAssetKeys;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.BlockStateModelGenerator.CrossType;
import net.minecraft.client.data.BlockStateVariantMap;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.ItemModels;
import net.minecraft.client.data.Model;
import net.minecraft.client.data.ModelIds;
import net.minecraft.client.data.Models;
import net.minecraft.client.data.MultipartBlockModelDefinitionCreator;
import net.minecraft.client.data.TextureMap;
import net.minecraft.client.data.TexturedModel;
import net.minecraft.client.data.VariantsBlockModelDefinitionCreator;
import net.minecraft.client.render.item.model.ItemModel.Unbaked;
import net.minecraft.client.render.item.model.RangeDispatchItemModel;
import net.minecraft.client.render.item.model.SelectItemModel.SwitchCase;
import net.minecraft.client.render.item.property.select.DisplayContextProperty;
import net.minecraft.client.render.model.json.WeightedVariant;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

// Datagen is limited and does not work for the more complex items.
public class ModModelGenerator extends FabricModelProvider {

    public static final TexturedModel.Factory ALL_CUBE_COLUMN = TexturedModel.makeFactory(
            TextureMap::all, Models.CUBE_COLUMN);
    public static final TexturedModel.Factory ALL_CUBE_COLUMN_HORIZONTAL = TexturedModel.makeFactory(
            TextureMap::all, Models.CUBE_COLUMN_HORIZONTAL);

    public ModModelGenerator(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        generateBlockStatesOnly(blockStateModelGenerator);

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
                ModBlocks.MIDNIGHT_ESSENCE,
        });

        registerSimpleCrossBlocks(blockStateModelGenerator, new Block[] {
                ModBlocks.CRIMSON_SNARE, ModBlocks.DEAD_CRIMSON_SNARE
        });

        registerUpFacingCrossBlock(blockStateModelGenerator, ModBlocks.CRIMSON_SPINES);
        registerUpFacingCrossBlock(blockStateModelGenerator, ModBlocks.DEAD_CRIMSON_SPINES);
        registerUpFacingCrossBlock(blockStateModelGenerator, ModBlocks.VERDANT_VINE_SNARE);
        registerUpFacingCrossBlock(blockStateModelGenerator, ModBlocks.DEAD_VERDANT_VINE_SNARE);
        registerBark(blockStateModelGenerator, ModBlocks.VERDANT_VINE_BLOCK);
        registerBark(blockStateModelGenerator, ModBlocks.DEAD_VERDANT_VINE_BLOCK);

        registerCauldrons(blockStateModelGenerator);

        this.registerFlowerPotNoPlant(blockStateModelGenerator, ModBlocks.VERDANT_VINE_SNARE,
                ModBlocks.POTTED_VERDANT_VINE_SNARE, CrossType.NOT_TINTED);
        this.registerFlowerPotNoPlant(blockStateModelGenerator, ModBlocks.TWISTING_VERDANT_VINES,
                ModBlocks.POTTED_TWISTING_VERDANT_VINES, CrossType.NOT_TINTED);
        this.registerFlowerPotNoPlant(blockStateModelGenerator, ModBlocks.DEAD_VERDANT_VINE_SNARE,
                ModBlocks.POTTED_DEAD_VERDANT_VINE_SNARE, CrossType.NOT_TINTED);
        this.registerFlowerPotNoPlant(blockStateModelGenerator,
                ModBlocks.DEAD_TWISTING_VERDANT_VINES, ModBlocks.POTTED_DEAD_TWISTING_VERDANT_VINES,
                CrossType.NOT_TINTED);

        blockStateModelGenerator.registerAnvil(ModBlocks.STEEL_ANVIL);
        blockStateModelGenerator.registerAnvil(ModBlocks.CHIPPED_STEEL_ANVIL);
        blockStateModelGenerator.registerAnvil(ModBlocks.DAMAGED_STEEL_ANVIL);

        // Currently uses the same blockstates and models as Amethyst, just with different textures.
        // Item coreModel must be written by hand
        blockStateModelGenerator.registerAmethyst(ModBlocks.ROSEITE_CLUSTER);
        blockStateModelGenerator.registerAmethyst(ModBlocks.LARGE_ROSEITE_BUD);
        blockStateModelGenerator.registerAmethyst(ModBlocks.MEDIUM_ROSEITE_BUD);
        blockStateModelGenerator.registerAmethyst(ModBlocks.SMALL_ROSEITE_BUD);

        blockStateModelGenerator.registerDoubleBlockAndItem(ModBlocks.TALL_CRIMSON_SPINES,
                CrossType.NOT_TINTED);
        blockStateModelGenerator.registerDoubleBlockAndItem(ModBlocks.DEAD_TALL_CRIMSON_SPINES,
                CrossType.NOT_TINTED);

        BlockStateModelGenerator.BlockTexturePool roseiteTexturePool = blockStateModelGenerator.registerCubeAllModelTexturePool(
                ModBlocks.ROSEITE_BLOCK);
        roseiteTexturePool.stairs(ModBlocks.ROSEITE_STAIRS);
        roseiteTexturePool.slab(ModBlocks.ROSEITE_SLAB);

        registerAliasedModel(blockStateModelGenerator, ModBlocks.ALUMINUM_SHEET,
                ModBlocks.ALUMINUM_BLOCK);
        blockStateModelGenerator.registerMultifaceBlockModel(ModBlocks.ALUMINUM_SHEET);

        registerBranch(blockStateModelGenerator, ModBlocks.VERDANT_VINE_BRANCH);
        registerBranch(blockStateModelGenerator, ModBlocks.DEAD_VERDANT_VINE_BRANCH);
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

    // Same as vanilla method, but don't register the plant block
    private void registerFlowerPotNoPlant(BlockStateModelGenerator blockStateModelGenerator,
            Block plantBlock, Block flowerPotBlock, BlockStateModelGenerator.CrossType tintType) {
        TextureMap textureMap = tintType.getFlowerPotTextureMap(plantBlock);
        WeightedVariant weightedVariant = BlockStateModelGenerator.createWeightedVariant(
                tintType.getFlowerPotCrossModel()
                        .upload(flowerPotBlock, textureMap,
                                blockStateModelGenerator.modelCollector));
        blockStateModelGenerator.blockStateCollector.accept(
                BlockStateModelGenerator.createSingletonBlockState(flowerPotBlock,
                        weightedVariant));
    }

    // Block that uses same texture for all sides, but is sided
    private void registerBark(BlockStateModelGenerator blockStateModelGenerator, Block block) {
        WeightedVariant verticalVariant = BlockStateModelGenerator.createWeightedVariant(
                ALL_CUBE_COLUMN.upload(block, blockStateModelGenerator.modelCollector));
        WeightedVariant horizontalVariant = BlockStateModelGenerator.createWeightedVariant(
                ALL_CUBE_COLUMN_HORIZONTAL.upload(block, blockStateModelGenerator.modelCollector));
        blockStateModelGenerator.blockStateCollector.accept(
                BlockStateModelGenerator.createAxisRotatedBlockState(block, verticalVariant,
                        horizontalVariant));
    }

    // Pattered after chorus plant
    private void registerBranch(BlockStateModelGenerator blockStateModelGenerator, Block block) {
        WeightedVariant sidedVariant = BlockStateModelGenerator.createWeightedVariant(
                ModelIds.getBlockSubModelId(block, "_side"));
        WeightedVariant nonSidedVariant = BlockStateModelGenerator.createWeightedVariant(
                ModelIds.getBlockSubModelId(block, "_noside"));
        blockStateModelGenerator.blockStateCollector.accept(
                MultipartBlockModelDefinitionCreator.create(block)
                        .with(BlockStateModelGenerator.createMultipartConditionBuilder()
                                .put(Properties.NORTH, true), sidedVariant)
                        .with(BlockStateModelGenerator.createMultipartConditionBuilder()
                                        .put(Properties.EAST, true),
                                sidedVariant.apply(BlockStateModelGenerator.ROTATE_Y_90)
                                        .apply(BlockStateModelGenerator.UV_LOCK))
                        .with(BlockStateModelGenerator.createMultipartConditionBuilder()
                                        .put(Properties.SOUTH, true),
                                sidedVariant.apply(BlockStateModelGenerator.ROTATE_Y_180)
                                        .apply(BlockStateModelGenerator.UV_LOCK))
                        .with(BlockStateModelGenerator.createMultipartConditionBuilder()
                                        .put(Properties.WEST, true),
                                sidedVariant.apply(BlockStateModelGenerator.ROTATE_Y_270)
                                        .apply(BlockStateModelGenerator.UV_LOCK))
                        .with(BlockStateModelGenerator.createMultipartConditionBuilder()
                                        .put(Properties.UP, true),
                                sidedVariant.apply(BlockStateModelGenerator.ROTATE_X_270)
                                        .apply(BlockStateModelGenerator.UV_LOCK))
                        .with(BlockStateModelGenerator.createMultipartConditionBuilder()
                                        .put(Properties.DOWN, true),
                                sidedVariant.apply(BlockStateModelGenerator.ROTATE_X_90)
                                        .apply(BlockStateModelGenerator.UV_LOCK))
                        .with(BlockStateModelGenerator.createMultipartConditionBuilder()
                                .put(Properties.NORTH, false), nonSidedVariant)
                        .with(BlockStateModelGenerator.createMultipartConditionBuilder()
                                        .put(Properties.EAST, false),
                                nonSidedVariant.apply(BlockStateModelGenerator.ROTATE_Y_90)
                                        .apply(BlockStateModelGenerator.UV_LOCK))
                        .with(BlockStateModelGenerator.createMultipartConditionBuilder()
                                        .put(Properties.SOUTH, false),
                                nonSidedVariant.apply(BlockStateModelGenerator.ROTATE_Y_180)
                                        .apply(BlockStateModelGenerator.UV_LOCK))
                        .with(BlockStateModelGenerator.createMultipartConditionBuilder()
                                        .put(Properties.WEST, false),
                                nonSidedVariant.apply(BlockStateModelGenerator.ROTATE_Y_270)
                                        .apply(BlockStateModelGenerator.UV_LOCK))
                        .with(BlockStateModelGenerator.createMultipartConditionBuilder()
                                        .put(Properties.UP, false),
                                nonSidedVariant.apply(BlockStateModelGenerator.ROTATE_X_270)
                                        .apply(BlockStateModelGenerator.UV_LOCK))
                        .with(BlockStateModelGenerator.createMultipartConditionBuilder()
                                        .put(Properties.DOWN, false),
                                nonSidedVariant.apply(BlockStateModelGenerator.ROTATE_X_90)
                                        .apply(BlockStateModelGenerator.UV_LOCK)));
    }

    private void registerCauldrons(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerItemModel(ModBlocks.ALUMINUM_CAULDRON.asItem());
        blockStateModelGenerator.registerSimpleState(ModBlocks.ALUMINUM_CAULDRON);
        blockStateModelGenerator.blockStateCollector.accept(
                BlockStateModelGenerator.createSingletonBlockState(ModBlocks.ALUMINUM_LAVA_CAULDRON,
                        BlockStateModelGenerator.createWeightedVariant(
                                ModModels.TEMPLATE_ALUMINUM_CAULDRON_FULL.upload(
                                        ModBlocks.ALUMINUM_LAVA_CAULDRON,
                                        ModTextureMaps.aluminumCauldron(
                                                TextureMap.getSubId(Blocks.LAVA, "_still")),
                                        blockStateModelGenerator.modelCollector))));
        registerAluminumLeveledCauldron(blockStateModelGenerator, ModBlocks.ALUMINUM_WATER_CAULDRON,
                Blocks.WATER, "_still");
        registerAluminumLeveledCauldron(blockStateModelGenerator,
                ModBlocks.ALUMINUM_POWDER_SNOW_CAULDRON, Blocks.POWDER_SNOW);
        registerAluminumLeveledCauldron(blockStateModelGenerator,
                ModBlocks.ALUMINUM_DEAD_SPORE_CAULDRON, ModBlocks.DEAD_SPORE_BLOCK);
        registerAluminumLeveledCauldron(blockStateModelGenerator,
                ModBlocks.ALUMINUM_VERDANT_SPORE_CAULDRON, ModBlocks.VERDANT_SPORE_BLOCK);
        registerAluminumLeveledCauldron(blockStateModelGenerator,
                ModBlocks.ALUMINUM_CRIMSON_SPORE_CAULDRON, ModBlocks.CRIMSON_SPORE_BLOCK);
        registerAluminumLeveledCauldron(blockStateModelGenerator,
                ModBlocks.ALUMINUM_ZEPHYR_SPORE_CAULDRON, ModBlocks.ZEPHYR_SPORE_BLOCK);
        registerAluminumLeveledCauldron(blockStateModelGenerator,
                ModBlocks.ALUMINUM_SUNLIGHT_SPORE_CAULDRON, ModBlocks.SUNLIGHT_SPORE_BLOCK);
        registerAluminumLeveledCauldron(blockStateModelGenerator,
                ModBlocks.ALUMINUM_ROSEITE_SPORE_CAULDRON, ModBlocks.ROSEITE_SPORE_BLOCK);
        registerAluminumLeveledCauldron(blockStateModelGenerator,
                ModBlocks.ALUMINUM_MIDNIGHT_SPORE_CAULDRON, ModBlocks.MIDNIGHT_SPORE_BLOCK);

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
    }

    private void registerSimpleCrossBlocks(BlockStateModelGenerator blockStateModelGenerator,
            Block[] blocks) {
        for (Block block : blocks) {
            blockStateModelGenerator.registerTintableCross(block, CrossType.NOT_TINTED);
        }
    }

    private void registerUpFacingCrossBlock(BlockStateModelGenerator blockStateModelGenerator,
            Block block) {
        blockStateModelGenerator.registerItemModel(block);
        WeightedVariant weightedVariant = BlockStateModelGenerator.createWeightedVariant(
                Models.CROSS.upload(block, TextureMap.cross(block),
                        blockStateModelGenerator.modelCollector));
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockModelDefinitionCreator.of(block, weightedVariant)
                        .coordinate(
                                BlockStateModelGeneratorAccessor.worldsinger$getUpDefaultFacingVariantMap()));
    }

    private void registerGenericLeveledCauldron(BlockStateModelGenerator blockStateModelGenerator,
            Block cauldronBlock, TextureMap textureMap, Model level1Model, Model level2Model,
            Model level3Model) {
        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockModelDefinitionCreator.of(cauldronBlock)
                        .with(BlockStateVariantMap.models(LeveledCauldronBlock.LEVEL)
                                .register(1, BlockStateModelGenerator.createWeightedVariant(
                                        level1Model.upload(cauldronBlock, "_level1", textureMap,
                                                blockStateModelGenerator.modelCollector)))
                                .register(2, BlockStateModelGenerator.createWeightedVariant(
                                        level2Model.upload(cauldronBlock, "_level2", textureMap,
                                                blockStateModelGenerator.modelCollector)))
                                .register(3, BlockStateModelGenerator.createWeightedVariant(
                                        level3Model.upload(cauldronBlock, "_full", textureMap,
                                                blockStateModelGenerator.modelCollector)))));
    }

    private void registerLeveledCauldron(BlockStateModelGenerator blockStateModelGenerator,
            Block cauldronBlock, Block contentBlock) {

        TextureMap textureMap = TextureMap.cauldron(TextureMap.getId(contentBlock));
        registerGenericLeveledCauldron(blockStateModelGenerator, cauldronBlock, textureMap,
                Models.TEMPLATE_CAULDRON_LEVEL1, Models.TEMPLATE_CAULDRON_LEVEL2,
                Models.TEMPLATE_CAULDRON_FULL);
    }

    private void registerAluminumLeveledCauldron(BlockStateModelGenerator blockStateModelGenerator,
            Block cauldronBlock, Block contentBlock) {
        registerAluminumLeveledCauldron(blockStateModelGenerator, cauldronBlock, contentBlock, "");
    }

    private void registerAluminumLeveledCauldron(BlockStateModelGenerator blockStateModelGenerator,
            Block cauldronBlock, Block contentBlock, String suffix) {
        TextureMap textureMap = ModTextureMaps.aluminumCauldron(
                TextureMap.getSubId(contentBlock, suffix));
        registerGenericLeveledCauldron(blockStateModelGenerator, cauldronBlock, textureMap,
                ModModels.TEMPLATE_ALUMINUM_CAULDRON_LEVEL1,
                ModModels.TEMPLATE_ALUMINUM_CAULDRON_LEVEL2,
                ModModels.TEMPLATE_ALUMINUM_CAULDRON_FULL);
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
                ModItems.ROSEITE_CORE,
                ModItems.MIDNIGHT_CREATURE_SPAWN_EGG
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
        registerBasicItems(itemModelGenerator, new ItemConvertible[] {
                ModBlocks.SMALL_ROSEITE_BUD,
                ModBlocks.MEDIUM_ROSEITE_BUD,
                ModBlocks.LARGE_ROSEITE_BUD,
                ModBlocks.ROSEITE_CLUSTER,
                ModBlocks.TWISTING_VERDANT_VINES,
                ModBlocks.DEAD_TWISTING_VERDANT_VINES,
                ModBlocks.ALUMINUM_SHEET,
                ModBlocks.CRIMSON_SPIKE,
                ModBlocks.DEAD_CRIMSON_SPIKE,
        });

        // Steel armor
        itemModelGenerator.registerArmor(ModItems.STEEL_HELMET, ModEquipmentAssetKeys.STEEL,
                ItemModelGenerator.HELMET_TRIM_ID_PREFIX, false);
        itemModelGenerator.registerArmor(ModItems.STEEL_CHESTPLATE, ModEquipmentAssetKeys.STEEL,
                ItemModelGenerator.CHESTPLATE_TRIM_ID_PREFIX, false);
        itemModelGenerator.registerArmor(ModItems.STEEL_LEGGINGS, ModEquipmentAssetKeys.STEEL,
                ItemModelGenerator.LEGGINGS_TRIM_ID_PREFIX, false);
        itemModelGenerator.registerArmor(ModItems.STEEL_BOOTS, ModEquipmentAssetKeys.STEEL,
                ItemModelGenerator.BOOTS_TRIM_ID_PREFIX, false);
        // TODO: Make sure steel armor entity coreModel is registered properly

        // Tinted
        registerSporeBottle(itemModelGenerator, ModItems.DEAD_SPORES_BOTTLE);
        registerSporeSplashBottle(itemModelGenerator, ModItems.DEAD_SPORES_SPLASH_BOTTLE);
        registerSporeBottle(itemModelGenerator, ModItems.VERDANT_SPORES_BOTTLE);
        registerSporeSplashBottle(itemModelGenerator, ModItems.VERDANT_SPORES_SPLASH_BOTTLE);
        registerSporeBottle(itemModelGenerator, ModItems.CRIMSON_SPORES_BOTTLE);
        registerSporeSplashBottle(itemModelGenerator, ModItems.CRIMSON_SPORES_SPLASH_BOTTLE);
        registerSporeBottle(itemModelGenerator, ModItems.SUNLIGHT_SPORES_BOTTLE);
        registerSporeSplashBottle(itemModelGenerator, ModItems.SUNLIGHT_SPORES_SPLASH_BOTTLE);
        registerSporeBottle(itemModelGenerator, ModItems.ROSEITE_SPORES_BOTTLE);
        registerSporeSplashBottle(itemModelGenerator, ModItems.ROSEITE_SPORES_SPLASH_BOTTLE);
        registerSporeBottle(itemModelGenerator, ModItems.ZEPHYR_SPORES_BOTTLE);
        registerSporeSplashBottle(itemModelGenerator, ModItems.ZEPHYR_SPORES_SPLASH_BOTTLE);
        registerSporeBottle(itemModelGenerator, ModItems.MIDNIGHT_SPORES_BOTTLE);
        registerSporeSplashBottle(itemModelGenerator, ModItems.MIDNIGHT_SPORES_SPLASH_BOTTLE);

        registerCannonball(itemModelGenerator, ModItems.CERAMIC_CANNONBALL);
        registerOverlays(itemModelGenerator);
    }

    private void registerOverlays(ItemModelGenerator itemModelGenerator) {
        for (ItemOverlay itemOverlay : ItemOverlay.VALUES) {
            Identifier id = itemOverlay.getId();
            Models.GENERATED.upload(id, TextureMap.layer0(id), itemModelGenerator.modelCollector);
        }
    }

    // Ugh this is complicated
    private void registerCannonball(ItemModelGenerator itemModelGenerator, Item item) {
        Identifier baseModelId = ModelIds.getItemModelId(item);
        CannonballCore[] possibleCores = CannonballCore.values();
        List<SwitchCase<CannonballCore>> coreEntries = new ArrayList<>(possibleCores.length);
        for (CannonballCore core : possibleCores) {
            Unbaked coreModel;
            Identifier modelIdWithCore = baseModelId.withSuffixedPath("_core_" + core.asString());
            if (core.canHaveFuse()) {
                List<RangeDispatchItemModel.Entry> fuseEntries = new ArrayList<>(
                        CannonballComponent.MAX_FUSE);
                // Generate models with core and fuse
                for (int j = 1; j <= CannonballComponent.MAX_FUSE; ++j) {
                    Identifier modelIdWithCoreAndFuse = modelIdWithCore.withSuffixedPath(
                            "_fuse_" + j);
                    Models.GENERATED_THREE_LAYERS.upload(modelIdWithCoreAndFuse,
                            ModTextureMaps.cannonball(item, core, j),
                            itemModelGenerator.modelCollector);
                    Unbaked model = ItemModels.basic(modelIdWithCoreAndFuse);
                    fuseEntries.add(ItemModels.rangeDispatchEntry(model, j));
                }
                // Generate models with core and no fuse
                Models.GENERATED_THREE_LAYERS.upload(modelIdWithCore,
                        ModTextureMaps.cannonball(item, core, 0),
                        itemModelGenerator.modelCollector);
                Unbaked fallbackModel = ItemModels.basic(modelIdWithCore);
                // Add everything to coreEntries
                coreModel = ItemModels.rangeDispatch(new CannonballFuseProperty(), fallbackModel,
                        fuseEntries);
            } else {
                // Generate coreModel with core
                Models.GENERATED_THREE_LAYERS.upload(modelIdWithCore,
                        ModTextureMaps.cannonball(item, core, 0),
                        itemModelGenerator.modelCollector);
                coreModel = ItemModels.basic(modelIdWithCore);
            }
            coreEntries.add(ItemModels.switchCase(core, coreModel));

        }
        // Generate fallback model
        Models.GENERATED_THREE_LAYERS.upload(baseModelId,
                ModTextureMaps.cannonball(item, CannonballCore.HOLLOW, 0),
                itemModelGenerator.modelCollector);
        Unbaked baseModel = ItemModels.basic(baseModelId);
        // Put it all together!
        Unbaked guiItemModel = ItemModels.select(new CannonballCoreProperty(), baseModel,
                coreEntries);
        itemModelGenerator.output.accept(item,
                ItemModels.select(new DisplayContextProperty(), baseModel,
                        ItemModels.switchCase(ItemDisplayContext.GUI, guiItemModel)));
    }

    private void registerSporeBottle(ItemModelGenerator itemModelGenerator, Item item) {
        // Copying normal potion textures at the moment
        Identifier identifier = itemModelGenerator.uploadTwoLayers(item,
                TextureMap.getSubId(Items.POTION, "_overlay"), TextureMap.getId(Items.POTION));
        itemModelGenerator.output.accept(item,
                ItemModels.tinted(identifier, new SporeBottleTintSource()));
    }

    private void registerSporeSplashBottle(ItemModelGenerator itemModelGenerator, Item item) {
        // Copying normal potion textures at the moment
        Identifier identifier = itemModelGenerator.uploadTwoLayers(item,
                TextureMap.getSubId(Items.POTION, "_overlay"),
                TextureMap.getId(Items.SPLASH_POTION));
        itemModelGenerator.output.accept(item,
                ItemModels.tinted(identifier, new SporeBottleTintSource()));
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

    private void registerBasicItems(ItemModelGenerator itemModelGenerator,
            ItemConvertible[] items) {
        for (ItemConvertible item : items) {
            itemModelGenerator.register(item.asItem());
        }
    }
}
