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

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import io.github.drakonkinst.worldsinger.entity.model.MidnightCreatureEntityModel;
import io.github.drakonkinst.worldsinger.entity.render.state.MidnightCreatureEntityRenderState;
import io.github.drakonkinst.worldsinger.util.ColorUtil;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.LlamaEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.util.Identifier;

public class MidnightCreatureEntityRenderer extends
        ShapeshiftingEntityRenderer<MidnightCreatureEntity, MidnightCreatureEntityRenderState, MidnightCreatureEntityModel> {

    public static final int MIDNIGHT_OVERLAY_COLOR = ColorUtil.colorToInt(0, 0, 0, 251);
    public static final int MIDNIGHT_OVERLAY_UV = OverlayTexture.packUv(0, 0);
    public static final int MIDNIGHT_OVERLAY_HURT_COLOR = ColorUtil.colorToInt(150, 0, 0, 251);
    public static final int MIDNIGHT_OVERLAY_HURT_UV = OverlayTexture.packUv(0, 1);

    private final BlockRenderManager blockRenderManager;

    public MidnightCreatureEntityRenderer(EntityRendererFactory.Context context) {
        // Doesn't really matter which model layer we pick, it should never show up
        super(context,
                new MidnightCreatureEntityModel(context.getPart(EntityModelLayers.WITHER_SKULL)),
                0.5f);
        this.blockRenderManager = context.getBlockRenderManager();
    }

    @Override
    public MidnightCreatureEntityRenderState createRenderState() {
        return new MidnightCreatureEntityRenderState();
    }

    @Override
    protected void renderDefault(MidnightCreatureEntityRenderState entityRenderState,
            MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.translate(-0.5, 0.0, -0.5);
        blockRenderManager.renderBlockAsEntity(ModBlocks.MIDNIGHT_ESSENCE.getDefaultState(),
                matrixStack, vertexConsumerProvider, i,
                LivingEntityRenderer.getOverlay(entityRenderState, 0.0f));
        matrixStack.pop();
    }

    @Override
    public void modifyMorphRenderState(LivingEntity morph, LivingEntityRenderState renderState) {
        if (renderState instanceof LlamaEntityRenderState llamaEntityRenderState) {
            // Never render trader llama overlay
            llamaEntityRenderState.trader = false;
        }
        if (morph instanceof WaterCreatureEntity) {
            // Always render fish upright
            renderState.touchingWater = true;
        }
    }

    @Override
    public Identifier getTexture(MidnightCreatureEntityRenderState state) {
        return Worldsinger.id("textures/block/midnight_essence.png");
    }
}
