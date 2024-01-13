package io.github.drakonkinst.worldsinger.entity.ai.behavior;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetAttackTarget;
import net.tslat.smartbrainlib.util.BrainUtils;

// AttackTarget behavior that fails if no valid target is found.
// Useful for FirstApplicableBehavior, as it allows for pass-through.
// For optimization, caches the target when checking. Since this function is a prerequisite,
// there should be no change in behavior.
public class OptionalAttackTarget<E extends LivingEntity> extends SetAttackTarget<E> {

    private LivingEntity cachedTarget;

    public OptionalAttackTarget(boolean usingNearestAttackable) {
        super(usingNearestAttackable);
    }

    @Override
    protected boolean shouldRun(ServerWorld level, E entity) {
        cachedTarget = this.targetFinder.apply(entity);
        return super.shouldRun(level, entity) && cachedTarget != null;
    }

    @Override
    protected void start(E entity) {
        if (cachedTarget != null) {
            BrainUtils.setMemory(entity, MemoryModuleType.ATTACK_TARGET, cachedTarget);
            BrainUtils.clearMemory(entity, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        }
    }
}
