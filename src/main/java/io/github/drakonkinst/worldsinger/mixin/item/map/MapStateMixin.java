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

import com.google.common.collect.Maps;
import io.github.drakonkinst.worldsinger.item.map.CustomMapDecorationsComponent;
import io.github.drakonkinst.worldsinger.item.map.CustomMapIcon;
import io.github.drakonkinst.worldsinger.item.map.CustomMapStateAccess;
import io.github.drakonkinst.worldsinger.item.map.CustomPlayerUpdateTrackerAccess;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.item.map.MapState.PlayerUpdateTracker;
import net.minecraft.text.Text;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapState.class)
public abstract class MapStateMixin extends PersistentState implements CustomMapStateAccess {

    @Shadow
    @Final
    public byte scale;
    @Shadow
    @Final
    public int centerX;
    @Shadow
    @Final
    public int centerZ;
    @Shadow
    @Final
    private List<PlayerUpdateTracker> updateTrackers;
    @Unique
    private final Map<String, CustomMapIcon> customMapIcons = Maps.newLinkedHashMap();

    @Inject(method = "update", at = @At("TAIL"))
    private void addCustomIcons(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        CustomMapDecorationsComponent customMapDecorations = stack.getOrDefault(
                ModDataComponentTypes.CUSTOM_MAP_DECORATIONS,
                CustomMapDecorationsComponent.DEFAULT);
        if (!customMapIcons.keySet().containsAll(customMapDecorations.decorations().keySet())) {
            customMapDecorations.decorations().forEach((id, decoration) -> {
                if (!customMapIcons.containsKey(id)) {
                    addCustomIcon(decoration.type(), player.getWorld(), id, decoration.x(),
                            decoration.z(), decoration.rotation(), null);
                }
            });
        }
    }

    @Unique
    private void addCustomIcon(CustomMapIcon.Type type, World world, String key, double x, double z,
            double rotation, @Nullable Text text) {
        int scaleModifier = 1 << this.scale;
        float mapX = (float) (x - this.centerX) / scaleModifier;
        float mapY = (float) (z - this.centerZ) / scaleModifier;

        if (!isInMap(mapX, mapY)) {
            return;
        }

        rotation += rotation < 0.0 ? -8.0 : 8.0;
        byte iconX = (byte) ((int) ((double) (mapX * 2.0F) + 0.5));
        byte iconZ = (byte) ((int) ((double) (mapY * 2.0F) + 0.5));
        byte iconRotation = (byte) ((int) (rotation * 16.0 / 360.0));

        CustomMapIcon mapIcon = new CustomMapIcon(type, iconX, iconZ, iconRotation,
                Optional.ofNullable(text));
        CustomMapIcon previousIcon = customMapIcons.put(key, mapIcon);
        if (!mapIcon.equals(previousIcon)) {
            markCustomIconsDirty();
        }
    }

    @Unique
    private void markCustomIconsDirty() {
        this.markDirty();
        this.updateTrackers.forEach(
                playerUpdateTracker -> ((CustomPlayerUpdateTrackerAccess) playerUpdateTracker).worldsinger$markDirty());
    }

    @Unique
    private boolean isInMap(float mapX, float mapY) {
        return mapX >= -CustomMapIcon.MAP_LIMITS && mapY >= -CustomMapIcon.MAP_LIMITS
                && mapX <= CustomMapIcon.MAP_LIMITS && mapY <= CustomMapIcon.MAP_LIMITS;
    }

    @Override
    public void worldsinger$replaceCustomMapIcons(List<CustomMapIcon> customIcons) {
        this.customMapIcons.clear();
        for (int i = 0; i < customIcons.size(); ++i) {
            CustomMapIcon mapIcon = customIcons.get(i);
            this.customMapIcons.put("icon-" + i, mapIcon);
        }
    }

    @Override
    public Map<String, CustomMapIcon> worldsinger$getCustomMapIcons() {
        return customMapIcons;
    }
}
