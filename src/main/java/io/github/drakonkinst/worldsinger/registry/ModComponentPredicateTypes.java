package io.github.drakonkinst.worldsinger.registry;

import com.mojang.serialization.Codec;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.item.component.SilverLinedPredicate;
import net.minecraft.predicate.component.ComponentPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModComponentPredicateTypes {

    public static final ComponentPredicate.Type<SilverLinedPredicate> SILVER_LINED = register(
            "silver_lined", SilverLinedPredicate.CODEC);

    private static <T extends ComponentPredicate> ComponentPredicate.Type<T> register(String id,
            Codec<T> codec) {
        return Registry.register(Registries.DATA_COMPONENT_PREDICATE_TYPE, Worldsinger.idStr(id),
                new ComponentPredicate.Type<>(codec));
    }

    public static void initialize() {}

    private ModComponentPredicateTypes() {}
}
