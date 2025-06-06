package io.github.drakonkinst.worldsinger.mixin.client.entity.render;

import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.model.AbstractBoatEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.BoatEntityRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({ BoatEntityRenderer.class })
public abstract class BoatEntityRendererMixin extends AbstractBoatEntityRendererMixin {

    @Shadow
    @Final
    private EntityModel<BoatEntityRenderState> model;

    protected BoatEntityRendererMixin(Context context) {
        super(context);
    }

    @Override
    public AbstractBoatEntityModel worldsinger$getContextModel() {
        return (AbstractBoatEntityModel) this.model;
    }
}
