/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Drakonkinst
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.drakonkinst.worldsinger.entity.render;

import com.google.common.collect.ImmutableList;
import io.github.drakonkinst.worldsinger.entity.ShapeshiftingEntity;
import io.github.drakonkinst.worldsinger.entity.render.state.ShapeshiftingEntityRenderState;
import io.github.drakonkinst.worldsinger.mixin.accessor.EntityAccessor;
import io.github.drakonkinst.worldsinger.mixin.accessor.LimbAnimatorAccessor;
import io.github.drakonkinst.worldsinger.mixin.accessor.LivingEntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.util.Hand;

public abstract class ShapeshiftingEntityRenderer<T extends ShapeshiftingEntity, S extends ShapeshiftingEntityRenderState, M extends EntityModel<S>> extends
        MobEntityRenderer<T, S, M> {

    public static void updateMorphAttributes(ShapeshiftingEntity entity, LivingEntity morph) {
        // Copy LimbAnimator attributes
        LimbAnimator target = morph.limbAnimator;
        LimbAnimator source = entity.limbAnimator;
        target.setSpeed(source.getSpeed());
        ((LimbAnimatorAccessor) target).worldsinger$setLastSpeed(
                ((LimbAnimatorAccessor) source).worldsinger$getLastSpeed());
        ((LimbAnimatorAccessor) target).worldsinger$setAnimationProgress(
                source.getAnimationProgress());

        // Sync data
        morph.handSwinging = entity.handSwinging;
        morph.handSwingTicks = entity.handSwingTicks;
        morph.lastHandSwingProgress = entity.lastHandSwingProgress;
        morph.handSwingProgress = entity.handSwingProgress;
        morph.bodyYaw = entity.bodyYaw;
        morph.lastBodyYaw = entity.lastBodyYaw;
        morph.headYaw = entity.headYaw;
        morph.lastHeadYaw = entity.lastHeadYaw;
        morph.age = entity.age;
        morph.preferredHand = entity.preferredHand;
        ((LivingEntityAccessor) morph).worldsinger$setLeaningPitch(
                ((LivingEntityAccessor) entity).worldsinger$getLeaningPitch());
        ((LivingEntityAccessor) morph).worldsinger$setLastLeaningPitch(
                ((LivingEntityAccessor) entity).worldsinger$getLastLeaningPitch());
        morph.setOnGround(entity.isOnGround());
        morph.setVelocity(entity.getVelocity());
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            morph.setInvisible(entity.isInvisibleTo(player));
        }
        ((EntityAccessor) morph).worldsinger$setVehicle(entity.getVehicle());
        ((EntityAccessor) morph).worldsinger$setPassengerList(
                ImmutableList.copyOf(entity.getPassengerList()));
        ((EntityAccessor) morph).worldsinger$setTouchingWater(entity.isTouchingWater());

        // Pitch for Phantoms is inverted
        if (morph instanceof PhantomEntity) {
            morph.setPitch(-entity.getPitch());
            morph.lastPitch = -entity.lastPitch;
        } else {
            morph.setPitch(entity.getPitch());
            morph.lastPitch = entity.lastPitch;
        }

        if (entity.shouldCopyEquipmentVisuals()) {
            // Equip held items and armor
            // Note: Should not run these if using visual equipment
            morph.equipStack(EquipmentSlot.MAINHAND,
                    entity.getEquippedStack(EquipmentSlot.MAINHAND));
            morph.equipStack(EquipmentSlot.OFFHAND, entity.getEquippedStack(EquipmentSlot.OFFHAND));
            morph.equipStack(EquipmentSlot.HEAD, entity.getEquippedStack(EquipmentSlot.HEAD));
            morph.equipStack(EquipmentSlot.CHEST, entity.getEquippedStack(EquipmentSlot.CHEST));
            morph.equipStack(EquipmentSlot.LEGS, entity.getEquippedStack(EquipmentSlot.LEGS));
            morph.equipStack(EquipmentSlot.FEET, entity.getEquippedStack(EquipmentSlot.FEET));
        }

        if (morph instanceof MobEntity mobEntity) {
            mobEntity.setAttacking(
                    entity.isAttacking() || entity.isUsingItem() || entity.getAttacking() != null);
        }

        // Assign pose
        morph.setPose(entity.getPose());

        if (morph instanceof MobEntity) {
            ((MobEntity) morph).setAttacking(entity.isAttacking());
        }

        // Set active hand after configuring held items
        morph.setCurrentHand(
                entity.getActiveHand() == null ? Hand.MAIN_HAND : entity.getActiveHand());
        ((LivingEntityAccessor) morph).worldsinger$setLivingFlag(1, entity.isUsingItem());
        // morph.getItemUseTime(); // Shouldn't be needed?
        ((LivingEntityAccessor) morph).worldsinger$tickActiveItemStack();

        morph.deathTime = entity.deathTime;
        morph.hurtTime = entity.hurtTime;

        if (entity.shouldRenderNameTag() && entity.getCustomName() != null) {
            morph.setCustomName(entity.getCustomName());
        }
    }

    public ShapeshiftingEntityRenderer(Context context, M entityModel, float shadowRadius) {
        super(context, entityModel, shadowRadius);
    }

    protected void renderDefault(S entityRenderState, MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(entityRenderState, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public void updateRenderState(T entity, S renderState, float tickProgress) {
        super.updateRenderState(entity, renderState, tickProgress);
        LivingEntity morph = entity.getMorph();
        if (morph == null) {
            renderState.morphRenderState = null;
        } else {
            updateMorphAttributes(entity, morph);

            // Copy it to the render state
            // I had to do black magic to deal with type erasure, sorry
            EntityRenderer<? super LivingEntity, ?> morphRenderer = MinecraftClient.getInstance()
                    .getEntityRenderDispatcher()
                    .getRenderer(morph);
            renderState.morphRenderState = (LivingEntityRenderState) morphRenderer.getAndUpdateRenderState(
                    morph, tickProgress);
        }
    }

    @Override
    public void render(S renderState, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
            int light) {
        LivingEntityRenderState morphRenderState = renderState.morphRenderState;
        if (morphRenderState == null) {
            renderDefault(renderState, matrices, vertexConsumers, light);
            return;
        }

        // Unfortunately there's some type erasure here
        @SuppressWarnings("unchecked") EntityRenderer<LivingEntity, LivingEntityRenderState> morphRenderer = (EntityRenderer<LivingEntity, LivingEntityRenderState>) MinecraftClient.getInstance()
                .getEntityRenderDispatcher()
                .getRenderer(morphRenderState);
        morphRenderer.render(renderState.morphRenderState, matrices, vertexConsumers, light);
    }
}
