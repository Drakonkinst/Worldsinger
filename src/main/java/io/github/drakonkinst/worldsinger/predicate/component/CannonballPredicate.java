package io.github.drakonkinst.worldsinger.predicate.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballCore;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import java.util.Optional;
import net.minecraft.component.ComponentType;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.component.ComponentSubPredicate;

public record CannonballPredicate(Optional<CannonballCore> core, NumberRange.IntRange contentSize,
                                  NumberRange.IntRange fuse) implements
        ComponentSubPredicate<CannonballComponent> {

    public static final Codec<CannonballPredicate> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(CannonballCore.CODEC.optionalFieldOf("core")
                                    .forGetter(CannonballPredicate::core),
                            NumberRange.IntRange.CODEC.optionalFieldOf("content_size",
                                    NumberRange.IntRange.ANY).forGetter(CannonballPredicate::contentSize),
                            NumberRange.IntRange.CODEC.optionalFieldOf("fuse", NumberRange.IntRange.ANY)
                                    .forGetter(CannonballPredicate::fuse))
                    .apply(instance, CannonballPredicate::new));

    @Override
    public ComponentType<CannonballComponent> getComponentType() {
        return ModDataComponentTypes.CANNONBALL;
    }

    @Override
    public boolean test(CannonballComponent component) {
        if (core.isPresent() && core.get() != component.core()) {
            return false;
        }
        return contentSize.test(component.getNumContents()) && fuse.test(component.fuse());
    }
}
