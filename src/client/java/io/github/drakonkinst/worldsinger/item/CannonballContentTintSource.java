package io.github.drakonkinst.worldsinger.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballContent;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import java.util.List;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

public record CannonballContentTintSource(int index, int defaultColor) implements TintSource {

    public static final String ID = "cannonball_content";
    public static final MapCodec<CannonballContentTintSource> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codecs.rangedInt(0, CannonballComponent.MAX_CONTENTS_LENGTH - 1)
                                    .fieldOf("index")
                                    .forGetter(CannonballContentTintSource::index),
                            Codecs.RGB.fieldOf("default")
                                    .forGetter(CannonballContentTintSource::defaultColor))
                    .apply(instance, CannonballContentTintSource::new));
    private static final int COLOR_TRANSPARENT = ColorHelper.getWhite(0);

    public CannonballContentTintSource(int index) {
        this(index, COLOR_TRANSPARENT);
    }

    public CannonballContentTintSource() {
        this(-1);
    }

    @Override
    public int getTint(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity user) {
        CannonballComponent cannonballComponent = stack.get(ModDataComponentTypes.CANNONBALL);
        if (cannonballComponent == null || !cannonballComponent.core().isFillable()) {
            return COLOR_TRANSPARENT;
        }
        List<CannonballContent> contents = cannonballComponent.contents();
        if (contents == null || index < 0 || index >= contents.size()) {
            return COLOR_TRANSPARENT;
        }
        CannonballContent content = contents.get(index);
        AetherSpores sporeType = content.getSporeType();
        if (sporeType == null) {
            return COLOR_TRANSPARENT;
        }
        return ColorHelper.fullAlpha(sporeType.getColor());
    }

    @Override
    public MapCodec<? extends TintSource> getCodec() {
        return CODEC;
    }
}
