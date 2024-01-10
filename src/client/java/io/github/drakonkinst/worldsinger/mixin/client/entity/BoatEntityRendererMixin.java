package io.github.drakonkinst.worldsinger.mixin.client.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.drakonkinst.worldsinger.entity.render.BoatSilverLiningFeatureRenderer;
import java.util.List;
import java.util.Map;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.CompositeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.BoatEntity.Type;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntityRenderer.class)
public abstract class BoatEntityRendererMixin extends EntityRenderer<BoatEntity> implements
        FeatureRendererContext<BoatEntity, BoatEntityModel> {

    @Unique
    protected final List<FeatureRenderer<BoatEntity, BoatEntityModel>> features = Lists.newArrayList();
    @Shadow
    @Final
    private Map<Type, Pair<Identifier, CompositeEntityModel<BoatEntity>>> texturesAndModels;

    protected BoatEntityRendererMixin(Context ctx) {
        super(ctx);
    }

    @Override
    public BoatEntityModel getModel() {
        // Should not be used, model is generated based on Type variant
        return null;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addSilverLiningFeature(Context ctx, boolean chest, CallbackInfo ci) {
        this.features.add(new BoatSilverLiningFeatureRenderer(this, texturesAndModels));
    }

    @Inject(method = "render(Lnet/minecraft/entity/vehicle/BoatEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void renderSilverLining(BoatEntity boatEntity, float f, float g,
            MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i,
            CallbackInfo ci) {
        for (FeatureRenderer<BoatEntity, BoatEntityModel> featureRenderer : this.features) {
            featureRenderer.render(matrixStack, vertexConsumerProvider, i, boatEntity, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 0.0f);
        }
    }
}
