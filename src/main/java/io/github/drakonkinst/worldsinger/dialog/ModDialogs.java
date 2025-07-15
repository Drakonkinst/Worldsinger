package io.github.drakonkinst.worldsinger.dialog;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.dialog.AfterAction;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.DialogButtonData;
import net.minecraft.dialog.DialogCommonData;
import net.minecraft.dialog.action.SimpleDialogAction;
import net.minecraft.dialog.body.ItemDialogBody;
import net.minecraft.dialog.body.PlainMessageDialogBody;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.dialog.type.MultiActionDialog;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModDialogs {

    public static final String WORLDHOP_PAYLOAD_KEY = "worldhop_destination";
    public static final Identifier WORLDHOP_ID = Worldsinger.id("worldhop");
    public static final Identifier WORLDHOP_CONFIG_ID = Worldsinger.id("worldhop_config");
    public static final RegistryKey<Dialog> WORLDHOP = ModDialogs.of(WORLDHOP_ID);
    public static final RegistryKey<Dialog> WORLDHOP_CONFIG = ModDialogs.of(WORLDHOP_CONFIG_ID);

    private static final int BUTTON_WIDTH = 150;
    private static final int TEXT_WIDTH = 200;
    private static final DialogActionButtonData BACK_BUTTON = new DialogActionButtonData(
            new DialogButtonData(ScreenTexts.BACK, BUTTON_WIDTH), Optional.empty());

    public static void initialize() {}

    private static RegistryKey<Dialog> of(String id) {
        return RegistryKey.of(RegistryKeys.DIALOG, Worldsinger.id(id));
    }

    private static RegistryKey<Dialog> of(Identifier id) {
        return RegistryKey.of(RegistryKeys.DIALOG, id);
    }

    private static List<DialogActionButtonData> createWorldhopButtons() {
        CosmerePlanet[] options = CosmerePlanet.VALUES;
        List<DialogActionButtonData> result = new ArrayList<>(options.length);
        for (CosmerePlanet planet : options) {
            result.add(createWorldhopButton(planet.getTranslationKey()));
        }
        return result;
    }

    private static DialogActionButtonData createWorldhopButton(String id) {
        NbtCompound payload = new NbtCompound();
        payload.putString(WORLDHOP_PAYLOAD_KEY, id);
        return new DialogActionButtonData(new DialogButtonData(
                Text.translatable("cosmere.worldsinger.planet.%s.name".formatted(id)), Optional.of(
                Text.translatable("cosmere.worldsinger.planet.%s.description".formatted(id))),
                BUTTON_WIDTH),
                // FIXME: Don't run the command directly so that non-opped players can run it on startup
                Optional.of(new SimpleDialogAction(
                        new ClickEvent.Custom(WORLDHOP_ID, Optional.of(payload)))));
    }

    public static void bootstrap(Registerable<Dialog> registry) {
        ItemStack worldhopIcon = Items.ENDER_EYE.getDefaultStack();
        worldhopIcon.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        List<DialogActionButtonData> worldhopButtons = createWorldhopButtons();
        // Config version does not have a back button because that freezes
        registry.register(WORLDHOP_CONFIG, new MultiActionDialog(
                new DialogCommonData(Text.translatable("menu.worldsinger.worldhop.title"),
                        Optional.empty(), false, true, AfterAction.CLOSE,
                        List.of(new ItemDialogBody(Items.ENDER_EYE.getDefaultStack(),
                                Optional.empty(), true, false, 16, 16), new PlainMessageDialogBody(
                                Text.translatable("menu.worldsinger.worldhop.description"),
                                TEXT_WIDTH)), List.of()), worldhopButtons, Optional.empty(), 1));
        // Version with the back button
        registry.register(WORLDHOP, new MultiActionDialog(
                new DialogCommonData(Text.translatable("menu.worldsinger.worldhop.title"),
                        Optional.empty(), true, true, AfterAction.CLOSE,
                        List.of(new ItemDialogBody(Items.ENDER_EYE.getDefaultStack(),
                                Optional.empty(), false, false, 16, 16), new PlainMessageDialogBody(
                                Text.translatable("menu.worldsinger.worldhop.description"),
                                TEXT_WIDTH)), List.of()), worldhopButtons, Optional.of(BACK_BUTTON),
                1));
    }

    private ModDialogs() {}
}
