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

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

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

        addPottedPlantDrops(ModBlocks.POTTED_DEAD_TWISTING_VERDANT_VINES);
        addPottedPlantDrops(ModBlocks.POTTED_DEAD_VERDANT_VINE_SNARE);
        addPottedPlantDrops(ModBlocks.POTTED_TWISTING_VERDANT_VINES);
        addPottedPlantDrops(ModBlocks.POTTED_VERDANT_VINE_SNARE);
    }
}
