package io.github.drakonkinst.worldsinger.entity.render;

import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;

// Hacky workaround because getModel() method is colliding
public interface CustomFeatureRendererContext<S extends EntityRenderState, M extends EntityModel<? super S>> extends
        FeatureRendererContext<S, M> {

    M worldsinger$getContextModel();

    @Override
    default M getModel() {
        return worldsinger$getContextModel();
    }
}
