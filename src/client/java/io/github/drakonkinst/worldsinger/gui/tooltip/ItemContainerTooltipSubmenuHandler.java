package io.github.drakonkinst.worldsinger.gui.tooltip;

import io.github.drakonkinst.worldsinger.item.itemcontainer.ItemContainerInteractions;
import io.github.drakonkinst.worldsinger.network.packet.ItemContainerItemSelectedPayload;
import io.github.drakonkinst.worldsinger.registry.ModDataComponentTypes;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipSubmenuHandler;
import net.minecraft.client.input.Scroller;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.joml.Vector2i;

public class ItemContainerTooltipSubmenuHandler implements TooltipSubmenuHandler {

    private final MinecraftClient client;
    private final Scroller scroller;

    public ItemContainerTooltipSubmenuHandler(MinecraftClient client) {
        this.client = client;
        this.scroller = new Scroller();
    }

    @Override
    public boolean isApplicableTo(Slot slot) {
        return slot.getStack().contains(ModDataComponentTypes.ITEM_CONTAINER);
    }

    @Override
    public boolean onScroll(double horizontal, double vertical, int slotId, ItemStack item) {
        int numStacksShown = ItemContainerInteractions.getNumberOfStacksShown(item);
        if (numStacksShown <= 0) {
            return false;
        }
        Vector2i delta = this.scroller.update(horizontal, vertical);
        int amount = delta.y == 0 ? -delta.x : delta.y;
        if (amount != 0) {
            int currentSelectedIndex = ItemContainerInteractions.getSelectedStackIndex(item);
            int nextSelectedIndex = Scroller.scrollCycling(amount, currentSelectedIndex,
                    numStacksShown);
            if (currentSelectedIndex != nextSelectedIndex) {
                this.sendPacket(item, slotId, nextSelectedIndex);
            }
        }

        return true;
    }

    @Override
    public void reset(Slot slot) {
        this.reset(slot.getStack(), slot.id);
    }

    @Override
    public void onMouseClick(Slot slot, SlotActionType actionType) {
        if (actionType == SlotActionType.QUICK_MOVE || actionType == SlotActionType.SWAP) {
            this.reset(slot.getStack(), slot.id);
        }
    }

    private void sendPacket(ItemStack item, int slotId, int selectedItemIndex) {
        if (this.client.getNetworkHandler() != null
                && selectedItemIndex < ItemContainerInteractions.getNumberOfStacksShown(item)) {
            ItemContainerInteractions.setSelectedStackIndex(item, selectedItemIndex);
            ClientPlayNetworking.send(
                    new ItemContainerItemSelectedPayload(slotId, selectedItemIndex));
        }
    }

    public void reset(ItemStack item, int slotId) {
        this.sendPacket(item, slotId, -1);
    }
}
