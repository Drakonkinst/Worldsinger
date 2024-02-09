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
import io.github.drakonkinst.worldsinger.api.fluid.CauldronVariantBlock;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleSpawner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.cauldron.CauldronBehavior.CauldronBehaviorMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome.Precipitation;

public class SporeCauldronBlock extends LeveledCauldronBlock implements SporeEmitting,
        CauldronVariantBlock {

    // Unused Codec
    public static final MapCodec<SporeCauldronBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(LeveledCauldronBlock.createSettingsCodec(),
                            CauldronBehavior.CODEC.fieldOf("interactions")
                                    .forGetter(block -> block.behaviorMap),
                            AetherSpores.CODEC.fieldOf("sporeType")
                                    .forGetter(SporeCauldronBlock::getSporeType),
                            Block.CODEC.fieldOf("baseBlock")
                                    .forGetter(SporeCauldronBlock::worldsinger$getBaseBlock))
                    .apply(instance, SporeCauldronBlock::new));

    protected final AetherSpores sporeType;
    protected final Block baseCauldronBlock;

    public SporeCauldronBlock(Settings settings, CauldronBehaviorMap behaviorMap,
            AetherSpores sporeType, Block baseCauldronBlock) {
        super(Precipitation.NONE, behaviorMap, settings);
        this.baseCauldronBlock = baseCauldronBlock;
        this.sporeType = sporeType;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient() && this.isEntityTouchingFluid(state, pos, entity)) {
            if (entity.isOnFire()) {
                entity.extinguish();
            }
        }
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity,
            float fallDistance) {
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
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return Items.CAULDRON.getDefaultStack();
    }

    @Override
    public AetherSpores getSporeType() {
        return sporeType;
    }

    @Override
    public Block worldsinger$getBaseBlock() {
        return baseCauldronBlock;
    }

    @Override
    public void worldsinger$setBaseBlock(Block block) {
        throw new UnsupportedOperationException();
    }

    // @Override
    // public MapCodec<LeveledCauldronBlock> getCodec() {
    //     return super.getCodec();
    // }
}
