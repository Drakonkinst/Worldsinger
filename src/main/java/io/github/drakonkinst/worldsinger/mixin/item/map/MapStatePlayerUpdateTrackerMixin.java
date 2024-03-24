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
import io.github.drakonkinst.worldsinger.item.map.CustomMapDecoration;
import io.github.drakonkinst.worldsinger.item.map.CustomMapStateAccess;
import io.github.drakonkinst.worldsinger.item.map.CustomPlayerUpdateTrackerAccess;
import io.github.drakonkinst.worldsinger.network.packet.CustomMapUpdatePayload;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapState.PlayerUpdateTracker.class)
public abstract class MapStatePlayerUpdateTrackerMixin implements CustomPlayerUpdateTrackerAccess {

    @Shadow
    @Final
    MapState field_132;

    @Unique
    private boolean customIconsDirty;

    @Override
    public void worldsinger$markDirty() {
        customIconsDirty = true;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeDirtyFlags(MapState mapState, PlayerEntity player, CallbackInfo ci) {
        customIconsDirty = true;
    }

    @ModifyReturnValue(method = "getPacket", at = @At("RETURN"))
    private @Nullable Packet<?> addCustomIconsInfo(@Nullable Packet<?> original,
            MapIdComponent mapId) {
        if (!customIconsDirty) {
            return original;
        }

        MapState mapState = this.field_132;
        CustomMapStateAccess customMapState = (CustomMapStateAccess) mapState;
        List<CustomMapDecoration> customIcons = List.copyOf(
                customMapState.worldsinger$getCustomMapIcons().values());
        if (original == null) {
            customIconsDirty = false;
            return new CustomPayloadS2CPacket(
                    new CustomMapUpdatePayload(mapId, mapState.scale, mapState.locked,
                            Optional.empty(), Optional.empty(), Optional.of(customIcons)));
        } else if (original instanceof MapUpdateS2CPacket mapUpdateS2CPacket) {
            customIconsDirty = false;
            return new CustomPayloadS2CPacket(
                    new CustomMapUpdatePayload(mapUpdateS2CPacket, customIcons));
        } else {
            return original;
        }
    }
}
