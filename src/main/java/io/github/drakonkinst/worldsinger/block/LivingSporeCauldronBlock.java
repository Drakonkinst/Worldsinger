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
import io.github.drakonkinst.worldsinger.api.VariantApi;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.cauldron.CauldronBehavior.CauldronBehaviorMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class LivingSporeCauldronBlock extends SporeCauldronBlock implements SporeKillable,
        WaterReactiveBlock {

    // Unused Codec
    public static final MapCodec<LivingSporeCauldronBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(LeveledCauldronBlock.createSettingsCodec(),
                            CauldronBehavior.CODEC.fieldOf("interactions")
                                    .forGetter(block -> block.behaviorMap),
                            AetherSpores.CODEC.fieldOf("sporeType")
                                    .forGetter(LivingSporeCauldronBlock::getSporeType))
                    .apply(instance,
                            (settings1, behaviorMap1, sporeType1) -> new LivingSporeCauldronBlock(
                                    behaviorMap1, sporeType1, settings1)));
    private static final int CATALYZE_VALUE_PER_LEVEL = 80;

    public LivingSporeCauldronBlock(CauldronBehaviorMap behaviorMap, AetherSpores sporeType,
            Settings settings) {
        super(behaviorMap, sporeType, settings);
    }

    @Override
    public Block getDeadSporeBlock() {
        return ModBlocks.DEAD_SPORE_CAULDRON;
    }

    @Override
    public boolean canReactToWater(BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        if (world.hasRain(pos.up())) {
            this.reactToWater(world, pos, state, Integer.MAX_VALUE, random);
        }
    }

    @Override
    public boolean reactToWater(World world, BlockPos pos, BlockState state, int waterAmount,
            Random random) {
        BlockPos posAbove = pos.up();
        BlockState stateAbove = world.getBlockState(posAbove);
        if (!stateAbove.isIn(ModBlockTags.SPORES_CAN_GROW) && !stateAbove.isIn(
                ModBlockTags.SPORES_CAN_BREAK)) {
            return false;
        }
        Block emptyCauldronBlock = VariantApi.getBlockVariant(state.getBlock(), Blocks.CAULDRON)
                .orElse(Blocks.CAULDRON);
        world.setBlockState(pos, emptyCauldronBlock.getStateWithProperties(state));
        int catalyzeValue = CATALYZE_VALUE_PER_LEVEL * state.get(LEVEL);
        sporeType.doReactionFromFluidContainer(world, pos, catalyzeValue, waterAmount, random);
        return true;
    }

    @Override
    public Type getReactiveType() {
        return AetherSpores.getReactiveTypeFromSpore(sporeType);
    }

    @Override
    public boolean isSporeKillable(World world, BlockPos pos, BlockState state) {
        return !state.isIn(ModBlockTags.HAS_ALUMINUM);
    }
}
