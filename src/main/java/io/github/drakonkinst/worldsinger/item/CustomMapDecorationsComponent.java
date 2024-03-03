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

package io.github.drakonkinst.worldsinger.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.util.Util;

public record CustomMapDecorationsComponent(Map<String, Decoration> decorations) {

    public static final CustomMapDecorationsComponent DEFAULT = new CustomMapDecorationsComponent(
            Map.of());
    public static final Codec<CustomMapDecorationsComponent> CODEC = Codec.unboundedMap(
                    Codec.STRING, CustomMapDecorationsComponent.Decoration.CODEC)
            .xmap(CustomMapDecorationsComponent::new, CustomMapDecorationsComponent::decorations);

    public CustomMapDecorationsComponent with(String id,
            CustomMapDecorationsComponent.Decoration decoration) {
        return new CustomMapDecorationsComponent(Util.mapWith(this.decorations, id, decoration));
    }

    public record Decoration(CustomMapIcon.Type type, double x, double z, float rotation) {

        public static final Codec<CustomMapDecorationsComponent.Decoration> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(CustomMapIcon.Type.CODEC.fieldOf("type")
                                        .forGetter(CustomMapDecorationsComponent.Decoration::type),
                                Codec.DOUBLE.fieldOf("x")
                                        .forGetter(CustomMapDecorationsComponent.Decoration::x),
                                Codec.DOUBLE.fieldOf("z")
                                        .forGetter(CustomMapDecorationsComponent.Decoration::z),
                                Codec.FLOAT.fieldOf("rotation")
                                        .forGetter(CustomMapDecorationsComponent.Decoration::rotation))
                        .apply(instance, CustomMapDecorationsComponent.Decoration::new));
    }
}
