package io.github.drakonkinst.worldsinger.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

public class SailedInSporeSeaCriterion extends
        AbstractCriterion<SailedInSporeSeaCriterion.Conditions> {

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, int sporeId, boolean isSeetheActive) {
        super.trigger(player, conditions -> {
            if (conditions.isSeetheActive.isPresent()) {
                if (isSeetheActive != conditions.isSeetheActive.get()) {
                    return false;
                }
            }
            if (!conditions.sporeTypes().isEmpty()) {
                return conditions.sporeTypes().contains(sporeId);
            }
            return true;
        });
    }

    // TODO: Switch to AetherSpores.CODEC if possible
    public record Conditions(Optional<LootContextPredicate> player, List<Integer> sporeTypes,
                             Optional<Boolean> isSeetheActive) implements
            AbstractCriterion.Conditions {

        public static final Codec<SailedInSporeSeaCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player")
                                        .forGetter(Conditions::player), Codecs.NON_NEGATIVE_INT.listOf()
                                        .fieldOf("spore_types")
                                        .forGetter(Conditions::sporeTypes),
                                Codec.BOOL.optionalFieldOf("is_seethe_active")
                                        .forGetter(Conditions::isSeetheActive))
                        .apply(instance, SailedInSporeSeaCriterion.Conditions::new));

        public static AdvancementCriterion<SailedInSporeSeaCriterion.Conditions> create(
                AetherSpores sporeType, boolean isSeetheActive) {
            return ModCriteria.SAILED_IN_SPORE_SEA.create(
                    new SailedInSporeSeaCriterion.Conditions(Optional.empty(),
                            Collections.singletonList(sporeType.getId()),
                            Optional.of(isSeetheActive)));
        }

        public static AdvancementCriterion<SailedInSporeSeaCriterion.Conditions> create(
                boolean isSeetheActive) {
            return ModCriteria.SAILED_IN_SPORE_SEA.create(
                    new SailedInSporeSeaCriterion.Conditions(Optional.empty(),
                            Collections.emptyList(), Optional.of(isSeetheActive)));
        }
    }
}
