package io.github.drakonkinst.worldsinger.advancement;

import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;

public final class ModCriteria {

    public static final SailedInSporeSeaCriterion SAILED_IN_SPORE_SEA = register(
            "sailed_in_spore_sea", new SailedInSporeSeaCriterion());
    public static final SailedNearLunagreeCriterion SAILED_NEAR_LUNAGREE = register(
            "sailed_near_lunagree", new SailedNearLunagreeCriterion());
    public static final BondEntityCriterion BOND_ENTITY = register("bond_entity",
            new BondEntityCriterion());
    public static final FindIconOnMapCriterion FIND_ICON_ON_MAP = register("find_icon_on_map",
            new FindIconOnMapCriterion());

    public static void initialize() {}

    public static <T extends Criterion<?>> T register(String id, T criterion) {
        return Criteria.register(Worldsinger.idStr(id), criterion);
    }

    private ModCriteria() {}
}
