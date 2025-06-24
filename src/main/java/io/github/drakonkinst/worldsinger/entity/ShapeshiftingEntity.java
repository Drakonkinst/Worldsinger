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

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.cosmere.ShapeshiftingManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.DataTracker.Builder;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

// Might reuse this code for other creatures that can shapeshift later.
// Note that even non-player entities might not extend this class; that's why it's important to use
// the Shapeshifter interface where possible. This is just one example of how this can be saved
// and loaded.
// Morphs do not tick, so any visual updates need to be called manually. Morph NBT is NOT synced.
public abstract class ShapeshiftingEntity extends PathAwareEntity implements Shapeshifter {

    protected static final TrackedData<NbtCompound> MORPH = DataTracker.registerData(
            ShapeshiftingEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

    public static final String MORPH_KEY = "Morph";

    protected LivingEntity morph = null;
    private boolean hasLoadedMorph = false;

    protected ShapeshiftingEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker(Builder builder) {
        super.initDataTracker(builder);
        builder.add(MORPH, new NbtCompound());
    }

    @Override
    public void tick() {
        super.tick();

        // When initially loaded on the client side, sync it with the data
        if (this.getWorld().isClient() && !hasLoadedMorph) {
            checkMorphOnLoad();
            hasLoadedMorph = true;
        }

        tickMorph();
    }

    private void tickMorph() {
        if (morph == null) {
            return;
        }

        if (this.getWorld().isClient()) {
            ShapeshiftingManager.tickMorphClient(morph, random);
        } else {
            ShapeshiftingManager.tickMorphServer(this, morph);
        }
    }

    // Make attacking animations play for entities that don't use the arms
    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        ShapeshiftingManager.onAttackServer(world, this);
        return super.tryAttack(world, target);
    }

    private void checkMorphOnLoad() {
        NbtCompound morphData = this.getMorphData();
        if (morphData.isEmpty() && morph != null) {
            this.updateMorph(null);
        } else if (!morphData.isEmpty()) {
            ShapeshiftingManager.createMorphFromNbt(this, morphData, false);
        }
    }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put(MORPH_KEY, NbtCompound.CODEC, this.getMorphData());
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);
        view.read(MORPH_KEY, NbtCompound.CODEC).ifPresent(this::setMorphData);
        this.setMorphFromData();
    }

    public void setMorph(@Nullable LivingEntity morph) {
        this.morph = morph;
    }

    private void setMorphDataFromEntity(LivingEntity morph) {
        if (morph == null) {
            this.setMorphData(new NbtCompound());
            return;
        }
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(
                this.getErrorReporterContext(), Worldsinger.LOGGER)) {
            NbtWriteView nbtWriteView = NbtWriteView.create(logging, this.getRegistryManager());
            morph.writeData(nbtWriteView);
            Identifier entityId = EntityType.getId(morph.getType());
            nbtWriteView.putString("id", entityId == null ? "unknown" : entityId.toString());
            this.setMorphData(nbtWriteView.getNbt());
        }
    }

    private void setMorphFromData() {
        NbtCompound morphNbt = this.getMorphData();
        ShapeshiftingManager.createMorphFromNbt(this, morphNbt, false);
    }

    private void setMorphData(NbtCompound nbtCompound) {
        this.dataTracker.set(MORPH, nbtCompound);
    }

    public NbtCompound getMorphData() {
        return this.dataTracker.get(MORPH);
    }

    public void updateMorph(@Nullable LivingEntity morph) {
        this.setMorph(morph);
        this.calculateDimensions();

        World world = this.getWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            this.setMorphDataFromEntity(morph);
            ShapeshiftingManager.syncToNearbyPlayers(serverWorld, this);
        }
    }

    @Nullable
    public LivingEntity getMorph() {
        return morph;
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        if (morph == null) {
            return super.getDimensions(pose);
        }
        return morph.getDimensions(pose);
    }

    @Override
    public float getEyeHeight(EntityPose pose) {
        if (morph == null) {
            return super.getEyeHeight(pose);
        }
        return morph.getEyeHeight(pose);
    }
}
