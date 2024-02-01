package io.github.drakonkinst.worldsinger.entity;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.block.ModBlockTags;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightCreatureManager;
import io.github.drakonkinst.worldsinger.mixin.accessor.SpawnRestrictionAccessor;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensionTypes;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction.Entry;
import net.minecraft.entity.SpawnRestriction.Location;
import net.minecraft.entity.SpawnRestriction.SpawnPredicate;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
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

    public static void initialize() {
        // Overwrite chicken spawn restriction
        // TODO: We're using chickens as a long-term stand-in for seagulls.
        // Once we add a seagull mob sometime in the future, all this can be removed--along with
        // the mixins and access wideners that helped make this happen.
        // This is bad bad code and I'm sorry
        SpawnRestrictionAccessor.worldsinger$getSpawnRestrictionsMap()
                .put(EntityType.CHICKEN,
                        new Entry(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Location.ON_GROUND,
                                (SpawnPredicate<ChickenEntity>) ModEntityTypes::canChickensSpawn));

        // Register attributes
        FabricDefaultAttributeRegistry.register(ModEntityTypes.MIDNIGHT_CREATURE,
                MidnightCreatureManager.createMidnightCreatureAttributes());
    }

    private static boolean canChickensSpawn(EntityType<ChickenEntity> type, WorldAccess world,
            SpawnReason spawnReason, BlockPos pos, Random random) {
        DimensionType lumarDimension = world.getRegistryManager()
                .get(RegistryKeys.DIMENSION_TYPE)
                .get(ModDimensionTypes.LUMAR);
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
