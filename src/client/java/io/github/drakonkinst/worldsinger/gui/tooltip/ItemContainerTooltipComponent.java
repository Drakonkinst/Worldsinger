package io.github.drakonkinst.worldsinger.gui.tooltip;

import io.github.drakonkinst.worldsinger.item.component.ItemContainerComponent;
import io.github.drakonkinst.worldsinger.item.itemcontainer.ItemContainerInteractions;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

public class ItemContainerTooltipComponent implements TooltipComponent {

    private static final Identifier BUNDLE_PROGRESS_BAR_BORDER_TEXTURE = Identifier.ofVanilla(
            "container/bundle/bundle_progressbar_border");
    private static final Identifier BUNDLE_PROGRESS_BAR_FILL_TEXTURE = Identifier.ofVanilla(
            "container/bundle/bundle_progressbar_fill");
    private static final Identifier BUNDLE_PROGRESS_BAR_FULL_TEXTURE = Identifier.ofVanilla(
            "container/bundle/bundle_progressbar_full");
    private static final Identifier BUNDLE_SLOT_HIGHLIGHT_BACK_TEXTURE = Identifier.ofVanilla(
            "container/bundle/slot_highlight_back");
    private static final Identifier BUNDLE_SLOT_HIGHLIGHT_FRONT_TEXTURE = Identifier.ofVanilla(
            "container/bundle/slot_highlight_front");
    private static final Identifier BUNDLE_SLOT_BACKGROUND_TEXTURE = Identifier.ofVanilla(
            "container/bundle/slot_background");
    private static final int SLOTS_PER_ROW = 4;
    private static final int PROGRESS_BAR_MARGIN = 4;
    private static final int SLOT_DIMENSION = 24;
    private static final int ROW_WIDTH = 96;
    private static final int ROW_HEIGHT = 9;
    private static final int PROGRESS_BAR_WIDTH = 94;
    private static final int PROGRESS_BAR_HEIGHT = 13;
    private static final int EMPTY_DESCRIPTION_COLOR = -5592406;
    private static final int TEXT_LINE_MARGIN = 4;
    private static final int TOOLTIP_TOP_MARGIN = 8;
    private static final int MAX_VISIBLE_SLOTS = 12;
    private static final Text ITEM_CONTAINER_FULL = Text.translatable(
            "item.worldsinger.item_container.full");
    private static final Text ITEM_CONTAINER_EMPTY = Text.translatable(
            "item.worldsinger.item_container.empty");

    private static int getTooltipTextHeight(TextRenderer textRenderer, List<Text> tooltipText) {
        int height = 0;
        for (Text text : tooltipText) {
            height += getWrappedTextHeight(textRenderer, text);
        }
        if (tooltipText.size() >= 2) {
            height += (tooltipText.size() - 1) * TEXT_LINE_MARGIN;
        }
        return height;
    }

    private static int drawTooltipText(int x, int y, TextRenderer textRenderer,
            DrawContext drawContext, List<Text> tooltipText) {
        int offset = 0;
        for (int i = 0; i < tooltipText.size(); ++i) {
            Text text = tooltipText.get(i);
            if (i > 0) {
                offset += TEXT_LINE_MARGIN;
            }
            drawSingleTooltipText(x, y + offset, textRenderer, drawContext, text);
            int height = getWrappedTextHeight(textRenderer, text);
            offset += height;
        }
        return offset;
    }

    private static void drawSingleTooltipText(int x, int y, TextRenderer textRenderer,
            DrawContext drawContext, Text text) {
        drawContext.drawWrappedTextWithShadow(textRenderer, text, x, y, ROW_WIDTH, Colors.WHITE);
    }

    private static int getWrappedTextHeight(TextRenderer textRenderer, Text text) {
        return textRenderer.wrapLines(text, ROW_WIDTH).size() * ROW_HEIGHT;
    }

    private static boolean shouldDrawExtraItemsCount(boolean hasMoreItems, int column, int row) {
        return hasMoreItems && column * row == 1;
    }

    private static boolean shouldDrawItem(List<ItemStack> items, int itemIndex) {
        return items.size() >= itemIndex;
    }

    private static void drawExtraItemsCount(int x, int y, int numExtra, TextRenderer textRenderer,
            DrawContext drawContext) {
        drawContext.drawCenteredTextWithShadow(textRenderer, "+" + numExtra, x + 12, y + 10,
                Colors.WHITE);
    }

    private final ItemContainerComponent itemContainer;
    private final Text emptyDescription;

    public ItemContainerTooltipComponent(ItemContainerComponent itemContainer) {
        this.itemContainer = itemContainer;
        MutableText emptyDescription = this.itemContainer.getSettings().getEmptyDescription();
        if (emptyDescription == null) {
            this.emptyDescription = null;
        } else {
            this.emptyDescription = emptyDescription.copy().withColor(EMPTY_DESCRIPTION_COLOR);
        }
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        int height = this.getRowsHeight() + TOOLTIP_TOP_MARGIN;
        List<Text> tooltipText = getTooltipText();
        height += getTooltipTextHeight(textRenderer, tooltipText);
        if (itemContainer.shouldShowItemBar()) {
            height += PROGRESS_BAR_HEIGHT;
        }
        return height;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return ROW_WIDTH;
    }

    @Override
    public boolean isSticky() {
        return true;
    }

    private List<Text> getTooltipText() {
        List<Text> tooltipText = new ArrayList<>();
        if (this.itemContainer.isEmpty() && emptyDescription != null) {
            tooltipText.add(emptyDescription);
        }
        if (itemContainer.canToggleAutoPickup()) {
            tooltipText.add(getAutoPickupText());
        }
        return tooltipText;
    }

    private MutableText getAutoPickupText() {
        MutableText text = Text.empty();
        text.append(Text.translatable("item.worldsinger.item_container.auto_pickup",
                ItemContainerInteractions.getAutoPickupStatusText(
                        itemContainer.shouldAutoPickup())));
        text.append(" ");
        text.append(Text.translatable("item.worldsinger.item_container.auto_pickup_tooltip_hint")
                .formatted(Formatting.BLUE));
        return text;
    }

    private int getRowsHeight() {
        return this.getRows() * SLOT_DIMENSION;
    }

    private int getXMargin(int width) {
        return (width - ROW_WIDTH) / 2;
    }

    private int getRows() {
        return MathHelper.ceilDiv(this.getNumVisibleSlots(), SLOTS_PER_ROW);
    }

    private int getNumVisibleSlots() {
        int slotsToShow =
                this.itemContainer.useSingleStacksOnly() ? this.itemContainer.getMaxItemWeight()
                        : this.itemContainer.size();
        return Math.min(MAX_VISIBLE_SLOTS, slotsToShow);
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height,
            DrawContext context) {
        List<Text> tooltipText = getTooltipText();
        int elementOffset = drawTooltipText(x + this.getXMargin(width), y, textRenderer, context,
                tooltipText);
        elementOffset += drawItemSlots(textRenderer, x, y, width, context, elementOffset);
        if (itemContainer.shouldShowItemBar()) {
            this.drawProgressBar(x + this.getXMargin(width),
                    y + elementOffset + PROGRESS_BAR_MARGIN, textRenderer, context);
        }
    }

    private int drawItemSlots(TextRenderer textRenderer, int x, int y, int width,
            DrawContext context, int elementOffset) {
        boolean hasMoreItems = this.itemContainer.size() > MAX_VISIBLE_SLOTS;
        List<ItemStack> list = this.firstStacksInContents(
                this.itemContainer.getNumberOfStacksShown());

        int minX = x;
        if (this.itemContainer.shouldShowItemBar()) {
            // Make it align with the item bar
            minX += this.getXMargin(width);
        }
        int maxY = y + this.getRows() * SLOT_DIMENSION;
        int index = 1;

        for (int row = 1; row <= this.getRows(); row++) {
            for (int column = 1; column <= SLOTS_PER_ROW; column++) {
                int itemX = minX + (column - 1) * SLOT_DIMENSION;
                int itemY = maxY - row * SLOT_DIMENSION;
                if (shouldDrawExtraItemsCount(hasMoreItems, column, row)) {
                    drawExtraItemsCount(itemX, itemY + elementOffset,
                            this.numContentItemsAfter(list), textRenderer, context);
                } else if (shouldDrawItem(list, index)) {
                    this.drawItem(index, itemX, itemY + elementOffset, list, index, textRenderer,
                            context);
                    index++;
                } else if (this.itemContainer.useSingleStacksOnly()
                        && this.itemContainer.getMaxItemWeight() >= index) {
                    this.drawEmptySlot(itemX, itemY + elementOffset, context);
                    index++;
                }
            }
        }

        this.drawSelectedItemTooltip(textRenderer, context, x, y + elementOffset, width);
        return this.getRowsHeight();
    }

    private List<ItemStack> firstStacksInContents(int numberOfStacksShown) {
        int i = Math.min(this.itemContainer.size(), numberOfStacksShown);
        return this.itemContainer.stream().toList().subList(0, i);
    }

    private int numContentItemsAfter(List<ItemStack> items) {
        return this.itemContainer.stream().skip(items.size()).mapToInt(ItemStack::getCount).sum();
    }

    private void drawItem(int index, int x, int y, List<ItemStack> stacks, int seed,
            TextRenderer textRenderer, DrawContext drawContext) {
        int i = stacks.size() - index;
        boolean bl = i == this.itemContainer.getSelectedStackIndex();
        ItemStack itemStack = stacks.get(i);
        if (bl) {
            drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,
                    BUNDLE_SLOT_HIGHLIGHT_BACK_TEXTURE, x, y, SLOT_DIMENSION, SLOT_DIMENSION);
        } else {
            drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_BACKGROUND_TEXTURE,
                    x, y, SLOT_DIMENSION, SLOT_DIMENSION);
        }

        drawContext.drawItem(itemStack, x + SLOTS_PER_ROW, y + SLOTS_PER_ROW, seed);
        drawContext.drawStackOverlay(textRenderer, itemStack, x + SLOTS_PER_ROW, y + SLOTS_PER_ROW);
        if (bl) {
            drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED,
                    BUNDLE_SLOT_HIGHLIGHT_FRONT_TEXTURE, x, y, SLOT_DIMENSION, SLOT_DIMENSION);
        }
    }

    private void drawEmptySlot(int x, int y, DrawContext drawContext) {
        drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_SLOT_BACKGROUND_TEXTURE, x,
                y, SLOT_DIMENSION, SLOT_DIMENSION);
    }

    private void drawSelectedItemTooltip(TextRenderer textRenderer, DrawContext drawContext, int x,
            int y, int width) {
        if (this.itemContainer.hasSelectedStack()) {
            ItemStack itemStack = this.itemContainer.get(
                    this.itemContainer.getSelectedStackIndex());
            Text text = itemStack.getFormattedName();
            int i = textRenderer.getWidth(text.asOrderedText());
            int j = x + width / 2 - 12;
            TooltipComponent tooltipComponent = TooltipComponent.of(text.asOrderedText());
            drawContext.drawTooltipImmediately(textRenderer, List.of(tooltipComponent), j - i / 2,
                    y - 15, HoveredTooltipPositioner.INSTANCE,
                    itemStack.get(DataComponentTypes.TOOLTIP_STYLE));
        }
    }

    private void drawProgressBar(int x, int y, TextRenderer textRenderer, DrawContext drawContext) {
        drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.getProgressBarFillTexture(),
                x + 1, y, this.getProgressBarFill(), PROGRESS_BAR_HEIGHT);
        drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_PROGRESS_BAR_BORDER_TEXTURE,
                x, y, ROW_WIDTH, PROGRESS_BAR_HEIGHT);
        Text text = this.getProgressBarLabel();
        if (text != null) {
            drawContext.drawCenteredTextWithShadow(textRenderer, text, x + 48, y + 3, Colors.WHITE);
        }
    }

    private int getProgressBarFill() {
        return MathHelper.clamp(
                MathHelper.multiplyFraction(this.itemContainer.getOccupancy(), PROGRESS_BAR_WIDTH),
                0, PROGRESS_BAR_WIDTH);
    }

    private Identifier getProgressBarFillTexture() {
        return this.itemContainer.getOccupancy().compareTo(Fraction.ONE) >= 0
                ? BUNDLE_PROGRESS_BAR_FULL_TEXTURE : BUNDLE_PROGRESS_BAR_FILL_TEXTURE;
    }

    @Nullable
    private Text getProgressBarLabel() {
        if (this.itemContainer.isEmpty()) {
            return ITEM_CONTAINER_EMPTY;
        }
        if (this.itemContainer.getOccupancy().compareTo(Fraction.ONE) >= 0) {
            return ITEM_CONTAINER_FULL;
        }
        return null;
    }
}
