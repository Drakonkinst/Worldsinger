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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarLunagreeGenerator;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarRainlineManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarSeetheManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeGenerator;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeLocation;
import io.github.drakonkinst.worldsinger.cosmere.lumar.RainlineManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
import io.github.drakonkinst.worldsinger.network.packet.SeetheUpdatePayload;
import java.util.List;
import java.util.concurrent.Executor;
import net.minecraft.block.Block;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
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
public abstract class ServerWorldLumarMixin extends WorldLumarMixin implements
        StructureWorldAccess {

    @Shadow
    public abstract PersistentStateManager getPersistentStateManager();

    @Unique
    private boolean syncedSeething;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeLumarData(MinecraftServer server, Executor workerExecutor,
            Session session, ServerWorldProperties properties, RegistryKey<World> worldKey,
            DimensionOptions dimensionOptions,
            WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld,
            long seed, List<SpecialSpawner> spawners, boolean shouldTickTime,
            RandomSequencesState randomSequencesState, CallbackInfo ci) {
        if (CosmerePlanet.getPlanetFromKey(worldKey).equals(CosmerePlanet.LUMAR)) {
            SeetheManager seetheManager = this.getPersistentStateManager()
                    .getOrCreate(LumarSeetheManager.getPersistentStateType(),
                            LumarSeetheManager.NAME);
            LunagreeGenerator lunagreeGenerator = this.getPersistentStateManager()
                    .getOrCreate(LumarLunagreeGenerator.getPersistentStateType(
                            (ServerWorld) (Object) (this)), LumarLunagreeGenerator.NAME);
            RainlineManager rainlineManager = this.getPersistentStateManager()
                    .getOrCreate(LumarRainlineManager.getPersistentStateType(lunagreeGenerator),
                            LumarRainlineManager.NAME);
            lumarManager = new LumarManager(seetheManager, lunagreeGenerator, rainlineManager);
        }
    }

    @Inject(method = "tickIceAndSnow", at = @At("RETURN"))
    private void rainSporeBlocksUnderSporeFall(BlockPos xzPos, CallbackInfo ci,
            @Local(ordinal = 1) BlockPos pos, @Local(ordinal = 2) BlockPos belowPos) {
        if (!CosmerePlanet.isLumar((ServerWorld) (Object) this)) {
            return;
        }
        int x = pos.getX();
        int z = pos.getZ();
        if (!canPlaceSporeBlock(pos, belowPos)) {
            return;
        }
        LunagreeLocation nearestLocation = lumarManager.getLunagreeGenerator()
                .getNearestLunagree(x, z, LumarLunagreeGenerator.SPORE_FALL_RADIUS);
        if (nearestLocation == null) {
            return;
        }

        AetherSpores sporeType = AetherSpores.getAetherSporeTypeById(nearestLocation.sporeId());
        if (sporeType == null) {
            return;
        }
        Block blockToPlace = sporeType.getSolidBlock();
        // Prevent infinite stacking
        if (this.getBlockState(belowPos).isOf(blockToPlace)) {
            return;
        }

        this.setBlockState(pos, blockToPlace.getDefaultState());
    }

    @Inject(method = "tickWeather", at = @At("TAIL"))
    private void tickSeethe(CallbackInfo ci) {
        if (CosmerePlanet.isLumar((ServerWorld) (Object) this)) {
            SeetheManager seetheManager = lumarManager.getSeetheManager();
            seetheManager.serverTickWeather();

            // Sync seething
            // For now, we only care about whether the seethe state is changed
            boolean isSeething = seetheManager.isSeething();
            if (isSeething != syncedSeething) {
                syncedSeething = isSeething;
                CustomPayload payload = isSeething ? SeetheUpdatePayload.SEETHE_START
                        : SeetheUpdatePayload.SEETHE_STOP;
                this.getServer()
                        .getPlayerManager()
                        .sendToDimension(new CustomPayloadS2CPacket(payload),
                                this.getRegistryKey());
            }
        }
    }

    @ModifyExpressionValue(method = "tickWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean disableRainTickOnLumar(boolean original) {
        return original && !CosmerePlanet.isLumar((ServerWorld) (Object) this);
    }

    @Unique
    private boolean canPlaceSporeBlock(BlockPos pos, BlockPos belowPos) {
        return pos.getY() >= this.getBottomY() && pos.getY() < this.getTopY() && this.isSkyVisible(
                pos) && this.getBlockState(pos).isAir() && Block.isFaceFullSquare(
                this.getBlockState(belowPos).getCollisionShape(this, belowPos), Direction.UP);
    }
}
