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

import io.github.drakonkinst.worldsinger.entity.CannonballEntity;
import io.github.drakonkinst.worldsinger.entity.SporeBottleEntity;
import io.github.drakonkinst.worldsinger.util.math.ExtendedRaycastContext;
import io.github.drakonkinst.worldsinger.util.math.ExtendedRaycastContext.ExtendedFluidHandling;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ProjectileUtil.class)
public abstract class ProjectileUtilMixin {

    @Redirect(method = "getCollision(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;Ljava/util/function/Predicate;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/world/World;FLnet/minecraft/world/RaycastContext$ShapeType;)Lnet/minecraft/util/hit/HitResult;", at = @At(value = "NEW", target = "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/world/RaycastContext$ShapeType;Lnet/minecraft/world/RaycastContext$FluidHandling;Lnet/minecraft/entity/Entity;)Lnet/minecraft/world/RaycastContext;"))
    private static RaycastContext makeThrownPotionsBreakAgainstSporeSea(Vec3d start, Vec3d end,
            ShapeType shapeType, FluidHandling fluidHandling, Entity entity) {
        // Make potions break upon hitting spore sea instead of falling through
        if (entity instanceof PotionEntity || entity instanceof SporeBottleEntity
                || entity instanceof CannonballEntity) {
            return new ExtendedRaycastContext(start, end, shapeType,
                    ExtendedFluidHandling.SPORE_SEA, entity);
        }
        // Original
        return new RaycastContext(start, end, shapeType, fluidHandling, entity);
    }
}
