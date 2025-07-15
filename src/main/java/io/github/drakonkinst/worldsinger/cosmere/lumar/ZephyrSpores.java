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

import io.github.drakonkinst.worldsinger.block.LivingAetherSporeBlock;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.effect.ModStatusEffects;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.mixin.accessor.WindChargeEntityAccessor;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ZephyrSpores extends AetherSpores {

    public static final String NAME = "zephyr";
    public static final int ID = 4;

    private static final ZephyrSpores INSTANCE = new ZephyrSpores();
    private static final int COLOR = 0x4b9bb7;
    private static final int PARTICLE_COLOR = 0x64bdde;

    private static final float SPORE_TO_POWER_MULTIPLIER = 0.15f;

    public static ZephyrSpores getInstance() {
        return INSTANCE;
    }

    private ZephyrSpores() {}

    @Override
    public void onDeathFromStatusEffect(World world, LivingEntity entity, BlockPos pos, int water) {
        Vec3d startPos = AetherSpores.getTopmostSeaPosForEntity(world, entity,
                ModFluidTags.ZEPHYR_SPORES);
        Vec3d adjustedStartPos = new Vec3d(startPos.getX(), Math.ceil(startPos.getY()),
                startPos.getZ());
        this.doReaction(world, adjustedStartPos, LivingAetherSporeBlock.CATALYZE_VALUE, water,
                world.getRandom());
    }

    @Override
    public void doReaction(World world, Vec3d pos, int spores, int water, Random random) {
        float power = Math.min(spores, water) * SPORE_TO_POWER_MULTIPLIER + random.nextFloat();
        // Push the explosion to the top of the block, so that it is not obstructed by itself
        Vec3d centerPos = new Vec3d(pos.getX(), Math.ceil(pos.getY()), pos.getZ());

        world.createExplosion(null, null, WindChargeEntityAccessor.getExplosionBehavior(),
                centerPos.getX(), centerPos.getY(), centerPos.getZ(), power, false,
                World.ExplosionSourceType.TRIGGER, ParticleTypes.GUST_EMITTER_SMALL,
                ParticleTypes.GUST_EMITTER_LARGE, SoundEvents.ENTITY_WIND_CHARGE_WIND_BURST);

        // Also spawn some spore particles
        if (world instanceof ServerWorld serverWorld) {
            SporeParticleSpawner.spawnBlockParticles(serverWorld, ZephyrSpores.getInstance(),
                    BlockPosUtil.toBlockPos(pos), 1, 1);
        }
    }

    @Override
    public Item getBottledItem() {
        return ModItems.ZEPHYR_SPORES_BOTTLE;
    }

    @Override
    public Item getBucketItem() {
        return ModItems.ZEPHYR_SPORES_BUCKET;
    }

    @Override
    public Block getFluidBlock() {
        return ModBlocks.ZEPHYR_SPORE_SEA;
    }

    @Override
    public Block getSolidBlock() {
        return ModBlocks.ZEPHYR_SPORE_BLOCK;
    }

    @Override
    public FlowableFluid getFluid() {
        return ModFluids.ZEPHYR_SPORES;
    }

    @Override
    public RegistryEntry<StatusEffect> getStatusEffect() {
        return ModStatusEffects.ZEPHYR_SPORES;
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

    @Override
    public @Nullable BlockState getFluidCollisionState() {
        return Blocks.AIR.getDefaultState();
    }
}
