package io.github.drakonkinst.worldsinger.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

public class FindIconOnMapCriterion extends AbstractCriterion<FindIconOnMapCriterion.Conditions> {

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player,
            Set<RegistryKey<MapDecorationType>> seenMapDecorations) {
        super.trigger(player, conditions -> {
            for (RegistryEntry<MapDecorationType> mapDecorationType : conditions.decorationTypes()) {
                if (seenMapDecorations.contains(mapDecorationType.getKey().orElse(null))) {
                    return true;
                }
            }
            return false;
        });
    }

    // TODO: Switch to AetherSpores.CODEC if possible
    public record Conditions(Optional<LootContextPredicate> player,
                             List<RegistryEntry<MapDecorationType>> decorationTypes) implements
            AbstractCriterion.Conditions {

        public static final Codec<FindIconOnMapCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player")
                                        .forGetter(Conditions::player), MapDecorationType.CODEC.listOf()
                                        .fieldOf("decoration_types")
                                        .forGetter(Conditions::decorationTypes))
                        .apply(instance, FindIconOnMapCriterion.Conditions::new));

        public static AdvancementCriterion<FindIconOnMapCriterion.Conditions> create(
                RegistryEntry<MapDecorationType> mapDecorationType) {
            return ModCriteria.FIND_ICON_ON_MAP.create(
                    new FindIconOnMapCriterion.Conditions(Optional.empty(),
                            Collections.singletonList(mapDecorationType)));
        }
    }
}
