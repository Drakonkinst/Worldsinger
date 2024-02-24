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

package io.github.drakonkinst.worldsinger.command;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.CosmereWorldUtil;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.DeadSpores;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator.SporeSeaEntry;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.MultiNoiseSampler;
import org.jetbrains.annotations.Nullable;

// Not actually an actual command, but mixed into the vanilla /locate command
public class LocateSporeSeaCommand {

    private static final DynamicCommandExceptionType SPORE_SEA_NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType(
            id -> Text.stringifiedTranslatable("commands.locate.spore_sea.not_found", id));
    private static final DynamicCommandExceptionType SPORE_SEA_INVALID_EXCEPTION = new DynamicCommandExceptionType(
            id -> Text.stringifiedTranslatable("commands.locate.spore_sea.invalid", id));

    public static int executeLocateSporeSea(CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        String aetherSporeTypeStr = getString(context, "spore_type");
        Optional<AetherSpores> aetherSporeType = AetherSpores.getAetherSporeTypeFromString(
                aetherSporeTypeStr);
        if (aetherSporeType.isEmpty()) {
            throw RegistryEntryArgumentType.NOT_FOUND_EXCEPTION.create(aetherSporeTypeStr,
                    "spore_sea");
        }

        AetherSpores spores = aetherSporeType.get();
        if (spores.getId() == DeadSpores.ID) {
            throw SPORE_SEA_INVALID_EXCEPTION.create(spores.getName());
        }

        BlockPos originPos = BlockPos.ofFloored(source.getPosition());
        Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
        // Pair<BlockPos, SporeSeaEntry> result = LocateSporeSeaCommand.locateSporeSea(
        //         source.getWorld(), originPos, 6400, 64, false, IntSet.of(spores.getId()),
        //         biome -> ModBiomes.DEEP_SPORE_SEA.equals(biome.getKey().orElse(null)));
        Pair<BlockPos, SporeSeaEntry> result = LocateSporeSeaCommand.locateSporeSea(
                source.getWorld(), originPos.getX(), originPos.getZ(), 6400, 64, false,
                IntSet.of(spores.getId()), null);
        stopwatch.stop();
        if (result == null) {
            throw SPORE_SEA_NOT_FOUND_EXCEPTION.create(spores.getSeaDisplayName());
        } else {
            BlockPos locatedPos = result.getFirst();
            return LocateSporeSeaCommand.sendCoordinates(source, originPos, locatedPos, spores,
                    stopwatch.elapsed());
        }
    }

    private static int sendCoordinates(ServerCommandSource source, BlockPos originPos,
            BlockPos locatedPos, AetherSpores spores, Duration timeTaken) {
        Text coordinatesText = LocateSporeSeaCommand.createCoordinatesText(locatedPos);
        int distanceInBlocks = MathHelper.floor(
                BlockPosUtil.getDistance(originPos.getX(), 0, originPos.getZ(), locatedPos.getX(),
                        0, locatedPos.getZ()));
        source.sendFeedback(() -> Text.translatable("commands.locate.spore_sea.success",
                spores.getSeaDisplayName(), coordinatesText, distanceInBlocks), false);
        Worldsinger.LOGGER.info(
                "Locating element " + spores.getSeaDisplayName().getString() + " took "
                        + timeTaken.toMillis() + " ms");
        return 1;
    }

    private static Text createCoordinatesText(BlockPos blockPos) {
        return Texts.bracketed(
                        Text.translatable("chat.coordinates", blockPos.getX(), "~", blockPos.getZ()))
                .styled(style -> style.withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/tp @s " + blockPos.getX() + " ~ " + blockPos.getZ()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.translatable("chat.coordinates.tooltip"))));
    }

    @Nullable
    public static Pair<BlockPos, SporeSeaEntry> locateSporeSea(ServerWorld world, int originX,
            int originZ, int radius, int horizontalBlockCheckInterval, boolean mustBeAboveSeaLevel,
            IntSet filterSporeIds, @Nullable Predicate<RegistryEntry<Biome>> biomePredicate) {
        if (!CosmereWorldUtil.isLumar(world)) {
            return null;
        }
        MultiNoiseSampler noiseSampler = world.getChunkManager()
                .getNoiseConfig()
                .getMultiNoiseSampler();
        int cellRadius = Math.floorDiv(radius, horizontalBlockCheckInterval);
        for (Mutable mutable : BlockPos.iterateInSquare(BlockPos.ORIGIN, cellRadius, Direction.EAST,
                Direction.SOUTH)) {
            int x = originX + mutable.getX() * horizontalBlockCheckInterval;
            int z = originZ + mutable.getZ() * horizontalBlockCheckInterval;
            SporeSeaEntry entry = LumarChunkGenerator.getSporeSeaEntryAtPos(
                    world.getChunkManager().getNoiseConfig(), x, z);
            if (filterSporeIds.contains(entry.id())) {
                if (mustBeAboveSeaLevel) {
                    // TODO: Problem here is that this only works for loaded chunks. World#getTopY() returns the bottommost world position otherwise.
                    // TODO: Perhaps look for places with high continentalness instead? Or locate a good starting biome like a saltstone island.
                    int yPos = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
                    if (yPos > LumarChunkGenerator.SEA_LEVEL) {
                        return Pair.of(new BlockPos(x, yPos, z), entry);
                    }
                    continue;
                }
                if (biomePredicate != null) {
                    int biomeX = BiomeCoords.fromBlock(x);
                    int biomeY = BiomeCoords.fromBlock(LumarChunkGenerator.SEA_LEVEL);
                    int biomeZ = BiomeCoords.fromBlock(z);
                    if (!biomePredicate.test(world.getChunkManager()
                            .getChunkGenerator()
                            .getBiomeSource()
                            .getBiome(biomeX, biomeY, biomeZ, noiseSampler))) {
                        continue;
                    }
                }
                return Pair.of(new BlockPos(x, LumarChunkGenerator.SEA_LEVEL, z), entry);
            }
        }
        return null;
    }

}
