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
package io.github.drakonkinst.worldsinger.mixin.client.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.SaltedFoodUtil;
import io.github.drakonkinst.worldsinger.registry.ModItemRendering;
import io.github.drakonkinst.worldsinger.util.LayeredBakedModel;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    // Mixing in directly to DrawContext because we want salted overlay to ONLY appear in the
    // inventory, not in hand.
    // If we want a global overlay, a better option would be to mixin into getModel() directly
    @ModifyExpressionValue(method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;getModel(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)Lnet/minecraft/client/render/model/BakedModel;"))
    private BakedModel addSaltedOverlay(BakedModel original, @Nullable LivingEntity entity,
            @Nullable World world, ItemStack stack, int x, int y, int seed, int z) {
        if (SaltedFoodUtil.isSalted(stack)) {
            // Note: Should use separate caches if we implement this multiple times
            Identifier itemId = Registries.ITEM.getId(stack.getItem());
            BakedModel cachedModel = ModItemRendering.SALT_OVERLAY_CACHE.get(itemId);
            if (cachedModel != null) {
                return cachedModel;
            } else {
                BakedModelManager manager = MinecraftClient.getInstance().getBakedModelManager();
                BakedModel saltOverlayModel = manager.getModel(ModItemRendering.SALT_OVERLAY);
                if (saltOverlayModel == null || saltOverlayModel.equals(
                        manager.getMissingModel())) {
                    Worldsinger.LOGGER.warn("Could not locate salt overlay texture");
                    return original;
                }
                LayeredBakedModel layeredModel = new LayeredBakedModel(
                        List.of(original, saltOverlayModel));
                ModItemRendering.SALT_OVERLAY_CACHE.add(itemId, layeredModel);
                return layeredModel;
            }
        }
        return original;
    }
}
