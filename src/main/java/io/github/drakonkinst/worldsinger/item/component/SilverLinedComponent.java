package io.github.drakonkinst.worldsinger.item.component;

import com.mojang.serialization.Codec;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import java.util.function.Consumer;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;

public record SilverLinedComponent() implements TooltipAppender, ComponentType<Integer> {

    private static final Style SILVER_TEXT_STYLE = Style.EMPTY.withColor(
            TextColor.fromRgb(SilverLined.SILVER_TEXT_COLOR));

    @Override
    public Codec<Integer> getCodec() {
        return Codecs.NON_NEGATIVE_INT;
    }

    @Override
    public PacketCodec<? super RegistryByteBuf, Integer> getPacketCodec() {
        return PacketCodecs.VAR_INT;
    }

    @Override
    public void appendTooltip(TooltipContext context, Consumer<Text> textConsumer, TooltipType type,
            ComponentsAccess components) {
        int silverDurability = SilverLined.getSilverDurability(components);
        int maxSilverDurability = SilverLined.getMaxSilverDurability(components, 0);
        float displayMultiplier = components.getOrDefault(
                ModDataComponentTypes.SILVER_DURABILITY_DISPLAY_FACTOR, 1.0f);
        if (maxSilverDurability < 1 || silverDurability < 1) {
            return;
        }
        if (type.isAdvanced()) {
            int maxDisplayDurability = MathHelper.floor(maxSilverDurability * displayMultiplier);
            int displayDurability = MathHelper.floor(silverDurability * displayMultiplier);
            textConsumer.accept(
                    Text.translatable("item.worldsinger.silver_durability", displayDurability,
                            maxDisplayDurability).setStyle(SILVER_TEXT_STYLE));
        } else {
            textConsumer.accept(
                    Text.translatable("item.worldsinger.silver_lined").setStyle(SILVER_TEXT_STYLE));
        }

    }
}
