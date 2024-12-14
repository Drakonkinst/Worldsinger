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

package io.github.drakonkinst.worldsinger.mixin.item.map;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManagerAccess;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RainlineManager;
import io.github.drakonkinst.worldsinger.item.map.CustomMapDecorationsComponent;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FilledMapItem.class)
public abstract class FilledMapItemMixin extends Item {

    public FilledMapItemMixin(Settings settings) {
        super(settings);
    }

    @ModifyReturnValue(method = "createMap", at = @At("RETURN"))
    private static ItemStack addRainlineIcons(ItemStack original, World world, int x, int z,
            byte scale, boolean showIcons, boolean unlimitedTracking,
            @Local MapIdComponent mapIdComponent) {
        if (!CosmerePlanet.isLumar(world) || !(world instanceof ServerWorld serverWorld)) {
            return original;
        }

        CustomMapDecorationsComponent customMapDecorations = original.getOrDefault(
                ModDataComponentTypes.CUSTOM_MAP_DECORATIONS,
                CustomMapDecorationsComponent.DEFAULT);
        Map<String, CustomMapDecorationsComponent.Decoration> updatedDecorations = new HashMap<>(
                customMapDecorations.decorations());
        RainlineManager rainlineManager = ((LumarManagerAccess) world).worldsinger$getLumarManager()
                .getRainlineManager();
        rainlineManager.applyMapDecorations(serverWorld, updatedDecorations,
                world.getMapState(mapIdComponent));

        if (updatedDecorations.size() > customMapDecorations.decorations().size()) {
            original.set(ModDataComponentTypes.CUSTOM_MAP_DECORATIONS,
                    new CustomMapDecorationsComponent(updatedDecorations));
        }

        return original;
    }
}
