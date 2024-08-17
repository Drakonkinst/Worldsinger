/*
 * MIT License
 *
 * Copyright (c) 2024 Drakonkinst
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
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
import io.github.drakonkinst.worldsinger.entity.ModEntityTypes;
import io.github.drakonkinst.worldsinger.entity.VerdantSporeGrowthEntity;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import io.github.drakonkinst.worldsinger.util.EntityUtil;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import io.github.drakonkinst.worldsinger.util.math.Int3;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VerdantSpores extends GrowableAetherSpores<VerdantSporeGrowthEntity> {

    public static final String NAME = "verdant";
    public static final int ID = 1;

    private static final VerdantSpores INSTANCE = new VerdantSpores();
    private static final int COLOR = 0x2e522e;
    private static final int PARTICLE_COLOR = 0x64aa4a;

    public static VerdantSpores getInstance() {
        return INSTANCE;
    }

    private VerdantSpores() {
        super(VerdantSporeGrowthEntity.class);
    }

    @Override
    public int getSmallStage() {
        return VerdantSporeGrowthEntity.MAX_STAGE;
    }

    @Override
    public EntityType<VerdantSporeGrowthEntity> getSporeGrowthEntityType() {
        return ModEntityTypes.VERDANT_SPORE_GROWTH;
    }

    @Override
    public void onDeathFromStatusEffect(World world, LivingEntity entity, BlockPos pos, int water) {
        // Fill with snare blocks
        this.growVerdantSpores(world, entity);

        // Only spawn spore growth if in the spore sea
        if (!world.getFluidState(pos).isIn(ModFluidTags.VERDANT_SPORES)
                && !EntityUtil.isTouchingSporeSea(entity)) {
            return;
        }

        Vec3d startPos = AetherSpores.getTopmostSeaPosForEntity(world, entity,
                ModFluidTags.VERDANT_SPORES);
        this.spawnSporeGrowth(world, startPos, LivingAetherSporeBlock.CATALYZE_VALUE, water, true,
                false, false, Int3.ZERO);
    }

    // Fill entity's bounding box with Verdant Vine Snare
    private void growVerdantSpores(World world, LivingEntity entity) {
        BlockState newBlockState = ModBlocks.VERDANT_VINE_SNARE.getDefaultState();

        for (BlockPos pos : BlockPosUtil.iterateBoundingBoxForEntity(entity)) {
            BlockState blockState = world.getBlockState(pos);
            if (blockState.isIn(ModBlockTags.SPORES_CAN_GROW) && newBlockState.canPlaceAt(world,
                    pos)) {
                world.setBlockState(pos, newBlockState);
            }
        }
    }

    @Override
    public Item getBottledItem() {
        return ModItems.VERDANT_SPORES_BOTTLE;
    }

    @Override
    public Item getBucketItem() {
        return ModItems.VERDANT_SPORES_BUCKET;
    }

    @Override
    public Block getFluidBlock() {
        return ModBlocks.VERDANT_SPORE_SEA;
    }

    @Override
    public Block getSolidBlock() {
        return ModBlocks.VERDANT_SPORE_BLOCK;
    }

    @Override
    public FlowableFluid getFluid() {
        return ModFluids.VERDANT_SPORES;
    }

    @Override
    public RegistryEntry<StatusEffect> getStatusEffect() {
        return ModStatusEffects.VERDANT_SPORES;
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
    public BlockState getFluidCollisionState() {
        return ModBlocks.VERDANT_VINE_BLOCK.getDefaultState().with(ModProperties.CATALYZED, true);
    }
}
