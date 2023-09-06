package io.github.drakonkinst.worldsinger.effect;

import io.github.drakonkinst.worldsinger.block.LivingAetherSporeBlock;
import io.github.drakonkinst.worldsinger.block.ModBlockTags;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.block.SporeEmitting;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import io.github.drakonkinst.worldsinger.world.lumar.AetherSporeType;
import io.github.drakonkinst.worldsinger.world.lumar.SporeGrowthSpawner;
import io.github.drakonkinst.worldsinger.world.lumar.SporeParticleManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class SporeStatusEffect extends StatusEffect implements SporeEmitting {

    public static final float DEFAULT_DAMAGE = 15.0f;
    private static final int WATER_PER_ENTITY_BLOCK = 75;
    private final AetherSporeType aetherSporeType;
    private final float damage;

    protected SporeStatusEffect(AetherSporeType aetherSporeType) {
        this(aetherSporeType, DEFAULT_DAMAGE);
    }

    protected SporeStatusEffect(AetherSporeType aetherSporeType, float damage) {
        super(StatusEffectCategory.HARMFUL, aetherSporeType.getColor());
        this.aetherSporeType = aetherSporeType;
        this.damage = damage;
    }

    // Fill entity's bounding box with snares
    private static void growVerdantSpores(World world, LivingEntity entity) {
        BlockState newBlockState = ModBlocks.VERDANT_VINE_SNARE.getDefaultState()
                .with(Properties.PERSISTENT, false);

        for (BlockPos pos : BlockPosUtil.iterateBoundingBoxForEntity(entity)) {
            BlockState blockState = world.getBlockState(pos);
            if (blockState.isIn(ModBlockTags.SPORES_CAN_GROW) && newBlockState.canPlaceAt(world,
                    pos)) {
                world.setBlockState(pos, newBlockState);
            }
        }
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!SporeParticleManager.sporesCanAffect(entity)) {
            return;
        }
        boolean wasDamaged = entity.damage(entity.getDamageSources().drown(), damage);
        if (wasDamaged && entity.isDead()) {
            onDeathEffect(entity);
        }
    }

    private void onDeathEffect(LivingEntity entity) {
        World world = entity.getWorld();
        if (aetherSporeType == AetherSporeType.VERDANT) {
            SporeStatusEffect.growVerdantSpores(world, entity);

            // Do a little hack to move spore growth position to the topmost block
            int waterAmount = MathHelper.ceil(
                    entity.getWidth() * entity.getHeight() * WATER_PER_ENTITY_BLOCK);
            BlockPos.Mutable mutable = entity.getBlockPos().mutableCopy();

            do {
                mutable.move(Direction.UP);
            }
            while (world.getFluidState(mutable).isIn(ModFluidTags.VERDANT_SPORES)
                    && mutable.getY() < world.getTopY());

            if (world.getBlockState(mutable).isAir()) {
                // Found a good position, use it
                SporeGrowthSpawner.spawnVerdantSporeGrowth(world,
                        mutable.move(Direction.DOWN).toCenterPos(),
                        LivingAetherSporeBlock.CATALYZE_VALUE, waterAmount, true, false, false);
            } else {
                // Use original position
                SporeGrowthSpawner.spawnVerdantSporeGrowth(world, entity.getPos(),
                        LivingAetherSporeBlock.CATALYZE_VALUE, waterAmount, true, false, false);
            }
        }
    }

    public AetherSporeType getSporeType() {
        return aetherSporeType;
    }
}
