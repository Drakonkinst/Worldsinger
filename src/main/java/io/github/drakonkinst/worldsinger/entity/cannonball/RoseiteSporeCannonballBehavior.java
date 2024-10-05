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
import io.github.drakonkinst.worldsinger.entity.CannonballEntity;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballContent;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RoseiteSporeCannonballBehavior implements CannonballBehavior {

    private static final int WATER_AMOUNT_SINGLE = 200;
    private static final int WATER_AMOUNT_MULTIPLE = 150;

    private final Object2IntMap<CannonballContent> contentMap;

    public RoseiteSporeCannonballBehavior(Object2IntMap<CannonballContent> contentMap) {
        this.contentMap = contentMap;
    }

    @Override
    public void onCollisionClient(CannonballEntity entity, Vec3d hitPos) {

    }

    @Override
    public void onCollisionServer(CannonballEntity entity, Vec3d hitPos) {
        World world = entity.getWorld();
        // Doesn't split water evenly, but small decrease in water for having multiple spore types to account for multiple entries
        int waterAmountPerEntry =
                contentMap.size() > 1 ? WATER_AMOUNT_MULTIPLE : WATER_AMOUNT_SINGLE;
        for (Object2IntMap.Entry<CannonballContent> entry : contentMap.object2IntEntrySet()) {
            AetherSpores sporeType = entry.getKey().getSporeType();
            int strength = entry.getIntValue();
            if (sporeType == null) {
                continue;
            }
            if (!sporeType.isDead()) {
                AetherSpores.doParticleReaction(world, hitPos, sporeType, SPORE_AMOUNT * strength,
                        waterAmountPerEntry);
            }
        }
    }
}
