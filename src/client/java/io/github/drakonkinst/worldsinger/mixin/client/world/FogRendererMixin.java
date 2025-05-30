package io.github.drakonkinst.worldsinger.mixin.client.world;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.drakonkinst.worldsinger.world.SporeSeaFogModifier;
import io.github.drakonkinst.worldsinger.world.SunlightFogModifier;
import java.util.List;
import net.minecraft.client.render.fog.FogModifier;
import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @ModifyReturnValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList([Ljava/lang/Object;)Ljava/util/ArrayList;"))
    private static List<FogModifier> addCustomFogModifiers(List<FogModifier> list) {
        // Add to beginning
        list.addAll(0, List.of(new SporeSeaFogModifier(), new SunlightFogModifier()));
        return list;
    }
}
