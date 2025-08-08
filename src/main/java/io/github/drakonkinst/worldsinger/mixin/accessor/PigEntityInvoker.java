package io.github.drakonkinst.worldsinger.mixin.accessor;

import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PigVariant;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PigEntity.class)
public interface PigEntityInvoker {

    @Invoker("setVariant")
    void worldsinger$setVariant(RegistryEntry<PigVariant> variant);
}
