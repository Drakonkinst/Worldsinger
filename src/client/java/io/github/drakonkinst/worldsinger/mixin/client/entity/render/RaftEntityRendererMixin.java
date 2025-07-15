package io.github.drakonkinst.worldsinger.mixin.client.entity.render;

import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.RaftEntityRenderer;
import net.minecraft.client.render.entity.model.AbstractBoatEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.BoatEntityRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({ RaftEntityRenderer.class })
public abstract class RaftEntityRendererMixin extends AbstractBoatEntityRendererMixin {

    @Shadow
    @Final
    private EntityModel<BoatEntityRenderState> model;

    protected RaftEntityRendererMixin(Context context) {
        super(context);
    }

    @Override
    public AbstractBoatEntityModel worldsinger$getContextModel() {
        return (AbstractBoatEntityModel) this.model;
    }
}
