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

import io.github.drakonkinst.worldsinger.datagen.recipe.ModRecipeGenerator;
import io.github.drakonkinst.worldsinger.datagen.tag.ModBiomeTagGenerator;
import io.github.drakonkinst.worldsinger.datagen.tag.ModBlockTagGenerator;
import io.github.drakonkinst.worldsinger.datagen.tag.ModDamageTypeTagGenerator;
import io.github.drakonkinst.worldsinger.datagen.tag.ModEntityTagGenerator;
import io.github.drakonkinst.worldsinger.datagen.tag.ModFluidTagGenerator;
import io.github.drakonkinst.worldsinger.datagen.tag.ModItemTagGenerator;
import io.github.drakonkinst.worldsinger.dialog.ModDialogs;
import io.github.drakonkinst.worldsinger.registry.ModDamageTypes;
import io.github.drakonkinst.worldsinger.worldgen.biome.ModBiomes;
import io.github.drakonkinst.worldsinger.worldgen.carver.ModConfiguredCarvers;
import io.github.drakonkinst.worldsinger.worldgen.feature.ModConfiguredFeatures;
import io.github.drakonkinst.worldsinger.worldgen.feature.ModPlacedFeatures;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class ModDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        FabricDataGenerator.Pack pack = dataGenerator.createPack();
        // Core Registries
        pack.addProvider(ModModelGenerator::new);
        pack.addProvider(ModRecipeGenerator::new);
        pack.addProvider(ModBlockLootTableGenerator::new);
        pack.addProvider(ModChestLootTableGenerator::new);
        pack.addProvider(ModAdvancementGenerator::new);

        // Dynamic Registries
        addDynamicProvider(pack, "Worldsinger Damage Types", RegistryKeys.DAMAGE_TYPE);
        addDynamicProvider(pack, "Worldsinger Configured Carvers", RegistryKeys.CONFIGURED_CARVER);
        addDynamicProvider(pack, "Worldsinger Configured Features",
                RegistryKeys.CONFIGURED_FEATURE);
        addDynamicProvider(pack, "Worldsinger Placed Features", RegistryKeys.PLACED_FEATURE);
        addDynamicProvider(pack, "Worldsinger Biomes", RegistryKeys.BIOME);
        addDynamicProvider(pack, "Worldsinger Dialogs", RegistryKeys.DIALOG);

        // Tags
        ModBlockTagGenerator blockTagGenerator = pack.addProvider(ModBlockTagGenerator::new);
        pack.addProvider((output, registries) -> new ModItemTagGenerator(output, registries,
                blockTagGenerator));
        pack.addProvider(ModFluidTagGenerator::new);
        pack.addProvider(ModEntityTagGenerator::new);
        pack.addProvider(ModDamageTypeTagGenerator::new);
        pack.addProvider(ModBiomeTagGenerator::new);
    }

    private void addDynamicProvider(FabricDataGenerator.Pack pack, String id,
            RegistryKey<? extends Registry<?>> registryKey) {
        pack.addProvider((output, registriesFuture) -> new ModDynamicRegistryGenerator(output,
                registriesFuture, id, registryKey));
    }

    @Override
    public void buildRegistry(RegistryBuilder registryBuilder) {
        registryBuilder.addRegistry(RegistryKeys.DIALOG, ModDialogs::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.DAMAGE_TYPE, ModDamageTypes::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.CONFIGURED_CARVER,
                ModConfiguredCarvers::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE,
                ModConfiguredFeatures::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, ModPlacedFeatures::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.BIOME, ModBiomes::bootstrap);
    }
}
