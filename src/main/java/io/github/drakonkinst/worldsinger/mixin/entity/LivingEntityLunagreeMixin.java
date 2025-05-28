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

import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.DeadSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarLunagreeGenerator;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManagerAccess;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeGenerator;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeLocation;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeKillingUtil;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleManager;
import io.github.drakonkinst.worldsinger.entity.ModEntityTypeTags;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityLunagreeMixin extends Entity implements Attackable {

    public LivingEntityLunagreeMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void addLunagreeSporefallEffect(CallbackInfo ci) {
        World world = this.getWorld();
        LivingEntity entity = (LivingEntity) (Object) this;
        if (world.isClient || !(world instanceof ServerWorld serverWorld)) {
            return;
        }

        if (this.getType().isIn(ModEntityTypeTags.SPORES_NEVER_AFFECT)) {
            return;
        }
        if (entity instanceof PlayerEntity playerEntity && (playerEntity.isCreative()
                || playerEntity.isSpectator())) {
            return;
        }
        BlockPos blockPos = BlockPosUtil.toBlockPos(this.getEyePos());
        if (!world.isSkyVisible(blockPos)
                || world.getTopY(Type.MOTION_BLOCKING, blockPos.getX(), blockPos.getZ())
                > blockPos.getY()) {
            return;
        }

        LunagreeGenerator manager = ((LumarManagerAccess) world).worldsinger$getLumarManager()
                .getLunagreeGenerator();
        LunagreeLocation underLocation = manager.getNearestLunagree(serverWorld, entity.getBlockX(),
                entity.getBlockZ(), LumarLunagreeGenerator.SPORE_FALL_RADIUS);
        if (underLocation == null) {
            return;
        }

        AetherSpores sporeType = AetherSpores.getAetherSporeTypeById(underLocation.sporeId());
        if (sporeType == null) {
            return;
        }
        if (sporeType.getId() == DeadSpores.ID || SporeKillingUtil.isSporeKillingBlockNearby(world,
                blockPos)) {
            return;
        }
        SporeParticleManager.applySporeEffect(entity, sporeType.getStatusEffect(),
                SporeParticleManager.SPORE_EFFECT_DURATION_TICKS_DEFAULT);
    }
}
