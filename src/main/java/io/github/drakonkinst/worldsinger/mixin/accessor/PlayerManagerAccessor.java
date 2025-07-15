package io.github.drakonkinst.worldsinger.mixin.accessor;

import net.minecraft.server.PlayerManager;
import net.minecraft.world.PlayerSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerManager.class)
public interface PlayerManagerAccessor {

    @Accessor("saveHandler")
    PlayerSaveHandler worldsinger$getSaveHandler();
}
