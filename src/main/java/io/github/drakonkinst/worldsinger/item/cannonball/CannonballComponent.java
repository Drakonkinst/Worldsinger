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

package io.github.drakonkinst.worldsinger.item.cannonball;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.cosmere.lumar.CrimsonSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.DeadSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RoseiteSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SunlightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.VerdantSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.ZephyrSpores;
import io.netty.buffer.ByteBuf;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public record CannonballComponent(CannonballShell shell, CannonballCore core, int fuse,
                                  List<CannonballContents> contents) implements TooltipAppender {

    public static final int MAX_FUSE = 3;
    public static final int MAX_CONTENTS_LENGTH = 3;

    public static final Codec<CannonballComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            CannonballShell.CODEC.optionalFieldOf("shell", CannonballShell.CERAMIC)
                                    .forGetter(CannonballComponent::shell),
                            CannonballCore.CODEC.optionalFieldOf("core", CannonballCore.HOLLOW)
                                    .forGetter(CannonballComponent::core),
                            Codec.INT.optionalFieldOf("fuse", 0).forGetter(CannonballComponent::fuse),
                            CannonballContents.CODEC.listOf(0, MAX_CONTENTS_LENGTH)
                                    .optionalFieldOf("contents", Collections.emptyList())
                                    .forGetter(CannonballComponent::contents))
                    .apply(instance, CannonballComponent::new));
    public static final PacketCodec<ByteBuf, CannonballComponent> PACKET_CODEC = PacketCodec.tuple(
            CannonballShell.PACKET_CODEC, CannonballComponent::shell, CannonballCore.PACKET_CODEC,
            CannonballComponent::core, PacketCodecs.VAR_INT, CannonballComponent::fuse,
            CannonballContents.PACKET_CODEC.collect(PacketCodecs.toList()),
            CannonballComponent::contents, CannonballComponent::new);
    public static final CannonballComponent DEFAULT = new CannonballComponent(
            CannonballShell.CERAMIC, CannonballCore.HOLLOW, 0, Collections.emptyList());

    public enum CannonballCore implements StringIdentifiable {
        HOLLOW(0, "hollow", true, false, true),
        ROSEITE(1, "roseite", true, true, false),
        WATER(2, "water", false, false, false);

        private static final IntFunction<CannonballCore> BY_ID = ValueLists.createIdToValueFunction(
                CannonballCore::getId, values(), ValueLists.OutOfBoundsHandling.ZERO);
        public static final PacketCodec<ByteBuf, CannonballCore> PACKET_CODEC = PacketCodecs.indexed(
                BY_ID, CannonballCore::getId);
        public static final Codec<CannonballCore> CODEC = StringIdentifiable.createBasicCodec(
                CannonballCore::values);

        private final int id;
        private final String name;
        private final boolean isFillable;
        private final boolean canHaveFuse;
        private final boolean isReplaceable;

        CannonballCore(final int id, final String name, final boolean isFillable,
                final boolean canHaveFuse, final boolean isReplaceable) {
            this.id = id;
            this.name = name;
            this.isFillable = isFillable;
            this.canHaveFuse = canHaveFuse;
            this.isReplaceable = isReplaceable;
        }

        public boolean isFillable() {
            return isFillable;
        }

        public boolean canHaveFuse() {
            return canHaveFuse;
        }

        public boolean isReplaceable() {
            return isReplaceable;
        }

        public int getId() {
            return id;
        }

        @Override
        public String asString() {
            return name;
        }
    }

    public enum CannonballContents implements StringIdentifiable {
        DEAD_SPORES(DeadSpores.ID, "dead_spores", DeadSpores.getInstance().getColor()),
        VERDANT_SPORES(VerdantSpores.ID, "verdant_spores", VerdantSpores.getInstance().getColor()),
        CRIMSON_SPORES(CrimsonSpores.ID, "crimson_spores", CrimsonSpores.getInstance().getColor()),
        ZEPHYR_SPORES(ZephyrSpores.ID, "zephyr_spores", ZephyrSpores.getInstance().getColor()),
        SUNLIGHT_SPORES(SunlightSpores.ID, "sunlight_spores",
                SunlightSpores.getInstance().getColor()),
        ROSEITE_SPORES(RoseiteSpores.ID, "roseite_spores", RoseiteSpores.getInstance().getColor()),
        MIDNIGHT_SPORES(MidnightSpores.ID, "midnight_spores",
                MidnightSpores.getInstance().getColor());

        private static final IntFunction<CannonballContents> BY_ID = ValueLists.createIdToValueFunction(
                CannonballContents::getId, values(), ValueLists.OutOfBoundsHandling.ZERO);
        public static final PacketCodec<ByteBuf, CannonballContents> PACKET_CODEC = PacketCodecs.indexed(
                BY_ID, CannonballContents::getId);
        public static final Codec<CannonballContents> CODEC = StringIdentifiable.createBasicCodec(
                CannonballContents::values);

        private final int id;
        private final String name;

        CannonballContents(final int id, final String name, final int color) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String asString() {
            return name;
        }
    }

    public enum CannonballShell implements StringIdentifiable {
        CERAMIC(0, "ceramic");

        private static final IntFunction<CannonballShell> BY_ID = ValueLists.createIdToValueFunction(
                CannonballShell::getId, values(), ValueLists.OutOfBoundsHandling.ZERO);
        public static final PacketCodec<ByteBuf, CannonballShell> PACKET_CODEC = PacketCodecs.indexed(
                BY_ID, CannonballShell::getId);
        public static final Codec<CannonballShell> CODEC = StringIdentifiable.createBasicCodec(
                CannonballShell::values);

        private final int id;
        private final String name;

        CannonballShell(final int id, final String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public String asString() {
            return name;
        }
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> tooltip,
            TooltipType type) {
        // TODO: Model after FireworkExplosionComponent
    }
}
