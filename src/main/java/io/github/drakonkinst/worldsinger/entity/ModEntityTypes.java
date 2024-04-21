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
package io.github.drakonkinst.worldsinger.entity;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightCreatureManager;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensions;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.DimensionType;

@SuppressWarnings("UnqualifiedStaticUsage")
public final class ModEntityTypes {

    public static final EntityType<VerdantSporeGrowthEntity> VERDANT_SPORE_GROWTH = register(
            "verdant_spore_growth",
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, VerdantSporeGrowthEntity::new)
                    .dimensions(EntityDimensions.fixed(0.0f, 0.0f))
                    .trackRangeChunks(0)
                    .build());
    public static final EntityType<CrimsonSporeGrowthEntity> CRIMSON_SPORE_GROWTH = register(
            "crimson_spore_growth",
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, CrimsonSporeGrowthEntity::new)
                    .dimensions(EntityDimensions.fixed(0.0f, 0.0f))
                    .trackRangeChunks(0)
                    .build());
    public static final EntityType<RoseiteSporeGrowthEntity> ROSEITE_SPORE_GROWTH = register(
            "roseite_spore_growth",
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, RoseiteSporeGrowthEntity::new)
                    .dimensions(EntityDimensions.fixed(0.0f, 0.0f))
                    .trackRangeChunks(0)
                    .build());
    public static final EntityType<MidnightSporeGrowthEntity> MIDNIGHT_SPORE_GROWTH = register(
            "midnight_spore_growth",
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, MidnightSporeGrowthEntity::new)
                    .dimensions(EntityDimensions.fixed(0.0f, 0.0f))
                    .trackRangeChunks(0)
                    .build());
    public static final EntityType<ThrownSporeBottleEntity> THROWN_SPORE_BOTTLE = register(
            "spore_bottle", FabricEntityTypeBuilder.<ThrownSporeBottleEntity>create(SpawnGroup.MISC,
                            ThrownSporeBottleEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeChunks(4)
                    .trackedUpdateRate(10)
                    .build());
    public static final EntityType<MidnightCreatureEntity> MIDNIGHT_CREATURE = register(
            "midnight_creature",
            FabricEntityTypeBuilder.<MidnightCreatureEntity>create(SpawnGroup.CREATURE,
                            MidnightCreatureEntity::new)
                    .dimensions(EntityDimensions.changing(0.98f, 0.98f))
                    .build());
    public static final EntityType<RainlineEntity> RAINLINE = register("rainline",
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, RainlineEntity::new)
                    .dimensions(EntityDimensions.fixed(0.0f, 0.0f))
                    .trackRangeChunks(20)
                    .build());

    public static void initialize() {
        // Register attributes
        FabricDefaultAttributeRegistry.register(ModEntityTypes.MIDNIGHT_CREATURE,
                MidnightCreatureManager.createMidnightCreatureAttributes());
    }

    public static boolean canSeagullSpawn(EntityType<ChickenEntity> type, WorldAccess world,
            SpawnReason spawnReason, BlockPos pos, Random random) {
        DimensionType lumarDimension = world.getRegistryManager()
                .get(RegistryKeys.DIMENSION_TYPE)
                .get(ModDimensions.DIMENSION_TYPE_LUMAR);
        if (lumarDimension != null && lumarDimension.equals(world.getDimension())) {
            return world.getBlockState(pos.down()).isIn(ModBlockTags.SEAGULLS_SPAWNABLE_ON)
                    && world.getBaseLightLevel(pos, 0) > 8;
        }
        return AnimalEntity.isValidNaturalSpawn(type, world, spawnReason, pos, random);
    }

    private static <T extends Entity> EntityType<T> register(String id, EntityType<T> entityType) {
        return Registry.register(Registries.ENTITY_TYPE, Worldsinger.id(id), entityType);
    }

    private ModEntityTypes() {}
}
