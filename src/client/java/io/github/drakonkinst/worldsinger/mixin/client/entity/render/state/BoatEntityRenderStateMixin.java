package io.github.drakonkinst.worldsinger.mixin.client.entity.render.state;

import io.github.drakonkinst.worldsinger.entity.render.state.BoatEntityRenderStateSilverLining;
import net.minecraft.client.render.entity.state.BoatEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BoatEntityRenderState.class)
public abstract class BoatEntityRenderStateMixin implements BoatEntityRenderStateSilverLining {

    @Unique
    private int silverLiningVariant = 0;

    public void worldsinger$setSilverLiningVariant(int value) {
        this.silverLiningVariant = value;
    }

    @Override
    public int worldsinger$getSilverLiningVariant() {
        return silverLiningVariant;
    }
}
