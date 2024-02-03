/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Drakonkinst
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.drakonkinst.worldsinger.entity;

import io.github.drakonkinst.worldsinger.cosmere.ShapeshiftingManager;
import java.util.Collections;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.world.World;

public class PlayerMorphDummy extends LivingEntity {

    public static final String KEY_PLAYER = "Player";
    public static final String KEY_PLAYER_NAME = "PlayerName";
    private UUID playerUUID;
    private String playerName;

    public PlayerMorphDummy(World world, UUID uuid, String playerName) {
        super(EntityType.PLAYER, world);
        this.playerUUID = uuid;
        this.playerName = playerName;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        playerUUID = nbt.getUuid(KEY_PLAYER);
        playerName = nbt.getString(KEY_PLAYER_NAME);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putUuid(KEY_PLAYER, playerUUID);
        nbt.putString(KEY_PLAYER_NAME, playerName);
        return nbt;
    }

    @Override
    public boolean saveSelfNbt(NbtCompound nbt) {
        nbt.putString("id", ShapeshiftingManager.ID_PLAYER);
        writeNbt(nbt);
        return true;
    }

    @Override
    protected Text getDefaultName() {
        return Text.literal(playerName);
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        // Do nothing
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }
}
