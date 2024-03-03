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
package io.github.drakonkinst.worldsinger.util;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.item.map.MapIcon;

// Used to cache extended enum values
public final class ModEnums {

    public static class PathNodeType {

        public static final net.minecraft.entity.ai.pathing.PathNodeType AETHER_SPORE_SEA = ClassTinkerers.getEnum(
                net.minecraft.entity.ai.pathing.PathNodeType.class, "AETHER_SPORE_SEA");
        public static final net.minecraft.entity.ai.pathing.PathNodeType BLOCKING_SILVER = ClassTinkerers.getEnum(
                net.minecraft.entity.ai.pathing.PathNodeType.class, "BLOCKING_SILVER");
        public static final net.minecraft.entity.ai.pathing.PathNodeType DANGER_SILVER = ClassTinkerers.getEnum(
                net.minecraft.entity.ai.pathing.PathNodeType.class, "DANGER_SILVER");
        public static final net.minecraft.entity.ai.pathing.PathNodeType DAMAGE_SILVER = ClassTinkerers.getEnum(
                net.minecraft.entity.ai.pathing.PathNodeType.class, "DAMAGE_SILVER");
    }

    public static class MapIconType {

        public static final MapIcon.Type RAINLINE = ClassTinkerers.getEnum(MapIcon.Type.class,
                "RAINLINE");
    }
}
