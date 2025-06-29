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
import io.github.drakonkinst.worldsinger.registry.ModDamageTypes;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class ModDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        FabricDataGenerator.Pack pack = dataGenerator.createPack();
        pack.addProvider(ModModelGenerator::new);
        pack.addProvider(ModRecipeGenerator::new);
        pack.addProvider(ModBlockLootTableGenerator::new);
        pack.addProvider(ModChestLootTableGenerator::new);
        ModBlockTagGenerator blockTagGenerator = pack.addProvider(ModBlockTagGenerator::new);
        pack.addProvider((output, registries) -> new ModItemTagGenerator(output, registries,
                blockTagGenerator));
        pack.addProvider(ModFluidTagGenerator::new);
        pack.addProvider(ModEntityTagGenerator::new);
        pack.addProvider(ModDamageTypeGenerator::new);
        pack.addProvider(ModDamageTypeTagGenerator::new);
        pack.addProvider(ModBiomeTagGenerator::new);
        pack.addProvider(ModAdvancementGenerator::new);
    }

    @Override
    public void buildRegistry(RegistryBuilder registryBuilder) {
        registryBuilder.addRegistry(RegistryKeys.DAMAGE_TYPE, ModDamageTypes::generateTypes);
    }
}
