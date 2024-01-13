package io.github.drakonkinst.worldsinger.mixin.accessor;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerEntity.class)
public interface PlayerEntityInvoker {

    @Invoker("shouldDismount")
    boolean worldsinger$shouldDismount();
}
