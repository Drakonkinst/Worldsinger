package io.github.drakonkinst.worldsinger.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.worldsinger.block.ModBlockTags;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
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
                && !SeetheManager.areSporesFluidized(world);
        return !isInSolidSpores;
    }
}
