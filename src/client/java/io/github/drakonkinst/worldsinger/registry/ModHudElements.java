package io.github.drakonkinst.worldsinger.registry;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.effect.ModStatusEffects;
import io.github.drakonkinst.worldsinger.event.thirst.ThirstEvents;
import io.github.drakonkinst.worldsinger.gui.thirst.ThirstStatusBar;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public class ModHudElements {

    // TODO: Replace with an actual config
    public static boolean showThirstAlways = false;
    public static boolean showThirstWhenCritical = true;
    public static boolean showThirstWhenStatusEffect = true;
    public static long hideThirstAfterSeconds = 3;

    public static void registerHudElements() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR, Worldsinger.id("thirst"),
                (ThirstStatusBar::renderThirstBar));
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void registerHudEventHandlers() {
        // TODO: Add option for showing an item when consuming it modifies thirst?
        // TODO: Add option for showing when thirst decreases unnaturally?
        ThirstEvents.VISIBLE_PREDICATE.register(player -> showThirstAlways);
        ThirstEvents.VISIBLE_PREDICATE.register(
                player -> showThirstWhenCritical && player.getAttachedOrCreate(
                        ModAttachmentTypes.THIRST).isCritical());
        ThirstEvents.VISIBLE_PREDICATE.register(
                player -> showThirstWhenStatusEffect && player.hasStatusEffect(
                        ModStatusEffects.THIRST));

        // Cosmere-specific
        ThirstEvents.VISIBLE_PREDICATE.register(player ->
                player.getAttachedOrCreate(ModAttachmentTypes.MIDNIGHT_AETHER_BOND).getBondCount()
                        > 0);
    }
}
