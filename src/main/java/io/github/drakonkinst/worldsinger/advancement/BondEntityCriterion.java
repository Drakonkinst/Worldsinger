package io.github.drakonkinst.worldsinger.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.entity.LootContextPredicateValidator;
import net.minecraft.server.network.ServerPlayerEntity;

public class BondEntityCriterion extends AbstractCriterion<BondEntityCriterion.Conditions> {

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, Entity entity) {
        LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player,
                entity);
        this.trigger(player, conditions -> conditions.matches(lootContext));
    }

    public record Conditions(Optional<LootContextPredicate> player,
                             Optional<LootContextPredicate> entity) implements
            AbstractCriterion.Conditions {

        public static final Codec<BondEntityCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player")
                                        .forGetter(BondEntityCriterion.Conditions::player),
                                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("entity")
                                        .forGetter(BondEntityCriterion.Conditions::entity))
                        .apply(instance, BondEntityCriterion.Conditions::new));

        public static AdvancementCriterion<BondEntityCriterion.Conditions> any() {
            return ModCriteria.BOND_ENTITY.create(
                    new BondEntityCriterion.Conditions(Optional.empty(), Optional.empty()));
        }

        public static AdvancementCriterion<BondEntityCriterion.Conditions> create(
                EntityPredicate.Builder entity) {
            return ModCriteria.BOND_ENTITY.create(
                    new BondEntityCriterion.Conditions(Optional.empty(), Optional.of(
                            EntityPredicate.contextPredicateFromEntityPredicate(entity))));
        }

        public boolean matches(LootContext entity) {
            return this.entity.isEmpty() || this.entity.get().test(entity);
        }

        @Override
        public void validate(LootContextPredicateValidator validator) {
            AbstractCriterion.Conditions.super.validate(validator);
            validator.validateEntityPredicate(this.entity, "entity");
        }
    }
}
