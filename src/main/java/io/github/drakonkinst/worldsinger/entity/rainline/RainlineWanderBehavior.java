/*
 * MIT License
 *
 * Copyright (c) 2024 Drakonkinst
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

package io.github.drakonkinst.worldsinger.entity.rainline;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;

public class RainlineWanderBehavior implements RainlineBehavior {

    // Steering behaviors
    private static final float SPEED_BLOCKS_PER_TICK = 0.4f;
    private static final float WANDER_CIRCLE_RADIUS = 20.0f;
    private static final float WANDER_CIRCLE_DISTANCE = 20.0f;
    private static final float WANDER_ANGLE_CHANGE = 0.1f;
    private static final float STEERING_SCALE = 1.0f;

    private final Vector2d steeringForce = new Vector2d();
    private float wanderAngle = 0.0f;

    private Vec3d getNextVelocity(RainlineEntity entity) {
        Vec3d velocity = entity.getVelocity();
        calculateSteeringForce();
        return new Vec3d(velocity.getX() + steeringForce.x(), 0.0,
                velocity.getZ() + steeringForce.y());
    }

    @Override
    public void serverTick(RainlineEntity entity) {
        Vec3d pos = entity.getPos();
        Vec3d velocity = this.getVelocity();

        // Wander randomly
        if (velocity.equals(Vec3d.ZERO)) {
            // If no velocity, go in a random direction
            float randomAngle = this.random.nextFloat() * MathHelper.TAU;
            float velocityX = MathHelper.cos(randomAngle) * SPEED_BLOCKS_PER_TICK;
            float velocityZ = MathHelper.sin(randomAngle) * SPEED_BLOCKS_PER_TICK;
            steeringForce.set(velocityX, velocityZ);
        } else {
            // If current velocity, use wander steering behavior
            steeringForce.set(velocity.getX(), velocity.getZ())
                    .normalize()
                    .mul(WANDER_CIRCLE_DISTANCE);
            float displacementX = MathHelper.cos(wanderAngle) * WANDER_CIRCLE_RADIUS;
            float displacementZ = MathHelper.sin(wanderAngle) * WANDER_CIRCLE_RADIUS;
            steeringForce.add(displacementX, displacementZ).mul(STEERING_SCALE);
            wanderAngle += random.nextFloat() * WANDER_ANGLE_CHANGE * 2.0f - WANDER_ANGLE_CHANGE;
            if (wanderAngle < 0) {
                wanderAngle += MathHelper.TAU;
            }
            if (wanderAngle > MathHelper.TAU) {
                wanderAngle -= MathHelper.TAU;
            }
        }
    }

    @Override
    public boolean isFollowingPath() {
        return false;
    }

    private void calculateSteeringForce() {

        // TODO While wandering randomly, avoid lunagrees
    }
}
