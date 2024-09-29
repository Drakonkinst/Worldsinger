/*
 * MIT License
 *
 * Copyright (c) 2024 Drakonkinst
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

package io.github.drakonkinst.worldsinger.entity.cannonball;

import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleSpawner;
import io.github.drakonkinst.worldsinger.entity.CannonballEntity;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballContents;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class HollowSporeCannonballBehavior implements CannonballBehavior {

    private static final int SPORE_AMOUNT = 83;

    private final Object2IntMap<CannonballContents> contentMap;

    public HollowSporeCannonballBehavior(Object2IntMap<CannonballContents> contentMap) {
        this.contentMap = contentMap;
    }

    @Override
    public void onCollisionClient(CannonballEntity entity, Vec3d hitPos) {

    }

    @Override
    public void onCollisionServer(CannonballEntity entity, Vec3d hitPos) {
        if (!(entity.getWorld() instanceof ServerWorld world)) {
            return;
        }
        for (Object2IntMap.Entry<CannonballContents> entry : contentMap.object2IntEntrySet()) {
            AetherSpores sporeType = entry.getKey().getSporeType();
            int strength = entry.getIntValue();
            if (sporeType == null) {
                continue;
            }
            SporeParticleSpawner.spawnSporeCannonballParticle(world, sporeType, hitPos, strength);
            if (!sporeType.isDead()) {
                AetherSpores.doParticleReaction(world, hitPos, sporeType, SPORE_AMOUNT * strength,
                        0);
            }
        }
    }
}
