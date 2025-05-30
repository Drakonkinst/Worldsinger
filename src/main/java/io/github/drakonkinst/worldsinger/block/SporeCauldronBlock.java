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
package io.github.drakonkinst.worldsinger.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleSpawner;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.cauldron.CauldronBehavior.CauldronBehaviorMap;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.Precipitation;

public class SporeCauldronBlock extends LeveledCauldronBlock implements SporeEmitting {

    // Unused Codec
    public static final MapCodec<SporeCauldronBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(LeveledCauldronBlock.createSettingsCodec(),
                            CauldronBehavior.CODEC.fieldOf("interactions")
                                    .forGetter(block -> block.behaviorMap),
                            AetherSpores.CODEC.fieldOf("sporeType")
                                    .forGetter(SporeCauldronBlock::getSporeType))
                    .apply(instance,
                            (settings1, behaviorMap1, sporeType1) -> new SporeCauldronBlock(
                                    behaviorMap1, sporeType1, settings1)));

    protected final AetherSpores sporeType;

    public SporeCauldronBlock(CauldronBehaviorMap behaviorMap, AetherSpores sporeType,
            Settings settings) {
        super(Precipitation.NONE, behaviorMap, settings);
        this.sporeType = sporeType;
    }

    @Override
    protected boolean canBeFilledByDripstone(Fluid fluid) {
        return false;
    }

    protected boolean isEntityTouchingFluid(BlockState state, BlockPos pos, Entity entity) {
        return entity.getY() < (double) pos.getY() + this.getFluidHeight(state)
                && entity.getBoundingBox().maxY > (double) pos.getY() + 0.25D;
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity,
            double fallDistance) {
        super.onLandedUpon(world, state, pos, entity, fallDistance);
        if (!world.isClient() && this.isEntityTouchingFluid(state, pos, entity)) {
            if (world instanceof ServerWorld serverWorld) {
                SporeParticleSpawner.spawnBlockParticles(serverWorld, sporeType, pos, 0.6,
                        Math.min(fallDistance, 3.0));
                LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
            }
        }
    }

    @Override
    public AetherSpores getSporeType() {
        return sporeType;
    }

    // @Override
    // public MapCodec<LeveledCauldronBlock> getCodec() {
    //     return super.getCodec();
    // }
}
