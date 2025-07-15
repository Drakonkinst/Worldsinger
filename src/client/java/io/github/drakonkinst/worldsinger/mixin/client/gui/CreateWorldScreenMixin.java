package io.github.drakonkinst.worldsinger.mixin.client.gui;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @ModifyArg(method = "createLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServerLoader;tryLoad(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/screen/world/CreateWorldScreen;Lcom/mojang/serialization/Lifecycle;Ljava/lang/Runnable;Z)V"), index = 2)
    private Lifecycle removeExperimentalWarningOnWorldCreation(Lifecycle lifecycle) {
        if (lifecycle == Lifecycle.experimental()) {
            return Lifecycle.stable();
        }
        return lifecycle;
    }
}
