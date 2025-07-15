package io.github.drakonkinst.worldsinger.mixin.accessor;

import net.minecraft.entity.passive.TropicalFishEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TropicalFishEntity.class)
public interface TropicalFishEntityInvoker {

    @Invoker("getTropicalFishVariant")
    int worldsinger$getTropicalFishVariant();

    @Invoker("setTropicalFishVariant")
    void worldsinger$setTropicalFishVariant(int variant);
}
