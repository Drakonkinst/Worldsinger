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
package io.github.drakonkinst.worldsinger.mixin.client.world;

import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import io.github.drakonkinst.worldsinger.entity.PossessionClientUtil;
import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererPossessionMixin {

    // Allow the player model to still be rendered while possessing another mob
    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyConstant(method = "getEntitiesToRender", constant = @Constant(classValue = ClientPlayerEntity.class))
    private static boolean allowRenderPlayerModel(Object obj, Class<?> clazz, Camera camera,
            Frustum frustum, List<Entity> output) {
        CameraPossessable possessedEntity = PossessionClientUtil.getPossessedEntity();
        if (possessedEntity != null) {

            // If a Midnight Creature Entity, the entity used for rendering is not actually the
            // Midnight Creature itself. Therefore, we need to exclude the rendered entity from
            // being rendered while possessing it.
            if (possessedEntity instanceof MidnightCreatureEntity midnightCreature
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
