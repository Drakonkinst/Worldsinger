package io.github.drakonkinst.examplemod;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItems {

    private ModItems() {}

    public static final Item DEFAULT_CUBE = ModItems.register(
            new DefaultCubeItem(new FabricItemSettings()), "default_cube"
    );

    // By popular vote
    public static final Item BALLS = ModItems.register(
            new Item(new FabricItemSettings()
                    // Probably best to make a separate ModFoodComponents static class and have these all in one place
                    .food(new FoodComponent.Builder()
                            .alwaysEdible()
                            .hunger(4)
                            .saturationModifier(0.3f)
                            .statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 15 * Constants.SECONDS_TO_TICKS, 1), 1.0f)
                            .statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 5 * Constants.SECONDS_TO_TICKS, 1), 1.0f)
                            .build())),
            "balls"
    );

    private static final ItemGroup MODDED_ITEMS_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(DEFAULT_CUBE))
            .displayName(Text.translatable("itemGroup.tutorial.test_group"))
            .build();

    public static <T extends Item> T register(T item, String id) {
        Identifier itemId = new Identifier(ExampleMod.MOD_ID, id);
        T registeredItem = Registry.register(Registries.ITEM, itemId, item);
        return registeredItem;
    }

    public static void initialize() {
        // Custom item group
        Identifier moddedItemsIdentifier = new Identifier(ExampleMod.MOD_ID, "modded_items");
        Registry.register(Registries.ITEM_GROUP, moddedItemsIdentifier, MODDED_ITEMS_GROUP);
        RegistryKey<ItemGroup> moddedItemsItemGroupKey = RegistryKey.of(RegistryKeys.ITEM_GROUP, moddedItemsIdentifier);

        // ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> {
        //     itemGroup.add(ModItems.DEFAULT_CUBE);
        // });

        ItemGroupEvents.modifyEntriesEvent(moddedItemsItemGroupKey).register((itemGroup) -> {
            itemGroup.add(ModItems.DEFAULT_CUBE);
            itemGroup.add(ModItems.BALLS);
        });
    }

}
