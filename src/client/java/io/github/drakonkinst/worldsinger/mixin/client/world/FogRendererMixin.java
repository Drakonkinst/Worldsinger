package io.github.drakonkinst.worldsinger.mixin.client.world;

import io.github.drakonkinst.worldsinger.world.SporeSeaFogModifier;
import io.github.drakonkinst.worldsinger.world.SunlightFogModifier;
import java.util.List;
import net.minecraft.client.render.fog.FogModifier;
import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Shadow
    @Final
    private static List<FogModifier> FOG_MODIFIERS;

    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void addCustomFogModifiers(CallbackInfo ci) {
        // Add to beginning
        FOG_MODIFIERS.addAll(0, List.of(new SporeSeaFogModifier(), new SunlightFogModifier()));
    }
}
