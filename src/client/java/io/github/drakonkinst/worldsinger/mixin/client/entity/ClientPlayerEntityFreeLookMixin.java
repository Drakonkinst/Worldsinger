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
package io.github.drakonkinst.worldsinger.mixin.client.entity;

import com.mojang.authlib.GameProfile;
import io.github.drakonkinst.worldsinger.entity.attachments.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.cosmere.PossessionManager;
import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import io.github.drakonkinst.worldsinger.entity.FreeLook;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityFreeLookMixin extends AbstractClientPlayerEntity implements
        FreeLook {

    @Unique
    private float freeLookYaw = 0.0f;
    @Unique
    private float freeLookPitch = 0.0f;

    public ClientPlayerEntityFreeLookMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public void worldsinger$setFreeLookYaw(float yaw) {
        this.freeLookYaw = yaw;
    }

    @Override
    public void worldsinger$setFreeLookPitch(float pitch) {
        this.freeLookPitch = pitch;
    }

    @Override
    public boolean worldsinger$isFreeLookEnabled() {
        // Can add more conditions here, including a manual on/off, though this currently isn't
        // needed
        PossessionManager possessionManager = this.getAttached(ModAttachmentTypes.POSSESSION);
        if (possessionManager == null) {
            return false;
        }
        CameraPossessable possessionTarget = possessionManager.getPossessionTarget();
        return possessionTarget != null && possessionTarget.canFreeLook();
    }

    @Override
    public float worldsinger$getFreeLookYaw() {
        return freeLookYaw;
    }

    @Override
    public float worldsinger$getFreeLookPitch() {
        return freeLookPitch;
    }
}
