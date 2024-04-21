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

import io.github.drakonkinst.datatables.DataTable;
import io.github.drakonkinst.datatables.DataTableRegistry;
import io.github.drakonkinst.worldsinger.api.ModApi;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.api.sync.AttachmentSync;
import io.github.drakonkinst.worldsinger.block.LivingSporeGrowthBlock;
import io.github.drakonkinst.worldsinger.block.SporeKillable;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.cosmere.SilverLinedUtil;
import io.github.drakonkinst.worldsinger.entity.SilverLinedEntityData;
import io.github.drakonkinst.worldsinger.fluid.Fluidlogged;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.fluid.ModFluids;
import io.github.drakonkinst.worldsinger.registry.ModDataTables;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.registry.tag.ModItemTags;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import io.github.drakonkinst.worldsinger.util.ModProperties;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@SuppressWarnings("UnstableApiUsage")
public final class SporeKillingUtil {

    public static final int MAX_BLOCK_RADIUS = 5;
    public static final double BOAT_RADIUS = 2.0;

    public static boolean killSporeGrowthUsingTool(World world, LivingSporeGrowthBlock sporeGrowth,
            BlockState state, BlockPos pos, PlayerEntity player, Hand hand) {
        // Always mine with mainhand, so check if is the right item
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isIn(ModItemTags.KILLS_SPORE_GROWTHS)) {
            // Damage the tool
            SilverLined silverLinedData = ModApi.SILVER_LINED_ITEM.find(stack, null);
            if (silverLinedData != null) {
                if (silverLinedData.getSilverDurability() <= 0) {
                    return false;
                }
                if (!player.isCreative()) {
                    // It is silver-lined, so decrement the silver durability
                    if (!silverLinedData.decrementDurability()) {
                        SilverLinedUtil.onSilverLinedItemBreak(world, player);
                    }
                }
            } else if (!player.isCreative()) {
                // Assume it is a tool and damage its durability
                stack.damage(1, player,
                        hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
            // Kill the block
            world.setBlockState(pos, SporeKillingUtil.convertToDeadVariant(sporeGrowth, state));
            return true;
        }
        return false;
    }

    public static int killNearbySpores(World world, BlockPos pos, int radius) {
        int numKilled = 0;
        radius = Math.min(radius, MAX_BLOCK_RADIUS);
        int deadSporeFluidIndex = Fluidlogged.getFluidIndex(ModFluids.DEAD_SPORES);

        // Not sure if this is the right iteration method, but it works
        for (BlockPos currentPos : BlockPos.iterateOutwards(pos, radius, radius, radius)) {
            if (currentPos.equals(pos)) {
                continue;
            }

            BlockState blockState = world.getBlockState(currentPos);
            boolean wasChanged = false;

            // Kill SporeKillable block
            if (blockState.getBlock() instanceof SporeKillable sporeKillable
                    && sporeKillable.isSporeKillable(world, pos, blockState)) {
                if (BlockPosUtil.isInvestitureBlocked(world, pos, currentPos)) {
                    continue;
                }
                blockState = SporeKillingUtil.convertToDeadVariant(sporeKillable, blockState);
                wasChanged = true;
            }

            // Turn living spore fluids into dead spore fluid
            if (blockState.getBlock() instanceof Waterloggable) {
                FluidState fluidState = blockState.getFluidState();
                if (fluidState.isIn(ModFluidTags.AETHER_SPORES) && !fluidState.isIn(
                        ModFluidTags.DEAD_SPORES)) {
                    if (BlockPosUtil.isInvestitureBlocked(world, pos, currentPos)) {
                        continue;
                    }
                    blockState = blockState.with(ModProperties.FLUIDLOGGED, deadSporeFluidIndex);
                    wasChanged = true;
                }
            }

            if (wasChanged) {
                numKilled += 1;
                world.setBlockState(currentPos, blockState, Block.NOTIFY_ALL);
            }
        }
        return numKilled;
    }

    public static BlockState convertToDeadVariant(SporeKillable sporeKillable,
            BlockState blockState) {
        return sporeKillable.getDeadSporeBlock().getStateWithProperties(blockState);
    }

    public static boolean isSporeKillingBlockNearby(World world, BlockPos pos) {
        DataTable dataTable = DataTableRegistry.INSTANCE.get(ModDataTables.SPORE_KILLING_RADIUS);
        for (BlockPos currentPos : BlockPos.iterateOutwards(pos, MAX_BLOCK_RADIUS, MAX_BLOCK_RADIUS,
                MAX_BLOCK_RADIUS)) {
            BlockState blockState = world.getBlockState(currentPos);
            if (!blockState.isIn(ModBlockTags.KILLS_SPORES)) {
                continue;
            }

            int distance = BlockPosUtil.getDistance(pos, currentPos);
            if (dataTable.getIntForBlock(blockState) < distance) {
                continue;
            }

            if (BlockPosUtil.isInvestitureBlocked(world, currentPos, pos)) {
                continue;
            }
            return true;
        }
        return false;
    }

    public static boolean checkNearbyEntities(World world, Vec3d pos) {
        Box box = Box.of(pos, BOAT_RADIUS, BOAT_RADIUS, BOAT_RADIUS);
        return SporeKillingUtil.checkNearbyEntitiesInBox(world, box);
    }

    private static boolean checkNearbyEntitiesInBox(World world, Box box) {
        List<BoatEntity> entitiesInRange = world.getEntitiesByClass(BoatEntity.class, box,
                boatEntity -> {
                    SilverLinedEntityData silverData = boatEntity.getAttached(
                            ModAttachmentTypes.SILVER_LINED_BOAT);
                    boolean hasSilver = silverData != null && silverData.getSilverDurability() > 0;
                    if (hasSilver) {
                        silverData.decrementDurability();
                        if (!world.isClient()) {
                            AttachmentSync.sync(boatEntity, ModAttachmentTypes.SILVER_LINED_BOAT,
                                    silverData);
                        }
                    }
                    return hasSilver;
                });
        return !entitiesInRange.isEmpty();
    }

    public static boolean checkNearbyEntitiesForRange(World world, double minX, double minY,
            double minZ, double maxX, double maxY, double maxZ) {
        Box box = new Box(minX - BOAT_RADIUS, minY - BOAT_RADIUS, minZ - BOAT_RADIUS,
                maxX + BOAT_RADIUS, maxY + BOAT_RADIUS, maxZ + BOAT_RADIUS);
        return SporeKillingUtil.checkNearbyEntitiesInBox(world, box);
    }

    public static boolean isSporeKillingBlockNearbyForRange(World world, double minX, double minY,
            double minZ, double maxX, double maxY, double maxZ) {
        return SporeKillingUtil.isSporeKillingBlockNearbyForRange(world, MathHelper.floor(minX),
                MathHelper.floor(minY), MathHelper.floor(minZ), MathHelper.ceil(maxX),
                MathHelper.ceil(maxY), MathHelper.ceil(maxZ));
    }

    public static boolean isSporeKillingBlockNearbyForRange(World world, int minX, int minY,
            int minZ, int maxX, int maxY, int maxZ) {
        DataTable dataTable = DataTableRegistry.INSTANCE.get(ModDataTables.SPORE_KILLING_RADIUS);

        int searchMinX = minX - MAX_BLOCK_RADIUS;
        int searchMinY = minY - MAX_BLOCK_RADIUS;
        int searchMinZ = minZ - MAX_BLOCK_RADIUS;
        int searchMaxX = maxX + MAX_BLOCK_RADIUS;
        int searchMaxY = maxY + MAX_BLOCK_RADIUS;
        int searchMaxZ = maxZ + MAX_BLOCK_RADIUS;

        BlockPos.Mutable closestPos = new BlockPos.Mutable();
        for (BlockPos searchPos : BlockPos.iterate(searchMinX, searchMinY, searchMinZ, searchMaxX,
                searchMaxY, searchMaxZ)) {
            BlockState blockState = world.getBlockState(searchPos);
            if (!blockState.isIn(ModBlockTags.KILLS_SPORES)) {
                continue;
            }

            SporeKillingUtil.calcClosestPointOnCuboid(searchPos.getX(), searchPos.getY(),
                    searchPos.getZ(), minX, minY, minZ, maxX, maxY, maxZ, closestPos);
            int distance = BlockPosUtil.getDistance(searchPos, closestPos);
            if (dataTable.getIntForBlock(blockState) < distance) {
                continue;
            }

            if (BlockPosUtil.isInvestitureBlocked(world, searchPos, closestPos)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private static void calcClosestPointOnCuboid(int x, int y, int z, int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ, BlockPos.Mutable mutable) {
        int closestX = SporeKillingUtil.clamp(x, minX, maxX);
        int closestY = SporeKillingUtil.clamp(y, minY, maxY);
        int closestZ = SporeKillingUtil.clamp(z, minZ, maxZ);
        mutable.set(closestX, closestY, closestZ);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

    private static int getDistanceBetweenPointAndCube(int x, int y, int z, int minX, int minY,
            int minZ, int maxX, int maxY, int maxZ) {
        int closestX = SporeKillingUtil.clamp(x, minX, maxX);
        int closestY = SporeKillingUtil.clamp(y, minY, maxY);
        int closestZ = SporeKillingUtil.clamp(z, minZ, maxZ);
        return BlockPosUtil.getDistance(x, y, z, closestX, closestY, closestZ);
    }

    private SporeKillingUtil() {}
}
