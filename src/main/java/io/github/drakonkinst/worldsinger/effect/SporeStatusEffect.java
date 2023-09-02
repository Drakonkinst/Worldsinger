package io.github.drakonkinst.worldsinger.effect;

import io.github.drakonkinst.worldsinger.block.ModBlockTags;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.block.SporeEmitting;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import io.github.drakonkinst.worldsinger.world.lumar.AetherSporeType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SporeStatusEffect extends StatusEffect implements SporeEmitting {

    public static final float DEFAULT_DAMAGE = 15.0f;
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

    private static BlockPos toRoundedBlockPos(Vec3d pos) {
        int x = MathHelper.floor(pos.getX());
        int y = (int) Math.round(pos.getY());
        int z = MathHelper.floor(pos.getZ());
        return new BlockPos(x, y, z);
    }

    // Iterates over all blocks in an entity's bounding box. Number of blocks iterated is consistent
    // per entity type, regardless of position. Starts from the minimum y (entity's position).
    private static Iterable<BlockPos> iterateBoundingBoxForEntity(LivingEntity entity) {
        BlockPos blockPos = toRoundedBlockPos(entity.getPos());
        int width = MathHelper.ceil(entity.getWidth());
        int height = MathHelper.ceil(entity.getHeight());
        ModConstants.LOGGER.info(
                entity.getType().getUntranslatedName() + " has dimensions " + width + " x "
                        + height);

        int maxY = blockPos.getY() + height - 1;
        int minX = blockPos.getX() - (width / 2);
        int maxX = blockPos.getX() + ((width - 1) / 2);
        int minZ = blockPos.getZ() - (width / 2);
        int maxZ = blockPos.getZ() + ((width - 1) / 2);
        return BlockPos.iterate(minX, blockPos.getY(), minZ, maxX, maxY, maxZ);
    }

    // Fill entity's bounding box with snares
    private static void growVerdantSpores(World world, LivingEntity entity) {
        BlockState newBlockState = ModBlocks.VERDANT_VINE_SNARE.getDefaultState()
                .with(Properties.PERSISTENT, false);

        for (BlockPos pos : SporeStatusEffect.iterateBoundingBoxForEntity(entity)) {
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
        boolean wasDamaged = entity.damage(entity.getDamageSources().drown(), damage);
        if (wasDamaged && entity.isDead()) {
            onDeathEffect(entity);
        }
    }

    private void onDeathEffect(LivingEntity entity) {
        World world = entity.getWorld();
        if (aetherSporeType == AetherSporeType.VERDANT) {
            SporeStatusEffect.growVerdantSpores(world, entity);
        }
    }

    public AetherSporeType getSporeType() {
        return aetherSporeType;
    }
}