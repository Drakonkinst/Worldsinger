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
package io.github.drakonkinst.worldsinger.worldgen.structure;

import com.google.common.collect.Lists;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.util.ModConstants;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.RailBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public class CustomMineshaftGenerator {

    private static final int MAX_CHAIN_LENGTH = 8;
    private static final int MAX_DISTANCE_FROM_CENTER = 80;

    private static CustomMineshaftGenerator.MineshaftPart pieceGenerator(StructurePiece start,
            StructurePiecesHolder holder, Random random, int x, int y, int z, Direction orientation,
            int chainLength) {
        if (chainLength > MAX_CHAIN_LENGTH) {
            return null;
        }
        if (Math.abs(x - start.getBoundingBox().getMinX()) > MAX_DISTANCE_FROM_CENTER
                || Math.abs(z - start.getBoundingBox().getMinZ()) > MAX_DISTANCE_FROM_CENTER) {
            return null;
        }
        CustomMineshaftStructure.Type type = ((CustomMineshaftGenerator.MineshaftPart) start).mineshaftType;
        CustomMineshaftGenerator.MineshaftPart mineshaftPart = CustomMineshaftGenerator.pickPiece(
                holder, random, x, y, z, orientation, chainLength + 1, type);
        if (mineshaftPart != null) {
            holder.addPiece(mineshaftPart);
            mineshaftPart.fillOpenings(start, holder, random);
        }
        return mineshaftPart;
    }

    private static CustomMineshaftGenerator.MineshaftPart pickPiece(StructurePiecesHolder holder,
            Random random, int x, int y, int z, @Nullable Direction orientation, int chainLength,
            CustomMineshaftStructure.Type type) {
        // Generate crossing, stairs, or corridor with different weights
        int randomIndex = random.nextInt(100);
        if (randomIndex >= 80) {
            BlockBox blockBox = CustomMineshaftGenerator.MineshaftCrossing.getBoundingBox(holder,
                    random, x, y, z, orientation);
            if (blockBox != null) {
                return new CustomMineshaftGenerator.MineshaftCrossing(chainLength, blockBox,
                        orientation, type);
            }
        } else if (randomIndex >= 70) {
            BlockBox blockBox = CustomMineshaftGenerator.MineshaftStairs.getBoundingBox(holder,
                    random, x, y, z, orientation);
            if (blockBox != null) {
                return new CustomMineshaftGenerator.MineshaftStairs(chainLength, blockBox,
                        orientation, type);
            }
        } else {
            BlockBox blockBox = CustomMineshaftGenerator.MineshaftCorridor.getBoundingBox(holder,
                    random, x, y, z, orientation);
            if (blockBox != null) {
                return new CustomMineshaftGenerator.MineshaftCorridor(chainLength, random, blockBox,
                        orientation, type);
            }
        }
        return null;
    }

    public static class MineshaftCrossing extends CustomMineshaftGenerator.MineshaftPart {

        @Nullable
        public static BlockBox getBoundingBox(StructurePiecesHolder holder, Random random, int x,
                int y, int z, @Nullable Direction orientation) {
            if (orientation == null) {
                orientation = Direction.NORTH;
            }
            int maxY = random.nextInt(4) == 0 ? 6 : 2;
            BlockBox blockBox = switch (orientation) {
                default -> new BlockBox(-1, 0, -4, 3, maxY, 0);
                case SOUTH -> new BlockBox(-1, 0, 0, 3, maxY, 4);
                case WEST -> new BlockBox(-4, 0, -1, 0, maxY, 3);
                case EAST -> new BlockBox(0, 0, -1, 4, maxY, 3);
            };
            blockBox = blockBox.offset(x, y, z);
            if (holder.getIntersecting(blockBox) != null) {
                return null;
            }
            return blockBox;
        }

        private final Direction direction;
        private final boolean twoFloors;

        public MineshaftCrossing(StructureContext structureContext, NbtCompound nbt) {
            super(ModStructurePieceTypes.CUSTOM_MINESHAFT_CROSSING, nbt);
            this.twoFloors = nbt.getBoolean("tf");
            this.direction = Direction.fromHorizontalQuarterTurns(nbt.getInt("D"));
        }

        public MineshaftCrossing(int chainLength, BlockBox boundingBox,
                @Nullable Direction orientation, CustomMineshaftStructure.Type type) {
            super(ModStructurePieceTypes.CUSTOM_MINESHAFT_CROSSING, chainLength, type, boundingBox);
            this.direction = orientation;
            this.twoFloors = boundingBox.getBlockCountY() > 3;
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder,
                Random random) {
            int i = this.getChainLength();
            switch (this.direction) {
                default -> {
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(),
                            this.boundingBox.getMinZ() - 1, Direction.NORTH, i);
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(),
                            this.boundingBox.getMinZ() + 1, Direction.WEST, i);
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(),
                            this.boundingBox.getMinZ() + 1, Direction.EAST, i);
                }
                case SOUTH -> {
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(),
                            this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i);
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(),
                            this.boundingBox.getMinZ() + 1, Direction.WEST, i);
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(),
                            this.boundingBox.getMinZ() + 1, Direction.EAST, i);
                }
                case WEST -> {
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(),
                            this.boundingBox.getMinZ() - 1, Direction.NORTH, i);
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(),
                            this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i);
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(),
                            this.boundingBox.getMinZ() + 1, Direction.WEST, i);
                }
                case EAST -> {
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(),
                            this.boundingBox.getMinZ() - 1, Direction.NORTH, i);
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX() + 1, this.boundingBox.getMinY(),
                            this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i);
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(),
                            this.boundingBox.getMinZ() + 1, Direction.EAST, i);
                }
            }
            if (this.twoFloors) {
                if (random.nextBoolean()) {
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX() + 1, this.boundingBox.getMinY() + 3 + 1,
                            this.boundingBox.getMinZ() - 1, Direction.NORTH, i);
                }
                if (random.nextBoolean()) {
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX() - 1, this.boundingBox.getMinY() + 3 + 1,
                            this.boundingBox.getMinZ() + 1, Direction.WEST, i);
                }
                if (random.nextBoolean()) {
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY() + 3 + 1,
                            this.boundingBox.getMinZ() + 1, Direction.EAST, i);
                }
                if (random.nextBoolean()) {
                    CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX() + 1, this.boundingBox.getMinY() + 3 + 1,
                            this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i);
                }
            }
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor,
                ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos,
                BlockPos pivot) {
            if (this.cannotGenerate(world, chunkBox)) {
                return;
            }
            BlockState blockState = this.mineshaftType.getPlanks();
            if (this.twoFloors) {
                this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() + 1,
                        this.boundingBox.getMinY(), this.boundingBox.getMinZ(),
                        this.boundingBox.getMaxX() - 1, this.boundingBox.getMinY() + 3 - 1,
                        this.boundingBox.getMaxZ(), AIR, AIR, false);
                this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX(),
                        this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1,
                        this.boundingBox.getMaxX(), this.boundingBox.getMinY() + 3 - 1,
                        this.boundingBox.getMaxZ() - 1, AIR, AIR, false);
                this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() + 1,
                        this.boundingBox.getMaxY() - 2, this.boundingBox.getMinZ(),
                        this.boundingBox.getMaxX() - 1, this.boundingBox.getMaxY(),
                        this.boundingBox.getMaxZ(), AIR, AIR, false);
                this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX(),
                        this.boundingBox.getMaxY() - 2, this.boundingBox.getMinZ() + 1,
                        this.boundingBox.getMaxX(), this.boundingBox.getMaxY(),
                        this.boundingBox.getMaxZ() - 1, AIR, AIR, false);
                this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() + 1,
                        this.boundingBox.getMinY() + 3, this.boundingBox.getMinZ() + 1,
                        this.boundingBox.getMaxX() - 1, this.boundingBox.getMinY() + 3,
                        this.boundingBox.getMaxZ() - 1, AIR, AIR, false);
            } else {
                this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX() + 1,
                        this.boundingBox.getMinY(), this.boundingBox.getMinZ(),
                        this.boundingBox.getMaxX() - 1, this.boundingBox.getMaxY(),
                        this.boundingBox.getMaxZ(), AIR, AIR, false);
                this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX(),
                        this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1,
                        this.boundingBox.getMaxX(), this.boundingBox.getMaxY(),
                        this.boundingBox.getMaxZ() - 1, AIR, AIR, false);
            }
            this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMinX() + 1,
                    this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1,
                    this.boundingBox.getMaxY());
            this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMinX() + 1,
                    this.boundingBox.getMinY(), this.boundingBox.getMaxZ() - 1,
                    this.boundingBox.getMaxY());
            this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMaxX() - 1,
                    this.boundingBox.getMinY(), this.boundingBox.getMinZ() + 1,
                    this.boundingBox.getMaxY());
            this.generateCrossingPillar(world, chunkBox, this.boundingBox.getMaxX() - 1,
                    this.boundingBox.getMinY(), this.boundingBox.getMaxZ() - 1,
                    this.boundingBox.getMaxY());
            int y = this.boundingBox.getMinY() - 1;
            for (int x = this.boundingBox.getMinX(); x <= this.boundingBox.getMaxX(); ++x) {
                for (int z = this.boundingBox.getMinZ(); z <= this.boundingBox.getMaxZ(); ++z) {
                    this.tryPlaceFloor(world, chunkBox, blockState, x, y, z);
                }
            }
        }

        private void generateCrossingPillar(StructureWorldAccess world, BlockBox boundingBox, int x,
                int minY, int z, int maxY) {
            if (!this.getBlockAt(world, x, maxY + 1, z, boundingBox).isAir()) {
                this.fillWithOutline(world, boundingBox, x, minY, z, x, maxY, z,
                        this.mineshaftType.getPlanks(), AIR, false);
            }
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            super.writeNbt(context, nbt);
            nbt.putBoolean("tf", this.twoFloors);
            nbt.putInt("D", this.direction.getHorizontalQuarterTurns());
        }
    }

    public static class MineshaftStairs extends CustomMineshaftGenerator.MineshaftPart {

        @Nullable
        public static BlockBox getBoundingBox(StructurePiecesHolder holder, Random random, int x,
                int y, int z, @Nullable Direction orientation) {
            if (orientation == null) {
                orientation = Direction.NORTH;
            }
            BlockBox blockBox = switch (orientation) {
                default -> new BlockBox(0, -5, -8, 2, 2, 0);
                case SOUTH -> new BlockBox(0, -5, 0, 2, 2, 8);
                case WEST -> new BlockBox(-8, -5, 0, 0, 2, 2);
                case EAST -> new BlockBox(0, -5, 0, 8, 2, 2);
            };
            blockBox = blockBox.offset(x, y, z);
            if (holder.getIntersecting(blockBox) != null) {
                return null;
            }
            return blockBox;
        }

        public MineshaftStairs(int chainLength, BlockBox boundingBox, Direction orientation,
                CustomMineshaftStructure.Type type) {
            super(ModStructurePieceTypes.CUSTOM_MINESHAFT_STAIRS, chainLength, type, boundingBox);
            this.setOrientation(orientation);
        }

        public MineshaftStairs(StructureContext structureContext, NbtCompound nbt) {
            super(ModStructurePieceTypes.CUSTOM_MINESHAFT_STAIRS, nbt);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder,
                Random random) {
            int i = this.getChainLength();
            Direction direction = this.getFacing();
            if (direction != null) {
                switch (direction) {
                    default -> CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX(), this.boundingBox.getMinY(),
                            this.boundingBox.getMinZ() - 1, Direction.NORTH, i);
                    case SOUTH -> CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX(), this.boundingBox.getMinY(),
                            this.boundingBox.getMaxZ() + 1, Direction.SOUTH, i);
                    case WEST -> CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(),
                            this.boundingBox.getMinZ(), Direction.WEST, i);
                    case EAST -> CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                            this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(),
                            this.boundingBox.getMinZ(), Direction.EAST, i);
                }
            }
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor,
                ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos,
                BlockPos pivot) {
            if (this.cannotGenerate(world, chunkBox)) {
                return;
            }
            this.fillWithOutline(world, chunkBox, 0, 5, 0, 2, 7, 1, AIR, AIR, false);
            this.fillWithOutline(world, chunkBox, 0, 0, 7, 2, 2, 8, AIR, AIR, false);
            for (int i = 0; i < 5; ++i) {
                this.fillWithOutline(world, chunkBox, 0, 5 - i - (i < 4 ? 1 : 0), 2 + i, 2, 7 - i,
                        2 + i, AIR, AIR, false);
            }
        }
    }

    public static class MineshaftCorridor extends CustomMineshaftGenerator.MineshaftPart {

        @Nullable
        public static BlockBox getBoundingBox(StructurePiecesHolder holder, Random random, int x,
                int y, int z, Direction orientation) {
            int i = random.nextInt(3) + 2;
            while (i > 0) {
                int j = i * 5;
                BlockBox blockBox = switch (orientation) {
                    default -> new BlockBox(0, 0, -(j - 1), 2, 2, 0);
                    case SOUTH -> new BlockBox(0, 0, 0, 2, 2, j - 1);
                    case WEST -> new BlockBox(-(j - 1), 0, 0, 0, 2, 2);
                    case EAST -> new BlockBox(0, 0, 0, j - 1, 2, 2);
                };
                blockBox = blockBox.offset(x, y, z);
                if (holder.getIntersecting(blockBox) == null) {
                    return blockBox;
                }
                --i;
            }
            return null;
        }

        private static void fillColumn(StructureWorldAccess world, BlockState state,
                BlockPos.Mutable pos, int startY, int endY) {
            for (int y = startY; y < endY; ++y) {
                world.setBlockState(pos.setY(y), state, Block.NOTIFY_LISTENERS);
            }
        }

        private final boolean hasRails;
        private final boolean hasCobwebs;
        private final int length;
        private boolean hasSpawner;

        public MineshaftCorridor(StructureContext structureContext, NbtCompound nbt) {
            super(ModStructurePieceTypes.CUSTOM_MINESHAFT_CORRIDOR, nbt);
            this.hasRails = nbt.getBoolean("hr");
            this.hasCobwebs = nbt.getBoolean("sc");
            this.hasSpawner = nbt.getBoolean("hps");
            this.length = nbt.getInt("Num");
        }

        public MineshaftCorridor(int chainLength, Random random, BlockBox boundingBox,
                Direction orientation, CustomMineshaftStructure.Type type) {
            super(ModStructurePieceTypes.CUSTOM_MINESHAFT_CORRIDOR, chainLength, type, boundingBox);
            this.setOrientation(orientation);
            this.hasRails = type.canHaveRails() && random.nextInt(3) == 0;
            this.hasCobwebs = type.canHaveCobwebs() && !this.hasRails && random.nextInt(23) == 0;
            Axis facingAxis =
                    this.getFacing() != null ? this.getFacing().getAxis() : Direction.Axis.Z;
            this.length = (facingAxis == Direction.Axis.Z ? boundingBox.getBlockCountZ()
                    : boundingBox.getBlockCountX()) / 5;
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder,
                Random random) {
            int chainLength = this.getChainLength();
            int variantIndex = random.nextInt(4);
            Direction direction = this.getFacing();
            if (direction != null) {
                switch (direction) {
                    default -> {
                        if (variantIndex <= 1) {
                            CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                                    this.boundingBox.getMinX(),
                                    this.boundingBox.getMinY() - 1 + random.nextInt(3),
                                    this.boundingBox.getMinZ() - 1, direction, chainLength);
                            break;
                        }
                        if (variantIndex == 2) {
                            CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                                    this.boundingBox.getMinX() - 1,
                                    this.boundingBox.getMinY() - 1 + random.nextInt(3),
                                    this.boundingBox.getMinZ(), Direction.WEST, chainLength);
                            break;
                        }
                        CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                                this.boundingBox.getMaxX() + 1,
                                this.boundingBox.getMinY() - 1 + random.nextInt(3),
                                this.boundingBox.getMinZ(), Direction.EAST, chainLength);
                    }
                    case SOUTH -> {
                        if (variantIndex <= 1) {
                            CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                                    this.boundingBox.getMinX(),
                                    this.boundingBox.getMinY() - 1 + random.nextInt(3),
                                    this.boundingBox.getMaxZ() + 1, direction, chainLength);
                            break;
                        }
                        if (variantIndex == 2) {
                            CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                                    this.boundingBox.getMinX() - 1,
                                    this.boundingBox.getMinY() - 1 + random.nextInt(3),
                                    this.boundingBox.getMaxZ() - 3, Direction.WEST, chainLength);
                            break;
                        }
                        CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                                this.boundingBox.getMaxX() + 1,
                                this.boundingBox.getMinY() - 1 + random.nextInt(3),
                                this.boundingBox.getMaxZ() - 3, Direction.EAST, chainLength);
                    }
                    case WEST -> {
                        if (variantIndex <= 1) {
                            CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                                    this.boundingBox.getMinX() - 1,
                                    this.boundingBox.getMinY() - 1 + random.nextInt(3),
                                    this.boundingBox.getMinZ(), direction, chainLength);
                            break;
                        }
                        if (variantIndex == 2) {
                            CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                                    this.boundingBox.getMinX(),
                                    this.boundingBox.getMinY() - 1 + random.nextInt(3),
                                    this.boundingBox.getMinZ() - 1, Direction.NORTH, chainLength);
                            break;
                        }
                        CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                                this.boundingBox.getMinX(),
                                this.boundingBox.getMinY() - 1 + random.nextInt(3),
                                this.boundingBox.getMaxZ() + 1, Direction.SOUTH, chainLength);
                    }
                    case EAST -> {
                        if (variantIndex <= 1) {
                            CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                                    this.boundingBox.getMaxX() + 1,
                                    this.boundingBox.getMinY() - 1 + random.nextInt(3),
                                    this.boundingBox.getMinZ(), direction, chainLength);
                            break;
                        }
                        if (variantIndex == 2) {
                            CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                                    this.boundingBox.getMaxX() - 3,
                                    this.boundingBox.getMinY() - 1 + random.nextInt(3),
                                    this.boundingBox.getMinZ() - 1, Direction.NORTH, chainLength);
                            break;
                        }
                        CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                                this.boundingBox.getMaxX() - 3,
                                this.boundingBox.getMinY() - 1 + random.nextInt(3),
                                this.boundingBox.getMaxZ() + 1, Direction.SOUTH, chainLength);
                    }
                }
            }
            if (chainLength >= 8) {
                return;
            }
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                int currentZ = this.boundingBox.getMinZ() + 3;
                while (currentZ + 3 <= this.boundingBox.getMaxZ()) {
                    int branchVariantIndex = random.nextInt(5);
                    if (branchVariantIndex == 0) {
                        CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                                this.boundingBox.getMinX() - 1, this.boundingBox.getMinY(),
                                currentZ, Direction.WEST, chainLength + 1);
                    } else if (branchVariantIndex == 1) {
                        CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                                this.boundingBox.getMaxX() + 1, this.boundingBox.getMinY(),
                                currentZ, Direction.EAST, chainLength + 1);
                    }
                    currentZ += 5;
                }
            } else {
                int currentX = this.boundingBox.getMinX() + 3;
                while (currentX + 3 <= this.boundingBox.getMaxX()) {
                    int l = random.nextInt(5);
                    if (l == 0) {
                        CustomMineshaftGenerator.pieceGenerator(start, holder, random, currentX,
                                this.boundingBox.getMinY(), this.boundingBox.getMinZ() - 1,
                                Direction.NORTH, chainLength + 1);
                    } else if (l == 1) {
                        CustomMineshaftGenerator.pieceGenerator(start, holder, random, currentX,
                                this.boundingBox.getMinY(), this.boundingBox.getMaxZ() + 1,
                                Direction.SOUTH, chainLength + 1);
                    }
                    currentX += 5;
                }
            }
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor,
                ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos,
                BlockPos pivot) {
            if (this.cannotGenerate(world, chunkBox)) {
                return;
            }
            int maxZ = this.length * 5 - 1;
            BlockState blockState = this.mineshaftType.getPlanks();
            this.fillWithOutline(world, chunkBox, 0, 0, 0, 2, 1, maxZ, AIR, AIR, false);
            this.fillWithOutlineUnderSeaLevel(world, chunkBox, random, 0.8f, 0, 2, 0, 2, 2, maxZ,
                    AIR, AIR, false, false);
            if (this.hasCobwebs) {
                this.fillWithOutlineUnderSeaLevel(world, chunkBox, random, 0.6f, 0, 0, 0, 2, 1,
                        maxZ, Blocks.COBWEB.getDefaultState(), AIR, false, true);
            }
            for (int n = 0; n < this.length; ++n) {
                int z = 2 + n * 5;
                this.generateSupports(world, chunkBox, 0, 0, z, 2, 2, random);
                if (this.mineshaftType.canHaveCobwebs()) {
                    this.addCobwebsUnderground(world, chunkBox, random, 0.1f, 0, 2, z - 1);
                    this.addCobwebsUnderground(world, chunkBox, random, 0.1f, 2, 2, z - 1);
                    this.addCobwebsUnderground(world, chunkBox, random, 0.1f, 0, 2, z + 1);
                    this.addCobwebsUnderground(world, chunkBox, random, 0.1f, 2, 2, z + 1);
                    this.addCobwebsUnderground(world, chunkBox, random, 0.05f, 0, 2, z - 2);
                    this.addCobwebsUnderground(world, chunkBox, random, 0.05f, 2, 2, z - 2);
                    this.addCobwebsUnderground(world, chunkBox, random, 0.05f, 0, 2, z + 2);
                    this.addCobwebsUnderground(world, chunkBox, random, 0.05f, 2, 2, z + 2);
                }
                if (this.mineshaftType.canHaveLoot()) {
                    if (random.nextInt(100) == 0) {
                        this.addChest(world, chunkBox, random, 2, 0, z - 1,
                                this.mineshaftType.getLootTable());
                    }
                    if (random.nextInt(100) == 0) {
                        this.addChest(world, chunkBox, random, 0, 0, z + 1,
                                this.mineshaftType.getLootTable());
                    }
                }

                if (!this.hasCobwebs || this.hasSpawner || !this.mineshaftType.canHaveSpawner()) {
                    continue;
                }
                int y = z - 1 + random.nextInt(3);
                BlockPos.Mutable blockPos = this.offsetPos(1, 0, y);
                if (!chunkBox.contains(blockPos) || !this.isUnderSeaLevel(world, 1, 0, y,
                        chunkBox)) {
                    continue;
                }
                this.hasSpawner = true;
                world.setBlockState(blockPos, Blocks.SPAWNER.getDefaultState(),
                        Block.NOTIFY_LISTENERS);
                BlockEntity blockEntity = world.getBlockEntity(blockPos);
                if (!(blockEntity instanceof MobSpawnerBlockEntity mobSpawnerBlockEntity)) {
                    continue;
                }
                mobSpawnerBlockEntity.setEntityType(EntityType.CAVE_SPIDER, random);
            }
            for (int x = 0; x <= 2; ++x) {
                for (int z = 0; z <= maxZ; ++z) {
                    this.tryPlaceFloor(world, chunkBox, blockState, x, -1, z);
                }
            }
            this.fillSupportBeam(world, chunkBox, 0, -1, 2);
            if (this.length > 1) {
                this.fillSupportBeam(world, chunkBox, 0, -1, maxZ - 2);
            }
            if (this.hasRails) {
                BlockState blockState2 = Blocks.RAIL.getDefaultState()
                        .with(RailBlock.SHAPE, RailShape.NORTH_SOUTH);
                for (int z = 0; z <= maxZ; ++z) {
                    BlockState blockState3 = this.getBlockAt(world, 1, -1, z, chunkBox);
                    if (blockState3.isAir() || !blockState3.isOpaqueFullCube()) {
                        continue;
                    }
                    float f = this.isUnderSeaLevel(world, 1, 0, z, chunkBox) ? 0.7f : 0.9f;
                    this.addBlockWithRandomThreshold(world, chunkBox, random, f, 1, 0, z,
                            blockState2);
                }
            }
        }

        private void generateSupports(StructureWorldAccess world, BlockBox boundingBox, int minX,
                int minY, int z, int maxY, int maxX, Random random) {
            if (!this.isSolidCeiling(world, boundingBox, minX, maxX, maxY, z)) {
                return;
            }
            BlockState blockState = this.mineshaftType.getPlanks();
            BlockState blockState2 = this.mineshaftType.getFence();
            this.fillWithOutline(world, boundingBox, minX, minY, z, minX, maxY - 1, z,
                    blockState2.with(FenceBlock.WEST, true), AIR, false);
            this.fillWithOutline(world, boundingBox, maxX, minY, z, maxX, maxY - 1, z,
                    blockState2.with(FenceBlock.EAST, true), AIR, false);
            if (random.nextInt(4) == 0) {
                this.fillWithOutline(world, boundingBox, minX, maxY, z, minX, maxY, z, blockState,
                        AIR, false);
                this.fillWithOutline(world, boundingBox, maxX, maxY, z, maxX, maxY, z, blockState,
                        AIR, false);
            } else {
                this.fillWithOutline(world, boundingBox, minX, maxY, z, maxX, maxY, z, blockState,
                        AIR, false);
                this.addBlockWithRandomThreshold(world, boundingBox, random, 0.05f, minX + 1, maxY,
                        z - 1, Blocks.WALL_TORCH.getDefaultState()
                                .with(WallTorchBlock.FACING, Direction.SOUTH));
                this.addBlockWithRandomThreshold(world, boundingBox, random, 0.05f, minX + 1, maxY,
                        z + 1, Blocks.WALL_TORCH.getDefaultState()
                                .with(WallTorchBlock.FACING, Direction.NORTH));
            }
        }

        private void addCobwebsUnderground(StructureWorldAccess world, BlockBox box, Random random,
                float threshold, int x, int y, int z) {
            if (this.isUnderSeaLevel(world, x, y, z, box) && random.nextFloat() < threshold
                    && this.hasSolidNeighborBlocks(world, box, x, y, z, 2)) {
                this.addBlock(world, Blocks.COBWEB.getDefaultState(), x, y, z, box);
            }
        }

        @Override
        protected boolean addChest(StructureWorldAccess world, BlockBox boundingBox, Random random,
                int x, int y, int z, RegistryKey<LootTable> lootTable) {
            BlockPos.Mutable blockPos = this.offsetPos(x, y, z);
            if (boundingBox.contains(blockPos) && world.getBlockState(blockPos).isAir()
                    && !world.getBlockState(blockPos.down()).isAir()) {
                BlockState blockState = Blocks.RAIL.getDefaultState()
                        .with(RailBlock.SHAPE,
                                random.nextBoolean() ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
                this.addBlock(world, blockState, x, y, z, boundingBox);
                ChestMinecartEntity chestMinecartEntity = EntityType.CHEST_MINECART.create(
                        world.toServerWorld(), SpawnReason.CHUNK_GENERATION);
                if (chestMinecartEntity != null) {
                    chestMinecartEntity.initPosition((double) blockPos.getX() + 0.5,
                            (double) blockPos.getY() + 0.5, (double) blockPos.getZ() + 0.5);
                    chestMinecartEntity.setLootTable(lootTable, random.nextLong());
                    world.spawnEntity(chestMinecartEntity);
                }
                return true;
            }
            return false;
        }

        private void fillSupportBeam(StructureWorldAccess world, BlockBox box, int x, int y,
                int z) {
            BlockState logBlock = this.mineshaftType.getLog();
            BlockState plankBlock = this.mineshaftType.getPlanks();
            if (this.getBlockAt(world, x, y, z, box).isOf(plankBlock.getBlock())) {
                this.fillSupportBeam(world, logBlock, x, y, z, box);
            }
            if (this.getBlockAt(world, x + 2, y, z, box).isOf(plankBlock.getBlock())) {
                this.fillSupportBeam(world, logBlock, x + 2, y, z, box);
            }
        }

        private boolean hasSolidNeighborBlocks(StructureWorldAccess world, BlockBox box, int x,
                int y, int z, int count) {
            BlockPos.Mutable mutable = this.offsetPos(x, y, z);
            int numNeighbors = 0;
            for (Direction direction : ModConstants.CARDINAL_DIRECTIONS) {
                mutable.move(direction);
                if (box.contains(mutable) && world.getBlockState(mutable)
                        .isSideSolidFullSquare(world, mutable, direction.getOpposite())
                        && ++numNeighbors >= count) {
                    return true;
                }
                mutable.move(direction.getOpposite());
            }
            return false;
        }

        protected void fillSupportBeam(StructureWorldAccess world, BlockState state, int x, int y,
                int z, BlockBox box) {
            BlockPos.Mutable mutable = this.offsetPos(x, y, z);
            if (!box.contains(mutable)) {
                return;
            }
            int startY = mutable.getY();
            int yOffset = 1;
            boolean aboveWorldBottom = true;
            boolean belowWorldTop = true;
            while (aboveWorldBottom || belowWorldTop) {
                boolean canReplace;
                BlockState blockState;
                if (aboveWorldBottom) {
                    mutable.setY(startY - yOffset);
                    blockState = world.getBlockState(mutable);
                    canReplace = this.canReplace(blockState) && !blockState.isOf(Blocks.LAVA);
                    if (!canReplace && this.isUpsideSolidFullSquare(world, mutable, blockState)) {
                        CustomMineshaftGenerator.MineshaftCorridor.fillColumn(world, state, mutable,
                                startY - yOffset + 1, startY);
                        return;
                    }
                    aboveWorldBottom =
                            yOffset <= 20 && canReplace && mutable.getY() > world.getBottomY() + 1;
                }
                if (belowWorldTop) {
                    mutable.setY(startY + yOffset);
                    blockState = world.getBlockState(mutable);
                    canReplace = this.canReplace(blockState);
                    if (!canReplace && this.sideCoversSmallSquare(world, mutable, blockState)) {
                        world.setBlockState(mutable.setY(startY + 1), this.mineshaftType.getFence(),
                                Block.NOTIFY_LISTENERS);
                        CustomMineshaftGenerator.MineshaftCorridor.fillColumn(world,
                                Blocks.CHAIN.getDefaultState(), mutable, startY + 2,
                                startY + yOffset);
                        return;
                    }
                    belowWorldTop = yOffset <= 50 && canReplace
                            && mutable.getY() < world.getTopYInclusive() - 1;
                }
                ++yOffset;
            }
        }

        private boolean isUpsideSolidFullSquare(WorldView world, BlockPos pos, BlockState state) {
            return state.isSideSolidFullSquare(world, pos, Direction.UP);
        }

        private boolean sideCoversSmallSquare(WorldView world, BlockPos pos, BlockState state) {
            return Block.sideCoversSmallSquare(world, pos, Direction.DOWN)
                    && !(state.getBlock() instanceof FallingBlock);
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            super.writeNbt(context, nbt);
            nbt.putBoolean("hr", this.hasRails);
            nbt.putBoolean("sc", this.hasCobwebs);
            nbt.putBoolean("hps", this.hasSpawner);
            nbt.putInt("Num", this.length);
        }

        @Override
        protected void fillDownwards(StructureWorldAccess world, BlockState state, int x, int y,
                int z, BlockBox box) {
            BlockPos.Mutable mutable = this.offsetPos(x, y, z);
            if (!box.contains(mutable)) {
                return;
            }
            int yPos = mutable.getY();
            while (this.canReplace(world.getBlockState(mutable))
                    && mutable.getY() > world.getBottomY() + 1) {
                mutable.move(Direction.DOWN);
            }
            if (!this.isUpsideSolidFullSquare(world, mutable, world.getBlockState(mutable))) {
                return;
            }
            while (mutable.getY() < yPos) {
                mutable.move(Direction.UP);
                world.setBlockState(mutable, state, Block.NOTIFY_LISTENERS);
            }
        }
    }

    static abstract class MineshaftPart extends StructurePiece {

        protected CustomMineshaftStructure.Type mineshaftType;

        public MineshaftPart(StructurePieceType structurePieceType, int chainLength,
                CustomMineshaftStructure.Type type, BlockBox box) {
            super(structurePieceType, chainLength, box);
            this.mineshaftType = type;
        }

        public MineshaftPart(StructurePieceType structurePieceType, NbtCompound nbtCompound) {
            super(structurePieceType, nbtCompound);
            this.mineshaftType = CustomMineshaftStructure.Type.byId(nbtCompound.getInt("MST"));
        }

        @Override
        protected boolean canAddBlock(WorldView world, int x, int y, int z, BlockBox box) {
            BlockState blockState = this.getBlockAt(world, x, y, z, box);
            return !blockState.isOf(this.mineshaftType.getPlanks().getBlock()) && !blockState.isOf(
                    this.mineshaftType.getLog().getBlock()) && !blockState.isOf(
                    this.mineshaftType.getFence().getBlock()) && !blockState.isOf(Blocks.CHAIN);
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            nbt.putInt("MST", this.mineshaftType.ordinal());
        }

        protected boolean isSolidCeiling(BlockView world, BlockBox boundingBox, int minX, int maxX,
                int y, int z) {
            for (int x = minX; x <= maxX; ++x) {
                if (!this.getBlockAt(world, x, y + 1, z, boundingBox).isAir()) {
                    continue;
                }
                return false;
            }
            return true;
        }

        protected boolean cannotGenerate(WorldAccess world, BlockBox box) {
            int minX = Math.max(this.boundingBox.getMinX() - 1, box.getMinX());
            int minY = Math.max(this.boundingBox.getMinY() - 1, box.getMinY());
            int minZ = Math.max(this.boundingBox.getMinZ() - 1, box.getMinZ());
            int maxX = Math.min(this.boundingBox.getMaxX() + 1, box.getMaxX());
            int maxY = Math.min(this.boundingBox.getMaxY() + 1, box.getMaxY());
            int maxZ = Math.min(this.boundingBox.getMaxZ() + 1, box.getMaxZ());
            BlockPos.Mutable mutable = new BlockPos.Mutable((minX + maxX) / 2, (minY + maxY) / 2,
                    (minZ + maxZ) / 2);

            if (world.getBiome(mutable).isIn(BiomeTags.MINESHAFT_BLOCKING)) {
                return true;
            }
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (world.getBlockState(mutable.set(x, minY, z)).isLiquid()) {
                        return true;
                    }
                    if (!world.getBlockState(mutable.set(x, maxY, z)).isLiquid()) {
                        continue;
                    }
                    return true;
                }
            }
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minY; z <= maxY; ++z) {
                    if (world.getBlockState(mutable.set(x, z, minZ)).isLiquid()) {
                        return true;
                    }
                    if (!world.getBlockState(mutable.set(x, z, maxZ)).isLiquid()) {
                        continue;
                    }
                    return true;
                }
            }
            for (int x = minZ; x <= maxZ; ++x) {
                for (int z = minY; z <= maxY; ++z) {
                    if (world.getBlockState(mutable.set(minX, z, x)).isLiquid()) {
                        return true;
                    }
                    if (!world.getBlockState(mutable.set(maxX, z, x)).isLiquid()) {
                        continue;
                    }
                    return true;
                }
            }
            return false;
        }

        protected void tryPlaceFloor(StructureWorldAccess world, BlockBox box, BlockState state,
                int x, int y, int z) {
            if (!this.isUnderSeaLevel(world, x, y, z, box)) {
                return;
            }
            BlockPos.Mutable blockPos = this.offsetPos(x, y, z);
            BlockState blockState = world.getBlockState(blockPos);
            if (!blockState.isSideSolidFullSquare(world, blockPos, Direction.UP)) {
                world.setBlockState(blockPos, state, Block.NOTIFY_LISTENERS);
            }
        }
    }

    public static class MineshaftRoom extends CustomMineshaftGenerator.MineshaftPart {

        private final List<BlockBox> entrances = Lists.newLinkedList();

        public MineshaftRoom(int chainLength, Random random, int x, int z,
                CustomMineshaftStructure.Type type) {
            super(ModStructurePieceTypes.CUSTOM_MINESHAFT_ROOM, chainLength, type,
                    new BlockBox(x, 50, z, x + 7 + random.nextInt(6), 54 + random.nextInt(6),
                            z + 7 + random.nextInt(6)));
            this.mineshaftType = type;
        }

        public MineshaftRoom(StructureContext structureContext, NbtCompound nbt) {
            super(ModStructurePieceTypes.CUSTOM_MINESHAFT_ROOM, nbt);
            BlockBox.CODEC.listOf()
                    .parse(NbtOps.INSTANCE, nbt.getList("Entrances", NbtElement.INT_ARRAY_TYPE))
                    .resultOrPartial(Worldsinger.LOGGER::error)
                    .ifPresent(this.entrances::addAll);
        }

        @Override
        public void fillOpenings(StructurePiece start, StructurePiecesHolder holder,
                Random random) {
            BlockBox blockBox;
            CustomMineshaftGenerator.MineshaftPart mineshaftPart;
            int chainLength = this.getChainLength();
            int maxYRange = this.boundingBox.getBlockCountY() - 3 - 1;
            if (maxYRange <= 0) {
                maxYRange = 1;
            }
            for (int x = 0; x < this.boundingBox.getBlockCountX()
                    && (x += random.nextInt(this.boundingBox.getBlockCountX())) + 3
                    <= this.boundingBox.getBlockCountX(); x += 4) {
                mineshaftPart = CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                        this.boundingBox.getMinX() + x,
                        this.boundingBox.getMinY() + random.nextInt(maxYRange) + 1,
                        this.boundingBox.getMinZ() - 1, Direction.NORTH, chainLength);
                if (mineshaftPart == null) {
                    continue;
                }
                blockBox = mineshaftPart.getBoundingBox();
                this.entrances.add(new BlockBox(blockBox.getMinX(), blockBox.getMinY(),
                        this.boundingBox.getMinZ(), blockBox.getMaxX(), blockBox.getMaxY(),
                        this.boundingBox.getMinZ() + 1));
            }
            for (int x = 0; x < this.boundingBox.getBlockCountX()
                    && (x += random.nextInt(this.boundingBox.getBlockCountX())) + 3
                    <= this.boundingBox.getBlockCountX(); x += 4) {
                mineshaftPart = CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                        this.boundingBox.getMinX() + x,
                        this.boundingBox.getMinY() + random.nextInt(maxYRange) + 1,
                        this.boundingBox.getMaxZ() + 1, Direction.SOUTH, chainLength);
                if (mineshaftPart == null) {
                    continue;
                }
                blockBox = mineshaftPart.getBoundingBox();
                this.entrances.add(new BlockBox(blockBox.getMinX(), blockBox.getMinY(),
                        this.boundingBox.getMaxZ() - 1, blockBox.getMaxX(), blockBox.getMaxY(),
                        this.boundingBox.getMaxZ()));
            }
            for (int z = 0; z < this.boundingBox.getBlockCountZ()
                    && (z += random.nextInt(this.boundingBox.getBlockCountZ())) + 3
                    <= this.boundingBox.getBlockCountZ(); z += 4) {
                mineshaftPart = CustomMineshaftGenerator.pieceGenerator(start, holder, random,
                        this.boundingBox.getMinX() - 1,
                        this.boundingBox.getMinY() + random.nextInt(maxYRange) + 1,
                        this.boundingBox.getMinZ() + z, Direction.WEST, chainLength);
                if (mineshaftPart == null) {
                    continue;
                }
                blockBox = mineshaftPart.getBoundingBox();
                this.entrances.add(new BlockBox(this.boundingBox.getMinX(), blockBox.getMinY(),
                        blockBox.getMinZ(), this.boundingBox.getMinX() + 1, blockBox.getMaxY(),
                        blockBox.getMaxZ()));
            }
            for (int z = 0; z < this.boundingBox.getBlockCountZ()
                    && (z += random.nextInt(this.boundingBox.getBlockCountZ())) + 3
                    <= this.boundingBox.getBlockCountZ(); z += 4) {
                CustomMineshaftGenerator.MineshaftPart structurePiece = CustomMineshaftGenerator.pieceGenerator(
                        start, holder, random, this.boundingBox.getMaxX() + 1,
                        this.boundingBox.getMinY() + random.nextInt(maxYRange) + 1,
                        this.boundingBox.getMinZ() + z, Direction.EAST, chainLength);
                if (structurePiece == null) {
                    continue;
                }
                blockBox = structurePiece.getBoundingBox();
                this.entrances.add(new BlockBox(this.boundingBox.getMaxX() - 1, blockBox.getMinY(),
                        blockBox.getMinZ(), this.boundingBox.getMaxX(), blockBox.getMaxY(),
                        blockBox.getMaxZ()));
            }
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor,
                ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos,
                BlockPos pivot) {
            if (this.cannotGenerate(world, chunkBox)) {
                return;
            }
            this.fillWithOutline(world, chunkBox, this.boundingBox.getMinX(),
                    this.boundingBox.getMinY() + 1, this.boundingBox.getMinZ(),
                    this.boundingBox.getMaxX(),
                    Math.min(this.boundingBox.getMinY() + 3, this.boundingBox.getMaxY()),
                    this.boundingBox.getMaxZ(), AIR, AIR, false);
            for (BlockBox blockBox : this.entrances) {
                this.fillWithOutline(world, chunkBox, blockBox.getMinX(), blockBox.getMaxY() - 2,
                        blockBox.getMinZ(), blockBox.getMaxX(), blockBox.getMaxY(),
                        blockBox.getMaxZ(), AIR, AIR, false);
            }
            this.fillHalfEllipsoid(world, chunkBox, this.boundingBox.getMinX(),
                    this.boundingBox.getMinY() + 4, this.boundingBox.getMinZ(),
                    this.boundingBox.getMaxX(), this.boundingBox.getMaxY(),
                    this.boundingBox.getMaxZ(), AIR, false);
        }

        @Override
        public void translate(int x, int y, int z) {
            super.translate(x, y, z);
            this.entrances.replaceAll(blockBox -> blockBox.offset(x, y, z));
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            super.writeNbt(context, nbt);
            BlockBox.CODEC.listOf()
                    .encodeStart(NbtOps.INSTANCE, this.entrances)
                    .resultOrPartial(Worldsinger.LOGGER::error)
                    .ifPresent(nbtElement -> nbt.put("Entrances", nbtElement));
        }
    }
}


