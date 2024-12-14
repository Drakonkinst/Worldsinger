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
package io.github.drakonkinst.worldsinger.mixin.worldgen;

import io.github.drakonkinst.worldsinger.registry.ModLootTables;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensions;
import java.util.Map;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.ShipwreckGenerator;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShipwreckGenerator.Piece.class)
public abstract class ShipwreckGeneratorPieceMixin {

    @Unique
    private static final Map<String, RegistryKey<LootTable>> LUMAR_LOOT_TABLES = Map.of("map_chest",
            ModLootTables.LUMAR_SHIPWRECK_SPROUTER_CHEST, "treasure_chest",
            ModLootTables.LUMAR_SHIPWRECK_CAPTAIN_CHEST, "supply_chest",
            ModLootTables.LUMAR_SHIPWRECK_SUPPLY_CHEST);

    @Inject(method = "handleMetadata", at = @At("HEAD"), cancellable = true)
    private void injectLumarLootTables(String metadata, BlockPos pos, ServerWorldAccess world,
            Random random, BlockBox boundingBox, CallbackInfo ci) {
        if (world.getDimension()
                .equals(world.getRegistryManager()
                        .getOrThrow(RegistryKeys.DIMENSION_TYPE)
                        .get(ModDimensions.DIMENSION_TYPE_LUMAR))) {
            RegistryKey<LootTable> lootTable = LUMAR_LOOT_TABLES.get(metadata);
            if (lootTable != null) {
                LootableInventory.setLootTable(world, random, pos.down(), lootTable);
            }
            ci.cancel();
        }
    }
}
