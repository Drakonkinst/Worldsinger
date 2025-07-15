package io.github.drakonkinst.worldsinger.registry;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.entity.attachments.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.effect.ModStatusEffects;
import io.github.drakonkinst.worldsinger.event.thirst.ThirstEvents;
import io.github.drakonkinst.worldsinger.gui.thirst.ThirstStatusBar;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent;
import io.github.drakonkinst.worldsinger.item.component.CannonballComponent.CannonballContent;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.DrawItemStackOverlayCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Colors;

public class ModHudElements {

    // TODO: Replace with an actual config
    public static boolean showThirstAlways = false;
    public static boolean showThirstWhenBelowNaturalThirst = true;
    public static boolean showThirstWhenCritical = true;
    public static boolean showThirstWhenStatusEffect = true;
    public static long hideThirstAfterSeconds = 3;

    public static void registerHudElements() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR, Worldsinger.id("thirst"),
                (ThirstStatusBar::renderThirstBar));
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void registerHudEventHandlers() {
        DrawItemStackOverlayCallback.EVENT.register(((context, textRenderer, stack, x, y) -> {
            CannonballComponent component = stack.get(ModDataComponentTypes.CANNONBALL);
            if (component == null || !component.core().isFillable() || component.contents()
                    .isEmpty()) {
                return;
            }
            context.getMatrices().pushMatrix();
            List<CannonballContent> contents = component.contents();
            drawCannonballContentBar(context, contents, 0, x, y, 2, 6);
            drawCannonballContentBar(context, contents, 1, x, y, 6, 10);
            drawCannonballContentBar(context, contents, 2, x, y, 10, 14);
            context.getMatrices().popMatrix();
        }));
        // TODO: Add option for showing an item when consuming it modifies thirst?
        // TODO: Add option for showing when thirst decreases unnaturally?
        ThirstEvents.VISIBLE_PREDICATE.register(player -> showThirstAlways);
        ThirstEvents.VISIBLE_PREDICATE.register(
                player -> showThirstWhenCritical && player.getAttachedOrCreate(
                        ModAttachmentTypes.THIRST).isCritical());
        ThirstEvents.VISIBLE_PREDICATE.register(
                player -> showThirstWhenBelowNaturalThirst && player.getAttachedOrCreate(
                        ModAttachmentTypes.THIRST).isBelowNaturalThirst());
        ThirstEvents.VISIBLE_PREDICATE.register(
                player -> showThirstWhenStatusEffect && player.hasStatusEffect(
                        ModStatusEffects.THIRST));

        // Cosmere-specific
        ThirstEvents.VISIBLE_PREDICATE.register(player ->
                player.getAttachedOrCreate(ModAttachmentTypes.MIDNIGHT_AETHER_BOND).getBondCount()
                        > 0);
    }

    private static void drawCannonballContentBar(DrawContext context,
            List<CannonballContent> contents, int index, int x, int y, int from, int to) {
        if (index >= contents.size()) {
            return;
        }
        int startY = y + 16 - to;
        int endY = y + 16 - from;
        context.fill(x, startY, x + 1, endY, contents.get(index).getBarColor() | Colors.BLACK);
    }
}
