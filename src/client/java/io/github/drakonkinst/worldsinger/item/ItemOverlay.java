package io.github.drakonkinst.worldsinger.item;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.SaltedFoodUtil;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

// Adding an item to this list will automatically generate a new item overlay that can be used in
// applyItemOverlays(), including model definition in datagen.
// Textures are pulled from worldsinger/items/<overlay>
public enum ItemOverlay {
    SALTED_FOOD("salted_overlay"),
    SILVER_LINED_AXE("silver_lined_axe_overlay"),
    SILVER_LINED_BOAT("silver_lined_boat_overlay"),
    SILVER_LINED_CHEST_BOAT("silver_lined_chest_boat_overlay"),
    SILVER_LINED_RAFT("silver_lined_raft_overlay"),
    SILVER_LINED_CHEST_RAFT("silver_lined_chest_raft_overlay");

    public static final ItemOverlay[] VALUES = values();

    // TODO: Can probably move to custom event if we're feeling fancy
    public static void applyItemOverlays(ItemRenderState renderState, ItemStack stack,
            ItemModelManager itemModelManager, ItemDisplayContext displayContext, ClientWorld world,
            LivingEntity entity, int seed) {
        if (displayContext != ItemDisplayContext.GUI) {
            return;
        }
        if (SaltedFoodUtil.isSalted(stack)) {
            // Apply salt overlay
            ItemModel saltedModel = MinecraftClient.getInstance()
                    .getBakedModelManager()
                    .getModel(SALTED_FOOD.getModelKey());
            if (saltedModel != null) {
                saltedModel.update(renderState, stack, itemModelManager, ItemDisplayContext.GUI,
                        world, entity, seed);
            }
        }
    }

    @Nullable
    private static ItemOverlay getSilverLinedOverlayForItem(ItemStack stack) {
        if (stack.isIn(ItemTags.AXES)) {
            return ItemOverlay.SILVER_LINED_AXE;
        }
        if (stack.isOf(Items.BAMBOO_CHEST_RAFT)) {
            return ItemOverlay.SILVER_LINED_CHEST_RAFT;
        } else if (stack.isIn(ItemTags.CHEST_BOATS)) {
            return ItemOverlay.SILVER_LINED_CHEST_BOAT;
        }
        if (stack.isOf(Items.BAMBOO_RAFT)) {
            return ItemOverlay.SILVER_LINED_RAFT;
        } else if (stack.isIn(ItemTags.BOATS)) {
            return ItemOverlay.SILVER_LINED_BOAT;
        }
        return null;
    }

    private final Identifier id;
    private final ExtraModelKey<ItemModel> modelKey;

    ItemOverlay(String name) {
        this.id = Worldsinger.id("item/" + name);
        this.modelKey = ExtraModelKey.create(() -> Worldsinger.idStr(name));
    }

    public Identifier getId() {
        return id;
    }

    public ExtraModelKey<ItemModel> getModelKey() {
        return modelKey;
    }
}
