package io.github.drakonkinst.worldsinger.mixin.featureflags;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.drakonkinst.worldsinger.registry.ModFeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FeatureFlags.class)
public abstract class FeatureFlagsMixin {

    @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/featuretoggle/FeatureManager$Builder;build()Lnet/minecraft/resource/featuretoggle/FeatureManager;"))
    private static void save(CallbackInfo ci, @Local FeatureManager.Builder builder) {
        ModFeatureFlags.initialize(builder);
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/featuretoggle/FeatureSet;of(Lnet/minecraft/resource/featuretoggle/FeatureFlag;)Lnet/minecraft/resource/featuretoggle/FeatureSet;"))
    private static FeatureSet addEnabledFeatures(FeatureFlag feature,
            Operation<FeatureSet> original) {
        FeatureFlag[] array = ModFeatureFlags.getEnabledFeatures();
        return FeatureSet.of(feature, array);
    }
}
