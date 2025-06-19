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
package io.github.drakonkinst.worldsinger.entity.ai;

import io.github.drakonkinst.worldsinger.entity.CameraPossessable;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;

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
            if (state == State.WAIT) {
                // Prevent strafe drifting
                this.sidewaysMovement = 0.0f;
                this.entity.setSidewaysSpeed(0.0F);
            }
        }
    }

    private void doPossessedTick() {
        if (this.state == State.STRAFE) {
            float speed = (float) this.entity.getAttributeValue(EntityAttributes.MOVEMENT_SPEED)
                    * POSSESSED_MOVEMENT_MULTIPLIER;
            if (castEntity.isSprinting()) {
                speed *= this.speedMultiplier;
            }
            this.entity.setMovementSpeed(speed);
            this.entity.setForwardSpeed(this.forwardMovement);
            this.entity.setSidewaysSpeed(this.sidewaysMovement);
            this.state = State.WAIT;
        } else if (this.state == State.JUMPING) {
            float baseMovementSpeed = (float) this.entity.getAttributeValue(
                    EntityAttributes.MOVEMENT_SPEED);
            // Instead of using the speed control, use the speed multiplier from the constructor
            float speed = this.speedMultiplier * baseMovementSpeed;
            this.entity.setMovementSpeed(speed);
            if (this.entity.isOnGround()) {
                this.state = State.WAIT;
            }
        } else {
            this.entity.setForwardSpeed(0.0F);
        }
    }
}
