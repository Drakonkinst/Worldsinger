package io.github.drakonkinst.worldsinger.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.drakonkinst.worldsinger.item.itemcontainer.ItemContainerSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BeesComponent;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.dynamic.Codecs;
import org.apache.commons.lang3.math.Fraction;

public class ItemContainerComponent implements TooltipData {

    public static final int MAX_ITEMS_PER_PAGE = 12;
    public static final int MAX_ITEMS_PER_ROW = 4;

    private static final int NORMAL_STACK_SIZE = 64;
    private static final Fraction NORMAL_STACK_SIZE_FRACTION = Fraction.getFraction(
            NORMAL_STACK_SIZE, 1);
    private static final Fraction BUNDLE_BASE_WEIGHT = Fraction.getFraction(NORMAL_STACK_SIZE,
            16); // 4
    public static final Codec<ItemContainerComponent> CODEC = ItemContainerComponentData.CODEC.flatXmap(
            ItemContainerComponent::validateWeight, component -> DataResult.success(
                    new ItemContainerComponentData(component.getMaxItemWeight(),
                            component.getValidItems(), component.isAutoPickupDisabled(),
                            component.getSettings(), component.getStacks())));
    public static final PacketCodec<RegistryByteBuf, ItemContainerComponent> PACKET_CODEC = ItemContainerComponentData.PACKET_CODEC.xmap(
            data -> new ItemContainerComponent(data.maxItemCount, data.validItems,
                    data.disableAutoPickup, data.settings, data.stacks),
            component -> new ItemContainerComponentData(component.getMaxItemWeight(),
                    component.getValidItems(), component.isAutoPickupDisabled(),
                    component.getSettings(), component.getStacks()));

    private static DataResult<ItemContainerComponent> validateWeight(
            ItemContainerComponentData data) {
        try {
            Fraction weight = calculateWeight(data.stacks());
            return DataResult.success(
                    new ItemContainerComponent(data.maxItemCount(), data.validItems(),
                            data.disableAutoPickup(), data.settings(), data.stacks(), weight, -1));
        } catch (ArithmeticException var2) {
            return DataResult.error(() -> "Excessive total item container weight");
        }
    }

    private static Fraction calculateWeight(List<ItemStack> stacks) {
        Fraction fraction = Fraction.ZERO;

        for (ItemStack itemStack : stacks) {
            fraction = fraction.add(getStackWeight(itemStack).multiplyBy(
                    Fraction.getFraction(itemStack.getCount(), 1)));
        }

        return fraction;
    }

    // Returns the "weight" of an item in terms of how they compare to a normal item which stacks to 64
    // Thus a normal item returns a weight of 1. An unstackable item returns 64.
    public static Fraction getStackWeight(ItemStack stack) {
        BundleContentsComponent bundleContentsComponent = stack.get(
                DataComponentTypes.BUNDLE_CONTENTS);
        if (bundleContentsComponent != null) {
            return BUNDLE_BASE_WEIGHT.add(
                    NORMAL_STACK_SIZE_FRACTION.divideBy(bundleContentsComponent.getOccupancy()));
        } else {
            List<BeehiveBlockEntity.BeeData> list = stack.getOrDefault(DataComponentTypes.BEES,
                    BeesComponent.DEFAULT).bees();
            return !list.isEmpty() ? NORMAL_STACK_SIZE_FRACTION
                    : Fraction.getFraction(NORMAL_STACK_SIZE, stack.getMaxCount());
        }
    }

    private final int maxItemWeight;
    private final TagKey<Item> validItems;
    private final boolean disableAutoPickup;
    private final ItemContainerSettings settings;
    private final List<ItemStack> stacks;

    // These two do not necessarily need to be saved
    private final Fraction weight;
    private final int selectedStackIndex;

    protected ItemContainerComponent(int maxItemWeight, TagKey<Item> validItems,
            boolean disableAutoPickup, ItemContainerSettings settings, List<ItemStack> stacks,
            Fraction weight, int selectedStackIndex) {
        this.maxItemWeight = maxItemWeight;
        this.validItems = validItems;
        this.disableAutoPickup = disableAutoPickup;
        this.settings = settings;
        this.stacks = stacks;
        this.weight = weight;
        this.selectedStackIndex = selectedStackIndex;
    }

    public ItemContainerComponent(int maxItemWeight, TagKey<Item> validItems,
            boolean disableAutoPickup, ItemContainerSettings settings, List<ItemStack> stacks) {
        this(maxItemWeight, validItems, disableAutoPickup, settings, stacks,
                calculateWeight(stacks), -1);
    }

    public ItemContainerComponent(int maxItemWeight, TagKey<Item> validItems,
            ItemContainerSettings settings) {
        this(maxItemWeight, validItems, false, settings, Collections.emptyList());
    }

    public int getNumberOfStacksShown() {
        int size = this.stacks.size();
        // One slot is taken up by the ellipses if it goes beyond one page
        int totalItemsShowing =
                size > MAX_ITEMS_PER_PAGE ? MAX_ITEMS_PER_PAGE - 1 : MAX_ITEMS_PER_PAGE;
        int columnIndex = size % MAX_ITEMS_PER_ROW;
        int numBlankSpaces = columnIndex == 0 ? 0 : MAX_ITEMS_PER_ROW - columnIndex;
        return Math.min(size, totalItemsShowing - numBlankSpaces);
    }

    public ItemStack get(int index) {
        return this.stacks.get(index);
    }

    public Stream<ItemStack> stream() {
        return this.stacks.stream().map(ItemStack::copy);
    }

    public Iterable<ItemStack> iterate() {
        return this.stacks;
    }

    public Iterable<ItemStack> iterateCopy() {
        return Lists.transform(this.stacks, ItemStack::copy);
    }

    public int size() {
        return this.stacks.size();
    }

    public boolean isEmpty() {
        return this.stacks.isEmpty();
    }

    public int getMaxItemWeight() {
        return maxItemWeight;
    }

    public TagKey<Item> getValidItems() {
        return validItems;
    }

    public ItemContainerSettings getSettings() {
        return settings;
    }

    public List<ItemStack> getStacks() {
        return stacks;
    }

    public Fraction getOccupancy() {
        return weight.divideBy(Fraction.getFraction(maxItemWeight, 1));
    }

    public boolean hasSelectedStack() {
        return this.selectedStackIndex > -1;
    }

    public int getSelectedStackIndex() {
        return selectedStackIndex;
    }

    public boolean shouldAutoPickup() {
        return settings.shouldAutoPickup() && !disableAutoPickup;
    }

    public boolean canToggleAutoPickup() {
        return settings.shouldAutoPickup();
    }

    public boolean canQuickDeposit(LivingEntity user) {
        return settings.canQuickDeposit() && !isEmpty() && (!canToggleAutoPickup()
                || !user.isSneaking());
    }

    public boolean shouldShowItemBar() {
        return settings.shouldShowItemBar();
    }

    // This boolean only matters if it would be enabled by default
    public boolean isAutoPickupDisabled() {
        return settings.shouldAutoPickup() && disableAutoPickup;
    }

    // We'll also use this to determine whether to render empty slots or not.
    // This also kind of assumes that the only items being inserted have a weight of 1, and might struggle with other items
    public boolean useSingleStacksOnly() {
        return settings.useSingleStacksOnly();
    }

    public boolean canBeStored(ItemStack stack) {
        return BundleContentsComponent.canBeBundled(stack) && stack.isIn(validItems);
    }

    public static class Builder {

        private int maxItemCount;
        private TagKey<Item> validItems;
        private boolean disableAutoPickup;
        private ItemContainerSettings settings;
        private final List<ItemStack> stacks;
        private Fraction weight;
        private int selectedStackIndex;

        public Builder(ItemContainerComponent base) {
            this.maxItemCount = base.maxItemWeight;
            this.validItems = base.validItems;
            this.disableAutoPickup = base.disableAutoPickup;
            this.settings = base.settings;
            this.stacks = new ArrayList<>(base.stacks);
            this.weight = base.weight;
            this.selectedStackIndex = base.selectedStackIndex;
        }

        public Builder clearContents() {
            this.stacks.clear();
            this.weight = Fraction.ZERO;
            this.selectedStackIndex = -1;
            return this;
        }

        private int getInsertionIndex(ItemStack stack) {
            if (!stack.isStackable() || this.settings.useSingleStacksOnly()) {
                return -1;
            } else {
                for (int i = 0; i < this.stacks.size(); ++i) {
                    if (ItemStack.areItemsAndComponentsEqual(this.stacks.get(i), stack)) {
                        return i;
                    }
                }
                return -1;
            }
        }

        private int getMaxAllowed(ItemStack stack) {
            Fraction weightRemaining = Fraction.getFraction(this.maxItemCount, 1)
                    .subtract(this.weight);
            Fraction itemWeight = ItemContainerComponent.getStackWeight(stack);
            return Math.max(weightRemaining.divideBy(itemWeight).intValue(), 0);
        }

        private int getNumToInsert(ItemStack stack) {
            if (stack.isEmpty()) {
                return 0;
            }
            int stackCount = this.settings.useSingleStacksOnly() ? 1 : stack.getCount();
            return Math.min(stackCount, this.getMaxAllowed(stack));
        }

        public int add(ItemStack stack) {
            if (!this.canBeStored(stack)) {
                return 0;
            }
            int numToInsert = getNumToInsert(stack);
            if (numToInsert == 0) {
                return 0;
            }
            this.weight = this.weight.add(ItemContainerComponent.getStackWeight(stack)
                    .multiplyBy(Fraction.getFraction(numToInsert, 1)));
            int insertionIndex = this.getInsertionIndex(stack);
            if (insertionIndex != -1) {
                ItemStack existingStack = this.stacks.remove(insertionIndex);
                int newCount = existingStack.getCount() + numToInsert;
                int numFullStacks = newCount / existingStack.getMaxCount();
                int numRemainder = newCount % existingStack.getMaxCount();
                stack.decrement(numToInsert);
                for (int i = 0; i < numFullStacks; ++i) {
                    this.stacks.addFirst(existingStack.copyWithCount(existingStack.getMaxCount()));
                }
                if (numRemainder > 0) {
                    this.stacks.addFirst(existingStack.copyWithCount(numRemainder));
                }
            } else {
                this.stacks.addFirst(stack.split(numToInsert));
            }

            return numToInsert;
        }

        public int add(Slot slot, PlayerEntity player) {
            ItemStack stack = slot.getStack();
            int maxAllowed = this.getMaxAllowed(stack);
            return this.canBeStored(stack) ? this.add(
                    slot.takeStackRange(stack.getCount(), maxAllowed, player)) : 0;
        }

        private boolean canBeStored(ItemStack stack) {
            return BundleContentsComponent.canBeBundled(stack) && stack.isIn(this.validItems);
        }

        public void setSelectedStackIndex(int selectedStackIndex) {
            this.selectedStackIndex =
                    this.selectedStackIndex != selectedStackIndex && !this.isOutOfBounds(
                            selectedStackIndex) ? selectedStackIndex : -1;
        }

        private boolean isOutOfBounds(int index) {
            return index < 0 || index >= this.stacks.size();
        }

        public ItemStack removeSelected() {
            if (this.stacks.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int index = this.isOutOfBounds(this.selectedStackIndex) ? 0 : this.selectedStackIndex;
            ItemStack selectedStack = this.stacks.remove(index).copy();
            this.weight = this.weight.subtract(ItemContainerComponent.getStackWeight(selectedStack)
                    .multiplyBy(Fraction.getFraction(selectedStack.getCount(), 1)));
            this.setSelectedStackIndex(-1);
            return selectedStack;
        }

        public Fraction getWeight() {
            return weight;
        }

        public boolean toggleAutoPickup() {
            disableAutoPickup = !disableAutoPickup;
            return disableAutoPickup;
        }

        public boolean isAutoPickupDisabled() {
            return disableAutoPickup;
        }

        public ItemContainerComponent build() {
            return new ItemContainerComponent(this.maxItemCount, this.validItems,
                    this.disableAutoPickup, this.settings, List.copyOf(this.stacks), this.weight,
                    this.selectedStackIndex);
        }
    }

    public record ItemContainerComponentData(int maxItemCount, TagKey<Item> validItems,
                                             boolean disableAutoPickup,
                                             ItemContainerSettings settings,
                                             List<ItemStack> stacks) {

        public static final Codec<ItemContainerComponentData> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(Codecs.NON_NEGATIVE_INT.fieldOf("max_item_count")
                                        .forGetter(ItemContainerComponentData::maxItemCount),
                                TagKey.codec(RegistryKeys.ITEM)
                                        .fieldOf("valid_items")
                                        .forGetter(ItemContainerComponentData::validItems),
                                Codec.BOOL.optionalFieldOf("disable_auto_pickup", false)
                                        .forGetter(ItemContainerComponentData::disableAutoPickup),
                                ItemContainerSettings.CODEC.fieldOf("settings")
                                        .forGetter(ItemContainerComponentData::settings),
                                ItemStack.CODEC.listOf()
                                        .fieldOf("items")
                                        .forGetter(ItemContainerComponentData::stacks))
                        .apply(instance, ItemContainerComponentData::new));
        public static final PacketCodec<RegistryByteBuf, ItemContainerComponentData> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.VAR_INT, ItemContainerComponentData::maxItemCount,
                TagKey.packetCodec(RegistryKeys.ITEM), ItemContainerComponentData::validItems,
                PacketCodecs.BOOLEAN, ItemContainerComponentData::disableAutoPickup,
                ItemContainerSettings.PACKET_CODEC, ItemContainerComponentData::settings,
                ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()),
                ItemContainerComponentData::stacks, ItemContainerComponentData::new);
    }
}
