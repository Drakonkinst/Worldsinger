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

package io.github.drakonkinst.worldsinger.datagen.tag;

import io.github.drakonkinst.worldsinger.registry.ModDamageTypes;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.DamageTypeTags;

public class ModDamageTypeTagGenerator extends FabricTagProvider<DamageType> {

    public ModDamageTypeTagGenerator(FabricDataOutput output,
            CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
    }

    @Override
    protected void configure(WrapperLookup wrapperLookup) {
        // Currently does not work with custom solution, so keeping these manual for now
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ARMOR).add(ModDamageTypes.VERDANT_SPORE)
                .add(ModDamageTypes.CRIMSON_SPORE)
                .add(ModDamageTypes.ZEPHYR_SPORE)
                .add(ModDamageTypes.SUNLIGHT)
                .add(ModDamageTypes.ROSEITE_SPORE)
                .add(ModDamageTypes.MIDNIGHT_ESSENCE)
                .add(ModDamageTypes.DROWN_SPORE)
                .add(ModDamageTypes.SPIKE_FALL)
                .add(ModDamageTypes.THIRST);
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_EFFECTS).add(ModDamageTypes.THIRST);
        getOrCreateTagBuilder(DamageTypeTags.IS_DROWNING).add(ModDamageTypes.DROWN_SPORE);
        getOrCreateTagBuilder(DamageTypeTags.IS_EXPLOSION).add(ModDamageTypes.ZEPHYR_SPORE);
        getOrCreateTagBuilder(DamageTypeTags.NO_KNOCKBACK).add(ModDamageTypes.ZEPHYR_SPORE);
        getOrCreateTagBuilder(DamageTypeTags.IS_FALL).add(ModDamageTypes.SPIKE_FALL);
        getOrCreateTagBuilder(DamageTypeTags.IS_FIRE).add(ModDamageTypes.SUNLIGHT)
                .add(ModDamageTypes.SUNLIGHT_SPORE);
        getOrCreateTagBuilder(DamageTypeTags.NO_IMPACT).add(ModDamageTypes.VERDANT_SPORE)
                .add(ModDamageTypes.CRIMSON_SPORE)
                .add(ModDamageTypes.ZEPHYR_SPORE)
                .add(ModDamageTypes.SUNLIGHT)
                .add(ModDamageTypes.ROSEITE_SPORE)
                .add(ModDamageTypes.MIDNIGHT_ESSENCE)
                .add(ModDamageTypes.DROWN_SPORE)
                .add(ModDamageTypes.THIRST);
    }
}
