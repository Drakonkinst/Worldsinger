package io.github.drakonkinst.worldsinger.gui.tooltip;

import io.github.drakonkinst.worldsinger.item.component.ItemContainerComponent;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
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
    private static final int SLOT_DIMENSION = 24;
    private static final int ROW_WIDTH = 96;
    private static final int PROGRESS_BAR_WIDTH = 94;
    private static final Text BUNDLE_FULL = Text.translatable("item.minecraft.bundle.full");
    private static final Text BUNDLE_EMPTY = Text.translatable("item.minecraft.bundle.empty");
    private final ItemContainerComponent itemContainer;

    public ItemContainerTooltipComponent(ItemContainerComponent itemContainer) {
        this.itemContainer = itemContainer;
    }

    @Override
    public int getHeight(TextRenderer textRenderer) {
        return this.itemContainer.isEmpty() ? getHeightOfEmpty(textRenderer,
                this.itemContainer.getSettings().getEmptyDescription())
                : this.getHeightOfNonEmpty();
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return ROW_WIDTH;
    }

    @Override
    public boolean isSticky() {
        return true;
    }

    private static int getHeightOfEmpty(TextRenderer textRenderer, Text emptyDescription) {
        return getDescriptionHeight(textRenderer, emptyDescription) + 13 + 8;
    }

    private int getHeightOfNonEmpty() {
        return this.getRowsHeight() + 13 + 8;
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
        return Math.min(12, this.itemContainer.size());
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height,
            DrawContext context) {
        if (this.itemContainer.isEmpty()) {
            this.drawEmptyTooltip(textRenderer, x, y, width, height, context);
        } else {
            this.drawNonEmptyTooltip(textRenderer, x, y, width, height, context);
        }
    }

    private void drawEmptyTooltip(TextRenderer textRenderer, int x, int y, int width, int height,
            DrawContext context) {
        drawEmptyDescription(x + this.getXMargin(width), y, textRenderer, context,
                this.itemContainer.getSettings().getEmptyDescription());
        this.drawProgressBar(x + this.getXMargin(width), y + getDescriptionHeight(textRenderer,
                        this.itemContainer.getSettings().getEmptyDescription()) + SLOTS_PER_ROW,
                textRenderer, context);
    }

    private void drawNonEmptyTooltip(TextRenderer textRenderer, int x, int y, int width, int height,
            DrawContext context) {
        boolean bl = this.itemContainer.size() > 12;
        List<ItemStack> list = this.firstStacksInContents(
                this.itemContainer.getNumberOfStacksShown());
        int i = x + this.getXMargin(width) + ROW_WIDTH;
        int j = y + this.getRows() * SLOT_DIMENSION;
        int k = 1;

        for (int l = 1; l <= this.getRows(); l++) {
            for (int m = 1; m <= SLOTS_PER_ROW; m++) {
                int n = i - m * SLOT_DIMENSION;
                int o = j - l * SLOT_DIMENSION;
                if (shouldDrawExtraItemsCount(bl, m, l)) {
                    drawExtraItemsCount(n, o, this.numContentItemsAfter(list), textRenderer,
                            context);
                } else if (shouldDrawItem(list, k)) {
                    this.drawItem(k, n, o, list, k, textRenderer, context);
                    k++;
                }
            }
        }

        this.drawSelectedItemTooltip(textRenderer, context, x, y, width);
        this.drawProgressBar(x + this.getXMargin(width), y + this.getRowsHeight() + SLOTS_PER_ROW,
                textRenderer, context);
    }

    private List<ItemStack> firstStacksInContents(int numberOfStacksShown) {
        int i = Math.min(this.itemContainer.size(), numberOfStacksShown);
        return this.itemContainer.stream().toList().subList(0, i);
    }

    private static boolean shouldDrawExtraItemsCount(boolean hasMoreItems, int column, int row) {
        return hasMoreItems && column * row == 1;
    }

    private static boolean shouldDrawItem(List<ItemStack> items, int itemIndex) {
        return items.size() >= itemIndex;
    }

    private int numContentItemsAfter(List<ItemStack> items) {
        return this.itemContainer.stream().skip(items.size()).mapToInt(ItemStack::getCount).sum();
    }

    private void drawItem(int index, int x, int y, List<ItemStack> stacks, int seed,
            TextRenderer textRenderer, DrawContext drawContext) {
        int i = stacks.size() - index;
        boolean bl = i == this.itemContainer.getSelectedStackIndex();
        ItemStack itemStack = (ItemStack) stacks.get(i);
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

    private static void drawExtraItemsCount(int x, int y, int numExtra, TextRenderer textRenderer,
            DrawContext drawContext) {
        drawContext.drawCenteredTextWithShadow(textRenderer, "+" + numExtra, x + 12, y + 10,
                Colors.WHITE);
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
                    (Identifier) itemStack.get(DataComponentTypes.TOOLTIP_STYLE));
        }
    }

    private void drawProgressBar(int x, int y, TextRenderer textRenderer, DrawContext drawContext) {
        drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.getProgressBarFillTexture(),
                x + 1, y, this.getProgressBarFill(), 13);
        drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUNDLE_PROGRESS_BAR_BORDER_TEXTURE,
                x, y, ROW_WIDTH, 13);
        Text text = this.getProgressBarLabel();
        if (text != null) {
            drawContext.drawCenteredTextWithShadow(textRenderer, text, x + 48, y + 3, Colors.WHITE);
        }
    }

    private static void drawEmptyDescription(int x, int y, TextRenderer textRenderer,
            DrawContext drawContext, Text emptyDescription) {
        drawContext.drawWrappedTextWithShadow(textRenderer, emptyDescription, x, y, ROW_WIDTH,
                -5592406);
    }

    private static int getDescriptionHeight(TextRenderer textRenderer, Text emptyDescription) {
        return textRenderer.wrapLines(emptyDescription, ROW_WIDTH).size() * 9;
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
            return BUNDLE_EMPTY;
        } else {
            return this.itemContainer.getOccupancy().compareTo(Fraction.ONE) >= 0 ? BUNDLE_FULL
                    : null;
        }
    }
}
