package io.github.drakonkinst.worldsinger.loot.condition;

import com.mojang.serialization.MapCodec;
import io.github.drakonkinst.worldsinger.Worldsinger;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModLootConditionTypes {

    public static final LootConditionType SPORE_SEA_LOCATION = register("spore_sea_location",
            SporeSeaLocationCheckLootCondition.CODEC);

    private static LootConditionType register(String id, MapCodec<? extends LootCondition> codec) {
        return Registry.register(Registries.LOOT_CONDITION_TYPE, Worldsinger.id(id),
                new LootConditionType(codec));
    }

    public static void initialize() {}

    private ModLootConditionTypes() {}
}
