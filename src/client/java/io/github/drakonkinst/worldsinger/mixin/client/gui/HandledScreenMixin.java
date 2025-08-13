package io.github.drakonkinst.worldsinger.mixin.client.gui;

import io.github.drakonkinst.worldsinger.gui.tooltip.ItemContainerTooltipSubmenuHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.tooltip.TooltipSubmenuHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements
        ScreenHandlerProvider<T> {

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract void addTooltipSubmenuHandler(TooltipSubmenuHandler handler);

    @Inject(method = "init", at = @At("TAIL"))
    private void addCustomSubmenuHandlers(CallbackInfo ci) {
        this.addTooltipSubmenuHandler(new ItemContainerTooltipSubmenuHandler(this.client));
    }
}
