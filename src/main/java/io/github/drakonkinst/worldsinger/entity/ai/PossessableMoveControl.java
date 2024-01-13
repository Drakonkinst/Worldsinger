package io.github.drakonkinst.worldsinger.entity.ai;

import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;

public class PossessableMoveControl<E extends MobEntity & CameraPossessable> extends MoveControl {

    private static final float POSSESSED_MOVEMENT_MULTIPLIER = 0.6f;

    private final E castEntity;
    private final float speedMultiplier;

    public PossessableMoveControl(E entity, float sprintingMultiplier) {
        super(entity);
        this.castEntity = entity;
        this.speedMultiplier = sprintingMultiplier;

    }

    @Override
    public void tick() {
        if (castEntity.isBeingPossessed()) {
            doPossessedTick();
        } else {
            super.tick();
        }
    }

    private void doPossessedTick() {
        if (this.state == MoveControl.State.STRAFE) {
            float baseMovementSpeed =
                    (float) this.entity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                            * POSSESSED_MOVEMENT_MULTIPLIER;
            // TODO: Seems a bit fast right now
            // Instead of using the speed control, use the speed multiplier from the constructor
            float speed = baseMovementSpeed;
            if (castEntity.isSprinting()) {
                speed *= this.speedMultiplier;
            }
            float forwards = this.forwardMovement;
            float sideways = this.sidewaysMovement;
            float magnitude = MathHelper.sqrt(forwards * forwards + sideways * sideways);
            if (magnitude < 1.0F) {
                magnitude = 1.0F;
            }

            magnitude = speed / magnitude;
            forwards *= magnitude;
            sideways *= magnitude;
            float yaw = this.entity.getYaw();
            float sinYaw = MathHelper.sin(yaw * MathHelper.RADIANS_PER_DEGREE);
            float cosYaw = MathHelper.cos(yaw * MathHelper.RADIANS_PER_DEGREE);
            float targetX = forwards * cosYaw - sideways * sinYaw;
            float targetZ = sideways * cosYaw + forwards * sinYaw;
            if (!this.isPosWalkable(targetX, targetZ)) {
                this.forwardMovement = 1.0F;
                this.sidewaysMovement = 0.0F;
            }

            this.entity.setMovementSpeed(speed);
            this.entity.setForwardSpeed(this.forwardMovement);
            this.entity.setSidewaysSpeed(this.sidewaysMovement);
            this.state = MoveControl.State.WAIT;
        } else if (this.state == MoveControl.State.JUMPING) {
            float baseMovementSpeed = (float) this.entity.getAttributeValue(
                    EntityAttributes.GENERIC_MOVEMENT_SPEED);
            // Instead of using the speed control, use the speed multiplier from the constructor
            float speed = this.speedMultiplier * baseMovementSpeed;
            this.entity.setMovementSpeed(speed);
            if (this.entity.isOnGround()) {
                this.state = MoveControl.State.WAIT;
            }
        } else {
            this.entity.setForwardSpeed(0.0F);
        }
    }

    private boolean isPosWalkable(float x, float z) {
        EntityNavigation entityNavigation = this.entity.getNavigation();
        if (entityNavigation != null) {
            PathNodeMaker pathNodeMaker = entityNavigation.getNodeMaker();
            if (pathNodeMaker != null && pathNodeMaker.getDefaultNodeType(this.entity.getWorld(),
                    MathHelper.floor(this.entity.getX() + (double) x), this.entity.getBlockY(),
                    MathHelper.floor(this.entity.getZ() + (double) z)) != PathNodeType.WALKABLE) {
                return false;
            }
        }

        return true;
    }
}
