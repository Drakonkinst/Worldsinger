package io.github.drakonkinst.worldsinger.entity;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.WaterReactionManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.DeadSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleSpawner;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.item.SporeBottleItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ThrownSporeBottleEntity extends ThrownItemEntity implements FlyingItemEntity {

    private static final int SPORE_AMOUNT = 75;
    private static final int WATER_AMOUNT_PER_LEVEL = 80;

    public ThrownSporeBottleEntity(EntityType<? extends ThrownSporeBottleEntity> entityType,
            World world) {
        super(entityType, world);
    }

    public ThrownSporeBottleEntity(World world, LivingEntity owner) {
        super(ModEntityTypes.THROWN_SPORE_BOTTLE, owner, world);
    }

    public ThrownSporeBottleEntity(World world, double x, double y, double z) {
        super(ModEntityTypes.THROWN_SPORE_BOTTLE, x, y, z, world);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        World world = this.getWorld();
        if (world.isClient()) {
            return;
        }

        Vec3d pos = this.getPos();
        if (world instanceof ServerWorld serverWorld) {
            SporeParticleSpawner.spawnSplashPotionParticles(serverWorld, this.getSporeType(), pos);
        }

        AetherSpores sporeType = this.getSporeType();
        if (!sporeType.isDead()) {
            this.handleLivingSporeBehavior(world, sporeType, pos);
        }

        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                SoundEvents.ENTITY_SPLASH_POTION_BREAK, SoundCategory.NEUTRAL, 1.0f,
                random.nextFloat() * 0.1f + 0.9f, world.getRandom().nextLong());
        this.discard();
    }

    private AetherSpores getSporeType() {
        ItemStack stack = this.getStack();
        if (stack.getItem() instanceof SporeBottleItem sporeBottleItem) {
            return sporeBottleItem.getSporeType();
        }
        Worldsinger.LOGGER.error("Unable to determine spore type for Thrown Spore Bottle Entity");
        return DeadSpores.getInstance();
    }

    private void handleLivingSporeBehavior(World world, AetherSpores sporeType, Vec3d pos) {
        BlockPos blockPos = this.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
            int waterAmount = WaterReactionManager.absorbWaterAndCollectReactives(world, blockPos,
                    null);
            sporeType.doReactionFromSplashBottle(world, pos, SPORE_AMOUNT, waterAmount, random,
                    false);
        } else if (blockState.isOf(Blocks.WATER_CAULDRON)) {
            int waterAmount = WATER_AMOUNT_PER_LEVEL * blockState.get(LeveledCauldronBlock.LEVEL);
            world.setBlockState(blockPos, Blocks.CAULDRON.getStateWithProperties(blockState));
            sporeType.doReactionFromSplashBottle(world, pos, SPORE_AMOUNT, waterAmount, random,
                    true);
        }
    }

    @Override
    protected float getGravity() {
        return 0.05f;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DEAD_SPORES_SPLASH_BOTTLE;
    }
}
