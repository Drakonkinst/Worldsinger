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
package io.github.drakonkinst.worldsinger.cosmere;

import io.github.drakonkinst.worldsinger.Worldsinger;
import io.github.drakonkinst.worldsinger.entity.PlayerMorphDummy;
import io.github.drakonkinst.worldsinger.entity.Shapeshifter;
import io.github.drakonkinst.worldsinger.mixin.accessor.BatEntityInvoker;
import io.github.drakonkinst.worldsinger.mixin.accessor.EntityAccessor;
import io.github.drakonkinst.worldsinger.mixin.accessor.GuardianEntityAccessor;
import io.github.drakonkinst.worldsinger.mixin.accessor.IronGolemEntityAccessor;
import io.github.drakonkinst.worldsinger.mixin.accessor.LivingEntityAccessor;
import io.github.drakonkinst.worldsinger.mixin.accessor.RavagerEntityAccessor;
import io.github.drakonkinst.worldsinger.mixin.accessor.ShulkerEntityAccessor;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public final class ShapeshiftingManager {

    public static final String EMPTY_MORPH = "minecraft:empty";
    public static final String ID_PLAYER = "minecraft:player";
    public static final Identifier SHAPESHIFTER_SYNC_PACKET_ID = Worldsinger.id(
            "shapeshifter_sync");
    public static final Identifier SHAPESHIFTER_ATTACK_PACKET_ID = Worldsinger.id(
            "shapeshifter_attack");
    private static final int ATTACK_TICKS = 10;
    private static final float GUARDIAN_TAIL_SPEED = 2.0f;
    private static final float GUARDIAN_SPIKE_SPEED = 0.06f;
    private static final float SQUID_THRUST_SPEED = 0.2f;
    private static final float SQUID_TILT_ANGLE_OFFSET = -90.0f;
    private static final float SQUID_TILT_SPEED = 0.02f;
    private static final int SHULKER_PEEK_AMOUNT = 30;
    private static final int SHULKER_PEEK_CHANCE = 20;
    private static final float SHULKER_PEEK_TO_PROGRESS = 0.01f;

    /* Networking */

    public static void syncToNearbyPlayers(ServerWorld world, Shapeshifter shapeshifter) {
        Packet<ClientCommonPacketListener> packet = ServerPlayNetworking.createS2CPacket(
                SHAPESHIFTER_SYNC_PACKET_ID, ShapeshiftingManager.createSyncPacket(shapeshifter));
        world.getChunkManager().sendToNearbyPlayers(shapeshifter.toEntity(), packet);
    }

    public static void syncToPlayer(ServerPlayerEntity playerEntity, Shapeshifter shapeshifter) {
        ServerPlayNetworking.send(playerEntity, SHAPESHIFTER_SYNC_PACKET_ID,
                ShapeshiftingManager.createSyncPacket(shapeshifter));
    }

    private static PacketByteBuf createSyncPacket(Shapeshifter shapeshifter) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        NbtCompound entityTag = new NbtCompound();

        LivingEntity morph = shapeshifter.getMorph();
        if (morph != null) {
            morph.writeNbt(entityTag);
        }

        LivingEntity shapeshifterEntity = shapeshifter.toEntity();
        buf.writeVarInt(shapeshifterEntity.getId());
        buf.writeString(morph == null ? EMPTY_MORPH
                : Registries.ENTITY_TYPE.getId(morph.getType()).toString());
        buf.writeNbt(entityTag);
        return buf;
    }

    private static void sendAttackPacket(ServerWorld world, Shapeshifter shapeshifter) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(shapeshifter.toEntity().getId());
        Packet<ClientCommonPacketListener> packet = ServerPlayNetworking.createS2CPacket(
                SHAPESHIFTER_ATTACK_PACKET_ID, buf);
        world.getChunkManager().sendToNearbyPlayers(shapeshifter.toEntity(), packet);
    }

    /* Morph Setup */

    public static void createMorphFromNbt(Shapeshifter shapeshifter, NbtCompound morphNbt,
            boolean showTransformEffects) {
        Optional<EntityType<?>> optionalType = EntityType.fromNbt(morphNbt);
        if (optionalType.isEmpty()) {
            return;
        }
        EntityType<?> type = optionalType.get();

        LivingEntity morph = shapeshifter.getMorph();
        if (morph == null || !type.equals(morph.getType())) {
            World world = shapeshifter.toEntity().getWorld();
            if (type.equals(EntityType.PLAYER)) {
                UUID playerUuid = morphNbt.getUuid(PlayerMorphDummy.KEY_PLAYER);
                String playerName = morphNbt.getString(PlayerMorphDummy.KEY_PLAYER_NAME);
                morph = Worldsinger.PROXY.createPlayerMorph(world, playerUuid, playerName);
            } else {
                morph = (LivingEntity) type.create(world);
            }
        }

        if (morph != null) {
            ShapeshiftingManager.onMorphEntitySpawn(shapeshifter, morph);
            morph.readNbt(morphNbt);
            shapeshifter.updateMorph(morph);
            shapeshifter.afterMorphEntitySpawn(morph, showTransformEffects);
        } else {
            Worldsinger.LOGGER.warn("Failed to create morph for type " + type);
        }
    }

    public static void createMorphFromEntity(Shapeshifter shapeshifter, LivingEntity toCopy,
            boolean showTransformEffects) {
        LivingEntity morph = shapeshifter.getMorph();
        if (morph == null || toCopy.getType().equals(morph.getType())) {
            World world = shapeshifter.toEntity().getWorld();
            if (toCopy instanceof PlayerEntity) {
                morph = Worldsinger.PROXY.createPlayerMorph(world, toCopy.getUuid(),
                        toCopy.getName().getString());
            } else {
                morph = (LivingEntity) toCopy.getType().create(world);
            }
        }
        if (morph != null) {
            ShapeshiftingManager.onMorphEntitySpawn(shapeshifter, morph);
            ShapeshiftingManager.copyVariantData(shapeshifter, morph, toCopy);
            shapeshifter.updateMorph(morph);
            shapeshifter.afterMorphEntitySpawn(morph, showTransformEffects);
        } else {
            Worldsinger.LOGGER.warn("Failed to create morph for type " + toCopy.getType());
        }
    }

    private static void onMorphEntitySpawn(Shapeshifter shapeshifter, LivingEntity morph) {
        morph.setInvulnerable(true);
        morph.setNoGravity(true);
        if (morph instanceof BatEntity batEntity) {
            batEntity.setRoosting(false);
        }
        shapeshifter.onMorphEntitySpawn(morph);
    }

    // Can add more variant data here
    // Assumes morph and toCopy are of the same type
    private static void copyVariantData(Shapeshifter shapeshifter, LivingEntity morph,
            LivingEntity toCopy) {
        // Baby
        if (morph instanceof MobEntity mobMorph && toCopy instanceof MobEntity mobToCopy) {
            mobMorph.setBaby(mobToCopy.isBaby());
        }

        // Slime/Magma Cube Size
        if (morph instanceof SlimeEntity slimeMorph && toCopy instanceof SlimeEntity slimeToCopy) {
            slimeMorph.setSize(slimeToCopy.getSize(), false);
        }

        // Phantom Size
        if (morph instanceof PhantomEntity phantomMorph
                && toCopy instanceof PhantomEntity phantomToCopy) {
            phantomMorph.setPhantomSize(phantomToCopy.getPhantomSize());
        }

        // Tropical Fish
        if (morph instanceof TropicalFishEntity tropicalFishMorph
                && toCopy instanceof TropicalFishEntity tropicalFishToCopy) {
            tropicalFishMorph.setVariant(tropicalFishToCopy.getVariant());
        }

        // TODO: Other entity variants (Villager, Parrot, Cat, Axolotl)

        if (shapeshifter.shouldCopyEquipmentVisuals()) {
            // Equipped items
            morph.equipStack(EquipmentSlot.MAINHAND,
                    toCopy.getEquippedStack(EquipmentSlot.MAINHAND));
            morph.equipStack(EquipmentSlot.OFFHAND, toCopy.getEquippedStack(EquipmentSlot.OFFHAND));
            morph.equipStack(EquipmentSlot.HEAD, toCopy.getEquippedStack(EquipmentSlot.HEAD));
            morph.equipStack(EquipmentSlot.CHEST, toCopy.getEquippedStack(EquipmentSlot.CHEST));
            morph.equipStack(EquipmentSlot.LEGS, toCopy.getEquippedStack(EquipmentSlot.LEGS));
            morph.equipStack(EquipmentSlot.FEET, toCopy.getEquippedStack(EquipmentSlot.FEET));
        }
    }

    /* Morph Behavior */

    // Since tryAttack() is only called from the server, it should use this code.
    // There is no packet that is sent when an entity attempts to attack another, with the
    // possible exception of EntityDamageS2CPacket. However, this packet only sends if the entity
    // actually takes damage, not if the attack is attempted, so we make our own.
    public static void onAttackServer(ServerWorld world, Shapeshifter shapeshifter) {
        ShapeshiftingManager.sendAttackPacket(world, shapeshifter);
    }

    public static void onAttackClient(Shapeshifter shapeshifter) {
        LivingEntity morph = shapeshifter.getMorph();
        if (morph instanceof WardenEntity wardenEntity) {
            wardenEntity.attackingAnimationState.start(shapeshifter.toEntity().age);
        } else if (morph instanceof IronGolemEntity) {
            ((IronGolemEntityAccessor) morph).worldsinger$setAttackTicksLeft(ATTACK_TICKS);
        } else if (morph instanceof RavagerEntity) {
            ((RavagerEntityAccessor) morph).worldsinger$setAttackTick(ATTACK_TICKS);
        }
    }

    public static void tickMorphServer(Shapeshifter shapeshifter, LivingEntity morph) {
        LivingEntity entity = shapeshifter.toEntity();
        morph.setPos(entity.getX(), entity.getY(), entity.getZ());
        morph.setHeadYaw(entity.getHeadYaw());
        morph.setJumping(((LivingEntityAccessor) entity).worldsinger$isJumping());
        morph.setSprinting(entity.isSprinting());
        morph.setStuckArrowCount(entity.getStuckArrowCount());
        morph.setSneaking(entity.isSneaking());
        morph.setSwimming(entity.isSwimming());
        morph.setCurrentHand(entity.getActiveHand());
        morph.setPose(entity.getPose());

        if (morph instanceof TameableEntity tameableEntity) {
            tameableEntity.setInSittingPose(entity.isSneaking());
            tameableEntity.setSitting(entity.isSneaking());
        }

        ((EntityAccessor) morph).worldsinger$setFlag(
                EntityAccessor.worldsinger$getFallFlyingIndex(), entity.isFallFlying());
        ((LivingEntityAccessor) morph).worldsinger$tickActiveItemStack();
    }

    public static void tickMorphClient(LivingEntity morph, Random random) {
        if (morph instanceof BatEntity batEntity) {
            ShapeshiftingManager.tickBatMorphClient(batEntity);
        } else if (morph instanceof SquidEntity squidEntity) {
            ShapeshiftingManager.tickSquidMorphClient(squidEntity, random);
        } else if (morph instanceof GuardianEntity guardianEntity) {
            ShapeshiftingManager.tickGuardianMorphClient(guardianEntity);
        } else if (morph instanceof ShulkerEntity shulkerEntity) {
            ShapeshiftingManager.tickShulkerMorphClient(shulkerEntity, random);
        } else if (morph instanceof IronGolemEntity ironGolemEntity) {
            ShapeshiftingManager.tickIronGolemMorphClient(ironGolemEntity);
        } else if (morph instanceof RavagerEntity ravagerEntity) {
            ShapeshiftingManager.tickRavagerMorphClient(ravagerEntity);
        }
    }

    private static void tickBatMorphClient(BatEntity batEntity) {
        ((BatEntityInvoker) batEntity).worldsinger$updateAnimations();
    }

    // Why is getting the squid to work so much work? I have no idea
    // Simulates squid movement for the client
    private static void tickSquidMorphClient(SquidEntity squidEntity, Random random) {
        squidEntity.prevTiltAngle = squidEntity.tiltAngle;
        squidEntity.prevTentacleAngle = squidEntity.tentacleAngle;
        squidEntity.thrustTimer += 1.0f / (random.nextFloat() + 1.0f) * SQUID_THRUST_SPEED;
        if (squidEntity.thrustTimer > 2.0f * MathHelper.PI) {
            squidEntity.thrustTimer = 0.0f;
        }
        squidEntity.tentacleAngle =
                MathHelper.abs(MathHelper.sin(squidEntity.thrustTimer)) * MathHelper.PI * 0.25f;
        squidEntity.tiltAngle +=
                (SQUID_TILT_ANGLE_OFFSET - squidEntity.tiltAngle) * SQUID_TILT_SPEED;
    }

    private static void tickGuardianMorphClient(GuardianEntity guardianEntity) {
        // Always extend spikes
        GuardianEntityAccessor accessor = (GuardianEntityAccessor) guardianEntity;
        float spikesExtension = accessor.worldsinger$getSpikesExtension();
        accessor.worldsinger$setPrevSpikesExtension(spikesExtension);
        accessor.worldsinger$setSpikesExtension(
                spikesExtension + (1.0f - spikesExtension) * GUARDIAN_SPIKE_SPEED);

        // Wag tail at the same speed as on land
        float tailAngle = accessor.worldsinger$getTailAngle();
        accessor.worldsinger$setPrevTailAngle(tailAngle);
        accessor.worldsinger$setTailAngle(tailAngle + GUARDIAN_TAIL_SPEED);
    }

    private static void tickShulkerMorphClient(ShulkerEntity shulkerEntity, Random random) {
        ShulkerEntityAccessor accessor = (ShulkerEntityAccessor) shulkerEntity;
        accessor.worldsinger$tickOpenProgress();
        int peekAmount = accessor.worldsinger$getPeekAmount();
        if (accessor.worldsinger$getOpenProgress() == peekAmount * SHULKER_PEEK_TO_PROGRESS
                && random.nextInt(SHULKER_PEEK_CHANCE) == 0) {
            if (peekAmount == SHULKER_PEEK_AMOUNT) {
                accessor.worldsinger$setPeekAmount(0);
            } else {
                accessor.worldsinger$setPeekAmount(SHULKER_PEEK_AMOUNT);
            }
        }
    }

    private static void tickIronGolemMorphClient(IronGolemEntity ironGolemEntity) {
        IronGolemEntityAccessor accessor = (IronGolemEntityAccessor) ironGolemEntity;
        int attackTicks = accessor.worldsinger$getAttackTicksLeft();
        if (attackTicks > 0) {
            accessor.worldsinger$setAttackTicksLeft(attackTicks - 1);
        }
    }

    private static void tickRavagerMorphClient(RavagerEntity ravagerEntity) {
        RavagerEntityAccessor accessor = (RavagerEntityAccessor) ravagerEntity;
        int attackTicks = accessor.worldsinger$getAttackTick();
        if (attackTicks > 0) {
            accessor.worldsinger$setAttackTick(attackTicks - 1);
        }
    }

    private ShapeshiftingManager() {}
}
