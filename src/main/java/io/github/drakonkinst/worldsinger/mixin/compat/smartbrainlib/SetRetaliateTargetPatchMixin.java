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
package io.github.drakonkinst.worldsinger.mixin.compat.smartbrainlib;

import java.util.function.BiPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRetaliateTarget;
import net.tslat.smartbrainlib.util.BrainUtils;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin({ SetRetaliateTarget.class })
public abstract class SetRetaliateTargetPatchMixin<E extends LivingEntity> extends
        ExtendedBehaviour<E> {

    @Shadow
    protected LivingEntity toTarget;

    @Shadow(remap = false)
    protected BiPredicate<E, LivingEntity> allyPredicate;

    /**
     * @author Drakonkinst
     * @reason Temporary fix to patch to 1.20.5 snapshots
     */
    @Overwrite
    protected void alertAllies(ServerWorld level, E owner) {
        double followRange = owner.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);

        for (LivingEntity ally : EntityRetrievalUtil.<LivingEntity>getEntities(level,
                owner.getBoundingBox().expand(followRange, 10, followRange),
                entity -> entity != owner && entity instanceof LivingEntity livingEntity
                        && this.allyPredicate.test(owner, livingEntity))) {
            BrainUtils.setTargetOfEntity(ally, this.toTarget);
        }
    }
}
