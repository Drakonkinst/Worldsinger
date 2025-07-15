package io.github.drakonkinst.worldsinger.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

public class SailedNearLunagreeCriterion extends
        AbstractCriterion<SailedNearLunagreeCriterion.Conditions> {

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, int sporeId, double distSqToLunagree) {
        super.trigger(player, conditions -> {
            if (!conditions.sporeTypes().isEmpty()) {
                if (!conditions.sporeTypes().contains(sporeId)) {
                    return false;
                }
            }
            return distSqToLunagree <= conditions.maxDistance() * conditions.maxDistance();
        });
    }

    // TODO: Switch to AetherSpores.CODEC if possible
    public record Conditions(Optional<LootContextPredicate> player, List<Integer> sporeTypes,
                             double maxDistance) implements AbstractCriterion.Conditions {

        public static final Codec<SailedNearLunagreeCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player")
                                        .forGetter(Conditions::player), Codecs.NON_NEGATIVE_INT.listOf()
                                        .fieldOf("spore_types")
                                        .forGetter(Conditions::sporeTypes),
                                Codec.DOUBLE.fieldOf("max_distance").forGetter(Conditions::maxDistance))
                        .apply(instance, SailedNearLunagreeCriterion.Conditions::new));

        public static AdvancementCriterion<SailedNearLunagreeCriterion.Conditions> create(
                double maxDistance) {
            return ModCriteria.SAILED_NEAR_LUNAGREE.create(
                    new SailedNearLunagreeCriterion.Conditions(Optional.empty(),
                            Collections.emptyList(), maxDistance));
        }
    }
}
