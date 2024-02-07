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

package io.github.drakonkinst.worldsinger.mixin.world;

import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarLunagreeManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeManagerAccess;
import io.github.drakonkinst.worldsinger.cosmere.lumar.NullLunagreeManager;
import io.github.drakonkinst.worldsinger.world.PersistentByteDataManagerAccess;
import io.github.drakonkinst.worldsinger.worldgen.dimension.ModDimensions;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage.Session;
import net.minecraft.world.spawner.SpecialSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldLunagreeManagerMixin extends World implements StructureWorldAccess,
        LunagreeManagerAccess {

    @Shadow
    public abstract PersistentStateManager getPersistentStateManager();

    @Unique
    private LunagreeManager lunagreeManager;

    protected ServerWorldLunagreeManagerMixin(MutableWorldProperties properties,
            RegistryKey<World> registryRef, DynamicRegistryManager registryManager,
            RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler,
            boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient,
                debugWorld, biomeAccess, maxChainedNeighborUpdates);
        throw new UnsupportedOperationException();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeLumarData(MinecraftServer server, Executor workerExecutor,
            Session session, ServerWorldProperties properties, RegistryKey<World> worldKey,
            DimensionOptions dimensionOptions,
            WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld,
            long seed, List<SpecialSpawner> spawners, boolean shouldTickTime,
            RandomSequencesState randomSequencesState, CallbackInfo ci) {
        if (worldKey.equals(ModDimensions.WORLD_LUMAR)) {
            lunagreeManager = ((PersistentByteDataManagerAccess) this.getPersistentStateManager()).worldsinger$getOrCreateFromBytes(
                    LumarLunagreeManager.getPersistentByteDataType((ServerWorld) (Object) this),
                    LunagreeManager.NAME);
        } else {
            lunagreeManager = new NullLunagreeManager();
        }
    }

    @Override
    public LunagreeManager worldsinger$getLunagreeManager() {
        return lunagreeManager;
    }
}
