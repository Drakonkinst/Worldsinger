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

package io.github.drakonkinst.worldsinger.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.CrimsonSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.DeadSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RoseiteSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SunlightSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.VerdantSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.ZephyrSpores;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import org.jetbrains.annotations.Nullable;

public record CannonballComponent(CannonballShell shell, CannonballCore core, int fuse,
                                  List<CannonballContent> contents) implements TooltipAppender {

    public static final int MAX_FUSE = 3;
    public static final int MAX_CONTENTS_LENGTH = 3;

    public static final Codec<CannonballComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            CannonballShell.CODEC.optionalFieldOf("shell", CannonballShell.CERAMIC)
                                    .forGetter(CannonballComponent::shell),
                            CannonballCore.CODEC.optionalFieldOf("core", CannonballCore.HOLLOW)
                                    .forGetter(CannonballComponent::core),
                            Codec.INT.optionalFieldOf("fuse", 0).forGetter(CannonballComponent::fuse),
                            CannonballContent.CODEC.listOf(0, MAX_CONTENTS_LENGTH)
                                    .optionalFieldOf("contents", Collections.emptyList())
                                    .forGetter(CannonballComponent::contents))
                    .apply(instance, CannonballComponent::new));
    public static final PacketCodec<ByteBuf, CannonballComponent> PACKET_CODEC = PacketCodec.tuple(
            CannonballShell.PACKET_CODEC, CannonballComponent::shell, CannonballCore.PACKET_CODEC,
            CannonballComponent::core, PacketCodecs.VAR_INT, CannonballComponent::fuse,
            CannonballContent.PACKET_CODEC.collect(PacketCodecs.toList()),
            CannonballComponent::contents, CannonballComponent::new);
    public static final CannonballComponent DEFAULT = new CannonballComponent(
            CannonballShell.CERAMIC, CannonballCore.HOLLOW, 0, Collections.emptyList());

    public static Object2IntMap<CannonballContent> getContentMap(CannonballComponent component) {
        Object2IntMap<CannonballContent> map = new Object2IntArrayMap<>();
        for (CannonballContent contents : component.contents()) {
            int quantity = map.getOrDefault(contents, 0);
            map.put(contents, quantity + 1);
        }
        return map;
    }

    public enum CannonballCore implements StringIdentifiable {
        HOLLOW(0, "hollow", true, false, true),
        ROSEITE(1, "roseite", true, true, false),
        WATER(2, "water", false, false, false);

        private static final IntFunction<CannonballCore> BY_ID = ValueLists.createIndexToValueFunction(
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

    // TODO: This assumes all cannonball contents are spores, which may not be true in the future
    public enum CannonballContent implements StringIdentifiable {
        DEAD_SPORES(DeadSpores.ID, "dead_spores", DeadSpores.getInstance().getColor(), 0xaaaaaa),
        VERDANT_SPORES(VerdantSpores.ID, "verdant_spores", VerdantSpores.getInstance().getColor(),
                VerdantSpores.getInstance().getColor()),
        CRIMSON_SPORES(CrimsonSpores.ID, "crimson_spores", CrimsonSpores.getInstance().getColor(),
                CrimsonSpores.getInstance().getColor()),
        ZEPHYR_SPORES(ZephyrSpores.ID, "zephyr_spores", ZephyrSpores.getInstance().getColor(),
                ZephyrSpores.getInstance().getParticleColor()),
        SUNLIGHT_SPORES(SunlightSpores.ID, "sunlight_spores",
                SunlightSpores.getInstance().getColor(), SunlightSpores.getInstance().getColor()),
        ROSEITE_SPORES(RoseiteSpores.ID, "roseite_spores", RoseiteSpores.getInstance().getColor(),
                RoseiteSpores.getInstance().getColor()),
        MIDNIGHT_SPORES(MidnightSpores.ID, "midnight_spores", 0x444444,
                MidnightSpores.getInstance().getColor());

        private static final IntFunction<CannonballContent> BY_ID = ValueLists.createIndexToValueFunction(
                CannonballContent::getId, values(), ValueLists.OutOfBoundsHandling.ZERO);
        public static final PacketCodec<ByteBuf, CannonballContent> PACKET_CODEC = PacketCodecs.indexed(
                BY_ID, CannonballContent::getId);
        public static final Codec<CannonballContent> CODEC = StringIdentifiable.createBasicCodec(
                CannonballContent::values);

        private final int id;
        private final String name;
        private final int textColor;
        private final int barColor;

        CannonballContent(final int id, final String name, final int textColor,
                final int barColor) {
            this.id = id;
            this.name = name;
            this.textColor = textColor;
            this.barColor = barColor;
        }

        public int getId() {
            return id;
        }

        public int getTextColor() {
            return textColor;
        }

        public int getBarColor() {
            return barColor;
        }

        @Override
        public String asString() {
            return name;
        }

        @Nullable
        public AetherSpores getSporeType() {
            return AetherSpores.getAetherSporeTypeById(id);
        }
    }

    public enum CannonballShell implements StringIdentifiable {
        CERAMIC(0, "ceramic");

        private static final IntFunction<CannonballShell> BY_ID = ValueLists.createIndexToValueFunction(
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

    public int getNumContents() {
        if (contents == null) {
            return 0;
        }
        return contents.size();
    }

    @Override
    public void appendTooltip(TooltipContext context, Consumer<Text> textConsumer, TooltipType type,
            ComponentsAccess components) {
        CannonballComponent component = components.get(ModDataComponentTypes.CANNONBALL);
        if (component == null) {
            return;
        }
        component.appendTooltip(textConsumer);
    }

    public void appendTooltip(Consumer<Text> tooltip) {
        appendCoreTooltip(tooltip);
        appendContentsTooltip(tooltip);
        appendFuseTooltip(tooltip);
    }

    private void appendCoreTooltip(Consumer<Text> textConsumer) {
        MutableText text = switch (this.core()) {
            case WATER -> Text.translatable("item.worldsinger.cannonball.core.water");
            case HOLLOW -> Text.translatable("item.worldsinger.cannonball.core.hollow");
            case ROSEITE -> Text.translatable("item.worldsinger.cannonball.core.roseite");
        };
        textConsumer.accept(text.formatted(Formatting.GRAY));
    }

    private void appendContentsTooltip(Consumer<Text> textConsumer) {
        Object2IntMap<CannonballContent> contentMap = getContentMap(this);
        MutableText contentText = Text.empty();
        int numEntries = 0;
        for (Object2IntMap.Entry<CannonballContent> entry : contentMap.object2IntEntrySet()) {
            if (numEntries > 0) {
                contentText.append(", ").formatted(Formatting.GRAY);
            }
            numEntries++;
            CannonballContent contents = entry.getKey();
            MutableText entryText = Text.translatable(
                    "item.worldsinger.cannonball.contents." + contents.name);
            int quantity = entry.getIntValue();
            if (quantity > 1) {
                entryText = Text.translatable("item.worldsinger.cannonball.contents_quantity",
                        entryText, quantity);
            }
            contentText.append(entryText.setStyle(
                    Style.EMPTY.withColor(TextColor.fromRgb(contents.getTextColor()))));
        }
        if (numEntries > 0) {
            textConsumer.accept(contentText);
        }
    }

    private void appendFuseTooltip(Consumer<Text> textConsumer) {
        if (this.core().canHaveFuse() && this.fuse() > 0) {
            textConsumer.accept(Text.translatable("item.worldsinger.cannonball.fuse", this.fuse())
                    .formatted(Formatting.GRAY));
        }
    }
}
