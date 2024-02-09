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
package io.github.drakonkinst.worldsinger.api;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.api.fluid.FluidVariantApi;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.item.SilverLinedBoatItemData;
import java.util.HashMap;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;

public class ModApi {

    public static final ItemApiLookup<SilverLined, Void> SILVER_LINED_ITEM = ItemApiLookup.get(
            Worldsinger.id("silver_lined"), SilverLined.class, Void.class);

    public static void initialize() {
        ModApi.SILVER_LINED_ITEM.registerForItems(
                (stack, context) -> new SilverLinedBoatItemData(stack), Items.ACACIA_BOAT,
                Items.BIRCH_BOAT, Items.CHERRY_BOAT, Items.DARK_OAK_BOAT, Items.JUNGLE_BOAT,
                Items.MANGROVE_BOAT, Items.OAK_BOAT, Items.SPRUCE_BOAT, Items.BAMBOO_RAFT,
                Items.ACACIA_CHEST_BOAT, Items.BIRCH_CHEST_BOAT, Items.CHERRY_CHEST_BOAT,
                Items.DARK_OAK_CHEST_BOAT, Items.JUNGLE_CHEST_BOAT, Items.MANGROVE_CHEST_BOAT,
                Items.OAK_CHEST_BOAT, Items.SPRUCE_CHEST_BOAT, Items.BAMBOO_CHEST_RAFT);
        FluidVariantApi.registerCauldronVariants(ModBlocks.ALUMINUM_CAULDRON, new HashMap<>() {{
            put(Blocks.CAULDRON, ModBlocks.ALUMINUM_CAULDRON);
            put(Blocks.WATER_CAULDRON, ModBlocks.ALUMINUM_WATER_CAULDRON);
            put(Blocks.LAVA_CAULDRON, ModBlocks.ALUMINUM_LAVA_CAULDRON);
            put(Blocks.POWDER_SNOW_CAULDRON, ModBlocks.ALUMINUM_POWDER_SNOW_CAULDRON);
            put(ModBlocks.DEAD_SPORE_CAULDRON, ModBlocks.ALUMINUM_DEAD_SPORE_CAULDRON);
            put(ModBlocks.VERDANT_SPORE_CAULDRON, ModBlocks.ALUMINUM_VERDANT_SPORE_CAULDRON);
            put(ModBlocks.CRIMSON_SPORE_CAULDRON, ModBlocks.ALUMINUM_CRIMSON_SPORE_CAULDRON);
            put(ModBlocks.ZEPHYR_SPORE_CAULDRON, ModBlocks.ALUMINUM_ZEPHYR_SPORE_CAULDRON);
            put(ModBlocks.SUNLIGHT_SPORE_CAULDRON, ModBlocks.ALUMINUM_SUNLIGHT_SPORE_CAULDRON);
            put(ModBlocks.ROSEITE_SPORE_CAULDRON, ModBlocks.ALUMINUM_ROSEITE_SPORE_CAULDRON);
            put(ModBlocks.MIDNIGHT_SPORE_CAULDRON, ModBlocks.ALUMINUM_MIDNIGHT_SPORE_CAULDRON);
        }});
    }
}
