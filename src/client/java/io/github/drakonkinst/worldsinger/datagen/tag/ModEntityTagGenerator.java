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

package io.github.drakonkinst.worldsinger.datagen.tag;

import io.github.drakonkinst.worldsinger.entity.ModEntityTypeTags;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypes;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.EntityTypeTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.EntityTypeTags;

public class ModEntityTagGenerator extends EntityTypeTagProvider {

    public ModEntityTagGenerator(FabricDataOutput output,
            CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(WrapperLookup wrapperLookup) {
        valueLookupBuilder(ModEntityTypeTags.HAS_IRON).add(EntityType.IRON_GOLEM)
                .addOptionalTag(ConventionalEntityTypeTags.MINECARTS);
        valueLookupBuilder(ModEntityTypeTags.HAS_STEEL);
        valueLookupBuilder(ModEntityTypeTags.SPORE_GROWTHS).add(ModEntityTypes.CRIMSON_SPORE_GROWTH)
                .add(ModEntityTypes.VERDANT_SPORE_GROWTH)
                .add(ModEntityTypes.ROSEITE_SPORE_GROWTH)
                .add(ModEntityTypes.MIDNIGHT_SPORE_GROWTH);
        // Blacklist for what entities the Midnight Creature is allowed to imitate. Creatures that do not extend LivingEntity are automatically excluded.
        valueLookupBuilder(ModEntityTypeTags.MIDNIGHT_CREATURES_CANNOT_IMITATE).add(
                        EntityType.ENDER_DRAGON)
                .add(EntityType.GHAST)
                .add(EntityType.HAPPY_GHAST)
                .add(EntityType.WITHER)
                .add(ModEntityTypes.MIDNIGHT_CREATURE);
        // Mobs that are covered in or made of water-based materials, and thus catalyze spores on contact.
        valueLookupBuilder(ModEntityTypeTags.SPORES_ALWAYS_AFFECT).add(EntityType.SNOW_GOLEM)
                .add(EntityType.DROWNED);
        // Mobs that do not consist of anything water-based and thus do not catalyze spores
        valueLookupBuilder(ModEntityTypeTags.SPORES_NEVER_AFFECT).add(EntityType.IRON_GOLEM)
                .add(EntityType.ALLAY)
                .add(EntityType.VEX)
                .add(EntityType.BLAZE)
                .add(EntityType.SHULKER)
                .addOptionalTag(EntityTypeTags.SKELETONS)
                .add(EntityType.BOGGED)
                .add(EntityType.WITHER)
                .add(ModEntityTypes.MIDNIGHT_CREATURE)
                .addOptionalTag(ModEntityTypeTags.SPORE_GROWTHS);
        // Mobs that will not be suffocated by spores
        valueLookupBuilder(ModEntityTypeTags.SPORES_NEVER_SUFFOCATE).add(
                ModEntityTypes.MIDNIGHT_CREATURE);
    }
}
