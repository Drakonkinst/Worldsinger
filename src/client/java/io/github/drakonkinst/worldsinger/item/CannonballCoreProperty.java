package io.github.drakonkinst.worldsinger.item;

import com.mojang.serialization.MapCodec;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballCore;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import org.jetbrains.annotations.Nullable;

public record CannonballCoreProperty() implements SelectProperty<CannonballCore> {

    public static final SelectProperty.Type<CannonballCoreProperty, CannonballCore> TYPE = SelectProperty.Type.create(
            MapCodec.unit(new CannonballCoreProperty()), CannonballCore.CODEC);

    @Nullable
    @Override
    public CannonballCore getValue(ItemStack stack, @Nullable ClientWorld world,
            @Nullable LivingEntity user, int seed,
            ModelTransformationMode modelTransformationMode) {
        CannonballComponent cannonballComponent = stack.getOrDefault(
                ModDataComponentTypes.CANNONBALL, CannonballComponent.DEFAULT);
        return cannonballComponent.core();
    }

    @Override
    public Type<? extends SelectProperty<CannonballCore>, CannonballCore> getType() {
        return TYPE;
    }
}
