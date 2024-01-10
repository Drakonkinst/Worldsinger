package io.github.drakonkinst.worldsinger.registry;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.client.render.DimensionEffects;

public final class ModClientEnums {

    public static class SkyType {

        public static final DimensionEffects.SkyType LUMAR = ClassTinkerers.getEnum(
                DimensionEffects.SkyType.class, "LUMAR");
    }

    public static class CameraSubmersionType {

        public static final net.minecraft.client.render.CameraSubmersionType SPORE_SEA = ClassTinkerers.getEnum(
                net.minecraft.client.render.CameraSubmersionType.class, "SPORE_SEA");
    }

    private ModClientEnums() {}
}
