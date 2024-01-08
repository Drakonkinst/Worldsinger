package io.github.drakonkinst.worldsinger.mixin.client.world;

import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererPossessionMixin {

    // Allow the player model to still be rendered while possessing another mob
    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyConstant(method = "render", constant = @Constant(classValue = ClientPlayerEntity.class))
    private static boolean allowRenderPlayerModel(Object obj, Class<?> clazz, MatrixStack matrices,
            float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera,
            GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager,
            Matrix4f projectionMatrix) {
        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
        if (cameraEntity instanceof CameraPossessable) {

            // If a Midnight Creature Entity, the entity used for rendering is not actually the
            // Midnight Creature itself. Therefore, we need to exclude the rendered entity from
            // being rendered while possessing it.
            if (cameraEntity instanceof MidnightCreatureEntity midnightCreature
                    && midnightCreature.getMorph() != null && midnightCreature.equals(obj)
                    && !camera.isThirdPerson()) {
                return true;
            }

            // Allow ClientPlayerEntity to be rendered when using a CameraPossessable
            return false;
        }
        return clazz.isAssignableFrom(obj.getClass());
    }
}
