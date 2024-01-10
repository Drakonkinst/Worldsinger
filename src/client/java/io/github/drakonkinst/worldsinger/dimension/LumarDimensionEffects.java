package io.github.drakonkinst.worldsinger.dimension;

import io.github.drakonkinst.worldsinger.registry.ModClientEnums;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.Vec3d;

public class LumarDimensionEffects extends DimensionEffects {

    private static final float CLOUD_HEIGHT = 384.0f;

    public LumarDimensionEffects() {
        super(CLOUD_HEIGHT, true, ModClientEnums.SkyType.LUMAR, false, false);
    }

    @Override
    public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
        return color.multiply(sunHeight * 0.94f + 0.06f, sunHeight * 0.94f + 0.06f,
                sunHeight * 0.91f + 0.09f);
    }

    @Override
    public boolean useThickFog(int camX, int camY) {
        return false;
    }
}
