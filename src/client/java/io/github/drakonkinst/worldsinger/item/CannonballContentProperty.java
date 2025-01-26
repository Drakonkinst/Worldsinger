package io.github.drakonkinst.worldsinger.item;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballContent;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import java.util.List;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

public record CannonballContentProperty(int index) implements SelectProperty<CannonballContent> {

    public static final SelectProperty.Type<CannonballContentProperty, CannonballContent> TYPE = SelectProperty.Type.create(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Codecs.NON_NEGATIVE_INT.optionalFieldOf("index", 0)
                                    .forGetter(CannonballContentProperty::index))
                    .apply(instance, CannonballContentProperty::new)), CannonballContent.CODEC);

    @Nullable
    @Override
    public CannonballContent getValue(ItemStack stack, @Nullable ClientWorld world,
            @Nullable LivingEntity user, int seed,
            ModelTransformationMode modelTransformationMode) {
        CannonballComponent cannonballComponent = stack.getOrDefault(
                ModDataComponentTypes.CANNONBALL, CannonballComponent.DEFAULT);
        List<CannonballContent> contents = cannonballComponent.contents();
        if (index < 0 || index >= contents.size()) {
            return null;
        }
        return contents.get(index);
    }

    @Override
    public Type<? extends SelectProperty<CannonballContent>, CannonballContent> getType() {
        return TYPE;
    }

}
