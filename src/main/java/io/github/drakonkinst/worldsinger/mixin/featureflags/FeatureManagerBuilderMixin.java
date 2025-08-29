package io.github.drakonkinst.worldsinger.mixin.featureflags;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.resource.featuretoggle.FeatureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FeatureManager.Builder.class)
public abstract class FeatureManagerBuilderMixin {

    @ModifyExpressionValue(method = "addFlag", at = @At(value = "CONSTANT", args = { "intValue=64" }))
    private int increaseMax(int constant) {
        // No change for now
        return Math.max(constant, 64);
    }
}
