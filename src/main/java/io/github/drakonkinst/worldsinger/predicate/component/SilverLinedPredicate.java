package io.github.drakonkinst.worldsinger.predicate.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.item.component.SilverLinedComponent;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import net.minecraft.component.ComponentType;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.component.ComponentSubPredicate;

public record SilverLinedPredicate(NumberRange.IntRange durability) implements
        ComponentSubPredicate<SilverLinedComponent> {

    public static final Codec<SilverLinedPredicate> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            NumberRange.IntRange.CODEC.optionalFieldOf("silver_durability",
                                    NumberRange.IntRange.ANY).forGetter(SilverLinedPredicate::durability))
                    .apply(instance, SilverLinedPredicate::new));

    @Override
    public ComponentType<SilverLinedComponent> getComponentType() {
        return ModDataComponentTypes.SILVER_DURABILITY;
    }

    @Override
    public boolean test(SilverLinedComponent component) {
        int value = 0;
        if (component != null) {
            value = component.value();
        }
        return this.durability.test(value);
    }
}
