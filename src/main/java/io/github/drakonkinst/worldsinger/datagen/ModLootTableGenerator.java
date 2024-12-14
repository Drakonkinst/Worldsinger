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

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemConvertible;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;

public abstract class ModLootTableGenerator extends SimpleFabricLootTableProvider {

    public static LeafEntry.Builder<?> itemEntry(ItemConvertible item, int minCount, int maxCount) {
        return ItemEntry.builder(item)
                .apply(SetCountLootFunction.builder(
                        UniformLootNumberProvider.create(minCount, maxCount)));
    }

    public static LeafEntry.Builder<?> itemEntry(ItemConvertible item, int count) {
        return ItemEntry.builder(item)
                .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(count)));
    }

    public static LeafEntry.Builder<?> itemEntry(ItemConvertible item) {
        return ItemEntry.builder(item);
    }

    public static RegistryEntry<Enchantment> getEnchantment(WrapperLookup registryLookup,
            RegistryKey<Enchantment> enchantmentKey) {
        RegistryWrapper.Impl<Enchantment> impl = registryLookup.getWrapperOrThrow(
                RegistryKeys.ENCHANTMENT);
        return impl.getOrThrow(enchantmentKey);
    }

    public ModLootTableGenerator(FabricDataOutput output,
            CompletableFuture<WrapperLookup> registryLookup, LootContextType lootContextType) {
        super(output, registryLookup, lootContextType);
    }
}
