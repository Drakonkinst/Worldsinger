package io.github.drakonkinst.worldsinger.network;

import io.github.drakonkinst.worldsinger.entity.PlayerMorphDummy;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

// Proxy pattern that acts as a bridge to call client-specific code from the server
public class CommonProxy {

    public LivingEntity createPlayerMorph(World world, UUID uuid, String playerName) {
        return new PlayerMorphDummy(world, uuid, playerName);
    }

    // Handled by client
    public void setRenderViewEntity(Entity entity) {
        throw new UnsupportedOperationException();
    }

    // Handled by client
    public boolean resetRenderViewEntity() {
        throw new UnsupportedOperationException();
    }
}
