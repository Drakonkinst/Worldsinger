package io.github.drakonkinst.worldsinger.mixin.client.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.drakonkinst.worldsinger.api.ClientRainlineData;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.StandardFogModifier;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StandardFogModifier.class)
public abstract class StandardFogModifierMixin {

    @ModifyExpressionValue(method = "getFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainGradient(F)F"))
    private float renderRainlines(float original, ClientWorld world, Camera camera,
            int viewDistance, float skyDarkness) {
        ClientRainlineData rainlineData = ClientRainlineData.get(world);
        return Math.max(original, rainlineData.getRainlineGradient(true));
    }
}
