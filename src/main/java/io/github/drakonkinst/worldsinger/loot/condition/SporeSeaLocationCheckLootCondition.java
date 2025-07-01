package io.github.drakonkinst.worldsinger.loot.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator;
import io.github.drakonkinst.worldsinger.worldgen.lumar.LumarChunkGenerator.SporeSeaEntry;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.context.ContextParameter;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

// TODO: This should really be a List<AetherSpores> and use the AetherSpores.CODEC, but the advancement JSON handler was throwing a fit
// So we'll go with the hacky solution of integers instead
public record SporeSeaLocationCheckLootCondition(List<Integer> sporeSeas,
                                                 BlockPos offset) implements LootCondition {

    private static final MapCodec<BlockPos> OFFSET_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codec.INT.optionalFieldOf("offsetX", 0).forGetter(Vec3i::getX),
                            Codec.INT.optionalFieldOf("offsetY", 0).forGetter(Vec3i::getY),
                            Codec.INT.optionalFieldOf("offsetZ", 0).forGetter(Vec3i::getZ))
                    .apply(instance, BlockPos::new));
    public static final MapCodec<SporeSeaLocationCheckLootCondition> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Codecs.NON_NEGATIVE_INT.listOf()
                                    .fieldOf("spore_seas")
                                    .forGetter(SporeSeaLocationCheckLootCondition::sporeSeas),
                            OFFSET_CODEC.forGetter(SporeSeaLocationCheckLootCondition::offset))
                    .apply(instance, SporeSeaLocationCheckLootCondition::new));

    @Override
    public LootConditionType getType() {
        return ModLootConditionTypes.SPORE_SEA_LOCATION;
    }

    public boolean test(LootContext lootContext) {
        ServerWorld world = lootContext.getWorld();
        if (!CosmerePlanet.isLumar(world)) {
            return false;
        }
        Vec3d origin = lootContext.get(LootContextParameters.ORIGIN);
        if (origin == null) {
            return false;
        }
        int x = (int) origin.getX() + this.offset.getX();
        int z = (int) origin.getZ() + this.offset.getZ();
        SporeSeaEntry entry = LumarChunkGenerator.getSporeSeaEntryAtPos(
                world.getChunkManager().getNoiseConfig(), x, z);
        // AetherSpores sporeType = AetherSpores.getAetherSporeTypeById(entry.id());
        return sporeSeas.contains(entry.id());
    }

    @Override
    public Set<ContextParameter<?>> getAllowedParameters() {
        return Set.of(LootContextParameters.ORIGIN);
    }

    public static LootCondition.Builder builder(AetherSpores sporeSea) {
        return () -> new SporeSeaLocationCheckLootCondition(
                Collections.singletonList(sporeSea.getId()), BlockPos.ORIGIN);
    }

    public static LootCondition.Builder builder(List<AetherSpores> sporeSeas) {
        return () -> new SporeSeaLocationCheckLootCondition(
                sporeSeas.stream().map(AetherSpores::getId).toList(), BlockPos.ORIGIN);
    }

    public static LootCondition.Builder builder(List<AetherSpores> sporeSeas, BlockPos pos) {
        return () -> new SporeSeaLocationCheckLootCondition(
                sporeSeas.stream().map(AetherSpores::getId).toList(), pos);
    }
}
