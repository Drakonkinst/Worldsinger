package io.github.drakonkinst.worldsinger.mixin.client.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.drakonkinst.worldsinger.api.ClientRainlineData;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.AtmosphericFogModifier;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AtmosphericFogModifier.class)
public abstract class AtmosphericFogModifierMixin {

    @ModifyExpressionValue(method = "applyStartEndModifier", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"))
    private float renderRainlines(float original, FogData data, Entity cameraEntity,
            BlockPos cameraPos, ClientWorld world, float viewDistance,
            RenderTickCounter tickCounter) {
        ClientRainlineData rainlineData = ClientRainlineData.get(world);
        return Math.max(original, rainlineData.getRainlineGradient(true));
    }
}
