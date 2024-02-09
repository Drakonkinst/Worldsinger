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
package io.github.drakonkinst.worldsinger.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.block.ModBlockTags;
import io.github.drakonkinst.worldsinger.block.SporeKillable;
import io.github.drakonkinst.worldsinger.cosmere.SilverLined;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SeetheManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeKillingUtil;
import io.github.drakonkinst.worldsinger.cosmere.lumar.SporeParticleSpawner;
import io.github.drakonkinst.worldsinger.fluid.AetherSporeFluid;
import io.github.drakonkinst.worldsinger.fluid.ModFluidTags;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import io.github.drakonkinst.worldsinger.util.BlockPosUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.BoatEntity.Location;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnstableApiUsage")
@Mixin(BoatEntity.class)
public abstract class BoatEntityMovementMixin extends VehicleEntity {

    @Unique
    private static final double MAX_FLUID_HEIGHT_TO_NOT_EMBED = 0.05;

    @Unique
    private static final int UNDER_SPORES_SILVER_PENALTY_TICK = 10;
    @Unique
    private final boolean[] firstPaddle = { true, true };
    @Unique
    private boolean inSporeSea;
    @Unique
    private AetherSporeFluid lastAetherSporeFluid = null;
    @Shadow
    private double waterLevel;
    @Shadow
    private float velocityDecay;
    @Shadow
    private Location location;
    @Shadow
    @Final
    private float[] paddlePhases;

    public BoatEntityMovementMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract boolean isPaddleMoving(int paddle);

    @Inject(method = "tick", at = @At("TAIL"))
    private void injectTick(CallbackInfo ci) {
        addParticlesToRowing();
        killSporeSeaBlocks();
    }

    @Unique
    private void addParticlesToRowing() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld) || lastAetherSporeFluid == null) {
            return;
        }

        // Do not spawn particles on the first paddle of either oar
        for (int paddleIndex = 0; paddleIndex <= 1; ++paddleIndex) {
            if (this.isPaddleMoving(paddleIndex)) {
                checkRowingParticle(serverWorld, paddleIndex);
            } else {
                firstPaddle[paddleIndex] = true;
            }
        }
    }

    @Unique
    private void killSporeSeaBlocks() {
        if (!this.inSporeSea) {
            return;
        }
        SilverLined silverData = this.getAttachedOrCreate(ModAttachmentTypes.SILVER_LINED_BOAT);
        int silverDurability = silverData.getSilverDurability();
        if (silverDurability <= 0) {
            return;
        }

        World world = this.getWorld();
        int sporesKilled = 0;
        for (BlockPos pos : BlockPosUtil.iterateBoundingBoxForEntity(this, this.getBlockPos())) {
            BlockState state = world.getBlockState(pos);
            if (state.isIn(ModBlockTags.AETHER_SPORE_SEA_BLOCKS)
                    && state.getBlock() instanceof SporeKillable sporeKillable
                    && sporeKillable.isSporeKillable(world, pos, state)) {
                BlockState newBlockState = SporeKillingUtil.convertToDeadVariant(sporeKillable,
                        state);
                if (world.setBlockState(pos, newBlockState)) {
                    sporesKilled += 1;
                }
            }
        }

        int silverDamage =
                (this.location == Location.UNDER_FLOWING_WATER ? UNDER_SPORES_SILVER_PENALTY_TICK
                        : 0) + sporesKilled;
        if (silverDamage > 0) {
            silverData.setSilverDurability(silverDurability - silverDamage);
        }
    }

    @Unique
    private void checkRowingParticle(ServerWorld world, int paddleIndex) {
        if (isAtRowingApex(paddleIndex)) {
            if (firstPaddle[paddleIndex]) {
                firstPaddle[paddleIndex] = false;
                return;
            }
            if (this.inSporeSea) {
                Vec3d vec3d = this.getRotationVec(1.0f);
                double xOffset = paddleIndex == 1 ? -vec3d.z : vec3d.z;
                double zOffset = paddleIndex == 1 ? vec3d.x : -vec3d.x;
                Vec3d pos = new Vec3d(this.getX() + xOffset, this.getY(), this.getZ() + zOffset);
                SporeParticleSpawner.spawnRowingParticles(world,
                        lastAetherSporeFluid.getSporeType(), pos);
            }
        }
    }

    @Unique
    private boolean isAtRowingApex(int paddleIndex) {
        float paddlePhase = this.paddlePhases[paddleIndex];
        return paddlePhase % (Math.PI * 2) <= Math.PI / 4
                && (paddlePhase + (Math.PI / 8)) % (Math.PI * 2) >= Math.PI / 4;
    }

    @Inject(method = "getPaddleSoundEvent", at = @At("HEAD"), cancellable = true)
    private void addSporeSeaPaddleSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (this.inSporeSea) {
            cir.setReturnValue(ModSoundEvents.ENTITY_BOAT_PADDLE_SPORE_SEA);
        }
    }

    @Inject(method = "checkLocation", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;getNearbySlipperiness()F"), cancellable = true)
    private void checkSporeSeaLocation(CallbackInfoReturnable<Location> cir) {
        this.inSporeSea = false;
        this.lastAetherSporeFluid = null;
        Location location = this.getUnderSporeSeaLocation();
        if (location != null) {
            this.waterLevel = this.getBoundingBox().maxY;
            this.inSporeSea = true;
            cir.setReturnValue(location);
            return;
        }

        if (this.checkBoatInSporeSea()) {
            this.inSporeSea = true;
            double fluidHeight = this.getFluidHeight(ModFluidTags.AETHER_SPORES);
            World world = this.getWorld();
            if (!SeetheManager.areSporesFluidized(world)
                    && fluidHeight <= MAX_FLUID_HEIGHT_TO_NOT_EMBED) {
                cir.setReturnValue(Location.ON_LAND);
            } else {
                cir.setReturnValue(Location.IN_WATER);
            }
        }
    }

    @Unique
    private Location getUnderSporeSeaLocation() {
        Box box = this.getBoundingBox();
        double d = box.maxY + 0.001;
        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.ceil(box.maxX);
        int minY = MathHelper.floor(box.maxY);
        int maxY = MathHelper.ceil(d);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.ceil(box.maxZ);
        boolean bl = false;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    mutable.set(x, y, z);
                    FluidState fluidState = this.getWorld().getFluidState(mutable);
                    if (!fluidState.isIn(ModFluidTags.AETHER_SPORES) || !(d < (double) (
                            (float) mutable.getY() + fluidState.getHeight(this.getWorld(),
                                    mutable)))) {
                        continue;
                    }
                    if (fluidState.isStill()) {
                        bl = true;
                        continue;
                    }
                    return Location.UNDER_FLOWING_WATER;
                }
            }
        }
        return bl ? Location.UNDER_WATER : null;
    }

    @Unique
    private boolean checkBoatInSporeSea() {
        Box box = this.getBoundingBox();
        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.ceil(box.maxX);
        int minY = MathHelper.floor(box.minY);
        int maxY = MathHelper.ceil(box.minY + 0.001);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.ceil(box.maxZ);
        boolean inSporeSea = false;
        this.waterLevel = -Double.MAX_VALUE;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = minZ; z < maxZ; ++z) {
                    mutable.set(x, y, z);
                    FluidState fluidState = this.getWorld().getFluidState(mutable);
                    if (!fluidState.isIn(ModFluidTags.AETHER_SPORES)) {
                        continue;
                    }
                    if (fluidState.getFluid() instanceof AetherSporeFluid aetherSporeFluid) {
                        if (this.lastAetherSporeFluid != null
                                && !this.lastAetherSporeFluid.getSporeType().isDead()
                                && aetherSporeFluid.getSporeType().isDead()) {
                            // Do not allow dead spores to override living spores
                        } else {
                            this.lastAetherSporeFluid = aetherSporeFluid;
                        }
                    }
                    float f = (float) y + fluidState.getHeight(this.getWorld(), mutable);
                    this.waterLevel = Math.max(f, this.waterLevel);
                    inSporeSea |= box.minY < (double) f;
                }
            }
        }
        return inSporeSea;
    }

    @Inject(method = "updateVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;setVelocity(DDD)V"), slice = @Slice(to = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/entity/vehicle/BoatEntity;yawVelocity:F")))
    private void addSporeSeaVelocityLogic(CallbackInfo ci) {
        if (this.inSporeSea && !SeetheManager.areSporesFluidized(this.getWorld())) {
            this.velocityDecay = 0.0f;
        }
    }

    @WrapOperation(method = "updatePaddles", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/vehicle/BoatEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private void restrictMovementInSporeSea(BoatEntity instance, Vec3d velocity,
            Operation<Void> original) {
        if (this.inSporeSea && this.location != Location.ON_LAND
                && !SeetheManager.areSporesFluidized(this.getWorld())) {
            return;
        }
        original.call(instance, velocity);
    }

    @Inject(method = "updatePassengerPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setYaw(F)V"), cancellable = true)
    private void restrictMovementInSporeSeaPassenger(CallbackInfo ci) {
        if (this.inSporeSea && this.location != Location.ON_LAND) {
            World world = this.getWorld();
            if (!SeetheManager.areSporesFluidized(world)) {
                ci.cancel();
            }
        }
    }

    // Switches to the entity-based collision shape, which can use the entity world object
    // to check fluidization and see the spore sea block as solid
    @Redirect(method = "getNearbySlipperiness", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;"))
    private VoxelShape checkFluidizedBlock(BlockState instance, BlockView blockView,
            BlockPos blockPos) {
        return instance.getCollisionShape(blockView, blockPos, ShapeContext.of(this));
    }

}
