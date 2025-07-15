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
package io.github.drakonkinst.worldsinger.entity.attachments.player;

import io.github.drakonkinst.worldsinger.api.sync.AttachmentSync;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightAetherBondManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightCreatureManager;
import io.github.drakonkinst.worldsinger.entity.MidnightCreatureEntity;
import io.github.drakonkinst.worldsinger.entity.attachments.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.world.World;

public class PlayerMidnightAetherBondManager implements MidnightAetherBondManager {

    private static final String KEY_BOND_COUNT = "bond_count";
    private static final int EXPIRY_TIME = MidnightCreatureManager.MAX_DRAIN_INTERVAL_TICKS * 2;
    private static final int UPDATE_INTERVAL = 20;

    private final Int2LongMap expiryMap = new Int2LongOpenHashMap();
    private int bondCount = 0;
    private long age = 0;
    private boolean isDirty = false;

    @Override
    public void serverTick(PlayerEntity player) {
        ++age;
        if (age % UPDATE_INTERVAL == 0) {
            clearExpiredEntries();
        }
        if (isDirty) {
            AttachmentSync.sync(player, ModAttachmentTypes.MIDNIGHT_AETHER_BOND, this);
            isDirty = false;
        }
    }

    @Override
    public void updateBond(int id) {
        expiryMap.put(id, age);
        bondCount = expiryMap.size();
    }

    @Override
    public void removeBond(int id) {
        expiryMap.remove(id);
        bondCount = expiryMap.size();
        isDirty = true;
    }

    private void clearExpiredEntries() {
        boolean anyRemoved = expiryMap.int2LongEntrySet()
                .removeIf(entry -> age > entry.getLongValue() + EXPIRY_TIME);
        if (anyRemoved) {
            isDirty = true;
        }
        bondCount = expiryMap.size();
    }

    @Override
    // Called on server-side
    public void onDeath(PlayerEntity player) {
        World world = player.getWorld();
        for (Entry entry : expiryMap.int2LongEntrySet()) {
            Entity entity = world.getEntityById(entry.getIntKey());
            if (entity instanceof MidnightCreatureEntity midnightCreature) {
                midnightCreature.forgetAboutPlayer(player);
            }
        }
        expiryMap.clear();
        bondCount = 0;
        isDirty = true;
    }

    @Override
    public void dispelAllBonds(PlayerEntity player, boolean playEffects) {
        if (!(player.getWorld() instanceof ServerWorld world)) {
            return;
        }
        boolean shouldPlayEffects = playEffects && !expiryMap.isEmpty();
        for (Entry entry : expiryMap.int2LongEntrySet()) {
            Entity entity = world.getEntityById(entry.getIntKey());
            if (entity instanceof MidnightCreatureEntity midnightCreature) {
                midnightCreature.dispel(world, playEffects);
            }
        }
        if (shouldPlayEffects) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSoundEvents.ENTITY_MIDNIGHT_CREATURE_BOND_BREAK, SoundCategory.PLAYERS, 1.0f,
                    0.5f);
        }
        expiryMap.clear();
        bondCount = 0;
        isDirty = true;
    }

    @Override
    public int getBondCount() {
        return bondCount;
    }

    @Override
    public void syncToNbt(NbtCompound nbt) {
        nbt.putInt(KEY_BOND_COUNT, bondCount);
    }

    @Override
    public void syncFromNbt(NbtCompound nbt) {
        this.bondCount = nbt.getInt(KEY_BOND_COUNT, 0);
    }
}
