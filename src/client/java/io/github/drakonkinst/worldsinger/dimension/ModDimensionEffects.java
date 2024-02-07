package io.github.drakonkinst.worldsinger.dimension;

import io.github.drakonkinst.worldsinger.mixin.client.accessor.DimensionEffectsAccessor;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensions;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.Identifier;

public class ModDimensionEffects {

    public static void initialize() {
        Object2ObjectMap<Identifier, DimensionEffects> dimensionEffectsMap = DimensionEffectsAccessor.worldsinger$getDimensionEffectsMap();
        dimensionEffectsMap.put(ModDimensions.LUMAR, new LumarDimensionEffects());
    }
}
