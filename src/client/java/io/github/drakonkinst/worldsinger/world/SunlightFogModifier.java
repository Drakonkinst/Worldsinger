package io.github.drakonkinst.worldsinger.world;

import io.github.drakonkinst.worldsinger.cosmere.lumar.SunlightSpores;
import io.github.drakonkinst.worldsinger.fluid.AetherSporeFluid;
import io.github.drakonkinst.worldsinger.util.ModEnums;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.FogModifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class SunlightFogModifier extends FogModifier {

    @Override
    public void applyStartEndModifier(FogData data, Entity cameraEntity, BlockPos cameraPos,
            ClientWorld world, float viewDistance, RenderTickCounter tickCounter) {
        // Same as spore sea for now
        if (cameraEntity.isSpectator()) {
            data.environmentalStart = -8.0F;
            data.environmentalEnd = viewDistance * 0.5F;
        } else {
            data.environmentalStart = AetherSporeFluid.FOG_START;
            data.environmentalEnd = AetherSporeFluid.FOG_END;
        }

        data.skyEnd = data.environmentalEnd;
        data.cloudEnd = data.environmentalEnd;
    }

    @Override
    public int getFogColor(ClientWorld world, Camera camera, int viewDistance, float skyDarkness) {
        return SunlightSpores.getInstance().getColor();
    }

    @Override
    public boolean shouldApply(@Nullable CameraSubmersionType submersionType, Entity cameraEntity) {
        return submersionType == ModEnums.CameraSubmersionType.SUNLIGHT;
    }
}
