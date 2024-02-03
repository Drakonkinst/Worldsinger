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
package io.github.drakonkinst.worldsinger.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.worldsinger.block.ModBlockTags;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarSeethe;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin extends ProjectileEntity {

    @Shadow
    protected boolean inGround;
    @Shadow
    private @Nullable BlockState inBlockState;

    public PersistentProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType,
            World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract boolean isNoClip();

    @Shadow
    protected abstract boolean shouldFall();

    @Shadow
    protected abstract void fall();

    @Inject(method = "tick", at = @At("HEAD"))
    private void collideWithSolidSpores(CallbackInfo ci) {
        // Spore sea blocks can change solidity without warning, so check if it should fall even if there is no block update
        if (this.inGround && !this.isNoClip() && this.inBlockState != null
                && this.inBlockState.isIn(ModBlockTags.AETHER_SPORE_SEA_BLOCKS)
                && this.shouldFall()) {
            this.fall();
        }
    }

    @ModifyReturnValue(method = "shouldFall", at = @At("RETURN"))
    private boolean doNotFallIfInSolidSpores(boolean shouldFall) {
        if (!shouldFall) {
            return false;
        }
        World world = this.getWorld();
        boolean isInSolidSpores = this.inBlockState != null && this.inBlockState.isIn(
                ModBlockTags.AETHER_SPORE_SEA_BLOCKS) && this.inBlockState.getFluidState().isStill()
                && !LumarSeethe.areSporesFluidized(world);
        return !isInSolidSpores;
    }
}
