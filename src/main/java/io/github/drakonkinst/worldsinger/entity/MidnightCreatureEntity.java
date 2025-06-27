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
import io.github.drakonkinst.worldsinger.api.ModAttachmentTypes;
import io.github.drakonkinst.worldsinger.block.ModBlocks;
import io.github.drakonkinst.worldsinger.cosmere.PossessionManager;
import io.github.drakonkinst.worldsinger.cosmere.ThirstManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.AetherSpores;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightAetherBondManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightCreatureManager;
import io.github.drakonkinst.worldsinger.cosmere.lumar.MidnightSpores;
import io.github.drakonkinst.worldsinger.effect.ModStatusEffects;
import io.github.drakonkinst.worldsinger.entity.ai.PossessableEntityNavigation;
import io.github.drakonkinst.worldsinger.entity.ai.PossessableMoveControl;
import io.github.drakonkinst.worldsinger.entity.ai.behavior.MidnightCreatureImitate;
import io.github.drakonkinst.worldsinger.entity.ai.behavior.OptionalAttackTarget;
import io.github.drakonkinst.worldsinger.entity.ai.behavior.StudyTarget;
import io.github.drakonkinst.worldsinger.entity.ai.sensor.ConditionalNearbyBlocksSensor;
import io.github.drakonkinst.worldsinger.entity.ai.sensor.NearestAttackableSensor;
import io.github.drakonkinst.worldsinger.entity.data.MidnightOverlayAccess;
import io.github.drakonkinst.worldsinger.entity.data.PlayerPossessionManager;
import io.github.drakonkinst.worldsinger.mixin.accessor.EntityAccessor;
import io.github.drakonkinst.worldsinger.particle.ModParticleTypes;
import io.github.drakonkinst.worldsinger.registry.ModDamageTypes;
import io.github.drakonkinst.worldsinger.registry.ModSoundEvents;
import io.github.drakonkinst.worldsinger.registry.tag.ModBlockTags;
import io.github.drakonkinst.worldsinger.registry.tag.ModItemTags;
import io.github.drakonkinst.worldsinger.util.EntityUtil;
import io.github.drakonkinst.worldsinger.util.ModEnums.PathNodeType;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.SchoolingFishEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRetaliateTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.util.BrainUtil;
import net.tslat.smartbrainlib.util.SensoryUtil;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class MidnightCreatureEntity extends ShapeshiftingEntity implements
        SmartBrainOwner<MidnightCreatureEntity>, Controllable, Monster, SilverVulnerable,
        CameraPossessable {

    // NBT
    public static final String MIDNIGHT_ESSENCE_AMOUNT_KEY = "MidnightEssenceAmount";
    public static final String CONTROLLER_KEY = "Controller";
    public static final String BRIBES_KEY = "Bribes";
    public static final String BRIBE_KEY = "Bribe";

    // Bribes
    public static final int WATER_BUCKET_BRIBE = 3;
    public static final int INITIAL_BRIBE = 2;
    public static final int POTION_BRIBE = 1;
    public static final float DAMAGE_FROM_SILVER = 4.0f;

    // Tracked Data
    private static final TrackedData<Optional<LazyEntityReference<LivingEntity>>> CONTROLLER_UUID = DataTracker.registerData(
            MidnightCreatureEntity.class, TrackedDataHandlerRegistry.LAZY_ENTITY_REFERENCE);
    private static final String MORPHED_NAME_TRANSLATION_KEY = Util.createTranslationKey("entity",
            Worldsinger.id("midnight_creature.morphed"));

    // Behavior
    private static final float POSSESS_THIRST_DAMAGE = 1.0f;
    private static final int ANGER_TIME = 20 * 30;
    private static final float SPRINTING_MULTIPLIER = 1.4f;
    private static final Set<RegistryEntry<StatusEffect>> IMMUNE_TO = Set.of(StatusEffects.WITHER,
            StatusEffects.POISON, StatusEffects.HUNGER, ModStatusEffects.CRIMSON_SPORES,
            ModStatusEffects.MIDNIGHT_SPORES, ModStatusEffects.ROSEITE_SPORES,
            ModStatusEffects.SUNLIGHT_SPORES, ModStatusEffects.VERDANT_SPORES,
            ModStatusEffects.ZEPHYR_SPORES);
    private static final int MAX_POSSESSION_EXPIRY = 20;

    // Particles
    private static final int AMBIENT_PARTICLE_INTERVAL = 10;
    private static final int NUM_DAMAGE_PARTICLES = 16;
    private static final int TRAIL_INTERVAL = 3;
    private static final int NUM_TRANSFORM_PARTICLES = 32;
    private static final int NUM_TRAIL_PARTICLES = 16;
    private static final float TRAIL_PARTICLE_SPEED = 0.1f;
    private static final float MOUTH_OFFSET = -0.2f;
    private static final int DISPEL_PARTICLES_PER_BLOCK = 10;
    private static final float DISPEL_PARTICLE_VELOCITY = 0.01f;

    private final Object2IntMap<UUID> waterBribes = new Object2IntOpenHashMap<>();
    private int midnightEssenceAmount = 0;
    private int drainIntervalTicks = 0;

    private int maxDrainInterval = MidnightCreatureManager.MAX_DRAIN_INTERVAL_TICKS;
    private int minBribe = 1;
    @Nullable
    private PlayerEntity controller;

    private boolean isBeingPossessed = false;
    private int possessionTimeout = 0;

    public MidnightCreatureEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 5;

        this.moveControl = new PossessableMoveControl<>(this, SPRINTING_MULTIPLIER);

        // Allow it to swim
        this.getNavigation().setCanSwim(true);

        // Set to same penalty as water
        this.setPathfindingPenalty(PathNodeType.AETHER_SPORE_SEA, 8.0F);
        this.setPathfindingPenalty(PathNodeType.DANGER_SILVER, 8.0F);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_SILVER, -1.0F);
    }

    public MidnightCreatureEntity(World world) {
        this(ModEntityTypes.MIDNIGHT_CREATURE, world);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new PossessableEntityNavigation<>(this, world);
    }

    // Data Tracker
    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CONTROLLER_UUID, Optional.empty());
    }

    public LazyEntityReference<LivingEntity> getControllerReference() {
        return this.dataTracker.get(CONTROLLER_UUID).orElse(null);
    }

    @Override
    public void setControllerUuid(UUID uuid) {
        if (this.getWorld().isClient()) {
            return;
        }

        if (uuid == null) {
            this.dataTracker.set(CONTROLLER_UUID, Optional.empty());
        } else {
            if (!Objects.equals(getControllerUuid(), uuid)) {
                onStartControlling();
            }
            this.dataTracker.set(CONTROLLER_UUID, Optional.of(new LazyEntityReference<>(uuid)));
        }
    }

    @Override
    public PlayerEntity getController() {
        UUID controllerUUID = getControllerUuid();
        if (controller == null && controllerUUID != null) {
            controller = this.getWorld().getPlayerByUuid(controllerUUID);
        }
        if (controller != null && controller.isRemoved()) {
            controller = null;
        }
        if (controller != null && (!controller.getUuid().equals(controllerUUID))) {
            controller = null;
        }
        return controller;
    }

    @Override
    @Nullable
    public UUID getControllerUuid() {
        LazyEntityReference<LivingEntity> reference = getControllerReference();
        if (reference == null) {
            return null;
        }
        return reference.getUuid();
    }

    // AI

    @Override
    protected Brain.Profile<?> createBrainProfile() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void mobTick(ServerWorld world) {
        // Drain water
        UUID controllerUuid = getControllerUuid();
        PlayerEntity controller = getController();
        if (controllerUuid != null) {
            ++drainIntervalTicks;
            if (drainIntervalTicks >= maxDrainInterval) {
                if (controller == null) {
                    resetController();
                } else {
                    drainWaterFromHost(controller, false);
                }
                drainIntervalTicks = 0;
            }
        }

        BlockState standingBlock = this.getSteppingBlockState();
        if (standingBlock.isIn(ModBlockTags.HAS_SILVER)) {
            this.damage(world, this.getDamageSources().magic(), DAMAGE_FROM_SILVER);
        }

        if (!isBeingPossessed) {
            // Brain logic
            this.tickBrain(this);
        }
    }

    @Override
    public boolean isBeingPossessed() {
        return isBeingPossessed;
    }

    @Override
    public List<? extends ExtendedSensor<? extends MidnightCreatureEntity>> getSensors() {
        return ObjectArrayList.of(
                // Keep track of all nearby entities (used to pick a transform target)
                new NearbyLivingEntitySensor<>(),
                // Filter NEAREST_ATTACKABLE from tracked nearby entities
                new NearestAttackableSensor<MidnightCreatureEntity>().setPredicate(
                        (target, entity) -> {
                            if (!AetherSpores.sporesCanAffect(target)) {
                                return false;
                            }
                            if (target.getUuid().equals(entity.getControllerUuid())) {
                                return false;
                            }
                            return SensoryUtil.isEntityAttackable(entity, target);
                        }),
                // Track who hurt the mob to retaliate
                new HurtBySensor<>(),
                // Track nearby
                new ConditionalNearbyBlocksSensor<MidnightCreatureEntity>().shouldRun(
                                entity -> entity.getMorph() == null)
                        .setRadius(4.0)
                        .setPredicate((blockState, entity) -> blockState.isOf(
                                ModBlocks.MIDNIGHT_ESSENCE)));
    }

    @Override
    public BrainActivityGroup<? extends MidnightCreatureEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(new FloatToSurfaceOfFluid<>(), new LookAtTarget<>(),
                new MidnightCreatureImitate<>(),
                new FollowEntity<MidnightCreatureEntity, LivingEntity>().following(
                                MidnightCreatureEntity::getController)
                        // Does not have as strict of a follow distance.
                        .stopFollowingWithin(8.0f).speedMod(SPRINTING_MULTIPLIER),
                new MoveToWalkTarget<>());
    }

    @SuppressWarnings("unchecked")
    @Override
    public BrainActivityGroup<? extends MidnightCreatureEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                // Set attack target
                new FirstApplicableBehaviour<>(
                        // Target the controller's attacker or target if controlled
                        new OptionalAttackTarget<MidnightCreatureEntity>(false).targetFinder(
                                entity -> {
                                    PlayerEntity controller = entity.getController();
                                    if (controller == null) {
                                        return null;
                                    }
                                    LivingEntity attacker = controller.getAttacker();
                                    if (attacker != null && canAttackWithController(attacker,
                                            controller)) {
                                        return attacker;
                                    }
                                    LivingEntity attacking = controller.getAttacking();
                                    if (attacking != null && canAttackWithController(attacking,
                                            controller)) {
                                        return attacking;
                                    }
                                    return null;
                                }).attackPredicate(entity -> entity.getControllerUuid() != null),
                        // Retaliate against attackers
                        new SetRetaliateTarget<MidnightCreatureEntity>().alertAlliesWhen(
                                (owner, attacker) -> true).isAllyIf((owner, ally) -> {
                            if (!(ally instanceof MidnightCreatureEntity midnightAlly)) {
                                return false;
                            }

                            // Not an ally if controlled by another player
                            if (midnightAlly.getControllerUuid() != null && !Objects.equals(
                                    midnightAlly.getControllerUuid(), owner.getControllerUuid())) {
                                return false;
                            }

                            // Not an ally if already targeting
                            Entity lastHurtBy = BrainUtil.getMemory(ally,
                                    MemoryModuleType.HURT_BY_ENTITY);
                            return !ally.isTeammate(lastHurtBy);
                        }),
                        // Attack NEAREST_ATTACKABLE if uncontrolled
                        new SetAttackTarget<MidnightCreatureEntity>().attackPredicate(
                                entity -> entity.getControllerUuid() == null),
                        // Look at a player
                        new SetPlayerLookTarget<MidnightCreatureEntity>(),
                        // Look somewhere random
                        new SetRandomLookTarget<MidnightCreatureEntity>()).startCondition(
                        entity -> !entity.isBeingPossessed()),
                new OneRandomBehaviour<MidnightCreatureEntity>(new SetRandomWalkTarget<>(),
                        new Idle<>().runFor(
                                entity -> entity.getRandom().nextBetween(30, 60))).startCondition(
                        entity -> !entity.isBeingPossessed()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public BrainActivityGroup<? extends MidnightCreatureEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                // Invalidate target if they become the mob's controller or become untargetable
                new InvalidateAttackTarget<MidnightCreatureEntity>().invalidateIf(
                        (entity, target) -> {
                            if (entity.isBeingPossessed()) {
                                return true;
                            }
                            if (target instanceof PlayerEntity player) {
                                if (player.isCreative() || player.isSpectator()) {
                                    return true;
                                }
                                return player.getUuid().equals(entity.getControllerUuid());
                            }
                            return false;
                        }),
                // Sprint towards target
                new SetWalkTargetToAttackTarget<>().speedMod(
                        (owner, target) -> SPRINTING_MULTIPLIER),
                // Begin attack
                new FirstApplicableBehaviour<>(
                        // If not aggro-ed and target is holding water, study them
                        new StudyTarget<MidnightCreatureEntity>(100).canStudy((entity, target) -> {
                                    if (entity.getControllerUuid() != null) {
                                        return false;
                                    }
                                    return target.getMainHandStack()
                                            .isIn(ModItemTags.TEMPTS_MIDNIGHT_CREATURES)
                                            || target.getOffHandStack()
                                            .isIn(ModItemTags.TEMPTS_MIDNIGHT_CREATURES);
                                })
                                .whenStopping(entity -> BrainUtil.setForgettableMemory(entity,
                                        MemoryModuleType.UNIVERSAL_ANGER, true, ANGER_TIME)),
                        // Start attacking
                        new AnimatableMeleeAttack<MidnightCreatureEntity>(0)));
    }

    private boolean canAttackWithController(LivingEntity target, LivingEntity controller) {
        // Stop hitting yourself and never hit the controller
        if (this.equals(target) || controller.equals(target)) {
            return false;
        }

        // Like other mobs, never attack Creeper
        // Don't bother attacking Ghast since it flies
        if (target instanceof CreeperEntity || target instanceof GhastEntity) {
            return false;
        }

        // Don't hit animals tamed by the controller
        if (target instanceof TameableEntity tamedEntity) {
            return !tamedEntity.isTamed() || !controller.equals(tamedEntity.getOwner());
        }

        // Don't hit other mobs controlled by the controller
        if (target instanceof Controllable controllable) {
            PlayerEntity otherController = controllable.getController();
            return otherController == null || !otherController.equals(controller);
        }

        // Don't hit players on the same team
        if (target instanceof PlayerEntity playerTarget
                && controller instanceof PlayerEntity playerController
                && !playerController.shouldDamagePlayer(playerTarget)) {
            return false;
        }

        return true;
    }

    // Tick
    @Override
    public void tick() {
        super.tick();

        // Only PlayerEntity and HostileEntity tick hand swing by default, so add it here too
        this.tickHandSwing();

        World world = this.getWorld();

        if (world.isClient() && !this.firstUpdate) {
            tickParticleEffects();
        }

        // Possession expiry timer
        if (possessionTimeout > 0) {
            --possessionTimeout;
        }
        if (possessionTimeout <= 0) {
            isBeingPossessed = false;
        }
    }

    private void tickParticleEffects() {
        // Ambient particles
        if (this.age % AMBIENT_PARTICLE_INTERVAL == 0 && random.nextInt(3) != 0) {
            MidnightSpores.addMidnightParticle(this.getWorld(), this.getBoundingBox(), random, 0.1);
        }

        // Update client controller
        PlayerEntity controller = getController();
        if (controller != null) {
            Vec3d start = controller.getEyePos().add(0.0, MOUTH_OFFSET, 0.0);
            Vec3d destination = EntityUtil.getCenterPos(this);
            Vec3d direction = destination.subtract(start).normalize();
            for (int i = this.age % TRAIL_INTERVAL; i < NUM_TRAIL_PARTICLES; i += TRAIL_INTERVAL) {
                addTrailParticle(start, destination, i, direction);
            }
        }
    }

    private void addTrailParticle(Vec3d start, Vec3d destination, int offset, Vec3d direction) {
        double delta = (double) offset / NUM_TRAIL_PARTICLES;
        Vec3d pos = start.lerp(destination, delta);
        this.getWorld()
                .addParticleClient(ModParticleTypes.MIDNIGHT_TRAIL, pos.getX(), pos.getY(),
                        pos.getZ(), direction.getX() * TRAIL_PARTICLE_SPEED,
                        direction.getY() * TRAIL_PARTICLE_SPEED,
                        direction.getZ() * TRAIL_PARTICLE_SPEED);
    }

    // Luhel Bond

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // Only possess if BOTH hands are empty. This helps avoid issues like dispelling the
        // creature and possessing it at the same time which can occur in singleplayer worlds.
        if (!player.shouldCancelInteraction() && player.getStackInHand(Hand.MAIN_HAND).isEmpty()
                && player.getStackInHand(Hand.OFF_HAND).isEmpty() && getMorph() != null
                && player.getUuid().equals(this.getControllerUuid())) {
            // Lots of weird interactions happens when riding something, so don't do that
            if (player.hasVehicle()) {
                player.stopRiding();
            }
            PossessionManager possessionManager = player.getAttachedOrCreate(
                    ModAttachmentTypes.POSSESSION, () -> PlayerPossessionManager.create(player));
            possessionManager.setPossessionTarget(this);
            return ActionResult.SUCCESS;
        }

        int waterAmount = MidnightCreatureManager.getWaterAmountPerUnit(stack);
        if (waterAmount <= 0) {
            return super.interactMob(player, hand);
        }

        ItemStack remainderStack = MidnightCreatureManager.getStackAfterDraining(stack);
        player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, remainderStack));
        acceptWaterBribe(player, waterAmount);
        this.getWorld()
                .playSound(player, this.getX(), this.getY(), this.getZ(),
                        ModSoundEvents.ENTITY_MIDNIGHT_CREATURE_DRINK, this.getSoundCategory(),
                        1.0f, 1.0f);
        return ActionResult.SUCCESS;
    }

    private void drainWaterFromHost(PlayerEntity host, boolean isInitial) {
        ThirstManager thirstManager = host.getAttachedOrCreate(ModAttachmentTypes.THIRST);
        int currentWaterLevel = thirstManager.get();
        MidnightAetherBondManager bondData = host.getAttachedOrCreate(
                ModAttachmentTypes.MIDNIGHT_AETHER_BOND);
        if (currentWaterLevel <= 0) {
            PossessionManager possessionManager = host.getAttached(ModAttachmentTypes.POSSESSION);
            if (possessionManager != null && this.equals(possessionManager.getPossessionTarget())) {
                // They are trapped in the bond! Start killing them
                if (host.getWorld() instanceof ServerWorld serverWorld) {
                    host.damage(serverWorld,
                            ModDamageTypes.createSource(host.getWorld(), ModDamageTypes.THIRST),
                            POSSESS_THIRST_DAMAGE);
                }
            } else {
                bondData.removeBond(this.getId());
                resetController();
            }
        } else {
            if (!isInitial && !host.isCreative()) {
                thirstManager.remove(1);
            }
            bondData.updateBond(this.getId());
        }
    }

    public void forgetAboutPlayer(PlayerEntity player) {
        UUID uuid = player.getUuid();
        if (isBeingPossessed) {
            PossessionManager possessionManager = player.getAttached(ModAttachmentTypes.POSSESSION);
            if (possessionManager != null && this.equals(possessionManager.getPossessionTarget())) {
                possessionManager.resetPossessionTarget();
                // Always called server-side, so no need to reset camera entity here
            }
        }
        if (uuid.equals(getControllerUuid())) {
            resetController();
        } else {
            waterBribes.removeInt(uuid);
        }
    }

    public void setController(PlayerEntity player) {
        UUID newControllerUuid = player.getUuid();
        UUID currentControllerUuid = getControllerUuid();
        if (currentControllerUuid != null) {
            if (currentControllerUuid.equals(newControllerUuid)) {
                return;
            } else {
                PlayerEntity formerController = getController();
                if (formerController != null) {
                    MidnightAetherBondManager midnightBondManager = formerController.getAttachedOrCreate(
                            ModAttachmentTypes.MIDNIGHT_AETHER_BOND);
                    midnightBondManager.removeBond(this.getId());
                }
            }
        }

        setControllerUuid(newControllerUuid);
        // this.controlLevel = ControlLevel.NORMAL;
        this.getWorld()
                .playSound(player, this.getX(), this.getY(), this.getZ(),
                        ModSoundEvents.ENTITY_MIDNIGHT_CREATURE_BOND, this.getSoundCategory(), 1.0f,
                        this.random.nextFloat() * 0.4F + 0.4F);

        // Immediately drain water from host, which will send updates
        drainWaterFromHost(player, true);
    }

    public void resetController() {
        UUID controllerUuid = getControllerUuid();
        if (controllerUuid != null) {
            waterBribes.removeInt(controllerUuid);
        }
        setControllerUuid(null);
        // this.controlLevel = ControlLevel.OUT_OF_CONTROL;
    }

    public void acceptWaterBribe(PlayerEntity player, int waterAmount) {
        UUID uuid = player.getUuid();
        int currentBribe = waterBribes.computeIfAbsent(uuid, id -> 0);
        currentBribe += waterAmount;
        waterBribes.put(uuid, currentBribe);
        UUID controllerUuid = getControllerUuid();
        if (controllerUuid == null && currentBribe < minBribe) {
            // Check if the bribe is above the minimum for an entity of that size
            return;
        }
        Entry<UUID> entry = Collections.max(waterBribes.object2IntEntrySet(),
                Map.Entry.comparingByValue());
        if (!entry.getKey().equals(getControllerUuid())) {
            setController(player);
        }
    }

    // Shapeshifting Logic
    @Override
    public void onMorphEntitySpawn(LivingEntity morph) {
        super.onMorphEntitySpawn(morph);
        ((MidnightOverlayAccess) morph).worldsinger$setMidnightOverlay(true);
        if (morph instanceof PufferfishEntity pufferfishEntity) {
            pufferfishEntity.setPuffState(PufferfishEntity.FULLY_PUFFED);
        } else if (morph instanceof SchoolingFishEntity) {
            // Don't turn on its side
            ((EntityAccessor) morph).worldsinger$setTouchingWater(true);
        }
    }

    @Override
    public void afterMorphEntitySpawn(LivingEntity morph, boolean showTransformEffects) {
        super.afterMorphEntitySpawn(morph, showTransformEffects);
        if (showTransformEffects && this.getWorld().isClient()) {
            MidnightSpores.addMidnightParticles(this.getWorld(), this, random, 0.2,
                    NUM_TRANSFORM_PARTICLES);
            this.getWorld()
                    .playSoundFromEntityClient(this,
                            ModSoundEvents.ENTITY_MIDNIGHT_CREATURE_TRANSFORM,
                            this.getSoundCategory(), 2.0f, 1.0f);
        }

        updateStats(morph, showTransformEffects);
    }

    private void updateStats(LivingEntity morph, boolean showTransformEffects) {
        EntityAttributeInstance movementSpeedAttribute = EntityUtil.getRequiredAttributeInstance(
                this, EntityAttributes.MOVEMENT_SPEED);
        EntityAttributeInstance knockbackResistanceAttribute = EntityUtil.getRequiredAttributeInstance(
                this, EntityAttributes.KNOCKBACK_RESISTANCE);
        EntityAttributeInstance maxHealthAttribute = EntityUtil.getRequiredAttributeInstance(this,
                EntityAttributes.MAX_HEALTH);
        EntityAttributeInstance attackDamageAttribute = EntityUtil.getRequiredAttributeInstance(
                this, EntityAttributes.ATTACK_DAMAGE);

        if (morph == null) {
            movementSpeedAttribute.setBaseValue(0.0);
            knockbackResistanceAttribute.setBaseValue(1.0);
            maxHealthAttribute.setBaseValue(MidnightCreatureManager.DEFAULT_MAX_HEALTH);
            attackDamageAttribute.setBaseValue(0.0);
            return;
        }
        float volume = EntityUtil.getSize(morph);
        double maxHealth = MidnightCreatureManager.getMaxHealthForSize(volume);
        double attackDamage = MidnightCreatureManager.getAttackDamageForSize(volume);
        // Speed is the same for all mobs
        movementSpeedAttribute.setBaseValue(MidnightCreatureManager.DEFAULT_MOVEMENT_SPEED);
        knockbackResistanceAttribute.setBaseValue(0.0);
        maxHealthAttribute.setBaseValue(maxHealth);
        attackDamageAttribute.setBaseValue(attackDamage);
        maxDrainInterval = MidnightCreatureManager.getDrainIntervalForSize(volume);
        minBribe = MidnightCreatureManager.getMinBribeForSize(volume);

        if (showTransformEffects) {
            this.setHealth(this.getMaxHealth());
        }
    }

    // Discards the mob (without dropping XP) in a puff of particles
    public void dispel(ServerWorld world, boolean playEffects) {
        if (this.getMorph() == null) {
            // Attempt to place itself back as a block
            BlockPos pos = this.getBlockPos();
            if (world.getBlockState(pos).isAir()) {
                world.setBlockState(pos, ModBlocks.MIDNIGHT_ESSENCE.getDefaultState());
                this.discard();
                return;
            }
        }

        // If it cannot revert to block form, play a dispelling animation
        if (playEffects) {
            // Spawn particles across their bounding box
            float halfWidth = this.getWidth() * 0.5f;
            float halfHeight = this.getHeight() * 0.5f;
            int numParticles = EntityUtil.getBlocksInBoundingBox(this) * DISPEL_PARTICLES_PER_BLOCK;
            world.spawnParticles(ModParticleTypes.MIDNIGHT_ESSENCE, this.getX(),
                    this.getY() + halfHeight, this.getZ(), numParticles, halfWidth, halfHeight,
                    halfWidth, DISPEL_PARTICLE_VELOCITY);
        }
        this.discard();
    }

    // This is only run on the client-side
    @Override
    public void onDamaged(DamageSource damageSource) {
        super.onDamaged(damageSource);
        MidnightSpores.addMidnightParticles(this.getWorld(), this, random, 0.25,
                MidnightCreatureEntity.NUM_DAMAGE_PARTICLES);
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (this.getMorph() == null) {
            view.putInt(MIDNIGHT_ESSENCE_AMOUNT_KEY, midnightEssenceAmount);
        }

        UUID controllerUuid = getControllerUuid();
        if (controllerUuid != null) {
            view.put(CONTROLLER_KEY, Uuids.INT_STREAM_CODEC, controllerUuid);
        }

        WriteView.ListView listView = view.getList(BRIBES_KEY);
        waterBribes.object2IntEntrySet().forEach(uuidEntry -> {
            WriteView itemView = listView.add();
            itemView.put(UUID_KEY, Uuids.INT_STREAM_CODEC, uuidEntry.getKey());
            itemView.putInt(BRIBE_KEY, uuidEntry.getIntValue());
        });
        if (listView.isEmpty()) {
            view.remove(BRIBES_KEY);
        }
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);
        if (this.getMorph() == null) {
            this.midnightEssenceAmount = view.getInt(MIDNIGHT_ESSENCE_AMOUNT_KEY, 0);
        }

        UUID controllerUuid = view.read(CONTROLLER_KEY, Uuids.INT_STREAM_CODEC).orElse(null);
        setControllerUuid(controllerUuid);
        PlayerEntity player = getController();
        if (player != null) {
            setController(player);
        }

        waterBribes.clear();
        // TODO: Make this typed?
        view.getOptionalListReadView(BRIBES_KEY).ifPresent(list -> {
            list.forEach(item -> {
                item.read(UUID_KEY, Uuids.INT_STREAM_CODEC).ifPresent(uuid -> {
                    waterBribes.put(uuid, item.getInt(BRIBE_KEY, 0));
                });
            });
        });
    }

    public void setMidnightEssenceAmount(int midnightEssenceAmount) {
        this.midnightEssenceAmount = midnightEssenceAmount;
    }

    @Override
    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        if (IMMUNE_TO.contains(effect.getEffectType())) {
            return false;
        }
        return super.canHaveStatusEffect(effect);
    }

    // Never push controller if being possessed
    @Override
    protected void pushAway(Entity entity) {
        if (isBeingPossessed && this.getController().equals(entity)) {
            return;
        }
        super.pushAway(entity);
    }

    // Never run out of air (since they don't need to breathe)
    @Override
    protected int getNextAirUnderwater(int air) {
        return air;
    }

    @Override
    public void commandMovement(float yaw, float pitch, float forwardSpeed, float sidewaysSpeed,
            boolean jumping, boolean sprinting) {
        this.setHeadYaw(yaw);   // Turns the head
        this.setYaw(yaw);       // Controls movement direction
        this.setPitch(pitch);
        EntityUtil.fixYawAndPitch(this);

        if (forwardSpeed != 0 || sidewaysSpeed != 0) {
            this.setSprinting(sprinting);
            this.getMoveControl().strafeTo(forwardSpeed, sidewaysSpeed);
        } else {
            this.setForwardSpeed(0.0f);
            this.setSidewaysSpeed(0.0f);
        }

        if (jumping) {
            this.getJumpControl().setActive();
        }

        this.possessionTimeout = MAX_POSSESSION_EXPIRY;
        this.isBeingPossessed = true;
    }

    @Override
    public void onStartControlling() {
        BrainUtil.clearMemory(this, MemoryModuleType.LOOK_TARGET);
        BrainUtil.clearMemory(this, MemoryModuleType.WALK_TARGET);
        BrainUtil.clearMemory(this, MemoryModuleType.PATH);
        BrainUtil.clearMemory(this, MemoryModuleType.ATTACK_TARGET);
        this.setForwardSpeed(0.0f);
        this.stopMovement();
    }

    @Override
    public void onStartPossessing(PlayerEntity possessor) {
        if (this.getWorld().isClient()) {
            possessor.playSoundToPlayer(ModSoundEvents.ENTITY_MIDNIGHT_CREATURE_POSSESS,
                    SoundCategory.PLAYERS, 0.5f, 0.5f);
        } else {
            BrainUtil.clearMemory(this, MemoryModuleType.LOOK_TARGET);
            BrainUtil.clearMemory(this, MemoryModuleType.WALK_TARGET);
            BrainUtil.clearMemory(this, MemoryModuleType.PATH);
            BrainUtil.clearMemory(this, MemoryModuleType.ATTACK_TARGET);
            this.setForwardSpeed(0.0f);
            this.stopMovement();
        }
    }

    @Override
    public void onStopPossessing(PlayerEntity possessor) {
        if (this.getWorld().isClient()) {
            possessor.playSoundToPlayer(ModSoundEvents.ENTITY_MIDNIGHT_CREATURE_POSSESS,
                    SoundCategory.PLAYERS, 0.5f, 0.5f);
        }
        this.setForwardSpeed(0.0f);
        this.setSprinting(false);
        this.stopMovement();
        isBeingPossessed = false;
    }

    @Override
    public boolean shouldKeepPossessing(PlayerEntity possessor) {
        return possessor.getUuid().equals(getControllerUuid());
    }

    @Override
    protected Text getDefaultName() {
        if (morph != null) {
            return Text.translatable(MORPHED_NAME_TRANSLATION_KEY, morph.getName());
        }
        return super.getDefaultName();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.ENTITY_MIDNIGHT_CREATURE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSoundEvents.ENTITY_MIDNIGHT_CREATURE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.ENTITY_MIDNIGHT_CREATURE_DEATH;
    }

    protected SoundEvent getStepSound() {
        return ModSoundEvents.ENTITY_MIDNIGHT_CREATURE_STEP;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Setting at 0.75 pitch to sound slightly different from Zombie
        this.playSound(this.getStepSound(), 0.15f, 0.75f);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return super.canImmediatelyDespawn(distanceSquared) && !isBeingPossessed;
    }

    // CameraPossessable attributes

    @Override
    public boolean canPerformAttack() {
        return true;
    }

    @Override
    public AttackOrigin getEntityAttackOrigin() {
        return AttackOrigin.POSSESSED;
    }

    @Override
    public boolean canSwitchPerspectives() {
        return true;
    }

    @Override
    public boolean canFreeLook() {
        return true;
    }

    @Override
    public boolean shouldPossessorLookAt() {
        return true;
    }

    /* Should act like a hostile mob though it does not extend HostileEntity */

    @Override
    protected boolean isDisallowedInPeaceful() {
        return true;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_HOSTILE_SWIM;
    }

    @Override
    protected SoundEvent getSplashSound() {
        return SoundEvents.ENTITY_HOSTILE_SPLASH;
    }

    @Override
    public FallSounds getFallSounds() {
        return new FallSounds(SoundEvents.ENTITY_HOSTILE_SMALL_FALL,
                SoundEvents.ENTITY_HOSTILE_BIG_FALL);
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    public int getMidnightEssenceAmount() {
        return midnightEssenceAmount;
    }

    @Override
    public LivingEntity toEntity() {
        return this;
    }
}
