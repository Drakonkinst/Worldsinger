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
import io.github.drakonkinst.worldsinger.cosmere.LunagreeData;
import io.github.drakonkinst.worldsinger.entity.LunagreeDataAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.stat.StatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityLunagreeMixin extends AbstractClientPlayerEntity implements
        LunagreeDataAccess {

    @Unique
    private LunagreeData lunagreeData;

    public ClientPlayerEntityLunagreeMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeLunagreeData(MinecraftClient client, ClientWorld world,
            ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook,
            boolean lastSneaking, boolean lastSprinting, CallbackInfo ci) {
        lunagreeData = new LunagreeData();
    }

    @Override
    public LunagreeData worldsinger$getLunagreeData() {
        return lunagreeData;
    }
}
