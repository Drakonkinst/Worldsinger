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
import io.github.drakonkinst.worldsinger.item.ModItems;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballContents;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballCore;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import io.github.drakonkinst.worldsinger.registry.ModItemRendering;
import io.github.drakonkinst.worldsinger.util.LayeredBakedModel;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    @Shadow
    public abstract void fill(RenderLayer layer, int x1, int y1, int x2, int y2, int color);

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

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemBarVisible()Z"))
    private void renderCannonballContentBar(TextRenderer textRenderer, ItemStack stack, int x,
            int y, String countOverride, CallbackInfo ci) {
        if (!stack.isOf(ModItems.CERAMIC_CANNONBALL)) {
            return;
        }
        CannonballComponent component = stack.get(ModDataComponentTypes.CANNONBALL);
        if (component == null || !component.core().isFillable()) {
            return;
        }

        List<CannonballContents> contents = component.contents();
        drawCannonballContentBar(contents, 0, x, y, 2, 6);
        drawCannonballContentBar(contents, 1, x, y, 6, 10);
        drawCannonballContentBar(contents, 2, x, y, 10, 14);
    }

    @Unique
    private void drawCannonballContentBar(List<CannonballContents> contents, int index, int x,
            int y, int from, int to) {
        if (index >= contents.size()) {
            return;
        }
        int startY = y + 16 - to;
        int endY = y + 16 - from;
        this.fill(RenderLayer.getGuiOverlay(), x, startY, x + 1, endY,
                contents.get(index).getColor() | Colors.BLACK);
    }

    @ModifyExpressionValue(method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;getModel(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)Lnet/minecraft/client/render/model/BakedModel;"))
    private BakedModel addCannonballModelOverlays(BakedModel original,
            @Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y,
            int seed, int z) {
        // Just supports ceramic cannonballs rn because I can't be bothered
        if (stack.isOf(ModItems.CERAMIC_CANNONBALL)) {
            CannonballComponent component = stack.get(ModDataComponentTypes.CANNONBALL);
            if (component == null) {
                return original;
            }
            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            Identifier variantId = Identifier.of(itemId + '-' + component.encodeModelString());
            BakedModel cachedModel = ModItemRendering.CERAMIC_CANNONBALL_CACHE.get(variantId);
            if (cachedModel != null) {
                return cachedModel;
            } else {
                BakedModelManager manager = MinecraftClient.getInstance().getBakedModelManager();
                List<BakedModel> modelList = new ArrayList<>();
                modelList.add(original);

                if (component.core() == CannonballCore.ROSEITE) {
                    ModItemRendering.attemptAddModel(modelList, manager,
                            ModItemRendering.CANNONBALL_CORE_ROSEITE);
                } else if (component.core() == CannonballCore.WATER) {
                    ModItemRendering.attemptAddModel(modelList, manager,
                            ModItemRendering.CANNONBALL_CORE_WATER);
                }

                if (component.core().canHaveFuse()) {
                    if (component.fuse() >= 3) {
                        ModItemRendering.attemptAddModel(modelList, manager,
                                ModItemRendering.CANNONBALL_FUSE_3);
                    } else if (component.fuse() == 2) {
                        ModItemRendering.attemptAddModel(modelList, manager,
                                ModItemRendering.CANNONBALL_FUSE_2);
                    } else if (component.fuse() > 0) {
                        ModItemRendering.attemptAddModel(modelList, manager,
                                ModItemRendering.CANNONBALL_FUSE_1);
                    }
                }

                LayeredBakedModel layeredModel = new LayeredBakedModel(modelList);
                ModItemRendering.CERAMIC_CANNONBALL_CACHE.add(variantId, layeredModel);
                return layeredModel;
            }
        }
        return original;
    }
}
