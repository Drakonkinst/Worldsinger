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
package io.github.drakonkinst.worldsinger.mixin.entity.player;

import com.mojang.authlib.GameProfile;
import io.github.drakonkinst.worldsinger.cosmere.CosmerePlanet;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LumarManagerAccess;
import io.github.drakonkinst.worldsinger.cosmere.lumar.LunagreeGenerator;
import io.github.drakonkinst.worldsinger.world.LunagreeDataReceiver;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityLunagreeDataMixin extends PlayerEntity implements
        LunagreeDataReceiver {

    @Unique
    private static final int UPDATE_DELAY = 20;

    @Unique
    private boolean shouldCheckPosition = true;
    @Unique
    private long nextUpdateTick = 0;
    @Unique
    private long currentCellKey = Long.MAX_VALUE;

    public ServerPlayerEntityLunagreeDataMixin(World world, BlockPos pos, float yaw,
            GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "playerTick", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/server/network/ServerPlayerEntity;age:I"))
    private void checkUpdateLunagreeData(CallbackInfo ci) {
        ServerWorld world = this.getServerWorld();
        if (!CosmerePlanet.isLumar(world)) {
            return;
        }
        if (shouldCheckPosition && world.getTime() > nextUpdateTick) {
            LunagreeGenerator manager = ((LumarManagerAccess) world).worldsinger$getLumarManager()
                    .getLunagreeGenerator();
            if (!manager.isNull()) {
                long key = manager.getKeyForPos(this.getBlockX(), this.getBlockZ());
                if (key != currentCellKey) {
                    manager.updateLunagreeDataForPlayer((ServerPlayerEntity) (Object) this);
                    currentCellKey = key;
                }
            }
            shouldCheckPosition = false;
            nextUpdateTick = world.getTime() + UPDATE_DELAY;
        }
    }

    @Override
    public void worldsinger$setShouldCheckPosition() {
        shouldCheckPosition = true;
    }

    @Shadow
    public abstract ServerWorld getServerWorld();
}
