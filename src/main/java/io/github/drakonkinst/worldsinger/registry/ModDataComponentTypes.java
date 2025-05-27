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

package io.github.drakonkinst.worldsinger.registry;

import com.mojang.serialization.Codec;
import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.map.CustomMapDecorationsComponent;
import java.util.function.UnaryOperator;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.dynamic.Codecs;

@SuppressWarnings("UnqualifiedStaticUsage")
public final class ModDataComponentTypes {

    public static final ComponentType<Boolean> SALTED = register("salted",
            builder -> builder.codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN));
    public static final ComponentType<Integer> SILVER_DURABILITY = register("silver_durability",
            builder -> builder.codec(Codecs.NON_NEGATIVE_INT).packetCodec(PacketCodecs.VAR_INT));
    public static final ComponentType<Integer> MAX_SILVER_DURABILITY = register(
            "max_silver_durability",
            builder -> builder.codec(Codecs.POSITIVE_INT).packetCodec(PacketCodecs.VAR_INT));
    public static final ComponentType<Float> SILVER_DURABILITY_DISPLAY_FACTOR = register(
            "silver_durability_display_factor",
            builder -> builder.codec(Codecs.POSITIVE_FLOAT).packetCodec(PacketCodecs.FLOAT));
    public static final ComponentType<CustomMapDecorationsComponent> CUSTOM_MAP_DECORATIONS = register(
            "custom_map_decorations",
            builder -> builder.codec(CustomMapDecorationsComponent.CODEC));
    public static final ComponentType<CannonballComponent> CANNONBALL = register("cannonball",
            builder -> builder.codec(CannonballComponent.CODEC)
                    .packetCodec(CannonballComponent.PACKET_CODEC)
                    .cache());

    public static void initialize() {}

    private static <T> ComponentType<T> register(String id,
            UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Worldsinger.id(id),
                builderOperator.apply(ComponentType.builder()).build());
    }

    private ModDataComponentTypes() {}
}
