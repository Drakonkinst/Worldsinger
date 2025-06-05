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

package io.github.drakonkinst.worldsinger.mixin.client.item;

import io.github.drakonkinst.worldsinger.registry.ModItemRendering;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    // FIXME: RESTORE
    // @ModifyReturnValue(method = "getModel", at = @At("RETURN"))
    // private BakedModel addSilverLinedModelOverlays(BakedModel original, ItemStack stack,
    //         @Nullable World world, @Nullable LivingEntity entity, int seed) {
    //     if (SilverLinedUtil.isSilverLined(stack)) {
    //         // Note: Should use separate caches if we implement this multiple times
    //         Identifier itemId = Registries.ITEM.getId(stack.getItem());
    //         BakedModel cachedModel = ModItemRendering.SILVER_LINED_CACHE.get(itemId);
    //         if (cachedModel != null) {
    //             return cachedModel;
    //         } else {
    //             BakedModelManager manager = MinecraftClient.getInstance().getBakedModelManager();
    //             BakedModel silverLinedOverlayModel = manager.getModel(
    //                     getSilverLinedOverlayForItem(stack));
    //             if (silverLinedOverlayModel == null || silverLinedOverlayModel.equals(
    //                     manager.getMissingModel())) {
    //                 Worldsinger.LOGGER.warn("Could not locate silver-lined overlay texture");
    //                 return original;
    //             }
    //             LayeredBakedModel layeredModel = new LayeredBakedModel(
    //                     List.of(original, silverLinedOverlayModel));
    //             ModItemRendering.SILVER_LINED_CACHE.add(itemId, layeredModel);
    //             return layeredModel;
    //         }
    //     }
    //     return original;
    // }

    @Unique
    private Identifier getSilverLinedOverlayForItem(ItemStack stack) {
        if (stack.isIn(ItemTags.AXES)) {
            return ModItemRendering.SILVER_LINED_AXE_OVERLAY;
        }
        if (stack.isOf(Items.BAMBOO_CHEST_RAFT)) {
            return ModItemRendering.SILVER_LINED_CHEST_RAFT_OVERLAY;
        } else if (stack.isIn(ItemTags.CHEST_BOATS)) {
            return ModItemRendering.SILVER_LINED_CHEST_BOAT_OVERLAY;
        }
        if (stack.isOf(Items.BAMBOO_RAFT)) {
            return ModItemRendering.SILVER_LINED_RAFT_OVERLAY;
        } else if (stack.isIn(ItemTags.BOATS)) {
            return ModItemRendering.SILVER_LINED_BOAT_OVERLAY;
        }
        return null;
    }
}
