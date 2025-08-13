package io.github.drakonkinst.worldsinger.item.itemcontainer;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

// Contains item container settings that we want to be available as presets, but not individually customizable
public enum ItemContainerSettings implements StringIdentifiable {
    POUCH(0, "pouch", true, Text.empty(), () -> SoundEvents.ITEM_BUNDLE_INSERT,
            () -> SoundEvents.ITEM_BUNDLE_INSERT_FAIL, () -> SoundEvents.ITEM_BUNDLE_REMOVE_ONE,
            () -> SoundEvents.ITEM_BUNDLE_DROP_CONTENTS),
    QUIVER(1, "quiver", true, Text.translatable("item.worldsinger.quiver.empty.description"),
            () -> SoundEvents.ITEM_BUNDLE_INSERT, () -> SoundEvents.ITEM_BUNDLE_INSERT_FAIL,
            () -> SoundEvents.ITEM_BUNDLE_REMOVE_ONE, () -> SoundEvents.ITEM_BUNDLE_DROP_CONTENTS);

    private static final IntFunction<ItemContainerSettings> BY_ID = ValueLists.createIndexToValueFunction(
            ItemContainerSettings::getId, values(), ValueLists.OutOfBoundsHandling.ZERO);
    public static final PacketCodec<ByteBuf, ItemContainerSettings> PACKET_CODEC = PacketCodecs.indexed(
            BY_ID, ItemContainerSettings::getId);
    public static final Codec<ItemContainerSettings> CODEC = StringIdentifiable.createBasicCodec(
            ItemContainerSettings::values);

    // TODO: Add more settings, like canBeNested, singleStackSlots, showAllEmptySlots
    private final int id;
    private final String name;
    private final boolean autoPickup;
    private final Text emptyDescription;
    private final Supplier<SoundEvent> insertSound;
    private final Supplier<SoundEvent> insertFailSound;
    private final Supplier<SoundEvent> removeOneSound;
    private final Supplier<SoundEvent> dropContentsSound;

    ItemContainerSettings(int id, String name, boolean autoPickup, Text emptyDescription,
            Supplier<SoundEvent> insertSound, Supplier<SoundEvent> insertFailSound,
            Supplier<SoundEvent> removeOneSound, Supplier<SoundEvent> dropAllSound) {
        this.id = id;
        this.name = name;
        this.autoPickup = autoPickup;
        this.emptyDescription = emptyDescription;
        this.insertSound = insertSound;
        this.insertFailSound = insertFailSound;
        this.removeOneSound = removeOneSound;
        this.dropContentsSound = dropAllSound;
    }

    public int getId() {
        return id;
    }

    public boolean shouldAutoPickup() {
        return autoPickup;
    }

    public Text getEmptyDescription() {
        return emptyDescription;
    }

    public SoundEvent getInsertSound() {
        return insertSound.get();
    }

    public SoundEvent getInsertFailSound() {
        return insertFailSound.get();
    }

    public SoundEvent getRemoveOneSound() {
        return removeOneSound.get();
    }

    public SoundEvent getDropContentsSound() {
        return dropContentsSound.get();
    }

    @Override
    public String asString() {
        return name;
    }
}
