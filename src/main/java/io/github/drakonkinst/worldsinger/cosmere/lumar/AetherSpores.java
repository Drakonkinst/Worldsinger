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

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import io.github.drakonkinst.worldsinger.block.SporeEmitting;
import io.github.drakonkinst.worldsinger.cosmere.WaterReactionManager;
import io.github.drakonkinst.worldsinger.cosmere.WaterReactive;
import io.github.drakonkinst.worldsinger.effect.ModStatusEffects;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypeTags;
import io.github.drakonkinst.worldsinger.fluid.AetherSporeFluid;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.item.SporeBottleItem;
import io.github.drakonkinst.worldsinger.registry.ModDamageTypes;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import io.github.drakonkinst.worldsinger.util.EntityUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AetherSpores implements StringIdentifiable, Comparable<AetherSpores> {

    private static final Map<String, AetherSpores> AETHER_SPORE_MAP = new Object2ObjectArrayMap<>();
    public static final Codec<AetherSpores> CODEC = StringIdentifiable.createBasicCodec(
            () -> AETHER_SPORE_MAP.values().toArray(new AetherSpores[0]));
    public static final PacketCodec<ByteBuf, AetherSpores> PACKET_CODEC = PacketCodecs.indexed(
            AetherSpores::getAetherSporeTypeById, AetherSpores::getId);

    private static final Map<TagKey<Fluid>, RegistryEntry<StatusEffect>> FLUID_TO_STATUS_EFFECT = ImmutableMap.of(
            ModFluidTags.VERDANT_SPORES, ModStatusEffects.VERDANT_SPORES,
            ModFluidTags.CRIMSON_SPORES, ModStatusEffects.CRIMSON_SPORES,
            ModFluidTags.ZEPHYR_SPORES, ModStatusEffects.ZEPHYR_SPORES,
            ModFluidTags.SUNLIGHT_SPORES, ModStatusEffects.SUNLIGHT_SPORES,
            ModFluidTags.ROSEITE_SPORES, ModStatusEffects.ROSEITE_SPORES,
            ModFluidTags.MIDNIGHT_SPORES, ModStatusEffects.MIDNIGHT_SPORES);

    private static final int WATER_AMOUNT_PER_LEVEL = 80;

    public static Map<String, AetherSpores> getAetherSporeMap() {
        return AETHER_SPORE_MAP;
    }

    public static boolean hasRainlinePathsInSea(int sporeId) {
        return sporeId != CrimsonSpores.ID;
    }

    public static void clearAllSporeEffects(LivingEntity entity) {
        for (RegistryEntry<StatusEffect> statusEffect : FLUID_TO_STATUS_EFFECT.values()) {
            entity.removeStatusEffect(statusEffect);
        }
    }

    public static int getBottleColor(ItemStack stack) {
        if (stack.getItem() instanceof SporeBottleItem sporeBottleItem) {
            return sporeBottleItem.getSporeType().getColor();
        }
        return -1;
    }

    public static WaterReactive.Type getReactiveTypeFromSpore(AetherSpores sporeType) {
        return switch (sporeType.getId()) {
            case VerdantSpores.ID -> WaterReactive.Type.VERDANT_SPORES;
            case CrimsonSpores.ID -> WaterReactive.Type.CRIMSON_SPORES;
            case SunlightSpores.ID -> WaterReactive.Type.SUNLIGHT_SPORES;
            case ZephyrSpores.ID -> WaterReactive.Type.ZEPHYR_SPORES;
            case RoseiteSpores.ID -> WaterReactive.Type.ROSEITE_SPORES;
            case MidnightSpores.ID -> WaterReactive.Type.MIDNIGHT_SPORES;
            default -> WaterReactive.Type.MISC;
        };
    }

    // Called when an entity enters the spore sea
    public static void onEnterSporeSea(Entity entity) {
        World world = entity.getWorld();
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        // Find the fluid
        AetherSporeFluid targetFluid = null;
        Set<Fluid> touchingFluids = BlockPos.stream(entity.getBoundingBox())
                .map(pos -> world.getFluidState(pos).getFluid())
                .collect(Collectors.toSet());
        for (Fluid fluid : touchingFluids) {
            if (fluid instanceof AetherSporeFluid aetherSporeFluid) {
                targetFluid = aetherSporeFluid;
                break;
            }
        }

        if (targetFluid == null) {
            return;
        }
        SporeParticleSpawner.spawnSplashParticles(serverWorld, targetFluid.getSporeType(), entity,
                entity.fallDistance, true);
    }

    public static void onStepOnSpores(Entity entity) {
        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        // Never spawn particles when sneaking
        if (entity.isSneaking()) {
            return;
        }

        // Find the block
        BlockState steppingBlock = entity.getSteppingBlockState();
        if ((steppingBlock.isIn(ModBlockTags.AETHER_SPORE_SEA_BLOCKS) || steppingBlock.isIn(
                ModBlockTags.AETHER_SPORE_BLOCKS))
                && steppingBlock.getBlock() instanceof SporeEmitting sporeEmittingBlock) {
            SporeParticleSpawner.spawnFootstepParticles(serverWorld,
                    sporeEmittingBlock.getSporeType(), entity);
        }
    }

    public static void checkApplySporeSeaEffectsOnTick(LivingEntity entity) {
        if (EntityUtil.isSubmergedInSporeSea(entity)) {
            if (entity instanceof PlayerEntity playerEntity && (playerEntity.isCreative()
                    || playerEntity.isSpectator())) {
                return;
            }
            AetherSpores.applySporeSeaEffects(entity);
        }
    }

    public static void applySporeSeaEffects(LivingEntity entity) {
        if (AetherSpores.sporesCanAffect(entity)) {
            for (Map.Entry<TagKey<Fluid>, RegistryEntry<StatusEffect>> entry : FLUID_TO_STATUS_EFFECT.entrySet()) {
                if (entity.isSubmergedIn(entry.getKey())) {
                    SporeParticleManager.applySporeEffect(entity, entry.getValue(),
                            SporeParticleManager.SPORE_EFFECT_DURATION_TICKS_DEFAULT);
                }
            }
        }

        if (!entity.getType().isIn(ModEntityTypeTags.SPORES_NEVER_SUFFOCATE)
                && entity.getWorld() instanceof ServerWorld serverWorld) {
            // Also take suffocation damage, mainly for dead spores
            entity.damage(serverWorld,
                    ModDamageTypes.createSource(entity.getWorld(), ModDamageTypes.DROWN_SPORE),
                    1.0f);
        }
    }

    // Create a spore explosion as if generated from spore particles. Reacts
    // to nearby water sources, and spore amount cannot be changed.
    public static void doParticleReaction(World world, Vec3d pos, AetherSpores sporeType,
            int sporeAmount, int startingWaterAmount) {
        BlockPos blockPos = BlockPosUtil.toBlockPos(pos);
        BlockState blockState = world.getBlockState(blockPos);
        int waterAmount = startingWaterAmount;
        boolean affectingFluidContainer = false;
        if (blockState.getFluidState().isIn(FluidTags.WATER)) {
            waterAmount += WaterReactionManager.absorbWaterAndCollectReactives(world, blockPos,
                    null);
        } else if (blockState.isOf(Blocks.WATER_CAULDRON)) {
            waterAmount += WATER_AMOUNT_PER_LEVEL * blockState.get(LeveledCauldronBlock.LEVEL);
            world.setBlockState(blockPos, Blocks.CAULDRON.getStateWithProperties(blockState));
        }

        if (waterAmount > 0) {
            sporeType.doReactionFromParticle(world, pos, sporeAmount, waterAmount,
                    world.getRandom(), affectingFluidContainer);
        }
    }

    public static boolean sporesCanAffect(Entity entity) {
        // Allow data-driven way to prevent spore entities
        if (entity.getType().isIn(ModEntityTypeTags.SPORES_NEVER_AFFECT)) {
            return false;
        }
        // Players in creative or spectator cannot be affected
        return !(entity instanceof PlayerEntity playerEntity) || (!playerEntity.isCreative()
                && !playerEntity.isSpectator());
    }

    public static Optional<AetherSpores> getAetherSporeTypeFromString(String str) {
        return Optional.ofNullable(AETHER_SPORE_MAP.get(str));
    }

    // Do a little hack to move spore growth position to the topmost block
    public static Vec3d getTopmostSeaPosForEntity(World world, Entity entity,
            TagKey<Fluid> fluidTag) {
        BlockPos.Mutable mutable = entity.getBlockPos().mutableCopy();

        while (world.getFluidState(mutable).isIn(fluidTag) && mutable.getY() < world.getTopY(
                Type.WORLD_SURFACE, mutable.getX(), mutable.getY())) {
            mutable.move(Direction.UP);
        }

        if (world.getBlockState(mutable).isAir()) {
            // Found a good position, use it
            return mutable.move(Direction.DOWN).toCenterPos();
        } else {
            // Use original position
            return entity.getPos();
        }
    }

    @Nullable
    public static AetherSpores getAetherSporeTypeById(int id) {
        for (AetherSpores aetherSporeType : AETHER_SPORE_MAP.values()) {
            if (aetherSporeType.getId() == (id)) {
                return aetherSporeType;
            }
        }
        return null;
    }

    protected AetherSpores() {
        AETHER_SPORE_MAP.put(this.getName(), this);
    }

    public abstract int getColor();

    public abstract int getId();

    public abstract String getName();

    @Override
    public String asString() {
        return getName();
    }

    public abstract void onDeathFromStatusEffect(World world, LivingEntity entity, BlockPos pos,
            int water);

    public abstract Item getBottledItem();

    public abstract Item getBucketItem();

    public abstract Block getFluidBlock();

    public abstract Block getSolidBlock();

    public abstract FlowableFluid getFluid();

    @Nullable
    public abstract RegistryEntry<StatusEffect> getStatusEffect();

    public abstract int getParticleColor();

    public abstract void doReaction(World world, Vec3d pos, int spores, int water, Random random);

    // By default, act as normal
    public void doReactionFromFluidContainer(World world, BlockPos fluidContainerPos, int spores,
            int water, Random random) {
        this.doReaction(world, fluidContainerPos, spores, water, random);
    }

    public final void doReaction(World world, BlockPos pos, int spores, int water, Random random) {
        this.doReaction(world, pos.toCenterPos(), spores, water, random);
    }

    // By default, act as normal
    public void doReactionFromParticle(World world, Vec3d pos, int spores, int water, Random random,
            boolean affectingFluidContainer) {
        this.doReaction(world, pos, spores, water, random);
    }

    @Nullable
    public BlockState getFluidCollisionState() {
        return null;
    }

    public Text getSeaDisplayName() {
        return Text.translatable("cosmere.worldsinger.spore_sea." + getName());
    }

    public boolean isDead() {
        return false;
    }

    @Override
    public int compareTo(@NotNull AetherSpores o) {
        return this.getId() - o.getId();
    }
}
