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

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.registry.ModLootTables;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class CustomMineshaftStructure extends Structure {

    public static final Codec<CustomMineshaftStructure> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(CustomMineshaftStructure.configCodecBuilder(instance),
                            (Type.CODEC.fieldOf("mineshaft_type")).forGetter(
                                    mineshaftStructure -> mineshaftStructure.type))
                    .apply(instance, CustomMineshaftStructure::new));
    private final Type type;

    public CustomMineshaftStructure(Structure.Config config, Type type) {
        super(config);
        this.type = type;
    }

    @Override
    public Optional<Structure.StructurePosition> getStructurePosition(Structure.Context context) {
        context.random().nextDouble();
        ChunkPos chunkPos = context.chunkPos();
        BlockPos blockPos = new BlockPos(chunkPos.getCenterX(), 50, chunkPos.getStartZ());
        StructurePiecesCollector structurePiecesCollector = new StructurePiecesCollector();
        int i = this.addPieces(structurePiecesCollector, context);
        return Optional.of(new Structure.StructurePosition(blockPos.add(0, i, 0),
                Either.right(structurePiecesCollector)));
    }

    private int addPieces(StructurePiecesCollector collector, Structure.Context context) {
        ChunkPos chunkPos = context.chunkPos();
        ChunkRandom chunkRandom = context.random();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        CustomMineshaftGenerator.MineshaftRoom mineshaftRoom = new CustomMineshaftGenerator.MineshaftRoom(
                0, chunkRandom, chunkPos.getOffsetX(2), chunkPos.getOffsetZ(2), this.type);
        collector.addPiece(mineshaftRoom);
        mineshaftRoom.fillOpenings(mineshaftRoom, collector, chunkRandom);

        int seaLevel = chunkGenerator.getSeaLevel();
        if (this.type.isSurface()) {
            BlockPos blockPos = collector.getBoundingBox().getCenter();
            int surfaceHeight = chunkGenerator.getHeight(blockPos.getX(), blockPos.getZ(),
                    Heightmap.Type.WORLD_SURFACE_WG, context.world(), context.noiseConfig());
            int yPos = surfaceHeight <= seaLevel ? seaLevel
                    : MathHelper.nextBetween(chunkRandom, seaLevel, surfaceHeight);
            int shiftAmount = yPos - blockPos.getY();
            collector.shift(shiftAmount);
            return shiftAmount;
        }

        return collector.shiftInto(seaLevel, chunkGenerator.getMinimumY(), chunkRandom, 10);
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.MINESHAFT;
    }

    public enum Type implements StringIdentifiable {
        LUMAR_OAK("lumar_oak", Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_FENCE, true, true,
                false, false, true, ModLootTables.LUMAR_SALTSTONE_MINESHAFT_CHEST),
        LUMAR_BIRCH("lumar_birch", Blocks.BIRCH_LOG, Blocks.BIRCH_PLANKS, Blocks.BIRCH_FENCE, true,
                true, false, false, true, ModLootTables.LUMAR_SALTSTONE_MINESHAFT_CHEST);

        public static final Codec<Type> CODEC;
        private static final IntFunction<Type> BY_ID;

        static {
            CODEC = StringIdentifiable.createCodec(Type::values);
            BY_ID = ValueLists.createIdToValueFunction((ToIntFunction<Type>) Enum::ordinal,
                    Type.values(), ValueLists.OutOfBoundsHandling.ZERO);
        }

        public static Type byId(int id) {
            return BY_ID.apply(id);
        }

        private final String name;
        private final BlockState log;
        private final BlockState planks;
        private final BlockState fence;
        private final boolean isSurface;
        private final boolean hasRails;
        private final boolean hasCobwebs;
        private final boolean hasSpawner;
        private final boolean hasLoot;
        private final RegistryKey<LootTable> lootTable;

        Type(String name, Block log, Block planks, Block fence, boolean isSurface, boolean hasRails,
                boolean hasCobwebs, boolean hasSpawner, boolean hasLoot,
                RegistryKey<LootTable> lootTable) {
            this.name = name;
            this.log = log.getDefaultState();
            this.planks = planks.getDefaultState();
            this.fence = fence.getDefaultState();
            this.isSurface = isSurface;
            this.hasRails = hasRails;
            this.hasCobwebs = hasCobwebs;
            this.hasLoot = hasLoot;
            this.hasSpawner = hasSpawner;
            this.lootTable = lootTable;
        }

        public String getName() {
            return this.name;
        }

        public BlockState getLog() {
            return this.log;
        }

        public BlockState getPlanks() {
            return this.planks;
        }

        public BlockState getFence() {
            return this.fence;
        }

        public RegistryKey<LootTable> getLootTable() {
            return lootTable;
        }

        public boolean isSurface() {
            return this.isSurface;
        }

        public boolean canHaveCobwebs() {
            return hasCobwebs;
        }

        public boolean canHaveRails() {
            return hasRails;
        }

        public boolean canHaveSpawner() {
            return hasSpawner;
        }

        public boolean canHaveLoot() {
            return hasLoot;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }
}
