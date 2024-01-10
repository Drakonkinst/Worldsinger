package io.github.drakonkinst.worldsinger.entity.render;

import com.mojang.datafixers.util.Pair;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.component.ModComponents;
import io.github.drakonkinst.worldsinger.component.SilverLinedComponent;
import io.github.drakonkinst.worldsinger.cosmere.SilverLiningLevel;
import java.util.Map;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.CompositeEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.BoatEntity.Type;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.util.Identifier;

public class BoatSilverLiningFeatureRenderer extends FeatureRenderer<BoatEntity, BoatEntityModel> {

    private static final int CHEST_VALUE = 8;
    private static final int RAFT_VALUE = 4;
    private static final Identifier[] TEXTURE_MAP = BoatSilverLiningFeatureRenderer.generateTextureMap();

    private static Identifier[] generateTextureMap() {
        String[] entityTypes = { "boat", "chest_boat" };
        String[] boatVariants = { "boat", "raft" };
        String[] silverLevels = { "low", "medium", "high", "perfect" };
        Identifier[] textureMap = new Identifier[entityTypes.length * boatVariants.length
                * silverLevels.length];

        int i = 0;
        for (String entityType : entityTypes) {
            for (String boatVariant : boatVariants) {
                for (String silverLevel : silverLevels) {
                    textureMap[i++] = Worldsinger.id(
                            "textures/entity/" + entityType + "/" + boatVariant + "_silver_lining_"
                                    + silverLevel + ".png");
                }
            }
        }

        return textureMap;
    }

    // Boat variant can be represented as a binary number
    // Chest = 1 bit, Raft = 1 bit, Silver Lining State = 2 bits
    // Returns a negative number if not silver-lined
    private static int encodeBoatVariant(BoatEntity entity) {
        SilverLinedComponent silverData = ModComponents.SILVER_LINED.get(entity);
        float durabilityFraction =
                (float) silverData.getSilverDurability() / silverData.getMaxSilverDurability();
        SilverLiningLevel level = SilverLiningLevel.fromDurability(durabilityFraction);
        if (level == SilverLiningLevel.NONE) {
            return -1;
        }

        boolean hasChest = entity instanceof ChestBoatEntity;
        boolean isRaft = entity.getVariant() == Type.BAMBOO;
        int silverLiningValue = level.ordinal() - 1;
        int encoded = (hasChest ? CHEST_VALUE : 0) + (isRaft ? RAFT_VALUE : 0) + silverLiningValue;
        return encoded;
    }

    protected static <T extends Entity> void renderModel(EntityModel<T> model, Identifier texture,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(
                RenderLayer.getEntityCutoutNoCull(texture));
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f,
                1.0f);
    }

    private final Map<Type, Pair<Identifier, CompositeEntityModel<BoatEntity>>> texturesToModels;

    public BoatSilverLiningFeatureRenderer(
            FeatureRendererContext<BoatEntity, BoatEntityModel> context,
            Map<Type, Pair<Identifier, CompositeEntityModel<BoatEntity>>> texturesToModels) {
        super(context);
        this.texturesToModels = texturesToModels;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            BoatEntity entity, float limbAngle, float limbDistance, float tickDelta,
            float animationProgress, float headYaw, float headPitch) {
        int encodedVariant = BoatSilverLiningFeatureRenderer.encodeBoatVariant(entity);
        if (encodedVariant < 0) {
            return;
        }

        Identifier texture = TEXTURE_MAP[encodedVariant];
        BoatSilverLiningFeatureRenderer.renderModel(this.getModelForBoat(entity), texture, matrices,
                vertexConsumers, light);
    }

    private EntityModel<BoatEntity> getModelForBoat(BoatEntity entity) {
        return texturesToModels.get(entity.getVariant()).getSecond();
    }
}
