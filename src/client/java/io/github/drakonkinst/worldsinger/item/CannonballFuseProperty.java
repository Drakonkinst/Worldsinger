package io.github.drakonkinst.worldsinger.item;

import com.mojang.serialization.MapCodec;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record CannonballFuseProperty() implements NumericProperty {

    public static final MapCodec<CannonballFuseProperty> CODEC = MapCodec.unit(
            new CannonballFuseProperty());

    @Override
    public float getValue(ItemStack stack, @Nullable ClientWorld world,
            @Nullable LivingEntity holder, int seed) {
        CannonballComponent cannonballComponent = stack.getOrDefault(
                ModDataComponentTypes.CANNONBALL, CannonballComponent.DEFAULT);
        return cannonballComponent.fuse();
    }

    @Override
    public MapCodec<? extends NumericProperty> getCodec() {
        return CODEC;
    }
}
