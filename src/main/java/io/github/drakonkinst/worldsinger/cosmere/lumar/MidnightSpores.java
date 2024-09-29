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
package io.github.drakonkinst.worldsinger.cosmere.lumar;

import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.effect.ModStatusEffects;
import io.github.drakonkinst.worldsinger.entity.MidnightSporeGrowthEntity;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypes;
import io.github.drakonkinst.worldsinger.entity.cannonball.CannonballBehavior;
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.particle.ModParticleTypes;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import io.github.drakonkinst.worldsinger.util.BoxUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class MidnightSpores extends GrowableAetherSpores<MidnightSporeGrowthEntity> {

    public static final String NAME = "midnight";
    public static final int ID = 6;

    private static final MidnightSpores INSTANCE = new MidnightSpores();
    private static final int COLOR = 0x111111;
    private static final int PARTICLE_COLOR = 0x111111;

    public static MidnightSpores getInstance() {
        return INSTANCE;
    }

    // Client-side only
    public static void addMidnightParticle(WorldAccess world, Box box, Random random,
            double velocity) {
        Vec3d pos = BoxUtil.getRandomPointInBox(box, random);
        double velocityX = velocity * random.nextGaussian();
        double velocityY = velocity * random.nextGaussian();
        double velocityZ = velocity * random.nextGaussian();
        world.addParticle(ModParticleTypes.MIDNIGHT_ESSENCE, pos.getX(), pos.getY(), pos.getZ(),
                velocityX, velocityY, velocityZ);
    }

    // Client-side only
    public static void addMidnightParticles(WorldAccess world, Entity entity, Random random,
            double velocity, int count) {
        Box boundingBox = entity.getBoundingBox();
        MidnightSpores.addMidnightParticles(world, boundingBox, random, velocity, count);
    }

    // Client-side only
    public static void addMidnightParticles(WorldAccess world, Box box, Random random,
            double velocity, int count) {
        for (int i = 0; i < count; ++i) {
            MidnightSpores.addMidnightParticle(world, box, random, velocity);
        }
    }

    private MidnightSpores() {
        super(MidnightSporeGrowthEntity.class);
    }

    @Override
    public EntityType<MidnightSporeGrowthEntity> getSporeGrowthEntityType() {
        return ModEntityTypes.MIDNIGHT_SPORE_GROWTH;
    }

    @Override
    public int getSmallStage() {
        return 0;
    }

    @Override
    public void doReactionFromParticle(World world, Vec3d pos, int spores, int water, Random random,
            boolean affectingFluidContainer) {
        if (spores >= CannonballBehavior.SPORE_AMOUNT) {
            // Normal behavior
            super.doReactionFromParticle(world, pos, spores, water, random,
                    affectingFluidContainer);
        } else {
            // Only spawn a single Midnight Essence from a Midnight Splash Bottle
            // TODO: Does this work for waterlogged blocks etc? Should we just spawn a small growth?
            BlockPos blockPos = BlockPosUtil.toBlockPos(pos);
            if (affectingFluidContainer) {
                blockPos = blockPos.up();
            }
            if (world.getBlockState(blockPos).isAir()) {
                world.setBlockState(blockPos, ModBlocks.MIDNIGHT_ESSENCE.getDefaultState());
            }
        }
    }

    @Override
    public BlockState getFluidCollisionState() {
        return ModBlocks.MIDNIGHT_ESSENCE.getDefaultState();
    }

    @Override
    public void onDeathFromStatusEffect(World world, LivingEntity entity, BlockPos pos, int water) {
        // Status effects are only applied server-side, and synced to client
        // So this is only called server-side
        if (world instanceof ServerWorld serverWorld) {
            Vec3d centerPos = pos.toCenterPos();
            serverWorld.spawnParticles(ModParticleTypes.MIDNIGHT_ESSENCE, centerPos.getX(),
                    centerPos.getY(), centerPos.getZ(), 20, 0.0, 0.0, 0.0, 0.5);

        }
    }

    @Override
    public Item getBottledItem() {
        return ModItems.MIDNIGHT_SPORES_BOTTLE;
    }

    @Override
    public Item getBucketItem() {
        return ModItems.MIDNIGHT_SPORES_BUCKET;
    }

    @Override
    public Block getFluidBlock() {
        return ModBlocks.MIDNIGHT_SPORE_SEA;
    }

    @Override
    public Block getSolidBlock() {
        return ModBlocks.MIDNIGHT_SPORE_BLOCK;
    }

    @Override
    public FlowableFluid getFluid() {
        return ModFluids.MIDNIGHT_SPORES;
    }

    @Override
    public RegistryEntry<StatusEffect> getStatusEffect() {
        return ModStatusEffects.MIDNIGHT_SPORES;
    }

    @Override
    public int getColor() {
        return COLOR;
    }

    @Override
    public int getParticleColor() {
        return PARTICLE_COLOR;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getId() {
        return ID;
    }
}
